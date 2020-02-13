package com.egern.ast

import com.egern.visitor.Visitor

class FuncCall(val id: String, val args: List<Expr>) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        args.map { it.accept(visitor) }
        visitor.postVisit(this)
    }
}