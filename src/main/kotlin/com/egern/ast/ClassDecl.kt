package com.egern.ast

import com.egern.types.ExprType
import com.egern.visitor.Visitor

class ClassDecl(
    val id: String,
    val constructor: List<Pair<String, ExprType>>,
    val superclass: String,
    val varDecl: List<VarDecl<*>>,
    val funcDecl: List<FuncDecl>,
    lineNumber: Int, charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    lateinit var endLabel: String

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        varDecl.forEach {
            it.accept(visitor)
        }
        visitor.midVisit(this)
        funcDecl.forEach {
            it.accept(visitor)
        }
        visitor.postVisit(this)
    }
}