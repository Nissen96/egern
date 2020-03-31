package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class Block(val stmts: List<ASTNode>, lineNumber: Int, charPosition: Int) : ASTNode(lineNumber, charPosition) {
    lateinit var symbolTable: SymbolTable

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        stmts.forEach {
            visitor.preStmtVisit()
            it.accept(visitor)
            visitor.postStmtVisit()
        }
        visitor.postVisit(this)
    }
}
