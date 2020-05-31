package com.egern.emit

import com.egern.codegen.*

class LinuxEmitter(
    instructions: List<Instruction>,
    dataFields: MutableList<String>,
    staticStrings: Map<String, String>,
    vTableSize: Int,
    syntax: SyntaxManager
) : Emitter(instructions, dataFields, staticStrings, vTableSize, syntax) {
    override fun emitProgramEpilogue() {
        // Empty epilogue
        builder.newline()
    }

    override fun emitMainLabel(): String {
        return "main"
    }
}
