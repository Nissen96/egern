package com.egern.ast

import MainBaseVisitor
import java.lang.Exception

class BuildASTVisitor : MainBaseVisitor<ASTNode>() {

    override fun visitProg(ctx: MainParser.ProgContext): ASTNode {
        return Program(
            ctx.funcDecl().map { it.accept(this) as FuncDecl },
            ctx.stmt().map { it.accept(this) as Statement })
    }

    override fun visitStmt(ctx: MainParser.StmtContext): ASTNode {
        return when {
            ctx.funcCall() != null -> ctx.funcCall().accept(this)
            ctx.ifElse() != null -> ctx.ifElse().accept(this)
            ctx.returnStmt() != null -> ctx.returnStmt().accept(this)
            ctx.printStmt() != null -> ctx.printStmt().accept(this)
            ctx.varAssign() != null -> ctx.varAssign().accept(this)
            ctx.varDecl() != null -> ctx.varDecl().accept(this)
            else -> throw Exception("Invalid Statement Type!")
        }
    }

    override fun visitReturnStmt(ctx: MainParser.ReturnStmtContext): ASTNode {
        val exprOrFuncCall = ctx.exprOrFuncCall();
        return when {
            exprOrFuncCall?.expr() != null -> {
                ReturnStmt(exprOrFuncCall.expr().accept(this) as Expr)
            }
            exprOrFuncCall?.funcCall() != null -> {
                ReturnStmt(exprOrFuncCall.funcCall().accept(this) as FuncCall)
            }
            else -> {
                ReturnStmt()
            }
        }
    }

    override fun visitPrintStmt(ctx: MainParser.PrintStmtContext): ASTNode {
        val exprOrFuncCall = ctx.exprOrFuncCall();
        return when {
            exprOrFuncCall?.expr() != null -> {
                PrintStmt(exprOrFuncCall.expr().accept(this) as Expr)
            }
            exprOrFuncCall?.funcCall() != null -> {
                PrintStmt(exprOrFuncCall.funcCall().accept(this) as FuncCall)
            }
            else -> {
                throw Exception("Invalid Statement Type in Print!");
            }
        }
    }

    override fun visitFuncDecl(ctx: MainParser.FuncDeclContext): ASTNode {
        return FuncDecl(ctx.ID().text, ctx.paramList().ID().map { it.text }, ctx.block().accept(this) as Block)
    }

    override fun visitFuncCall(ctx: MainParser.FuncCallContext): ASTNode {
        return FuncCall(ctx.ID().text, ctx.argList().ID().map { it.text })
    }

    override fun visitVarDecl(ctx: MainParser.VarDeclContext): ASTNode {
        val assign = ctx.varAssign();
        val exprOrFuncCall = assign.exprOrFuncCall();
        return if (exprOrFuncCall.expr() != null) {
            VarDecl(assign.ID().map { it.text }, exprOrFuncCall.expr().accept(this) as Expr)
        } else {
            VarDecl(assign.ID().map { it.text }, exprOrFuncCall.funcCall().accept(this) as FuncCall)
        }
    }

    override fun visitVarAssign(ctx: MainParser.VarAssignContext): ASTNode {
        val exprOrFuncCall = ctx.exprOrFuncCall();
        return if (exprOrFuncCall.expr() != null) {
            VarAssign(ctx.ID().map { it.text }, exprOrFuncCall.expr().accept(this) as Expr)
        } else {
            VarAssign(ctx.ID().map { it.text }, exprOrFuncCall.funcCall().accept(this) as FuncCall)
        }
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
        return if (ctx.arithExpr() != null) {
            visitArithExpr(ctx.arithExpr())
        } else {
            visitCompExpr(ctx.compExpr())
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