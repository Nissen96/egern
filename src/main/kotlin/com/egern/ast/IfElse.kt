package com.egern.ast

import com.egern.visitor.Visitor

class IfElse(val expression: Expr, val ifBlock: Block, val elseBlock: Block?) : Statement() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expression.accept(visitor)
        ifBlock.accept(visitor)
        elseBlock?.accept(visitor)
    }
}