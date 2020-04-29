package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor

class VarDecl(
    val ids: List<String>,
    val expr: Expr,
    lineNumber: Int = -1,
    charPosition: Int = -1
) : Statement(lineNumber, charPosition) {
    lateinit var symbolTable: SymbolTable

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}
