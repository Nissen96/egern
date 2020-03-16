package com.egern.ast

import com.egern.visitor.Visitor

class VarAssign<T : Expr>(
    val ids: List<String>,
    val indexExprs: List<ArrayIndexExpr>,
    val expr: T,
    lineNumber: Int,
    charPosition: Int
) :
    Statement(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        indexExprs.map {
            it.reference = true
            it.accept(visitor)
        }
        visitor.postVisit(this)
    }
}