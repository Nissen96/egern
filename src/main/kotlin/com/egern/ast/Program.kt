package com.egern.ast

import com.egern.visitor.Visitor

class Program(val children: List<ASTNode>, lineNumber: Int, charPosition: Int) : ASTNode(lineNumber, charPosition) {
    var variableCount: Int = 0
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
        visitor.midVisit(this)
        children.forEach {
            when (it) {
                is FuncDecl -> it.accept(visitor)
            }
        }
        visitor.postVisit(this)
    }
}