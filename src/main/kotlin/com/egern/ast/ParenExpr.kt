package com.egern.ast

import com.egern.visitor.Visitor

class ParenExpr(val expr: Expr, lineNumber: Int, charPosition: Int) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}