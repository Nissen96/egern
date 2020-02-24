package com.egern.codegen

import com.egern.ast.*
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.util.*
import com.egern.visitor.Visitor
import kotlin.collections.ArrayList

class CodeGenerationVisitor(private var symbolTable: SymbolTable) : Visitor {
    val instructions = ArrayList<Instruction>()
    private val numVariablesStack = stackOf<Int>()

    companion object {
        // CONSTANT OFFSETS FROM RBP
        const val LOCAL_VAR_OFFSET = 1
        const val RETURN_OFFSET = -1
        const val STATIC_LINK_OFFSET = -2
        const val PARAM_OFFSET = -3
    }

    private fun add(instruction: Instruction) {
        instructions.add(instruction)
    }

    private fun followStaticLink(diff: Int) {
        add(
            Instruction(
                InstructionType.MOV, InstructionArg(RBP, Direct), InstructionArg(StaticLink, Direct),
                comment = "Prepare to follow static link pointer"
            )
        )
        for (i in 0..diff) {
            add(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(StaticLink, IndirectRelative(STATIC_LINK_OFFSET)),
                    InstructionArg(StaticLink, Direct),
                    comment = "Following static link pointer"
                )
            )
        }
    }

    override fun preVisit(program: Program) {
        numVariablesStack.push(program.variableCount)
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.AllocateStackSpace,
                MetaOperationArg(program.variableCount)
            )
        )
    }

    override fun preVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable
        numVariablesStack.push(funcDecl.variableCount)
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(funcDecl.startLabel), Direct)
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.AllocateStackSpace,
                MetaOperationArg(funcDecl.variableCount)
            )
        )
    }

    override fun postVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable.parent!!
        numVariablesStack.pop()
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(funcDecl.endLabel), Direct)
            )
        )
    }

    override fun postVisit(funcCall: FuncCall) {
        val func = symbolTable.lookup(funcCall.id)!!
        val decl = func.info as FuncDecl
        for (arg in funcCall.args.take(6)) {
            val index = funcCall.args.indexOf(arg)
            add(
                Instruction(
                    InstructionType.POP,
                    InstructionArg(Register(ParamReg(index)), Direct),
                    comment = "Pop expression to param register $index"
                )
            )
        }
        add(Instruction(InstructionType.CALL, InstructionArg(Memory(decl.startLabel), Direct)))
    }

    override fun preVisit(block: Block) {
        symbolTable = block.symbolTable
    }

    override fun postVisit(block: Block) {
        symbolTable = block.symbolTable.parent!!
    }

    override fun visit(intExpr: IntExpr) {
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(ImmediateValue(intExpr.value.toString()), Direct),
                comment = "Push static integer value"
            )
        )
    }

    override fun visit(idExpr: IdExpr) {
        val idLocation = getIdLocation(idExpr.id)
        add(
            Instruction(
                InstructionType.PUSH,
                idLocation,
                comment = "Push value of ${idExpr.id} in scope"
            )
        )
    }

    private fun getIdLocation(id: String): InstructionArg {
        /**
         * Get the location of a local variable or parameter from id
         * The id can be in the current scope or any parent scope of this,
         * so potentially a number of static links must first be followed to locate the correct stack frame
         *
         * For local variables and for the 7th+ parameter, the position is some offset from the static link register
         * First 6 parameters are for the current scope saved in registers
         * For any enclosing scope, they have been saved at the top of the relevant stack frame
         */
        // Find static link address for scope containing given id
        val symbol = symbolTable.lookup(id) ?: throw Exception("Symbol $id is undefined")
        val scopeDiff = symbolTable.scope - symbol.scope
        val symbolOffset = symbol.info as Int

        // Symbol is a parameter (1-6) in current scope - value is in register
        if (scopeDiff == 0 && symbol.type == SymbolType.Parameter && symbolOffset < 6) {
            return InstructionArg(Register(ParamReg(symbolOffset)), Direct)
        }

        // Get base pointer of scope containing symbol and find offset for symbol location
        followStaticLink(scopeDiff)
        val containerNumVariables = numVariablesStack.peek(scopeDiff)!!
        val offset = symbolOffset + when (symbol.type) {
            SymbolType.Variable -> LOCAL_VAR_OFFSET
            SymbolType.Parameter -> when {
                // Param saved by caller after its local variables
                symbolOffset < 6 -> LOCAL_VAR_OFFSET + containerNumVariables
                else -> PARAM_OFFSET
            }
            else -> throw Exception("Invalid id $id")
        }

        return InstructionArg(StaticLink, IndirectRelative(offset))
    }

    override fun postVisit(compExpr: CompExpr) {
        // Pop expressions to register 1 and 2
        add(
            Instruction(
                InstructionType.POP,
                InstructionArg(Register(OpReg2), Direct),
                comment = "Pop expression to register 2"
            )
        )
        add(
            Instruction(
                InstructionType.POP,
                InstructionArg(Register(OpReg1), Direct),
                comment = "Pop expression to register 1"
            )
        )
        add(
            Instruction(
                InstructionType.CMP,
                InstructionArg(Register(OpReg2), Direct),
                InstructionArg(Register(OpReg1), Direct),
                comment = "Compare with ${compExpr.op.value}"
            )
        )
        val trueLabel = LabelGenerator.nextLabel("cmp_true")
        val endLabel = LabelGenerator.nextLabel("cmp_end")
        val jumpOperator = when (compExpr.op) {
            CompOp.EQ -> InstructionType.JE
            CompOp.NEQ -> InstructionType.JNE
            CompOp.LT -> InstructionType.JL
            CompOp.GT -> InstructionType.JG
            CompOp.LTE -> InstructionType.JLE
            CompOp.GTE -> InstructionType.JGE
        }
        add(Instruction(jumpOperator, InstructionArg(Memory(trueLabel), Direct), comment = "Jump if true"))
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(ImmediateValue("0"), Direct),
                comment = "Push false if comparison was false"
            )
        )
        add(
            Instruction(
                InstructionType.JMP,
                InstructionArg(Memory(endLabel), Direct),
                comment = "Skip pushing false if success"
            )
        )
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(trueLabel), Direct)
            )
        )
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(ImmediateValue("1"), Direct),
                comment = "Push true if comparison was true"
            )
        )
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(endLabel), Direct)
            )
        )
    }

    override fun postVisit(arithExpr: ArithExpr) {
        // Pop expressions to register 1 and 2
        add(
            Instruction(
                InstructionType.POP,
                InstructionArg(Register(OpReg1), Direct),
                comment = "Pop expression to register 1"
            )
        )
        add(
            Instruction(
                InstructionType.POP,
                InstructionArg(Register(OpReg2), Direct),
                comment = "Pop expression to register 2"
            )
        )
        val arithOperator = when (arithExpr.op) {
            ArithOp.PLUS -> InstructionType.ADD
            ArithOp.MINUS -> InstructionType.SUB
            ArithOp.TIMES -> InstructionType.IMUL
            ArithOp.DIVIDE -> InstructionType.IDIV
        }
        add(
            Instruction(
                arithOperator,
                InstructionArg(Register(OpReg1), Direct),
                InstructionArg(Register(OpReg2), Direct),
                comment = "Do arithmetic operation"
            )
        )
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(Register(OpReg2), Direct),
                comment = "Push result to stack"
            )
        )
    }

    override fun postVisit(printStmt: PrintStmt) {
        add(Instruction(InstructionType.META, MetaOperation.CallerSave))
        add(Instruction(InstructionType.META, MetaOperation.Print))
        add(Instruction(InstructionType.META, MetaOperation.CallerRestore))
    }

    override fun postVisit(returnStmt: ReturnStmt) {
        if (returnStmt.expr != null) {
            add(
                Instruction(
                    InstructionType.POP,
                    InstructionArg(ReturnValue, Direct),
                    comment = "Pop expression to return value register"
                )
            )
        }
        add(Instruction(InstructionType.RET))
    }

    override fun preMidVisit(ifElse: IfElse) {
        add(
            Instruction(
                InstructionType.POP,
                InstructionArg(Register(OpReg1), Direct),
                comment = "Pop expression to register"
            )
        )
        add(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ImmediateValue("1"), Direct),
                InstructionArg(Register(OpReg2), Direct),
                comment = "Move true to other register"
            )
        )
        add(
            Instruction(
                InstructionType.CMP,
                InstructionArg(Register(OpReg1), Direct),
                InstructionArg(Register(OpReg2), Direct),
                comment = "Compare the expression to true"
            )
        )
        if (ifElse.elseBlock != null) {
            add(
                Instruction(
                    InstructionType.JNE,
                    InstructionArg(Memory(ifElse.elseLabel), Direct),
                    comment = "Jump to optional else part"
                )
            )
        }
    }

    override fun postMidVisit(ifElse: IfElse) {
        add(
            Instruction(
                InstructionType.JMP,
                InstructionArg(Memory(ifElse.endLabel), Direct),
                comment = "Skip else part if successful"
            )
        )
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(ifElse.elseLabel), Direct)
            )
        )
    }

    override fun postVisit(ifElse: IfElse) {
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(ifElse.endLabel), Direct)
            )
        )
    }

    override fun postVisit(varAssign: VarAssign<*>) {
        // Find each variable/parameter location and set their value to the expression result
        add(Instruction(InstructionType.POP, InstructionArg(Register(DataReg), Direct), comment = "Expression result"))
        val symbols = varAssign.ids.map { symbolTable.lookup(it)!! }
        for (symbol in symbols) {
            val idLocation = getIdLocation(symbol.id)
            add(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(Register(DataReg), Direct),
                    idLocation,
                    comment = "Set value of ${symbol.type.toString().toLowerCase()} ${symbol.id} to expression result"
                )
            )
        }
    }
}
