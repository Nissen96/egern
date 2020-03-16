package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.types.ExprType
import com.egern.visitor.Visitor

class ClassDecl(
    val id: String,
    val varDecl: List<VarDecl<*>>,
    val funcDecl: List<FuncDecl>,
    lineNumber: Int, charPosition: Int
) :
    ASTNode(lineNumber, charPosition), Scopable {
    override lateinit var symbolTable: SymbolTable

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        varDecl.forEach {
            it.accept(visitor)
        }
        funcDecl.forEach {
            it.accept(visitor)
        }
        visitor.postVisit(this)
    }
}