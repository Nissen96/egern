package com.egern

import MainLexer
import MainParser
import com.egern.ast.ArithExpr
import com.egern.ast.BuildASTVisitor
import com.egern.ast.Program
import com.egern.ast.ReturnStmt
import com.egern.symbols.SymbolVisitor
import com.egern.visitor.PrintVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    println("Egern Compiler v0.1.0 - We in business! Input something + CTRL+D to run")
    val input = CharStreams.fromStream(System.`in`)
    val lexer = MainLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MainParser(tokens)

    val cst = parser.prog()
    val ast = BuildASTVisitor().visit(cst) as Program

    val printVisitor = PrintVisitor()
    ast.accept(printVisitor)
    val symbolVisitor = SymbolVisitor()
    ast.accept(symbolVisitor)
    for (id in symbolVisitor.currentTable.symbols) {
        println(id)
    }
    println(symbolVisitor.currentTable.scope)
}

fun hello(): String {
    return "hello"
}