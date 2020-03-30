package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class Block(val children: List<ASTNode>, lineNumber: Int, charPosition: Int) : ASTNode(lineNumber, charPosition) {
    lateinit var symbolTable: SymbolTable

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
        visitor.postVisit(this)
    }
}
