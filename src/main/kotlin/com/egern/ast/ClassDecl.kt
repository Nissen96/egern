package com.egern.ast

import com.egern.types.ExprType
import com.egern.visitor.Visitor

class ClassDecl(
    val id: String,
    val constructor: List<Triple<String, ExprType, Modifier?>>,
    val superclass: String?,
    val superclassArgs: List<Expr>?,
    val fieldDecls: List<FieldDecl>,
    val methods: List<FuncDecl>,
    lineNumber: Int = -1,
    charPosition: Int = -1
) : ASTNode(lineNumber, charPosition) {
    lateinit var endLabel: String

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        fieldDecls.forEach {
            visitor.preStmtVisit()
            it.accept(visitor)
            visitor.postStmtVisit()
        }
        visitor.midVisit(this)
        methods.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}