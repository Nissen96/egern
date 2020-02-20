package com.egern.ast

import com.egern.visitor.Visitor

class FuncBody(val funcDecls: List<FuncDecl>, val statements: List<Statement>, val funcCalls: List<FuncCall>) :
    ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        for (statement in statements) {
            statement.accept(visitor)
        }
        for (funcCall in funcCalls) {
            funcCall.accept(visitor)
        }
        for (funcDecl in funcDecls) {
            funcDecl.accept(visitor)
        }
        visitor.postVisit(this)
    }
}
