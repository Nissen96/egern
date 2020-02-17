package com.egern.codegen

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class CodeGenerationVisitor(val symbolTable: SymbolTable) : Visitor {
    private val instructions = ArrayList<Instruction>();

    fun add(instruction: Instruction) {
        instructions.add(instruction)
    }

    // TODO: Generate code

}