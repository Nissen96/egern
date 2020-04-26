package com.egern.emit

import com.egern.codegen.InstructionType

abstract class SyntaxManager {
    abstract fun argOrder(source: String, destination: String): Pair<String, String>
    abstract fun immediate(value: String): String
    abstract fun register(reg: String): String
    abstract fun indirect(target: String): String
    abstract fun indirectRelative(target: String, offset: Int): String
    abstract fun commentSym(): String
    abstract fun indirectFuncCall(): String
    abstract val ops: Map<InstructionType, String>
}



