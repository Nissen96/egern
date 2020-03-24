package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitable
import com.egern.visitor.Visitor

class VarDecl<T : Expr>(
    val ids: List<String>,
    val expr: T,
    val classId: String? = null,
    lineNumber: Int,
    charPosition: Int
) :
    Statement(lineNumber, charPosition), Scopable {
    lateinit var staticDataField: String
    override lateinit var symbolTable: SymbolTable

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}
