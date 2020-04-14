package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.types.ExprType
import com.egern.visitor.Visitor
import java.util.*

class FuncDecl(
    val id: String,
    val params: List<Pair<String, ExprType>>,
    val returnType: ExprType,
    val stmts: List<ASTNode>,
    val funcDecls: List<FuncDecl>,
    val modifiers: EnumSet<Modifier>,
    lineNumber: Int, charPosition: Int
) :
    ASTNode(lineNumber, charPosition) {
    lateinit var symbolTable: SymbolTable
    var variableCount: Int = 0
    lateinit var startLabel: String
    lateinit var endLabel: String

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        stmts.forEach {
            visitor.preStmtVisit()
            it.accept(visitor)
            visitor.postStmtVisit()
        }
        funcDecls.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}