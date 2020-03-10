package com.egern.visitor

import com.egern.ast.*

class PrintProgramVisitor(private val indentation: Int = 4) : Visitor {
    private var level = 0
    private val fib = listOf(0, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144)
    private var dontIndent = false

    private fun printIndented(text: Any = "") {
        val indent = if (indentation >= 0) indentation * level else fib[level] * 4
        print(" ".repeat(indent) + "$text")
    }

    override fun midVisit(arithExpr: ArithExpr) {
        print(" ${arithExpr.op.value} ")
    }

    override fun preVisit(block: Block) {
        println("{")
        level++
    }

    override fun preFuncCallVisit(block: Block) {
        printIndented()
    }

    override fun postFuncCallVisit(block: Block) {
        println(";")
    }

    override fun postVisit(block: Block) {
        level--
        printIndented("}")
    }

    override fun midVisit(compExpr: CompExpr) {
        print(" ${compExpr.op.value} ")
    }

    override fun preVisit(funcCall: FuncCall) {
        print("${funcCall.id}(")
    }

    override fun midVisit(funcCall: FuncCall) {
        print(", ")
    }

    override fun postVisit(funcCall: FuncCall) {
        print(")")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        println()
        printIndented("func ${funcDecl.id}(")
        print(funcDecl.params.joinToString(", ") { "${it.first}: ${it.second.toString().toLowerCase()}" }) // Params
        println("): ${funcDecl.returnType.toString().toLowerCase()} {")
        level++
    }

    override fun preFuncCallVisit(funcDecl: FuncDecl) {
        printIndented()
    }

    override fun postFuncCallVisit(funcDecl: FuncDecl) {
        println(";")
    }

    override fun postVisit(funcDecl: FuncDecl) {
        level--
        printIndented("}\n")
    }

    override fun visit(idExpr: IdExpr) {
        print(idExpr.id)
    }

    override fun preVisit(ifElse: IfElse) {
        if (dontIndent) {
            print("if (")
            dontIndent = false
        } else {
            printIndented("if (")
        }
    }

    override fun preMidVisit(ifElse: IfElse) {
        print(") ")
    }

    override fun postMidVisit(ifElse: IfElse) {
        if (ifElse.elseBlock != null) print(" else ")
        dontIndent = ifElse.elseBlock is IfElse
    }

    override fun postVisit(ifElse: IfElse) {
        println()
    }

    override fun visit(intExpr: IntExpr) {
        print(intExpr.value)
    }

    override fun preVisit(parenExpr: ParenExpr) {
        print("(")
    }

    override fun postVisit(parenExpr: ParenExpr) {
        print(")")
    }

    override fun preVisit(printStmt: PrintStmt) {
        printIndented("print(")
    }

    override fun postVisit(printStmt: PrintStmt) {
        println(");")
    }

    override fun preVisit(program: Program) {
        println("Main Scope {")
        level++
    }

    override fun preFuncCallVisit(program: Program) {
        printIndented()
    }

    override fun postFuncCallVisit(program: Program) {
        println(";")
    }

    override fun postVisit(program: Program) {
        level--
        printIndented("}")
        println()
    }

    override fun preVisit(returnStmt: ReturnStmt) {
        printIndented("return")
        if (returnStmt.expr != null) print(" ")
    }

    override fun postVisit(returnStmt: ReturnStmt) {
        print(";\n")
    }

    override fun preVisit(varAssign: VarAssign<*>) {
        printIndented(varAssign.ids.joinToString(" = ") + " = ")
    }

    override fun postVisit(varAssign: VarAssign<*>) {
        println(";")
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        printIndented("var " + varDecl.ids.joinToString(" = ") + " = ")
    }

    override fun postVisit(varDecl: VarDecl<*>) {
        println(";")
    }

    override fun preVisit(whileLoop: WhileLoop) {
        printIndented("while (")
    }

    override fun midVisit(whileLoop: WhileLoop) {
        print(") ")
    }

    override fun postVisit(whileLoop: WhileLoop) {
        println()
    }
}