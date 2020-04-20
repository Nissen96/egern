package com.egern.ast

import com.egern.visitor.Visitor

class StaticClassField(
    val classId: String,
    override val fieldId: String,
    override val reference: Boolean,
    lineNumber: Int,
    charPosition: Int
) : ClassField(classId, fieldId, reference, lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}