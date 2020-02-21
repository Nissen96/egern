package com.egern.ast

import com.egern.visitor.Visitor

class FuncBody(val children: List<ASTNode>) :
    ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        children.forEach {
            when (it) {
                is FuncDecl -> it.accept(visitor)
                is Statement -> it.accept(visitor)
                is FuncCall -> {
                    visitor.preMidVisit(this)
                    it.accept(visitor)
                    visitor.postMidVisit(this)
                }
            }
        }
        visitor.postVisit(this)
    }
}
