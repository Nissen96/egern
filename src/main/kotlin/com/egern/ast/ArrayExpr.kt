package com.egern.ast

import com.egern.util.forEach
import com.egern.visitor.Visitor

class ArrayExpr(val entries: List<Expr>, lineNumber: Int, charPosition: Int) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        entries.forEach({ it.accept(visitor) }, doBetween = { visitor.midVisit(this) })
        visitor.postVisit(this)
    }
}