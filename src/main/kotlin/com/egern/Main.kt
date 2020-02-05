package com.egern

import MainLexer
import MainParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

fun main(args: Array<String>) {
    println("We in business! Input something + CTRL+D to run")
    val input = CharStreams.fromStream(System.`in`)
    val lexer = MainLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MainParser(tokens)
}

fun hello(): String {
    return "hello"
}