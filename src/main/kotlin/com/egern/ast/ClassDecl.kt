package com.egern.ast

import com.egern.visitor.Visitor

class ClassDecl(
    val id: String,
    val varDecl: List<VarDecl<*>>,
    val funcDecl: List<FuncDecl>,
    lineNumber: Int, charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    //lateinit var fields

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