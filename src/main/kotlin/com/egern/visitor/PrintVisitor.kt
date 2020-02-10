package com.egern.visitor

import com.egern.ast.*

class PrintVisitor : Visitor {
    override fun visit(statement: Statement) {
        println("I am statement")
    }

    override fun visit(returnStmt: ReturnStmt) {
        println("I am returnStmt")
    }

    override fun visit(program: Program) {
        println("I am root")
    }

    override fun visit(ifElse: IfElse) {
        println("I am ifElse")
    }

    override fun visit(funcDecl: FuncDecl) {
        println("I am funcDecl")
    }

    override fun visit(funcCall: FuncCall) {
        println("I am funcCall")
    }

    override fun visit(expr: Expr) {
        println("I am expr")
    }

    override fun visit(compExpr: CompExpr) {
        println("I am compExpr")
    }

    override fun visit(block: Block) {
        println("I am block")
    }

    override fun visit(arithExpr: ArithExpr) {
        println("I am arithExpr")
    }

}