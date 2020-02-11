package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class FuncDecl(val id: String, val params: List<String>, val block: Block) : ASTNode(), Scopable {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        block.accept(visitor)
        visitor.postVisit(this)
    }

    override lateinit var symbolTable: SymbolTable

}