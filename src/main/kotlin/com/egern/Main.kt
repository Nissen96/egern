package com.egern

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import com.egern.antlr.*;

fun main() {
    val input = CharStreams.fromStream(System.`in`)
    val lexer = MainLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MainParser(tokens)
}