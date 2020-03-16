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
        for (index in indices.reversed()) {
            visitor.preMidVisit(this)
            index.accept(visitor)
            visitor.postMidVisit(this)
        }
        visitor.postVisit(this)
    }
}