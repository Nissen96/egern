package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class Block(val statements: List<Statement>, val funcCalls: List<FuncCall>) : ASTNode(), Scopable {
    override lateinit var symbolTable: SymbolTable

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        for (statement in statements) {
            statement.accept(visitor)
        }
        for (funcCall in funcCalls) {
            funcCall.accept(visitor)
        }
        visitor.postVisit(this)
    }
}
