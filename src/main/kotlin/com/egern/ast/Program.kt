package com.egern.ast

import com.egern.visitor.Visitor

class Program(
    val stmts: List<ASTNode>,
    var funcDecls: List<FuncDecl>,
    val classDecls: List<ClassDecl>,
    val interfaceDecls: List<InterfaceDecl>,
    lineNumber: Int,
    charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    var variableCount: Int = 0
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        (interfaceDecls + classDecls).forEach { it.accept(visitor) }
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