package com.egern.visitor

import com.egern.ast.*

class PrintVisitor : Visitor {

    var counter = 0

    override fun visit(statement: Statement) {
        println("${counter++} Statement")
    }

    override fun visit(returnStmt: ReturnStmt) {
        println("${counter++} returnStmt: has expr ${returnStmt.expr != null}")
    }

    override fun visit(program: Program) {
        println("${counter++} Program: ${program.funcDecls.size} function declarations, ${program.stmts.size} statements")
    }

    override fun visit(ifElse: IfElse) {
        println("${counter++} IfElse: has else block ${ifElse.elseBlock != null}")
    }

    override fun visit(funcDecl: FuncDecl) {
        println("${counter++} FuncDecl: My ID is ${funcDecl.id}, and I take ${funcDecl.params.size} parameters")
    }

    override fun visit(funcCall: FuncCall) {
        println("${counter++} FuncCall: My ID is ${funcCall.id}, and I take ${funcCall.args.size} parameters\" ")
    }

    override fun visit(expr: Expr) {
        println("${counter++} expr")
    }

    override fun visit(compExpr: CompExpr) {
        println("${counter++} compExpr: My op is ${compExpr.op}")
    }

    override fun visit(block: Block) {
        println("${counter++} block: I have ${block.statements.size} statement(s)")
    }

    override fun visit(arithExpr: ArithExpr) {
        when {
            arithExpr.id != null -> println("${counter++} arithExpr: of ID ${arithExpr.id}")
            arithExpr.value != null -> println("${counter++} arithExpr: of int ${arithExpr.value}")
            arithExpr.op != null -> println("${counter++} arithExpr: of op ${arithExpr.op}")
        }
    }

}