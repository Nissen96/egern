package com.egern.visitor

import com.egern.ast.*

class PrintProgramVisitor(private val indentation: Int = 4) : Visitor {
    private var level = 0

    private fun printIndented(text: Any) {
        print(" ".repeat(level * indentation) + "$text")
    }

    override fun midVisit(arithExpr: ArithExpr) {
        print(" ${arithExpr.op.value} ")
    }

    override fun preVisit(block: Block) {
        print("{")
        if (block.funcCalls.isNotEmpty() || block.statements.isNotEmpty()) {
            println()
            level++
        }
    }

    override fun postVisit(block: Block) {
        if (block.funcCalls.isNotEmpty() || block.statements.isNotEmpty()) {
            level--
            printIndented("}")
        } else {
            print("}")
        }
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
        printIndented("func ${funcDecl.id}(${funcDecl.params.joinToString(", ")}) {")
    }

    override fun postVisit(funcDecl: FuncDecl) {
        println()
    }

    override fun preVisit(funcBody: FuncBody) {
        if (funcBody.funcCalls.isNotEmpty() || funcBody.statements.isNotEmpty()) {
            println()
            level++
        }
    }

    override fun postVisit(funcBody: FuncBody) {
        if (funcBody.funcCalls.isNotEmpty() || funcBody.statements.isNotEmpty()) {
            level--
            printIndented("}")
        } else {
            print("}")
        }
    }

    override fun visit(idExpr: IdExpr) {
        print(idExpr.id)
    }

    override fun preVisit(ifElse: IfElse) {
        printIndented("if (")
    }

    override fun preMidVisit(ifElse: IfElse) {
        print(") ")
    }

    override fun postMidVisit(ifElse: IfElse) {
        print(" else ")
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
        print(");\n")
    }

    override fun preVisit(program: Program) {
        print("func main() {\n")
        level++
    }

    override fun postVisit(program: Program) {
        level--
        print("}\n")
    }

    override fun midVisit(program: Program) {
        print(";\n")
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
        print(";\n")
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        printIndented("var " + varDecl.ids.joinToString(" = ") + " = ")
    }

    override fun postVisit(varDecl: VarDecl<*>) {
        print(";\n")
    }
}