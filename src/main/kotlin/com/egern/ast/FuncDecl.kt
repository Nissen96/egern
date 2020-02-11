package com.egern.ast

import com.egern.visitor.Visitor

class FuncDecl(val id: String, val params: List<String>, val block: Block) : ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        block.accept(visitor)
        visitor.postVisit(this)
    }
}