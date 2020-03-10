package com.egern.emit

import com.egern.codegen.*

class LinuxEmitter(instructions: List<Instruction>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder("#"), syntax) {

    override fun emitProgramPrologue() {
        builder
            .addLine(".data")
            .addLine("format_int:")
            .addLine(".string \"%d\\n\"", comment = "Integer format string for C printf")
            .addLine("format_newline:")
            .addLine(".string \"\\n\"", comment = "Empty format string for C printf")
            .newline()
            .addLine(".text")
            .addLine(".globl", "main")
            .newline()
    }

    override fun emitProgramEpilogue() {
        // Empty epilogue
    }

    override fun emitPrint(arg: MetaOperationArg) {
        val isEmpty = printInstructionIsEmpty(arg)
        builder
            .newline()
            .addLine("# PRINTING USING PRINTF")
            .addLine(
                "movq", "\$format_${if (isEmpty) "newline" else "int"}", "%rdi",
                "Pass 1st argument in %rdi"
            )
        if (!isEmpty) {
            builder.addLine(
                "movq", "${8 * CALLER_SAVE_REGISTERS.size}(%rsp)", "%rsi",
                "Pass 2nd argument in %rsi"
            )
        }
        builder
            .addLine("xor", "%rax", "%rax", "No floating point registers used")
            .addLine("call", "printf", comment = "Call function printf")
    }

    override fun emitMainLabel(): String {
        return "main"
    }
}
