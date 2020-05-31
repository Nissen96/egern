package com.egern.ast

import com.egern.visitor.Visitor

class VoidExpr(
    lineNumber: Int = -1,
    charPosition: Int = -1
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}