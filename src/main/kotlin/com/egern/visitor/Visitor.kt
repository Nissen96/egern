package com.egern.visitor
import com.egern.ast.*

interface Visitor {
    fun visit(statement: Statement)
    fun visit(statement: ReturnStmt)
    fun visit(statement: Program)
    fun visit(statement: IfElse)
    fun visit(statement: FuncDecl)
    fun visit(statement: FuncCall)
    fun visit(statement: Expr)
    fun visit(statement: CompExpr)
    fun visit(statement: Block)
    fun visit(statement: ArithExpr)
}