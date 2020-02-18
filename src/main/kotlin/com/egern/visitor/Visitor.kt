package com.egern.visitor

import com.egern.ast.*

interface Visitor {
    fun midVisit(arithExpr: ArithExpr) {}

    fun preVisit(funcBody: FuncBody) {}
    fun postVisit(funcBody: FuncBody) {}

    fun preVisit(block: Block) {}
    fun postVisit(block: Block) {}

    fun midVisit(compExpr: CompExpr) {}

    fun preVisit(funcCall: FuncCall) {}
    fun midVisit(funcCall: FuncCall) {}
    fun postVisit(funcCall: FuncCall) {}

    fun preVisit(funcDecl: FuncDecl) {}
    fun postVisit(funcDecl: FuncDecl) {}

    fun preVisit(idExpr: IdExpr) {}

    fun preVisit(ifElse: IfElse) {}
    fun preMidVisit(ifElse: IfElse) {}
    fun postMidVisit(ifElse: IfElse) {}
    fun postVisit(ifElse: IfElse) {}

    fun preVisit(intExpr: IntExpr) {}

    fun preVisit(parenExpr: ParenExpr) {}
    fun postVisit(parenExpr: ParenExpr) {}

    fun preVisit(printStmt: PrintStmt) {}
    fun postVisit(printStmt: PrintStmt) {}

    fun preVisit(program: Program) {}
    fun midVisit(program: Program) {}
    fun postVisit(program: Program) {}

    fun preVisit(returnStmt: ReturnStmt) {}
    fun postVisit(returnStmt: ReturnStmt) {}

    fun preVisit(varAssign: VarAssign<*>) {}
    fun postVisit(varAssign: VarAssign<*>) {}

    fun preVisit(varDecl: VarDecl<*>) {}
    fun postVisit(varDecl: VarDecl<*>) {}
}