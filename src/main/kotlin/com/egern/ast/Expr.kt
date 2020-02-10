package com.egern.ast

import com.egern.visitor.Visitor

open class Expr : ASTNode() {
    override fun accept(visitor: Visitor) {
        return visitor.visit(this)
    }
}