package com.egern.ast

import com.egern.visitor.Visitor

class ClassField(val classId: String, val id: String, lineNumber: Int, charPosition: Int) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}