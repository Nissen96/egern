package com.egern.ast

import com.egern.visitor.Visitor

class FuncBody(val statements: List<Statement>, val funcCalls: List<FuncCall>) : ASTNode() {
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
