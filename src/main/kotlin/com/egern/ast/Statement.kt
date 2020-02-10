package com.egern.ast

import com.egern.visitor.Visitor

open class Statement : ASTNode() {
    override fun accept(visitor: Visitor) {
        return visitor.visit(this)
    }
}