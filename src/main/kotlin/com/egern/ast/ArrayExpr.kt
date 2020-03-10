package com.egern.ast

import com.egern.visitor.Visitor

class ArrayExpr(val entries: List<Expr>, lineNumber: Int, charPosition: Int) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        for (entry in entries) {
            entry.accept(visitor);
        }
        visitor.postVisit(this)
    }
}