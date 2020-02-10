package com.egern

import MainLexer
import MainParser
import com.egern.ast.ArithExpr
import com.egern.ast.BuildASTVisitor
import com.egern.ast.Program
import com.egern.ast.ReturnStmt
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    println("Egern Compiler v0.1 - We in business! Input something + CTRL+D to run")
    val input = CharStreams.fromStream(System.`in`)
    val lexer = MainLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MainParser(tokens)

    val cst = parser.prog()
    val ast = BuildASTVisitor().visit(cst) as Program

    val ret = ast.stmts[0] as ReturnStmt
    val stmt = ret.expr as ArithExpr
    println(stmt.lhs?.value)
    println(stmt.op)
    println(stmt.rhs?.value)
}

fun hello(): String {
    return "hello"
}