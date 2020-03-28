package com.egern.emit

import com.egern.codegen.Instruction
import com.egern.codegen.InstructionType
import com.egern.codegen.MetaOperationArg

class WindowsEmitter(instructions: List<Instruction>, dataFields: List<String>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder(";"), syntax) {

    override fun emitProgramPrologue() {
        builder
            .addLine("global", "_main")
            .addLine("extern", "printf")
            .addLine("extern", "malloc")
            .addLine("segment", ".data")
            .addLine("format_int: db \"%d\", 10, 0")
            .addLine("format_newline:  db \"\", 10, 0", comment = "Empty format string for C printf")
            emitDataSection()
            builder.addLine("segment", ".text")
    }

	override fun emitDataSection() {
        builder
            .addLine("section .bss")
            .addLine("alignb", "8")
            .addLine("Handle", "resq 1")
            .addLine("Written", "resq 1")
            .newline()
    }

    override fun emitProgramEpilogue() {
        //builder.addLine("format: db \"%d\", 10, 0")
    }

    override fun emitRequestProgramHeap() {
        //builder.addLine("call malloc")
    }

    override fun emitFreeProgramHeap() {
        builder.addLine("call free")
    }

    override fun emitPrint(isEmpty: Boolean) {
        val (arg1, arg2) = syntax.argOrder(syntax.register("format_${if (isEmpty) "newline" else "int"}"), syntax.register("rdi"))
        val (arg3, arg4) = syntax.argOrder(syntax.indirectRelative("rsp", (8 * CALLER_SAVE_REGISTERS.size) + 32, 1), syntax.register("rdx"))
        builder
            .newline()
            .addLine("sub", "rsp", "32")
            .addLine("; PRINTING USING PRINTF")
            .addLine(
                "mov", "rcx", arg2,
                "Pass 1st argument in %rdi"
            )
        if (!isEmpty) {
            builder.addLine(
                syntax.ops.getValue(InstructionType.MOV), arg3, arg4,
                "Pass 2nd argument in %rsi"
            )
        }
        builder
            .addLine("xor", syntax.register("rax"), syntax.register("rax"), "No floating point registers used")

            .addLine("call", "printf", comment = "Call function printf")
            .addLine("add", "rsp", "32")

    }

    override fun emitMainLabel(): String {
        return "_main"
    }

}
