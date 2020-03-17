package com.egern.ast

import com.egern.visitor.Visitor

class VarAssign<T : Expr>(
    val ids: List<String>,
    val indexExprs: List<ArrayIndexExpr>,
    val classFields: List<ClassField>,
    val expr: T,
    lineNumber: Int,
    charPosition: Int
) :
    Statement(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        indexExprs.map { it.accept(visitor) }
        classFields.map { it.accept(visitor) }
        visitor.midVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}