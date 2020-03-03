package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.types.ExprType
import com.egern.visitor.Visitor

class FuncDecl(
    val id: String,
    val params: List<Pair<String, ExprType>>,
    val returnType: ExprType,
    private val funcBody: FuncBody
) :
    ASTNode(), Scopable {
    override lateinit var symbolTable: SymbolTable
    var variableCount: Int = 0
    lateinit var startLabel: String
    lateinit var endLabel: String

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        funcBody.accept(visitor)
        visitor.postVisit(this)
    }
}