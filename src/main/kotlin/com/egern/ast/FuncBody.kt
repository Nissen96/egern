package com.egern.ast

import com.egern.visitor.Visitor

class FuncBody(val children: List<ASTNode>, lineNumber: Int, charPosition: Int) :
    ASTNode(lineNumber, charPosition) {
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
