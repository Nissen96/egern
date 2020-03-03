package com.egern.ast

import com.egern.visitor.Visitor

class VarAssign<T : Expr>(val ids: List<String>, val expr: T) : Statement() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}