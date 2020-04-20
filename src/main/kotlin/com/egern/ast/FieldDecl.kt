package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.visitor.Visitor
import java.util.EnumSet

class FieldDecl(
    val ids: List<String>,
    val expr: Expr,
    val modifiers: EnumSet<Modifier>,
    lineNumber: Int,
    charPosition: Int
) :
    Statement(lineNumber, charPosition) {
    lateinit var staticDataField: String
    lateinit var symbolTable: SymbolTable

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr.accept(visitor)
        visitor.postVisit(this)
    }
}
