package com.egern.ast

import MainBaseVisitor
import java.lang.Exception

class BuildASTVisitor : MainBaseVisitor<ASTNode>() {

    override fun visitProg(ctx: MainParser.ProgContext): ASTNode {
        return Program(
            ctx.funcDecl().map { it.accept(this) as FuncDecl },
            ctx.stmt().map { it.accept(this) as Statement },
            ctx.funcCall().map { it.accept(this) as FuncCall }
        )
    }

    override fun visitStmt(ctx: MainParser.StmtContext): ASTNode {
        return when {
            ctx.ifElse() != null -> ctx.ifElse().accept(this)
            ctx.returnStmt() != null -> ctx.returnStmt().accept(this)
            ctx.printStmt() != null -> ctx.printStmt().accept(this)
            ctx.varAssign() != null -> ctx.varAssign().accept(this)
            ctx.varDecl() != null -> ctx.varDecl().accept(this)
            else -> throw Exception("Invalid Statement Type!")
        }
    }

    override fun visitReturnStmt(ctx: MainParser.ReturnStmtContext): ASTNode {
        return ReturnStmt(ctx.expr().accept(this) as Expr)
    }

    override fun visitPrintStmt(ctx: MainParser.PrintStmtContext): ASTNode {
        return PrintStmt(ctx.expr().accept(this) as Expr)
    }

    override fun visitFuncDecl(ctx: MainParser.FuncDeclContext): ASTNode {
        return FuncDecl(ctx.ID().text, ctx.paramList().ID().map { it.text }, ctx.block().accept(this) as Block)
    }

    override fun visitFuncCall(ctx: MainParser.FuncCallContext): ASTNode {
        return FuncCall(ctx.ID().text, ctx.argList().ID().map { it.text })
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
            ctx.block(1).accept(this) as Block
        )
    }

    override fun visitBlock(ctx: MainParser.BlockContext): ASTNode {
        return Block(ctx.stmt().map { it.accept(this) as Statement })
    }

    override fun visitExpr(ctx: MainParser.ExprContext): ASTNode {
        return when {
            ctx.arithExpr() != null -> visitArithExpr(ctx.arithExpr())
            ctx.compExpr()  != null -> visitCompExpr(ctx.compExpr())
            ctx.funcCall()  != null -> visitFuncCall(ctx.funcCall())
            else -> throw Exception("Invalid Expression Type!")
        }
    }

    override fun visitCompExpr(ctx: MainParser.CompExprContext): ASTNode {
        return CompExpr(
            ctx.arithExpr(0).accept(this) as ArithExpr,
            ctx.arithExpr(1).accept(this) as ArithExpr,
            ctx.op.text
        )
    }

    override fun visitArithExpr(ctx: MainParser.ArithExprContext): ASTNode {
        return when {
            ctx.ID() != null -> ArithExpr(ctx.ID().text)
            ctx.INT() != null -> ArithExpr(ctx.INT().text.toInt())
            ctx.arithExpr().size == 1 -> ArithExpr(ctx.arithExpr(0).accept(this) as ArithExpr)
            ctx.arithExpr().size == 2 -> ArithExpr(
                ctx.arithExpr(0).accept(this) as ArithExpr,
                ctx.arithExpr(1).accept(this) as ArithExpr,
                ctx.op.text
            )
            else -> throw Exception("Invalid Arithmetic Expression")
        }
    }
}