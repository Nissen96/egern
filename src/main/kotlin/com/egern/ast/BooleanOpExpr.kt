package com.egern.ast

import com.egern.visitor.Visitor

class BooleanOpExpr(val lhs: Expr, val rhs: Expr? = null, val op: BooleanOp) : Expr() {
    override fun accept(visitor: Visitor) {
        lhs.accept(visitor)
        visitor.midVisit(this)
        rhs?.accept(visitor)
        visitor.postVisit(this)
    }
}