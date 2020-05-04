package com.egern.emit

import com.egern.codegen.InstructionType
import java.io.File

abstract class SyntaxManager {
    abstract fun argOrder(source: String, destination: String): Pair<String, String>
    abstract fun immediate(value: String): String
    abstract fun register(reg: String): String
    abstract fun indirect(target: String): String
    abstract fun indirectRelative(target: String, offset: Int): String
    abstract fun indirectFuncCall(): String
    abstract fun emitPrologue(
        asmStringBuilder: AsmStringBuilder,
        mainLabel: String,
        platformPrefix: String,
        dataFields: List<String>,
        staticStrings: Map<String, String>
    )

    abstract val commentSymbol: String
    abstract val ops: Map<InstructionType, String>

    abstract fun emitRuntime(asmStringBuilder: AsmStringBuilder)
    protected fun emitRuntime(asmStringBuilder: AsmStringBuilder, filename: String) {
        File(filename).forEachLine { asmStringBuilder.add(it).newline() }
    }
}



