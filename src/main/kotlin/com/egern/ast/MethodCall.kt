package com.egern.ast

import com.egern.visitor.Visitor

class MethodCall(val objectId: String, val methodId: String, val args: List<Expr>, lineNumber: Int, charPosition: Int) :
    Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        if (args.isNotEmpty()) {
            args.dropLast(1).map {
                it.accept(visitor)
                visitor.midVisit(this)
            }
            args.last().accept(visitor)
        }
        visitor.postVisit(this)
    }
}