package com.egern.ast

import com.egern.util.forEach
import com.egern.visitor.Visitor

class ObjectInstantiation(
    val classId: String,
    val args: List<Expr>,
    lineNumber: Int,
    charPosition: Int
) : Expr(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        args.forEach({ it.accept(visitor) }, doBetween = { visitor.midVisit(this) })
        visitor.postVisit(this)
    }
}