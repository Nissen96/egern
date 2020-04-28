package com.egern.ast

import com.egern.visitor.Visitor

class IntExpr(
    val value: Int,
    val isVoid: Boolean = false,
    lineNumber: Int = -1,
    charPosition: Int = -1
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}