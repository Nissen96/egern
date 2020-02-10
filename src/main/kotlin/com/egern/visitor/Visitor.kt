package com.egern.visitor
import com.egern.ast.*

interface Visitor {
    fun visit(statement: Statement)
    fun visit(returnStmt: ReturnStmt)
    fun visit(program: Program)
    fun visit(ifElse: IfElse)
    fun visit(funcDecl: FuncDecl)
    fun visit(funcCall: FuncCall)
    fun visit(expr: Expr)
    fun visit(compExpr: CompExpr)
    fun visit(block: Block)
    fun visit(arithExpr: ArithExpr)
}