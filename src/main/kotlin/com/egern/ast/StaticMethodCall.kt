package com.egern.ast

import com.egern.util.forEach
import com.egern.visitor.Visitor

class StaticMethodCall(
    val classId: String,
    override val methodId: String,
    override val args: List<Expr>,
    lineNumber: Int,
    charPosition: Int
) : MethodCall(classId, methodId, args, lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        args.forEach({ it.accept(visitor) }, doBetween = { visitor.midVisit(this) })
        visitor.postVisit(this)
    }
}