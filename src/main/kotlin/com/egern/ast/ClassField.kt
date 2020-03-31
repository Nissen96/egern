package com.egern.ast

import com.egern.visitor.Visitor

class ClassField(
    val objectId: String,
    val fieldId: String,
    val reference: Boolean,
    lineNumber: Int,
    charPosition: Int
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}