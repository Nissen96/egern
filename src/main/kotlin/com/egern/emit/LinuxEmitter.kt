package com.egern.emit

import com.egern.codegen.*
import java.lang.Exception

class LinuxEmitter(instructions: List<Instruction>) : Emitter(instructions, AsmStringBuilder("#")) {

    override fun mapInstructionType(type: InstructionType): String? {
        return when (type) {
            InstructionType.MOV -> "movq"
            InstructionType.ADD -> "addq"
            InstructionType.SUB -> "subq"
            InstructionType.INC -> "incq"
            InstructionType.DEC -> "decq"
            InstructionType.IMUL -> "imulq"
            InstructionType.IDIV -> "idiv"
            InstructionType.CMP -> "cmpq"
            InstructionType.JMP -> "jmp"
            InstructionType.JNE -> "jne"
            InstructionType.JE -> "je"
            InstructionType.JG -> "jg"
            InstructionType.JGE -> "jge"
            InstructionType.JL -> "jl"
            InstructionType.JLE -> "jle"
            InstructionType.PUSH -> "pushq"
            InstructionType.POP -> "popq"
            InstructionType.CALL -> "call"
            InstructionType.RET -> "ret"
            else -> null
        }
    }

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
