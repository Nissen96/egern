package com.egern.ast

import com.egern.visitor.Visitor

class WhileLoop(val expression: Expr, val block: Block) : Statement() {
    lateinit var startLabel: String
    lateinit var endLabel: String
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expression.accept(visitor)
        visitor.midVisit(this)
        block.accept(visitor)
        visitor.postVisit(this)
    }
}