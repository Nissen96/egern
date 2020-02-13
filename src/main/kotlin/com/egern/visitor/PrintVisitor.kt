package com.egern.visitor

import com.egern.ast.*

class PrintVisitor : Visitor {
    var counter = 0
    var level = 0

    fun printIndented(text: String) {
        print(counter++)
        for (i in 1..level) {
            print("\t")
        }
        println(" $text")
    }

    override fun preVisit(returnStmt: ReturnStmt) {
        printIndented("ReturnStmt: has expr ${returnStmt.expr != null}")
    }

    override fun preVisit(printStmt: PrintStmt) {
        printIndented("PrintStmt: has expr ${printStmt.expr != null}")
    }

    override fun preVisit(program: Program) {
        printIndented(
            "Program: " +
                    "${program.funcDecls.size} function declaration(s), " +
                    "${program.stmts.size} statement(s), " +
                    "${program.funcCalls.size} outer function call(s)"
        )
    }

    override fun preVisit(ifElse: IfElse) {
        printIndented("IfElse: has else block ${ifElse.elseBlock != null}")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        printIndented("FuncDecl: My ID is ${funcDecl.id}, and I take ${funcDecl.params.size} parameter(s)")
    }

    override fun preVisit(funcCall: FuncCall) {
        printIndented("FuncCall: My ID is ${funcCall.id}, and I take ${funcCall.args.size} parameter(s)")
        level++
    }

    override fun postVisit(funcCall: FuncCall) {
        level--
    }

    override fun preVisit(idExpr: IdExpr) {
        printIndented("IdExpr: My ID is ${idExpr.id}")
    }

    override fun preVisit(intExpr: IntExpr) {
        printIndented("IntExpr: My value is ${intExpr.value}")
    }

    override fun preVisit(parenExpr: ParenExpr) {
        printIndented("ParenExpr: I am a parenthesized expression")
    }

    override fun preVisit(compExpr: CompExpr) {
        printIndented("CompExpr: My operator is ${compExpr.op}")
    }

    override fun preVisit(block: Block) {
        printIndented(
            "Block: I have ${block.statements.size} statement(s) and " +
                    "${block.funcCalls.size} function call(s)"
        )
        level++
    }

    override fun postVisit(block: Block) {
        level--
    }

    override fun preVisit(arithExpr: ArithExpr) {
        printIndented("ArithExpr: My operator is ${arithExpr.op}")
    }

    override fun preVisit(varAssign: VarAssign<*>) {
        printIndented("VarAssign: I have ${varAssign.ids.size} id(s)")
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        printIndented("VarDecl: I have ${varDecl.ids.size} id(s)")
        level++
    }

    override fun postVisit(varDecl: VarDecl<*>) {
        level--
    }

}