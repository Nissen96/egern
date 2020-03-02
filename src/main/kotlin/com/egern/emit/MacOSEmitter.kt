package com.egern.emit

import com.egern.codegen.*

class MacOSEmitter(instructions: List<Instruction>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder(";"), syntax) {

    override fun emitProgramPrologue() {
        builder
            .addLine("global", "_main")
            .addLine("extern", "_printf")
            .addLine("default rel")
            .addLine("section .text")
    }

    override fun emitProgramEpilogue() {
        builder.addLine("format: db \"%d\", 10, 0")
    }

    override fun emitPrint(arg: MetaOperationArg) {
        // TODO: double check alignment (MacOS requires 16 byte)
        // TODO: handle print empty
        //val empty = arg.value == 0
        builder
            .newline()
            .addLine("; PRINTING USING PRINTF")
            .addLine("lea", "rdi", "[format]", "Pass 1st argument in rdi")
            .addLine("mov", "rsi", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]", "Pass 2nd argument in rdi")
            .addLine("xor", "rax", "rax")
            .addLine("call", "_printf", comment = "Call function printf")

    }

    override fun emitMainLabel(): String {
        return "_main"
    }
}