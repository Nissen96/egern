package com.egern.ast

import com.egern.visitor.Visitor

class IntExpr(val value: Int, lineNumber: Int, charPosition: Int, val isVoid: Boolean = false) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}