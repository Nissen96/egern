package com.egern.visitor

import com.egern.ast.*

class PrintVisitor : Visitor {
    var counter = 0

    override fun preVisit(returnStmt: ReturnStmt) {
        println("${counter++} ReturnStmt: has expr ${returnStmt.expr != null}")
    }

    override fun preVisit(printStmt: PrintStmt) {
        println("${counter++} PrintStmt: has expr ${printStmt.expr != null}")
    }

    override fun preVisit(program: Program) {
        println(
            "${counter++} Program: " +
                    "${program.funcDecls.size} function declaration(s), " +
                    "${program.stmts.size} statement(s), " +
                    "${program.funcCalls.size} outer function call(s)"
        )
    }

    override fun preVisit(ifElse: IfElse) {
        println("${counter++} IfElse: has else block ${ifElse.elseBlock != null}")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        println("${counter++} FuncDecl: My ID is ${funcDecl.id}, and I take ${funcDecl.params.size} parameter(s)")
    }

    override fun preVisit(funcCall: FuncCall) {
        println("${counter++} FuncCall: My ID is ${funcCall.id}, and I take ${funcCall.args.size} parameter(s)")
    }

    override fun preVisit(idExpr: IdExpr) {
        println("${counter++} IdExpr: My ID is ${idExpr.id}")
    }

    override fun preVisit(intExpr: IntExpr) {
        println("${counter++} IntExpr: My value is ${intExpr.value}")
    }

    override fun preVisit(parenExpr: ParenExpr) {
        println("${counter++} ParenExpr: I am a parenthesized expression")
    }

    override fun preVisit(compExpr: CompExpr) {
        println("${counter++} compExpr: My operator is ${compExpr.op}")
    }

    override fun preVisit(block: Block) {
        println(
            "${counter++} block: I have ${block.statements.size} statement(s) and " +
                    "${block.funcCalls.size} function call(s)"
        )
    }

    override fun preVisit(arithExpr: ArithExpr) {
        println("${counter++} arithExpr: My operator is ${arithExpr.op}")
    }

    override fun preVisit(varAssign: VarAssign<*>) {
        println("${counter++} varAssign: I have ${varAssign.ids.size} id(s)")
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        println("${counter++} varDecl: I have ${varDecl.ids.size} id(s)")
    }

}