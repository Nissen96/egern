package com.egern.ast

import com.egern.visitor.Visitor

class IntExpr(val value: Int) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
    }
}