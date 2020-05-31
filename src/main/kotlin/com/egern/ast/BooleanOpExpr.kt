package com.egern.ast

import com.egern.visitor.Visitor

class BooleanOpExpr(
    val lhs: Expr,
    val rhs: Expr? = null,
    val op: BooleanOp,
    lineNumber: Int,
    charPosition: Int
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        lhs.accept(visitor)
        visitor.midVisit(this)
        rhs?.accept(visitor)
        visitor.postVisit(this)
    }
}