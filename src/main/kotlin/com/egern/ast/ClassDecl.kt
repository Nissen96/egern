package com.egern.ast

import com.egern.types.ExprType
import com.egern.visitor.Visitor

class ClassDecl(
    val id: String,
    val constructor: List<Pair<String, ExprType>>,
    val superclass: String,
    val fieldDecls: List<FieldDecl>,
    val funcDecls: List<FuncDecl>,
    lineNumber: Int, charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    lateinit var endLabel: String

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        fieldDecls.forEach {
            it.accept(visitor)
        }
        visitor.midVisit(this)
        funcDecls.forEach {
            it.accept(visitor)
        }
        visitor.postVisit(this)
    }
}