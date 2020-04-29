package com.egern.ast

import com.egern.visitor.Visitor

class CompExpr(
    val lhs: Expr,
    val rhs: Expr,
    val op: CompOp,
    lineNumber: Int = -1,
    charPosition: Int = -1
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        lhs.accept(visitor)
        visitor.midVisit(this)
        rhs.accept(visitor)
        visitor.postVisit(this)
    }
}