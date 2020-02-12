package com.egern.ast

import com.egern.visitor.Visitor

class FuncCall(val id: String, val args: List<String>) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
    }
}