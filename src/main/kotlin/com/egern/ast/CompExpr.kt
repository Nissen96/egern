package com.egern.ast

import com.egern.visitor.Visitor

class CompExpr(val lhs: ArithExpr, val rhs: ArithExpr, val op: String) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        lhs.accept(visitor)
        rhs.accept(visitor)
    }
}