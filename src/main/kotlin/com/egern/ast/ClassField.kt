package com.egern.ast

import com.egern.visitor.Visitor

open class ClassField(
    val objectId: String,
    open val fieldId: String,
    open val reference: Boolean,
    lineNumber: Int,
    charPosition: Int
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}