package com.egern.ast

import MainBaseVisitor
import com.egern.types.*
import org.antlr.v4.runtime.tree.TerminalNode
import java.lang.Exception

class BuildASTVisitor : MainBaseVisitor<ASTNode>() {

    override fun visitProg(ctx: MainParser.ProgContext): ASTNode {
        val children = (ctx.children?.map { it.accept(this) } ?: emptyList()).toMutableList()
        children.add(ReturnStmt(IntExpr(0, isVoid = true)))  // Implicit "return 0"
        return Program(children, lineNumber = ctx.start.line, charPosition = ctx.start.charPositionInLine)
    }

    override fun visitClassDecl(ctx: MainParser.ClassDeclContext): ASTNode {
        val classId = ctx.CLASSNAME().text
        return ClassDecl(
            classId,
            if (ctx.paramList() != null) ctx.paramList().ID().mapIndexed { index, it -> it.text to getType(ctx.paramList().typeDecl()[index]) } else emptyList(),
            ctx.classBody().fieldDecl().map { visitFieldDecl(it, classId) as VarDecl<*> },
            ctx.classBody().methodDecl().map { visitMethodDecl(it, classId) as FuncDecl },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    private fun visitMethodDecl(ctx: MainParser.MethodDeclContext, classId: String): ASTNode {
        return visitFuncDecl(ctx.funcDecl(), classId)
    }

    override fun visitObjectInstantiation(ctx: MainParser.ObjectInstantiationContext): ASTNode {
        return ObjectInstantiation(
            ctx.CLASSNAME().text,
            ctx.argList().expr().map { it.accept(this) as Expr },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitMethodCall(ctx: MainParser.MethodCallContext): ASTNode {
        val funcCall = ctx.funcCall()
        return MethodCall(
            if (ctx.ID() != null) ctx.ID().text else "this",
            funcCall.ID().text,
            funcCall.argList().expr().map { it.accept(this) as Expr },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    private fun visitFieldDecl(ctx: MainParser.FieldDeclContext, classId: String): ASTNode {
        return visitVarDecl(ctx.varDecl(), classId)
    }

    private fun visitClassField(ctx: MainParser.ClassFieldContext, reference: Boolean): ClassField {
        return ClassField(
            if (ctx.ID().size > 1) ctx.ID(0).text else "this",
            ctx.ID(if (ctx.ID().size > 1) 1 else 0).text,
            reference = reference,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitClassField(ctx: MainParser.ClassFieldContext): ASTNode {
        return visitClassField(ctx, reference = false)
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
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitPrintStmt(ctx: MainParser.PrintStmtContext): ASTNode {
        return PrintStmt(
            ctx.expr()?.accept(this) as? Expr,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    private fun getType(ctx: MainParser.TypeDeclContext): ExprType {
        return when {
            ctx.PRIMITIVE() != null -> getPrimitiveType(ctx.PRIMITIVE())
            ctx.VOID() != null -> VOID
            ctx.arrayType() != null -> getArrayType(ctx.arrayType())
            ctx.CLASSNAME() != null -> CLASS(ctx.CLASSNAME().text)
            else -> throw Exception("Cannot find type")
        }
    }

    private fun getPrimitiveType(primitive: TerminalNode): ExprType {
        return ExprType.primitives()[primitive.symbol.text] ?: error("Primitive type not found")
    }

    private fun getArrayType(ctx: MainParser.ArrayTypeContext): ExprType {
        var depth = 1
        var element = ctx
        while (element.PRIMITIVE() == null) {
            element = element.arrayType()
            depth++
        }
        return ARRAY(depth, getPrimitiveType(element.PRIMITIVE()))
    }

    override fun visitFuncDecl(ctx: MainParser.FuncDeclContext): ASTNode {
        return visitFuncDecl(ctx, null)
    }

    private fun visitFuncDecl(ctx: MainParser.FuncDeclContext, classId: String?): ASTNode {
        val returnType = getType(ctx.typeDecl())
        val children = (ctx.funcBody().children?.map { it.accept(this) } ?: emptyList()).toMutableList()

        // Always add implicit return for void functions
        if (returnType == VOID) {
            children.add(ReturnStmt(IntExpr(0, isVoid = true)))
        }

        return FuncDecl(
            ctx.ID().text,
            ctx.paramList().ID().mapIndexed { index, it -> it.text to getType(ctx.paramList().typeDecl()[index]) },
            returnType,
            children,
            classId,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitFuncCall(ctx: MainParser.FuncCallContext): ASTNode {
        return FuncCall(
            ctx.ID().text,
            ctx.argList().expr().map { it.accept(this) as Expr },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitVarDecl(ctx: MainParser.VarDeclContext): ASTNode {
        return visitVarDecl(ctx, null)
    }

    private fun visitVarDecl(ctx: MainParser.VarDeclContext, classId: String?): ASTNode {
        return VarDecl(
            ctx.ID().map { it.text },
            ctx.expr().accept(this) as Expr,
            classId,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    private fun visitArrayIndexExprReference(ctx: MainParser.ArrayIndexExprContext): ArrayIndexExpr {
        return ArrayIndexExpr(
            ctx.idExpr().text,
            ctx.expr().map { visitExpr(it) as Expr },
            reference = true,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitVarAssign(ctx: MainParser.VarAssignContext): ASTNode {
        val ids = mutableListOf<String>()
        val indexExprs = mutableListOf<ArrayIndexExpr>()
        val classFields = mutableListOf<ClassField>()
        ctx.assignable().forEach {
            when {
                it.idExpr() != null -> ids.add(it.idExpr().text)
                it.arrayIndexExpr() != null -> indexExprs.add(visitArrayIndexExprReference(it.arrayIndexExpr()))
                it.classField() != null -> classFields.add(
                    visitClassField(it.classField(), reference = true)
                )
            }
        }

        return VarAssign(
            ids, indexExprs, classFields,
            ctx.expr().accept(this) as Expr,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitOpAssign(ctx: MainParser.OpAssignContext): ASTNode {
        val ids = mutableListOf<IdExpr>()
        val arrayIndexExprs = mutableListOf<ArrayIndexExpr>()
        val classFields = mutableListOf<ClassField>()
        when {
            ctx.assignable().idExpr() != null -> ids.add(
                IdExpr(ctx.assignable().idExpr().ID().text, lineNumber = ctx.start.line, charPosition = -1)
            )
            ctx.assignable().arrayIndexExpr() != null -> arrayIndexExprs.add(
                visitArrayIndexExprReference(ctx.assignable().arrayIndexExpr())
            )
            ctx.assignable().classField() != null -> classFields.add(
                visitClassField(ctx.assignable().classField(), reference = true)
            )
        }

        return VarAssign(
            ids.map { it.id },
            arrayIndexExprs,
            classFields,
            ArithExpr(
                (when {
                    ids.isNotEmpty() -> ids[0]
                    arrayIndexExprs.isNotEmpty() -> ctx.assignable().arrayIndexExpr().accept(this)
                    else -> ctx.assignable().classField().accept(this)
                }) as Expr,
                ctx.expr().accept(this) as Expr,
                ArithOp.fromString(ctx.op.text[0].toString())!!,
                lineNumber = ctx.start.line,
                charPosition = ctx.start.charPositionInLine
            ),
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitIfElse(ctx: MainParser.IfElseContext): ASTNode {
        return IfElse(
            ctx.expr().accept(this) as Expr, ctx.block(0).accept(this) as Block,
            if (ctx.ifElse() != null) (ctx.ifElse().accept(this) as IfElse) else (ctx.block(1)?.accept(this) as? Block),
            lineNumber = ctx.start.line, charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitWhileLoop(ctx: MainParser.WhileLoopContext): ASTNode {
        return WhileLoop(
            ctx.expr().accept(this) as Expr,
            ctx.block().accept(this) as Block,
            lineNumber = ctx.start.line, charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitBlock(ctx: MainParser.BlockContext): ASTNode {
        return Block(
            ctx.children?.map { it.accept(this) } ?: emptyList(),
            lineNumber = ctx.start.line, charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitExpr(ctx: MainParser.ExprContext): ASTNode {
        return when {
            ctx.booleanExpr() != null -> BooleanExpr(
                ctx.booleanExpr().BOOLEAN().text!!.toBoolean(),
                lineNumber = ctx.start.line,
                charPosition = ctx.start.charPositionInLine
            )
            ctx.intExpr() != null -> IntExpr(
                ctx.intExpr().INT().text.toInt(),
                lineNumber = ctx.start.line,
                charPosition = ctx.start.charPositionInLine
            )
            ctx.idExpr() != null -> IdExpr(
                ctx.idExpr().ID().text,
                lineNumber = ctx.start.line,
                charPosition = ctx.start.charPositionInLine
            )
            ctx.parenExpr() != null -> visitParenExpr(ctx.parenExpr())
            ctx.funcCall() != null -> visitFuncCall(ctx.funcCall())
            ctx.arrayExpr() != null -> visitArrayExpr(ctx.arrayExpr())
            ctx.arrayIndexExpr() != null -> visitArrayIndexExpr(ctx.arrayIndexExpr())
            ctx.lenExpr() != null -> LenExpr(
                ctx.lenExpr().expr().accept(this) as Expr,
                lineNumber = ctx.start.line,
                charPosition = ctx.start.charPositionInLine
            )
            ctx.classField() != null -> visitClassField(ctx.classField())
            ctx.methodCall() != null -> visitMethodCall(ctx.methodCall())
            ctx.objectInstantiation() != null -> visitObjectInstantiation(ctx.objectInstantiation())
            ctx.expr().size < 2 -> when (ctx.op.text) {
                ArithOp.MINUS.value -> ArithExpr(
                    IntExpr(-1, lineNumber = ctx.start.line, charPosition = -1),
                    ctx.expr()[0].accept(this) as Expr,
                    ArithOp.TIMES,
                    lineNumber = ctx.start.line,
                    charPosition = ctx.start.charPositionInLine
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
        return ArrayExpr(
            ctx.expr().map { visitExpr(it) as Expr },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitArrayIndexExpr(ctx: MainParser.ArrayIndexExprContext): ASTNode {
        return ArrayIndexExpr(
            ctx.idExpr().ID().text,
            ctx.expr().map { visitExpr(it) as Expr },
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
    }

    override fun visitParenExpr(ctx: MainParser.ParenExprContext): ASTNode {
        return ParenExpr(
            visitExpr(ctx.expr()) as Expr,
            lineNumber = ctx.start.line,
            charPosition = ctx.start.charPositionInLine
        )
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