package com.egern.ast

import com.egern.visitor.Visitor

class FuncDecl(val id: String, val params: List<String>, val block: Block) : ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
        block.accept(visitor)
    }
}