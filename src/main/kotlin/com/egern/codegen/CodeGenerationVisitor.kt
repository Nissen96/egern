package com.egern.codegen

import com.egern.ast.*
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.util.*
import com.egern.visitor.Visitor
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class CodeGenerationVisitor(private var symbolTable: SymbolTable) :
    Visitor {
    val instructions = ArrayList<Instruction>()
    private val functionStack = stackOf<FuncDecl>()

    companion object {
        // CONSTANT OFFSETS FROM RBP
        const val LOCAL_VAR_OFFSET = 1
        const val STATIC_LINK_OFFSET = -2
        const val PARAM_OFFSET = -3

        const val PARAMS_IN_REGISTERS = 6
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
        for (i in 0 until diff) {
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
        functionStack.push(null)
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(MainLabel, Direct)
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleePrologue
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.AllocateInternalHeap
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.AllocateStackSpace,
                MetaOperationArg(program.variableCount)
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleeSave
            )
        )
    }

    override fun midVisit(program: Program) {
        add(
            Instruction(
                InstructionType.JMP,
                InstructionArg(Memory("main_end"), Direct)
            )
        )
    }

    override fun postVisit(program: Program) {
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory("main_end"), Direct)
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.DeallocateInternalHeap
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleeRestore
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleeEpilogue
            )
        )
    }

    override fun preVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable
        functionStack.push(funcDecl)
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(funcDecl.startLabel), Direct)
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleePrologue
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.AllocateStackSpace,
                MetaOperationArg(funcDecl.variableCount)
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleeSave
            )
        )
    }

    override fun postVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable.parent!!
        functionStack.pop()
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(funcDecl.endLabel), Direct)
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleeRestore
            )
        )
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.CalleeEpilogue
            )
        )
    }

    override fun preVisit(funcCall: FuncCall) {
        add(Instruction(InstructionType.META, MetaOperation.CallerSave))
    }

    override fun postVisit(funcCall: FuncCall) {
        val func = symbolTable.lookup(funcCall.id)!!
        val decl = func.info["funcDecl"] as FuncDecl
        val scopeDiff = symbolTable.scope - decl.symbolTable.scope
        val numArgs = funcCall.args.size

        // Move first arguments (after caller saved registers) to registers
        for (index in (0 until min(PARAMS_IN_REGISTERS, numArgs))) {
            add(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(RSP, IndirectRelative(-numArgs + index + 1)),
                    InstructionArg(Register(ParamReg(index)), Direct),
                    comment = "Move argument to parameter register $index"
                )
            )
        }


        // Push remaining arguments to stack in reverse order
        for (index in (0 until numArgs - PARAMS_IN_REGISTERS)) {
            add(
                Instruction(
                    InstructionType.PUSH,
                    InstructionArg(
                        RSP,
                        IndirectRelative(-(2 * index))
                    ),
                    comment = "Push argument to stack"
                )
            )
        }

        if (scopeDiff < 0) {
            // Call is in nested func declaration
            add(Instruction(InstructionType.PUSH, InstructionArg(RBP, Direct), comment = "Push static link (inwards)"))
        } else {
            // Call is recursive or outwards
            followStaticLink(scopeDiff)
            // Find static link in parent
            // Follow static link is always 1 scope short of the scope we need when considering functions
            // We compare with the scope level of another function which is nested by at least 1
            add(
                Instruction(
                    InstructionType.PUSH,
                    InstructionArg(StaticLink, IndirectRelative(STATIC_LINK_OFFSET)),
                    comment = "Push static link (outwards)"
                )
            )
        }
        add(
            Instruction(
                InstructionType.CALL,
                InstructionArg(Memory(decl.startLabel), Direct),
                comment = "Call function"
            )
        )
        val parametersOnStack = max(numArgs - PARAMS_IN_REGISTERS, 0)
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.DeallocateStackSpace,
                MetaOperationArg(parametersOnStack + 1),
                comment = "Deallocate pushed arguments"
            )
        )
        add(Instruction(InstructionType.META, MetaOperation.DeallocateStackSpace, MetaOperationArg(numArgs)))
        add(Instruction(InstructionType.META, MetaOperation.CallerRestore))

        // Push return value to stack as FuncCall can be used as an expression
        add(Instruction(InstructionType.PUSH, InstructionArg(ReturnValue, Direct)))
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

    override fun visit(booleanExpr: BooleanExpr) {
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(ImmediateValue((if (booleanExpr.value) 1 else 0).toString()), Direct),
                comment = "Push static boolean value"
            )
        )
    }

    override fun visit(idExpr: IdExpr) {
        val idLocation = getIdLocation(idExpr.id, true)
        add(
            Instruction(
                InstructionType.PUSH,
                idLocation,
                comment = "Push value of ${idExpr.id} in scope"
            )
        )
    }

    private fun getIdLocation(id: String, checkDeclared: Boolean = false): InstructionArg {
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
        val symbol = symbolTable.lookup(id, checkDeclared) ?: throw Exception("Symbol $id is undefined")
        val scopeDiff = symbolTable.scope - symbol.scope
        val symbolOffset =
            symbol.info[if (symbol.type == SymbolType.Variable) "variableOffset" else "paramOffset"] as Int

        // Symbol is a parameter (1-6) in current scope - value is in register
        if (scopeDiff == 0 && symbol.type == SymbolType.Parameter && symbolOffset < PARAMS_IN_REGISTERS) {
            return InstructionArg(Register(ParamReg(symbolOffset)), Direct)
        }

        // Get base pointer of scope containing symbol and find offset for symbol location
        followStaticLink(scopeDiff)
        val container = functionStack.peek(scopeDiff)
        val offset = when (symbol.type) {
            SymbolType.Variable -> symbolOffset + LOCAL_VAR_OFFSET
            SymbolType.Parameter -> when {
                // Param saved by caller after its local variables
                symbolOffset < PARAMS_IN_REGISTERS -> symbolOffset + LOCAL_VAR_OFFSET + container!!.variableCount
                // Calculate offset for params on stack (in non-reversed order)
                else -> PARAM_OFFSET - (symbolOffset - PARAMS_IN_REGISTERS)
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
        // Pop expressions to register 1 and 2 in reverse order
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
        val arithOperator = when (arithExpr.op) {
            ArithOp.PLUS -> InstructionType.ADD
            ArithOp.MINUS -> InstructionType.SUB
            ArithOp.TIMES -> InstructionType.IMUL
            ArithOp.DIVIDE -> InstructionType.IDIV
            ArithOp.MODULO -> InstructionType.MOD
        }
        add(
            Instruction(
                arithOperator,
                InstructionArg(Register(OpReg2), Direct),
                InstructionArg(Register(OpReg1), Direct),
                comment = "Do arithmetic operation"
            )
        )
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(Register(OpReg1), Direct),
                comment = "Push result to stack"
            )
        )
    }

    override fun postVisit(booleanOpExpr: BooleanOpExpr) {
        // Pop expressions to register 1 and 2 in reverse order
        if (booleanOpExpr.rhs != null) {
            add(
                Instruction(
                    InstructionType.POP,
                    InstructionArg(Register(OpReg2), Direct),
                    comment = "Pop expression to register 2"
                )
            )
        }
        add(
            Instruction(
                InstructionType.POP,
                InstructionArg(Register(OpReg1), Direct),
                comment = "Pop expression to register 1"
            )
        )
        val operator = when (booleanOpExpr.op) {
            BooleanOp.AND -> InstructionType.AND
            BooleanOp.OR -> InstructionType.OR
            BooleanOp.NOT -> InstructionType.NOT
        }
        if (booleanOpExpr.rhs != null) {
            add(
                Instruction(
                    operator,
                    InstructionArg(Register(OpReg2), Direct),
                    InstructionArg(Register(OpReg1), Direct),
                    comment = "Do boolean operation"
                )
            )
        } else {
            add(
                Instruction(
                    operator,
                    InstructionArg(Register(OpReg1), Direct),
                    comment = "Do boolean operation"
                )
            )
        }
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(Register(OpReg1), Direct),
                comment = "Push result to stack"
            )
        )
    }

    override fun postVisit(arrayExpr: ArrayExpr) {
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.AllocateHeapSpace,
                MetaOperationArg(arrayExpr.entries.size + 1)
            )
        )

        val arrayLen = arrayExpr.entries.size
        add(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ImmediateValue(arrayLen.toString()), Direct),
                InstructionArg(ReturnValue, Indirect),
                comment = "Write size information before array"
            )
        )

        for (index in arrayExpr.entries.indices) {
            add(
                Instruction(
                    InstructionType.POP,
                    InstructionArg(Register(OpReg1), Direct),
                    comment = "Pop expression to register 1"
                )
            )
            add(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(Register(OpReg1), Direct),
                    InstructionArg(ReturnValue, IndirectRelative(-(arrayLen - index))),
                    comment = "Move expression to array index $index"
                )
            )
        }
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(ReturnValue, Direct),
                comment = "Push result to stack"
            )
        )
    }

    // Indexes into array, result is in OpReg2
    private fun indexIntoArray(arrayIndexExpr: ArrayIndexExpr) {
        val idLocation = getIdLocation(arrayIndexExpr.id)
        add(
            Instruction(
                InstructionType.MOV,
                idLocation,
                InstructionArg(Register(OpReg2), Direct),
                comment = "Move array pointer to data register"
            )
        )
        for ((index, _) in arrayIndexExpr.indices.withIndex()) {
            add(
                Instruction(
                    InstructionType.ADD,
                    InstructionArg(ImmediateValue("8"), Direct),
                    InstructionArg(Register(OpReg2), Direct),
                    comment = "Move past array info"
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
                    InstructionType.IMUL,
                    InstructionArg(ImmediateValue("8"), Direct),
                    InstructionArg(Register(OpReg1), Direct),
                    comment = "Multiply index with 8"
                )
            )
            add(
                Instruction(
                    InstructionType.ADD,
                    InstructionArg(Register(OpReg1), Direct),
                    InstructionArg(Register(OpReg2), Direct),
                    comment = "Move pointer by index"
                )
            )
            if (index < arrayIndexExpr.indices.size - 1) {
                add(
                    Instruction(
                        InstructionType.MOV,
                        InstructionArg(Register(OpReg2), Indirect),
                        InstructionArg(Register(OpReg2), Direct),
                        comment = "Follow pointer"
                    )
                )
            }
        }
        // Only follow last pointer if we want to return a value
        if (!arrayIndexExpr.reference) {
            add(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(Register(OpReg2), Indirect),
                    InstructionArg(Register(OpReg2), Direct),
                    comment = "Follow pointer"
                )
            )
        }
    }

    override fun postVisit(arrayIndexExpr: ArrayIndexExpr) {
        indexIntoArray(arrayIndexExpr)
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(Register(OpReg2), Direct),
                comment = "Push value to stack"
            )
        )
    }

    override fun postVisit(lenExpr: LenExpr) {
        add(
            Instruction(
                InstructionType.POP,
                InstructionArg(Register(OpReg1), Direct),
                comment = "Pop array reference from stack"
            )
        )
        add(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(Register(OpReg1), Indirect),
                comment = "Push array length to stack"
            )
        )
    }

    override fun postVisit(printStmt: PrintStmt) {
        add(Instruction(InstructionType.META, MetaOperation.CallerSave))
        add(
            Instruction(
                InstructionType.META,
                MetaOperation.Print,
                MetaOperationArg(if (printStmt.expr != null) 1 else 0)
            )
        )
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
        val endLabel = functionStack.peek()?.endLabel
        if (endLabel != null) {
            add(
                Instruction(
                    InstructionType.JMP,
                    InstructionArg(Memory(endLabel), Direct),
                    comment = "Jump to end of function"
                )
            )
        }
    }

    override fun preMidVisit(ifElse: IfElse) {
        chooseBranch(ifElse.elseLabel)
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

    override fun preVisit(whileLoop: WhileLoop) {
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(whileLoop.startLabel), Direct)
            )
        )
    }

    override fun midVisit(whileLoop: WhileLoop) {
        chooseBranch(whileLoop.endLabel)
    }

    override fun postVisit(whileLoop: WhileLoop) {
        add(
            Instruction(
                InstructionType.JMP,
                InstructionArg(Memory(whileLoop.startLabel), Direct),
                comment = "Jump back to continue loop"
            )
        )
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(Memory(whileLoop.endLabel), Direct)
            )
        )
    }

    private fun chooseBranch(label: String) {
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
                InstructionArg(Register(OpReg2), Direct),
                InstructionArg(Register(OpReg1), Direct),
                comment = "Compare the expression to true"
            )
        )
        add(
            Instruction(
                InstructionType.JL,
                InstructionArg(Memory(label), Direct),
                comment = "Jump if false"
            )
        )
    }

    override fun postVisit(varDecl: VarDecl<*>) {
        // First declaration of variable in this scope
        varDecl.ids.forEach { symbolTable.lookup(it)?.isDeclared = true }
        variableAssignment(varDecl.ids)
    }

    override fun postVisit(varAssign: VarAssign<*>) {
        variableAssignment(varAssign.ids, varAssign.indexExprs)
    }

    private fun variableAssignment(ids: List<String>, arrayIds: List<ArrayIndexExpr> = listOf()) {
        // Find each variable/parameter location and set their value to the expression result
        add(Instruction(InstructionType.POP, InstructionArg(Register(OpReg1), Direct), comment = "Expression result"))
        val symbols = ids.map { symbolTable.lookup(it)!! }
        for (symbol in symbols) {
            val idLocation = getIdLocation(symbol.id)
            add(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(Register(OpReg1), Direct),
                    idLocation,
                    comment = "Set value of ${symbol.type.toString().toLowerCase()} ${symbol.id} to expression result"
                )
            )
        }
        for (id in arrayIds) {
            add(
                Instruction(
                    InstructionType.POP,
                    InstructionArg(Register(OpReg2), Direct),
                    comment = "Pop pointer into register 2"
                )
            )
            add(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(Register(OpReg1), Direct),
                    InstructionArg(Register(OpReg2), Indirect),
                    comment = "Set value of ${id.id} at index to expression result"
                )
            )
        }
    }
}
