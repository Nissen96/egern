package com.egern.codegen

import com.egern.ast.Block
import com.egern.ast.FuncDecl
import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class CodeGenerationVisitor(var symbolTable: SymbolTable) : Visitor {
    private val instructions = ArrayList<Instruction>();

    companion object {
        // CONSTANT OFFSETS FROM RBP
        const val RSL_OFFSET = "-2"
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
                    InstructionArg(StaticLink, IndirectRelative(RSL_OFFSET)),
                    InstructionArg(StaticLink, Direct),
                    comment = "Following static link pointer"
                )
            )
        }
    }

    override fun preVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable
    }

    override fun postVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable.parent!!
    }

    override fun preVisit(block: Block) {
        symbolTable = block.symbolTable
    }

    override fun postVisit(block: Block) {
        symbolTable = block.symbolTable.parent!!
    }

    // TODO: Generate code
}
