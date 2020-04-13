package com.egern.ast

import com.egern.types.ExprType
import com.egern.visitor.Visitor

class CastExpr(
    val expr: Expr,
    val type: ExprType,
    lineNumber: Int,
    charPosition: Int
) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}