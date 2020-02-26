package com.egern.ast

import com.egern.visitor.Visitor

class FuncBody(val children: List<ASTNode>) :
    ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        children.forEach {
            when (it) {
                is Statement -> it.accept(visitor)
                is FuncCall -> {
                    visitor.preFuncCallVisit(this)
                    it.accept(visitor)
                    visitor.postFuncCallVisit(this)
                }
            }
        }
        children.forEach {
            when (it) {
                is FuncDecl -> it.accept(visitor)
            }
        }
        visitor.postVisit(this)
    }
}
