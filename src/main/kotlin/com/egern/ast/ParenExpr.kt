package com.egern.ast

import com.egern.visitor.Visitor

class ParenExpr(val expr: Expr) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}