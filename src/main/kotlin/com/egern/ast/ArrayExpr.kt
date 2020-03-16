package com.egern.ast

import com.egern.visitor.Visitor

class ArrayExpr(val entries: List<Expr>, lineNumber: Int, charPosition: Int) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        if (entries.isNotEmpty()) {
            entries.dropLast(1).map {
                it.accept(visitor)
                visitor.midVisit(this)
            }
            entries.last().accept(visitor)
        }
        visitor.postVisit(this)
    }
}