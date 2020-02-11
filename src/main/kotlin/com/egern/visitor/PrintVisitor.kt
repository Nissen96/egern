package com.egern.visitor

import com.egern.ast.*

class PrintVisitor : Visitor {
    var counter = 0

    override fun preVisit(returnStmt: ReturnStmt) {
        println("${counter++} returnStmt: has expr ${returnStmt.expr != null}")
    }

    override fun preVisit(printStmt: PrintStmt) {
        println("${counter++} printStmt: has expr ${printStmt.expr != null}")
    }

    override fun preVisit(program: Program) {
        println("${counter++} Program: ${program.funcDecls.size} function declarations, ${program.stmts.size} statements")
    }

    override fun preVisit(ifElse: IfElse) {
        println("${counter++} IfElse: has else block ${ifElse.elseBlock != null}")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        println("${counter++} FuncDecl: My ID is ${funcDecl.id}, and I take ${funcDecl.params.size} parameters")
    }

    override fun preVisit(funcCall: FuncCall) {
        println("${counter++} FuncCall: My ID is ${funcCall.id}, and I take ${funcCall.args.size} parameters\" ")
    }

    override fun preVisit(compExpr: CompExpr) {
        println("${counter++} compExpr: My op is ${compExpr.op}")
    }

    override fun preVisit(block: Block) {
        println("${counter++} block: I have ${block.statements.size} statement(s)")
    }

    override fun preVisit(arithExpr: ArithExpr) {
        when {
            arithExpr.id != null -> println("${counter++} arithExpr: of ID ${arithExpr.id}")
            arithExpr.value != null -> println("${counter++} arithExpr: of int ${arithExpr.value}")
            arithExpr.op != null -> println("${counter++} arithExpr: of op ${arithExpr.op}")
        }
    }

    override fun preVisit(varAssign: VarAssign<*>) {
        println("${counter++} varAssign: I have ${varAssign.ids.size} id(s)")
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        println("${counter++} varDecl: I have ${varDecl.ids.size} id(s)")
    }

}