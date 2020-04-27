package com.egern.emit

import com.egern.codegen.*

class LinuxEmitter(
    instructions: List<Instruction>,
    dataFields: MutableList<String>,
    staticStrings: Map<String, String>,
    syntax: SyntaxManager
) :
    Emitter(instructions, dataFields, staticStrings, syntax) {

    override fun emitProgramEpilogue() {
        // Empty epilogue
    }

    override fun emitMainLabel(): String {
        return "main"
    }
}
