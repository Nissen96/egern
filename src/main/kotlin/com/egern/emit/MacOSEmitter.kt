package com.egern.emit

import com.egern.codegen.*

class MacOSEmitter(instructions: List<Instruction>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder(";"), syntax) {

    override fun emitProgramPrologue() {
        builder
            .addLine("global", Pair("_main", null))
            .addLine("extern", Pair("_printf", null))
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
            .addLine("lea", Pair("rdi", "[format]"), "Pass 1st argument in rdi")
            .addLine("mov", Pair("rsi", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]"), "Pass 2nd argument in rdi")
            .addLine("xor", Pair("rax", "rax"))
            .addLine("call", Pair("_printf", null), "Call function printf")

    }

    override fun emitMainLabel(): String {
        return "_main"
    }
}