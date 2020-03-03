package com.egern.visitor

import com.egern.ast.*

interface Visitor {
    fun midVisit(arithExpr: ArithExpr) {}
    fun postVisit(arithExpr: ArithExpr) {}

    fun preVisit(block: Block) {}
    fun preFuncCallVisit(block: Block) {}
    fun postFuncCallVisit(block: Block) {}
    fun postVisit(block: Block) {}

    fun visit(booleanExpr: BooleanExpr) {}

    fun midVisit(booleanOpExpr: BooleanOpExpr) {}
    fun postVisit(booleanOpExpr: BooleanOpExpr) {}

    fun midVisit(compExpr: CompExpr) {}
    fun postVisit(compExpr: CompExpr) {}

    fun preVisit(funcCall: FuncCall) {}
    fun midVisit(funcCall: FuncCall) {}
    fun postVisit(funcCall: FuncCall) {}

    fun preVisit(funcDecl: FuncDecl) {}
    fun preFuncCallVisit(funcDecl: FuncDecl) {}
    fun postFuncCallVisit(funcDecl: FuncDecl) {}
    fun postVisit(funcDecl: FuncDecl) {}

    fun visit(idExpr: IdExpr) {}

    fun preVisit(ifElse: IfElse) {}
    fun preMidVisit(ifElse: IfElse) {}
    fun postMidVisit(ifElse: IfElse) {}
    fun postVisit(ifElse: IfElse) {}

    fun visit(intExpr: IntExpr) {}

    fun preVisit(parenExpr: ParenExpr) {}
    fun postVisit(parenExpr: ParenExpr) {}

    fun preVisit(printStmt: PrintStmt) {}
    fun postVisit(printStmt: PrintStmt) {}

    fun preVisit(program: Program) {}
    fun preFuncCallVisit(program: Program) {}
    fun postFuncCallVisit(program: Program) {}
    fun midVisit(program: Program) {}
    fun postVisit(program: Program) {}

    fun preVisit(returnStmt: ReturnStmt) {}
    fun postVisit(returnStmt: ReturnStmt) {}

    fun preVisit(varAssign: VarAssign<*>) {}
    fun postVisit(varAssign: VarAssign<*>) {}

    fun preVisit(varDecl: VarDecl<*>) {}
    fun postVisit(varDecl: VarDecl<*>) {}

    fun preVisit(whileLoop: WhileLoop) {}
    fun midVisit(whileLoop: WhileLoop) {}
    fun postVisit(whileLoop: WhileLoop) {}
}