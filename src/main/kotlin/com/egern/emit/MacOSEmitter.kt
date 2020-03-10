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

    override fun emitRequestProgramHeap() {
        builder.addLine("call malloc")
    }

    var printfCounter = 0;

    override fun emitPrint(isEmpty: Boolean) {
        // TODO: double check alignment (MacOS requires 16 byte)
        // TODO: handle print empty
        builder
            .newline()
            .addLine("; PRINTING USING PRINTF")
            .addLine("lea", "rdi", "[format]", "Pass 1st argument in rdi")
            .addLine("mov", "rsi", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]", "Pass 2nd argument in rdi")
            .newline()
            .addLine("; ALIGNMENT")
            .addLine("mov", "rdx", "rsp")
            .addLine("and", "rsp", "-16")
            .addLine("xor", "rbx", "rbx")
            .addLine("cmp", "rsp", "rdx")
            .addLine("je", "was_aligned_${printfCounter}")
            .addLine("inc", "rbx")
            .addLine("was_aligned_${printfCounter}:")
            .newline()
            .addLine("xor", "rax", "rax")
            .addLine("call", "_printf", comment = "Call function printf")
            .addLine("xor", "rcx", "rcx")
            .addLine("cmp", "rbx", "rcx")
            .addLine("je", "was_alinged_end_${printfCounter}")
            .addLine("add", "rsp", "8")
            .addLine("was_alinged_end_${printfCounter}:")

        printfCounter++
    }

    override fun emitMainLabel(): String {
        return "_main"
    }
}