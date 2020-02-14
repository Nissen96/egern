package com.egern

import MainLexer
import MainParser
import com.egern.ast.BuildASTVisitor
import com.egern.ast.Program
import com.egern.symbols.SymbolVisitor
import com.egern.types.TypeCheckingVisitor
import com.egern.visitor.PrintProgramVisitor
import com.egern.visitor.PrintSymbolTableVisitor
import com.egern.visitor.PrintVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    val doPrint = "-p" in args

    println("Egern Compiler v0.1.0! Input something + CTRL+D to run")
    val input = CharStreams.fromStream(System.`in`)
    val lexer = MainLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MainParser(tokens)

    val cst = parser.prog()
    val ast = BuildASTVisitor().visit(cst) as Program

    if (doPrint) {
        val printProgramVisitor = PrintProgramVisitor(4)
        ast.accept(printProgramVisitor)

        //val printVisitor = PrintVisitor()
        //ast.accept(printVisitor)
        //println()

        //val printSymbolVisitor = PrintSymbolTableVisitor()
        //ast.accept(printSymbolVisitor)
        //println()
    }

    val symbolVisitor = SymbolVisitor()
    ast.accept(symbolVisitor)

    val typeCheckingVisitor = TypeCheckingVisitor(symbolVisitor.currentTable)
    ast.accept(typeCheckingVisitor)
}

fun hello(): String {
    return "hello"
}