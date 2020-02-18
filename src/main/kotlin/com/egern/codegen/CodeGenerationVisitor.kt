package com.egern.codegen

import com.egern.ast.*
import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class CodeGenerationVisitor(var symbolTable: SymbolTable) : Visitor {
    val instructions = ArrayList<Instruction>()


    companion object {
        // CONSTANT OFFSETS FROM RBP
        const val LOCAL_VAR_OFFSET = "1"
        const val RETURN_OFFSET = "-1"
        const val STATIC_LINK_OFFSET = "-2"
        const val PARAM_OFFSET = "-3"
    }

    private fun add(instruction: Instruction) {
        instructions.add(instruction)
    }

    fun followStaticLink(diff: Int) {
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
        add(Instruction(InstructionType.LABEL, InstructionArg(ImmediateLabel("main"), Direct)))
        //TODO CALLER PROLOGUE?
    }

    override fun postVisit(program: Program) {
        //TODO CALLER EPILOGUE?
    }

    override fun preVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(ImmediateLabel(funcDecl.startLabel), Direct)
            )
        )

    }

    override fun postVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable.parent!!
        add(
            Instruction(
                InstructionType.LABEL,
                InstructionArg(ImmediateLabel(funcDecl.endLabel), Direct)
            )
        )
    }

    override fun preVisit(block: Block) {
        symbolTable = block.symbolTable
    }

    override fun postVisit(block: Block) {
        symbolTable = block.symbolTable.parent!!
    }

    // TODO: Generate code
}
