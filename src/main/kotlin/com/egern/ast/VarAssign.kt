package com.egern.ast

import com.egern.visitor.Visitable
import com.egern.visitor.Visitor

class VarAssign<T: Visitable>(val ids: List<String>, val expr: T) : Statement() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
        expr.accept(visitor)
    }
}
typealias VarDecl<T> = VarAssign<T>