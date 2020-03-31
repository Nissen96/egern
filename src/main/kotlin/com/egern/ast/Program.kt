package com.egern.ast

import com.egern.visitor.Visitor

class Program(
    val stmts: List<ASTNode>,
    val funcDecls: List<FuncDecl>,
    val classDecls: List<ClassDecl>,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    var variableCount: Int = 0
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        classDecls.forEach { it.accept(visitor) }
        stmts.forEach {
            visitor.preStmtVisit()
            it.accept(visitor)
            visitor.postStmtVisit()
        }
        visitor.midVisit(this)
        funcDecls.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}