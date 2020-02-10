package com.egern.ast

import com.egern.visitor.Visitor

class Block(val statements: List<Statement>) : ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
        for (statement in statements) {
            statement.accept(visitor)
        }
    }
}
