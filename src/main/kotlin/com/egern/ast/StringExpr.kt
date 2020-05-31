package com.egern.ast

import com.egern.visitor.Visitor

class StringExpr(val value: String, lineNumber: Int = -1, charPosition: Int = -1) : Expr(lineNumber, charPosition) {
    var dataLabel: String = ""
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}