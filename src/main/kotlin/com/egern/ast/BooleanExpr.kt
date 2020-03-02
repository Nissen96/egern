package com.egern.ast

import com.egern.visitor.Visitor

class BooleanExpr(val value: Boolean) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}