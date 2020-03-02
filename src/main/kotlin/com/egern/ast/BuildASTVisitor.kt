package com.egern.ast

import MainBaseVisitor
import java.lang.Exception

class BuildASTVisitor : MainBaseVisitor<ASTNode>() {

    override fun visitProg(ctx: MainParser.ProgContext): ASTNode {
        val children = (ctx.children?.map { it.accept(this) } ?: emptyList()).toMutableList()
        children.add(ReturnStmt(IntExpr(0)))  // Implicit "return 0"
        return Program(children)
    }

    override fun visitStmt(ctx: MainParser.StmtContext): ASTNode {
        return when {
            ctx.ifElse() != null -> ctx.ifElse().accept(this)
            ctx.returnStmt() != null -> ctx.returnStmt().accept(this)
            ctx.printStmt() != null -> ctx.printStmt().accept(this)
            ctx.varAssign() != null -> ctx.varAssign().accept(this)
            ctx.varDecl() != null -> ctx.varDecl().accept(this)
            ctx.whileLoop() != null -> ctx.whileLoop().accept(this)
            else -> throw Exception("Invalid Statement Type!")
        }
    }

    override fun visitReturnStmt(ctx: MainParser.ReturnStmtContext): ASTNode {
        // Return 0 implicitly if return value is undefined
        return ReturnStmt((if (ctx.expr() != null) ctx.expr().accept(this) else IntExpr(0)) as Expr)
    }

    override fun visitPrintStmt(ctx: MainParser.PrintStmtContext): ASTNode {
        return PrintStmt(ctx.expr()?.accept(this) as? Expr)
    }

    override fun visitFuncDecl(ctx: MainParser.FuncDeclContext): ASTNode {
        return FuncDecl(ctx.ID().text, ctx.paramList().ID().map { it.text }, ctx.funcBody().accept(this) as FuncBody)
    }

    override fun visitFuncBody(ctx: MainParser.FuncBodyContext): ASTNode {
        val children = (ctx.children?.map { it.accept(this) } ?: emptyList()).toMutableList()
        children.add(ReturnStmt(IntExpr(0)))  // Implicit "return 0"
        return FuncBody(children)
    }

    override fun visitFuncCall(ctx: MainParser.FuncCallContext): ASTNode {
        return FuncCall(ctx.ID().text, ctx.argList().expr().map { it.accept(this) as Expr })
    }

    override fun visitVarDecl(ctx: MainParser.VarDeclContext): ASTNode {
        val assign = ctx.varAssign()
        return VarDecl(assign.ID().map { it.text }, assign.expr().accept(this) as Expr)
    }

    override fun visitVarAssign(ctx: MainParser.VarAssignContext): ASTNode {
        return VarAssign(ctx.ID().map { it.text }, ctx.expr().accept(this) as Expr)
    }

    override fun visitIfElse(ctx: MainParser.IfElseContext): ASTNode {
        return IfElse(
            ctx.expr().accept(this) as Expr, ctx.block(0).accept(this) as Block,
            if (ctx.ifElse() != null) (ctx.ifElse().accept(this) as IfElse) else (ctx.block(1)?.accept(this) as? Block)
        )
    }

    override fun visitWhileLoop(ctx: MainParser.WhileLoopContext): ASTNode {
        return WhileLoop(
            ctx.expr().accept(this) as Expr,
            ctx.block().accept(this) as Block
        )
    }

    override fun visitBlock(ctx: MainParser.BlockContext): ASTNode {
        return Block(
            ctx.children?.map { it.accept(this) } ?: emptyList()
        )
    }

    override fun visitExpr(ctx: MainParser.ExprContext): ASTNode {
        return when {
            ctx.booleanExpr() != null -> BooleanExpr(ctx.booleanExpr().BOOLEAN().text.toBoolean())
            ctx.intExpr() != null -> IntExpr(ctx.intExpr().INT().text.toInt())
            ctx.idExpr() != null -> IdExpr(ctx.idExpr().ID().text)
            ctx.parenExpr() != null -> visitParenExpr(ctx.parenExpr())
            ctx.funcCall() != null -> visitFuncCall(ctx.funcCall())
            ctx.expr().size < 2 -> ArithExpr(IntExpr(-1), ctx.expr()[0].accept(this) as Expr, ArithOp.TIMES)
            ctx.op.text in ArithOp.operators() -> visitArithExpr(ctx.expr(), ctx.op.text)
            ctx.op.text in CompOp.operators() -> visitCompExpr(ctx.expr(), ctx.op.text)
            ctx.op.text in BooleanOp.operators() -> visitBooleanExpr(ctx.expr(), ctx.op.text)
            else -> throw Exception("Invalid Expression Type!")
        }
    }

    override fun visitParenExpr(ctx: MainParser.ParenExprContext): ASTNode {
        return ParenExpr(visitExpr(ctx.expr()) as Expr)
    }

    private fun visitBooleanExpr(exprs: List<MainParser.ExprContext>, op: String): ASTNode {
        return BooleanOpExpr(
            exprs[0].accept(this) as Expr,
            exprs[1].accept(this) as Expr,
            BooleanOp.fromString(op)!!
        )
    }

    private fun visitCompExpr(exprs: List<MainParser.ExprContext>, op: String): ASTNode {
        return CompExpr(
            exprs[0].accept(this) as Expr,
            exprs[1].accept(this) as Expr,
            CompOp.fromString(op)!!
        )
    }

    private fun visitArithExpr(exprs: List<MainParser.ExprContext>, op: String): ASTNode {
        return ArithExpr(
            exprs[0].accept(this) as Expr,
            exprs[1].accept(this) as Expr,
            ArithOp.fromString(op)!!
        )
    }
}