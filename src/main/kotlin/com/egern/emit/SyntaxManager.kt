package com.egern.emit

import com.egern.codegen.InstructionArg

interface SyntaxManager {
    fun argOrder(source: String, destination: String): Pair<String, String>
    fun immediate(value: String): String
    fun register(reg: String): String
    fun indirect(target: String): String
    fun indirectRelative(target: String, addressingOffset: Int, offset: Int): String
}

class IntelSyntax : SyntaxManager {
    override fun argOrder(source: String, destination: String): Pair<String, String> {
        return Pair(destination, source)
    }

    override fun immediate(value: String): String {
        return value
    }

    override fun register(reg: String): String {
        return reg
    }

    override fun indirect(target: String): String {
        return "qword [$target]"
    }

    override fun indirectRelative(target: String, addressingOffset: Int, offset: Int): String {
        return "qword [$target + ${addressingOffset * offset}]"
    }

}

class ATandTSyntax : SyntaxManager {
    override fun argOrder(source: String, destination: String): Pair<String, String> {
        return Pair(source, destination)
    }

    override fun immediate(value: String): String {
        return "$$value"
    }

    override fun register(reg: String): String {
        return "%$reg"
    }

    override fun indirect(target: String): String {
        return "($target)"
    }

    override fun indirectRelative(target: String, addressingOffset: Int, offset: Int): String {
        return "${addressingOffset * offset}($target)"
    }
}