package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class Block(val children: List<ASTNode>) : ASTNode(), Scopable {
    override lateinit var symbolTable: SymbolTable

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        children.forEach {
            when (it) {
                is Statement -> it.accept(visitor)
                is FuncCall -> {
                    visitor.preMidVisit(this)
                    it.accept(visitor)
                    visitor.postMidVisit(this)
                }
            }
        }
        visitor.postVisit(this)
    }
}
