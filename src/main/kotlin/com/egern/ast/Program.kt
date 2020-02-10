package com.egern.ast

import com.egern.visitor.Visitor

class Program(val funcDecls: List<FuncDecl>, val stmts: List<Statement>) : ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
        for (decls in funcDecls) {
            decls.accept(visitor)
        }
        for (stmt in stmts) {
            stmt.accept(visitor)
        }
    }
}