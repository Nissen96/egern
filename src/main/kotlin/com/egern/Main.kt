package com.egern

import MainLexer
import MainParser
import com.egern.antlr.ThrowingErrorListener
import com.egern.ast.BuildASTVisitor
import com.egern.ast.Program
import com.egern.classes.ClassVisitor
import com.egern.codegen.CodeGenerationVisitor
import com.egern.labels.LabelGenerationVisitor
import com.egern.emit.*
import com.egern.error.ErrorLogger
import com.egern.symbols.SymbolVisitor
import com.egern.types.TypeCheckingVisitor
import com.egern.util.Platform
import com.egern.util.PlatformManager
import com.egern.visitor.PrintProgramVisitor
import com.egern.visitor.PrintSymbolTableVisitor
import com.egern.weeding.WeedingVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.lang.Exception

fun main(args: Array<String>) {
    val quiet = "-q" !in args
    val doPrint = "-p" in args

    if (quiet) {
        println("Egern Compiler v0.1.0! Input something + CTRL+D to run")
    }

    val input = CharStreams.fromStream(System.`in`)
    val lexer = MainLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MainParser(tokens)

    parser.removeErrorListeners()
    parser.addErrorListener(ThrowingErrorListener.INSTANCE)

    val cst = parser.prog()
    val ast = BuildASTVisitor().visit(cst) as Program

    val weedingVisitor = WeedingVisitor()
    ast.accept(weedingVisitor)

    if (doPrint) {
        val printProgramVisitor = PrintProgramVisitor()
        ast.accept(printProgramVisitor)
        println()
    }

    val symbolVisitor = SymbolVisitor()
    ast.accept(symbolVisitor)

    val labelGenerationVisitor = LabelGenerationVisitor()
    ast.accept(labelGenerationVisitor)

    val classVisitor = ClassVisitor(symbolVisitor.classDefinitions, symbolVisitor.interfaces)
    ast.accept(classVisitor)

    if (doPrint) {
        val printVisitor = PrintSymbolTableVisitor()
        ast.accept(printVisitor)
        println()
    }

    val typeCheckingVisitor = TypeCheckingVisitor(
        symbolVisitor.symbolTable,
        classVisitor.classDefinitions,
        classVisitor.interfaces
    )
    ast.accept(typeCheckingVisitor)

    if (ErrorLogger.hasErrors()) {
        ErrorLogger.print()
        throw Exception("One or more errors occurred while compiling")
    }

    val platform = PlatformManager()

    val codeGenVisitor = CodeGenerationVisitor(
        symbolVisitor.symbolTable,
        classVisitor.classDefinitions,
        classVisitor.interfaces
    )
    ast.accept(codeGenVisitor)

    val emitter: Emitter = when (platform.platform) {
        Platform.Windows -> WindowsEmitter(
            codeGenVisitor.instructions,
            codeGenVisitor.dataFields,
            codeGenVisitor.staticStrings,
            codeGenVisitor.vTableSize,
            IntelSyntax()
        )
        Platform.Linux -> LinuxEmitter(
            codeGenVisitor.instructions,
            codeGenVisitor.dataFields,
            codeGenVisitor.staticStrings,
            codeGenVisitor.vTableSize,
            ATTSyntax()
        )
    }
    val code = emitter.emit()
    if (!doPrint) {
        print(code)
    }
}
