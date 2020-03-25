package com.egern.ast

import com.egern.visitor.Visitor

class VarAssign(
    val ids: List<String>,
    val indexExprs: List<ArrayIndexExpr>,
    val expr: Expr,
    lineNumber: Int,
    charPosition: Int
) :
    Statement(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        indexExprs.map { it.accept(visitor) }
        visitor.midVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}