package com.egern.ast

import com.egern.visitor.Visitor

class ArrayIndexExpr(
    val id: String,
    val indices: List<Expr>,
    val reference: Boolean = false,
    lineNumber: Int,
    charPosition: Int
) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        indices.reversed().forEach {
            visitor.preMidVisit(this)
            it.accept(visitor)
            visitor.postMidVisit(this)
        }
        visitor.postVisit(this)
    }
}