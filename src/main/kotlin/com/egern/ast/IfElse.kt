package com.egern.ast

import com.egern.visitor.Visitor

class IfElse(val expression: Expr, val ifBlock: Block, val elseBlock: ASTNode?) : Statement() {
    lateinit var elseLabel: String
    lateinit var endLabel: String
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expression.accept(visitor)
        visitor.preMidVisit(this)
        ifBlock.accept(visitor)
        visitor.postMidVisit(this)
        elseBlock?.accept(visitor)
        visitor.postVisit(this)
    }
}