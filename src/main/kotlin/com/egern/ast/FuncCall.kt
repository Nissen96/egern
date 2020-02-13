package com.egern.ast

import com.egern.visitor.Visitor

class FuncCall(val id: String, val args: List<Expr>) : Expr() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        if (args.isNotEmpty()) {
            args.dropLast(1).map {
                it.accept(visitor);
                visitor.midVisit(this)
            }
            args.last().accept(visitor)
        }
        visitor.postVisit(this)
    }
}