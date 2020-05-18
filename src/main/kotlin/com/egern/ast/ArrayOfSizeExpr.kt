package com.egern.ast

import com.egern.types.ExprType
import com.egern.visitor.Visitor

class ArrayOfSizeExpr(
    val type: ExprType,
    val size: Expr,
    lineNumber: Int,
    charPosition: Int
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        size.accept(visitor)
        visitor.postVisit(this)
    }
}