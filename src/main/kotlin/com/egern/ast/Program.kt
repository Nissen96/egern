package com.egern.ast

import com.egern.visitor.Visitor

class Program(val funcDecls: List<FuncDecl>, val stmts: List<Statement>, val funcCalls: List<FuncCall>) : ASTNode() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        for (decl in funcDecls) {
            decl.accept(visitor)
        }
        for (stmt in stmts) {
            stmt.accept(visitor)
        }
        for (call in funcCalls) {
            call.accept(visitor)
        }
    }
}