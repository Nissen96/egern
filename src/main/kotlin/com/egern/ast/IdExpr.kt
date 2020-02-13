package com.egern.ast

import com.egern.visitor.Visitor

class IdExpr(val id: String) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
    }
}