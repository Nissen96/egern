package com.egern.emit

import com.egern.codegen.*

class LinuxEmitter(
    instructions: List<Instruction>,
    private val dataFields: List<String>,
    private val staticStrings: Map<String, String>,
    syntax: SyntaxManager
) :
    Emitter(instructions, AsmStringBuilder("#"), syntax) {

    override fun emitProgramPrologue() {
        builder
            .addLine(".data")
            .addLine("format_int:")
            .addLine(".string \"%d\\n\"", comment = "Integer format string for C printf")
            .addLine("format_string:")
            .addLine(".string \"%s\\n\"", comment = "String format string for C printf")
            .addLine("format_newline:")
            .addLine(".string \"\\n\"", comment = "Empty format string for C printf")
            .newline()
        emitDataSection()
        builder
            .addLine(".text")
            .addLine(".globl", "main")
            .newline()
    }

    override fun emitDataSection() {
        staticStrings.forEach {
            builder.addLine("${it.key}: .asciz \"${it.value}\"")
        }
        builder.newline()
        builder
            .addLine(".bss")
            .addLine(".lcomm $HEAP_POINTER, 8")
            .addLine(".lcomm $VTABLE_POINTER, 8")
        dataFields.forEach {
            builder.addLine(".lcomm $it, 8")
        }
        builder.newline()
    }

    override fun emitProgramEpilogue() {
        // Empty epilogue
    }

    override fun emitRequestProgramHeap() {
        builder.addLine("call malloc")
    }

    override fun emitFreeProgramHeap() {
        builder.addLine("call free")
    }

    override fun emitPrint(value: Int) {
        val type = when (value) {
            0 -> "newline"
            2 -> "string"
            else -> "int"
        }
        builder
            .newline()
            .addLine("# PRINTING USING PRINTF")
            .addLine(
                "movq", "\$format_$type", "%rdi",
                "Pass 1st argument in %rdi"
            )
        if (value != 0) {
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
