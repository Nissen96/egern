package com.egern.ast

import com.egern.symbols.SymbolTable
import com.egern.types.ExprType
import com.egern.visitor.Visitor

class FuncDecl(
    val id: String,
    val params: List<Pair<String, ExprType>>,
    val returnType: ExprType,
    val children: List<ASTNode>,
    val classId: String? = null,
    lineNumber: Int, charPosition: Int
) :
    ASTNode(lineNumber, charPosition), Scopable {
    override lateinit var symbolTable: SymbolTable
    var variableCount: Int = 0
    lateinit var startLabel: String
    lateinit var endLabel: String

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
                is MethodCall -> it.accept(visitor)
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