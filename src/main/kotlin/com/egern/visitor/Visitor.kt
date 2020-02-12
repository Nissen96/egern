package com.egern.visitor

import com.egern.ast.*

interface Visitor {
    fun preVisit(returnStmt: ReturnStmt) {}
    fun preVisit(printStmt: PrintStmt) {}
    fun preVisit(program: Program) {}
    fun preVisit(ifElse: IfElse) {}
    fun preVisit(funcDecl: FuncDecl) {}
    fun preVisit(funcCall: FuncCall) {}
    fun preVisit(compExpr: CompExpr) {}
    fun preVisit(block: Block) {}
    fun preVisit(arithExpr: ArithExpr) {}
    fun preVisit(varAssign: VarAssign<*>) {}
    fun preVisit(varDecl: VarDecl<*>) {}

    fun midVisit(returnStmt: ReturnStmt) {}
    fun midVisit(printStmt: PrintStmt) {}
    fun midVisit(program: Program) {}
    fun midVisit(ifElse: IfElse) {}
    fun midVisit(funcDecl: FuncDecl) {}
    fun midVisit(funcCall: FuncCall) {}
    fun midVisit(compExpr: CompExpr) {}
    fun midVisit(block: Block) {}
    fun midVisit(arithExpr: ArithExpr) {}
    fun midVisit(varAssign: VarAssign<*>) {}
    fun midVisit(varDecl: VarDecl<*>) {}

    fun postVisit(printStmt: PrintStmt) {}
    fun postVisit(returnStmt: ReturnStmt) {}
    fun postVisit(program: Program) {}
    fun postVisit(ifElse: IfElse) {}
    fun postVisit(funcDecl: FuncDecl) {}
    fun postVisit(funcCall: FuncCall) {}
    fun postVisit(compExpr: CompExpr) {}
    fun postVisit(block: Block) {}
    fun postVisit(arithExpr: ArithExpr) {}
    fun postVisit(varAssign: VarAssign<*>) {}
    fun postVisit(varDecl: VarDecl<*>) {}
}