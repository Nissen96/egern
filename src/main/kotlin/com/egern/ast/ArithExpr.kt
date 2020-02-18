package com.egern.ast

import com.egern.visitor.Visitor

class ArithExpr(val lhs: Expr, val rhs: Expr, val op: ArithOp) : Expr() {
    override fun accept(visitor: Visitor) {
        lhs.accept(visitor)
        visitor.midVisit(this)
        rhs.accept(visitor)
        visitor.postVisit(this)
    }
}