package com.egern.ast

import MainBaseVisitor
import com.egern.types.*
import org.antlr.v4.runtime.tree.TerminalNode
import java.lang.Exception

class BuildASTVisitor : MainBaseVisitor<ASTNode>() {

    override fun visitProg(ctx: MainParser.ProgContext): ASTNode {
        val children = (ctx.children?.map { it.accept(this) } ?: emptyList()).toMutableList()
        children.add(ReturnStmt(IntExpr(0, -1, -1, isVoid = true), -1, -1))  // Implicit "return 0"
        return Program(children, ctx.start.line, ctx.start.charPositionInLine)
    }

    override fun visitStmt(ctx: MainParser.StmtContext): ASTNode {
        return when {
            ctx.ifElse() != null -> ctx.ifElse().accept(this)
            ctx.returnStmt() != null -> ctx.returnStmt().accept(this)
            ctx.printStmt() != null -> ctx.printStmt().accept(this)
            ctx.varAssign() != null -> ctx.varAssign().accept(this)
            ctx.varDecl() != null -> ctx.varDecl().accept(this)
            ctx.opAssign() != null -> ctx.opAssign().accept(this)
            ctx.whileLoop() != null -> ctx.whileLoop().accept(this)
            else -> throw Exception("Invalid Statement Type!")
        }
    }

    override fun visitReturnStmt(ctx: MainParser.ReturnStmtContext): ASTNode {
        // Return 0 implicitly if return value is undefined
        return ReturnStmt(
            (if (ctx.expr() != null) ctx.expr().accept(this) else IntExpr(0, -1, -1, isVoid = true)) as Expr,
            ctx.start.line,
            ctx.start.charPositionInLine
        )
    }

    override fun visitPrintStmt(ctx: MainParser.PrintStmtContext): ASTNode {
        return PrintStmt(ctx.expr()?.accept(this) as? Expr, ctx.start.line, ctx.start.charPositionInLine)
    }

    private fun getDefaultReturn(type: ExprType): ReturnStmt {
        val expr = when (type) {
            INT -> IntExpr(0, -1, -1, isVoid = false)
            BOOLEAN -> BooleanExpr(false, -1, -1)
            VOID -> IntExpr(0, -1, -1, isVoid = true)
            is ARRAY -> TODO()
        }
        return ReturnStmt(expr, -1, -1)
    }

    private fun getType(ctx: MainParser.TypeDeclContext): ExprType {
        return when {
            ctx.PRIMITIVE() != null -> getPrimitiveType(ctx.PRIMITIVE())
            ctx.VOID() != null -> VOID
            ctx.arrayType() != null -> ARRAY(getArrayType(ctx.arrayType()))
            else -> throw Exception("Cannot find type")
        }
    }

    private fun getPrimitiveType(primitive: TerminalNode): ExprType {
        return ExprType.primitives()[primitive.symbol.text] ?: error("Primitive type not found")
    }

    private fun getArrayType(ctx: MainParser.ArrayTypeContext): ExprType {
        return when {
            ctx.PRIMITIVE() != null -> getPrimitiveType(ctx.PRIMITIVE())
            ctx.arrayType() != null -> ARRAY(getArrayType(ctx.arrayType()))
            else -> throw Exception("Cannot find array entry type")
        }
    }

    override fun visitFuncDecl(ctx: MainParser.FuncDeclContext): ASTNode {
        val returnType = getType(ctx.typeDecl())
        val children = (ctx.funcBody().children?.map { it.accept(this) } ?: emptyList()).toMutableList()
        children.add(getDefaultReturn(returnType))  // Implicit "return 0"
        return FuncDecl(
            ctx.ID().text,
            ctx.paramList().ID().mapIndexed { index, it -> it.text to getType(ctx.paramList().typeDecl()[index]) },
            returnType,
            children,
            ctx.start.line,
            ctx.start.charPositionInLine
        )
    }

    override fun visitFuncCall(ctx: MainParser.FuncCallContext): ASTNode {
        return FuncCall(
            ctx.ID().text,
            ctx.argList().expr().map { it.accept(this) as Expr },
            ctx.start.line,
            ctx.start.charPositionInLine
        )
    }

    override fun visitVarDecl(ctx: MainParser.VarDeclContext): ASTNode {
        return VarDecl(
            ctx.ID().map { it.text },
            ctx.expr().accept(this) as Expr,
            ctx.start.line,
            ctx.start.charPositionInLine
        )
    }

    override fun visitVarAssign(ctx: MainParser.VarAssignContext): ASTNode {
        val ids = mutableListOf<String>()
        val indexExprs = mutableListOf<ArrayIndexExpr>()
        for (assignable in ctx.assignable()) {
            when {
                assignable.arrayIndexExpr() != null -> {
                    val expr = assignable.arrayIndexExpr()
                    indexExprs.add(
                        ArrayIndexExpr(
                            expr.idExpr().text,
                            expr.expr().map { visitExpr(it) as Expr },
                            ctx.start.line,
                            ctx.start.charPositionInLine
                        )
                    )
                }
                assignable.idExpr() != null -> ids.add(assignable.idExpr().text)
            }
        }
        return VarAssign(
            ids, indexExprs,
            ctx.expr().accept(this) as Expr,
            ctx.start.line,
            ctx.start.charPositionInLine
        )
    }

    override fun visitOpAssign(ctx: MainParser.OpAssignContext): ASTNode {
        return VarAssign(
            listOf(ctx.text),
            listOf(),
            ArithExpr(
                IdExpr(ctx.text, ctx.start.line, -1),
                ctx.expr().accept(this) as Expr,
                ArithOp.fromString(ctx.op.text[0].toString())!!,
                ctx.start.line,
                ctx.start.charPositionInLine
            ),
            ctx.start.line,
            ctx.start.charPositionInLine
        )
    }

    override fun visitIfElse(ctx: MainParser.IfElseContext): ASTNode {
        return IfElse(
            ctx.expr().accept(this) as Expr, ctx.block(0).accept(this) as Block,
            if (ctx.ifElse() != null) (ctx.ifElse().accept(this) as IfElse) else (ctx.block(1)?.accept(this) as? Block),
            ctx.start.line, ctx.start.charPositionInLine
        )
    }

    override fun visitWhileLoop(ctx: MainParser.WhileLoopContext): ASTNode {
        return WhileLoop(
            ctx.expr().accept(this) as Expr,
            ctx.block().accept(this) as Block,
            ctx.start.line, ctx.start.charPositionInLine
        )
    }

    override fun visitBlock(ctx: MainParser.BlockContext): ASTNode {
        return Block(
            ctx.children?.map { it.accept(this) } ?: emptyList(),
            ctx.start.line, ctx.start.charPositionInLine
        )
    }

    override fun visitExpr(ctx: MainParser.ExprContext): ASTNode {
        return when {
            ctx.booleanExpr() != null -> BooleanExpr(
                ctx.booleanExpr().BOOLEAN().text!!.toBoolean(),
                ctx.start.line,
                ctx.start.charPositionInLine
            )
            ctx.intExpr() != null -> IntExpr(
                ctx.intExpr().INT().text.toInt(),
                ctx.start.line,
                ctx.start.charPositionInLine
            )
            ctx.idExpr() != null -> IdExpr(ctx.idExpr().ID().text, ctx.start.line, ctx.start.charPositionInLine)
            ctx.parenExpr() != null -> visitParenExpr(ctx.parenExpr())
            ctx.funcCall() != null -> visitFuncCall(ctx.funcCall())
            ctx.arrayExpr() != null -> visitArrayExpr(ctx.arrayExpr())
            ctx.expr().size < 2 -> when (ctx.op.text) {
                ArithOp.MINUS.value -> ArithExpr(
                    IntExpr(-1, ctx.start.line, -1),
                    ctx.expr()[0].accept(this) as Expr,
                    ArithOp.TIMES,
                    ctx.start.line,
                    ctx.start.charPositionInLine
                )
                BooleanOp.NOT.value -> BooleanOpExpr(
                    ctx.expr()[0].accept(this) as Expr,
                    op = BooleanOp.NOT,
                    lineNumber = ctx.start.line,
                    charPosition = ctx.start.charPositionInLine
                )
                else -> throw Exception("Invalid Unary Expression Type!")
            }
            ctx.op.text in ArithOp.operators() -> visitArithExpr(ctx.expr(), ctx.op.text)
            ctx.op.text in CompOp.operators() -> visitCompExpr(ctx.expr(), ctx.op.text)
            ctx.op.text in BooleanOp.operators() -> visitBooleanExpr(ctx.expr(), ctx.op.text)
            else -> throw Exception("Invalid Expression Type!")
        }
    }

    override fun visitArrayExpr(ctx: MainParser.ArrayExprContext): ASTNode {
        return ArrayExpr(ctx.expr().map { visitExpr(it) as Expr }, ctx.start.line, ctx.start.charPositionInLine)
    }

    override fun visitParenExpr(ctx: MainParser.ParenExprContext): ASTNode {
        return ParenExpr(visitExpr(ctx.expr()) as Expr, ctx.start.line, ctx.start.charPositionInLine)
    }

    private fun visitBooleanExpr(exprs: List<MainParser.ExprContext>, op: String): ASTNode {
        return BooleanOpExpr(
            exprs[0].accept(this) as Expr,
            exprs[1].accept(this) as Expr,
            BooleanOp.fromString(op)!!,
            exprs[0].start.line, exprs[0].start.charPositionInLine
        )
    }

    private fun visitCompExpr(exprs: List<MainParser.ExprContext>, op: String): ASTNode {
        return CompExpr(
            exprs[0].accept(this) as Expr,
            exprs[1].accept(this) as Expr,
            CompOp.fromString(op)!!,
            exprs[0].start.line, exprs[0].start.charPositionInLine
        )
    }

    private fun visitArithExpr(exprs: List<MainParser.ExprContext>, op: String): ASTNode {
        return ArithExpr(
            exprs[0].accept(this) as Expr,
            exprs[1].accept(this) as Expr,
            ArithOp.fromString(op)!!,
            exprs[0].start.line, exprs[0].start.charPositionInLine
        )
    }
}