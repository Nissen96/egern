package com.egern.emit

import com.egern.codegen.*

class LinuxEmitter(instructions: List<Instruction>) : Emitter(instructions, AsmStringBuilder("#")) {
    override val instructionMap = mapOf(
        InstructionType.MOV to "movq",
        InstructionType.ADD to "addq",
        InstructionType.SUB to "subq",
        InstructionType.INC to "incq",
        InstructionType.DEC to "decq",
        InstructionType.IMUL to "imulq",
        InstructionType.IDIV to "idiv",
        InstructionType.CMP to "cmpq",
        InstructionType.JMP to "jmp",
        InstructionType.JNE to "jne",
        InstructionType.JE to "je",
        InstructionType.JG to "jg",
        InstructionType.JGE to "jge",
        InstructionType.JL to "jl",
        InstructionType.JLE to "jle",
        InstructionType.PUSH to "pushq",
        InstructionType.POP to "popq",
        InstructionType.CALL to "call",
        InstructionType.RET to "ret"
    )

    override fun argPair(arg1: String, arg2: String): Pair<String, String> {
        return Pair(arg1, arg2)
    }

    override fun emitRegister(register: String): String {
        return "%$register"
    }

    override fun emitImmediate(value: String): String {
        return "$$value"
    }

    override fun emitProgramPrologue() {
        builder
            .addLine(".data")
            .addLine("format_int:")
            .addLine(".string \"%d\\n\"", comment = "Integer format string for C printf")
            .addLine("format_newline:")
            .addLine(".string \"\\n\"", comment = "Empty format string for C printf")
            .newline()
            .addLine(".text")
            .addLine(".globl", Pair("main", null))
            .newline()
    }

    override fun emitProgramEpilogue() {
        // Empty epilogue
    }

    override fun emitPrint(arg: MetaOperationArg) {
        val empty = arg.value == 0
        builder
            .newline()
            .addLine("# PRINTING USING PRINTF")
            .addLine(
                "movq", Pair("\$format_${if (empty) "newline" else "int"}", "%rdi"),
                "Pass 1st argument in %rdi"
            )
        if (!empty) {
            builder.addLine(
                "movq", Pair("${8 * CALLER_SAVE_REGISTERS.size}(%rsp)", "%rsi"),
                "Pass 2nd argument in %rsi"
            )
        }
        builder
            .addLine("xor", Pair("%rax", "%rax"), "No floating point registers used")
            .addLine("call", Pair("printf", null), "Call function printf")
    }

    override fun emitIndirect(target: String): String {
        return "($target)"
    }

    override fun emitIndirectRelative(target: String, offset: Int): String {
        return "${ADDRESSING_OFFSET * offset}($target)"
    }

    override fun emitMainLabel(): String {
        return "main"
    }
}
