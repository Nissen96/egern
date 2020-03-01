package com.egern.emit

import com.egern.codegen.*

class MacOSEmitter(instructions: List<Instruction>) : Emitter(instructions, AsmStringBuilder(";")) {

    override fun mapInstructionType(type: InstructionType): String? {
        return when (type) {
            InstructionType.MOV -> "mov"
            InstructionType.ADD -> "add"
            InstructionType.SUB -> "sub"
            InstructionType.INC -> "inc"
            InstructionType.DEC -> "dec"
            InstructionType.IMUL -> "imul"
            InstructionType.IDIV -> "idiv"
            InstructionType.CMP -> "cmp"
            InstructionType.JMP -> "jmp"
            InstructionType.JNE -> "jne"
            InstructionType.JE -> "je"
            InstructionType.JG -> "jg"
            InstructionType.JGE -> "jge"
            InstructionType.JL -> "jl"
            InstructionType.JLE -> "jle"
            InstructionType.PUSH -> "push"
            InstructionType.POP -> "pop"
            InstructionType.CALL -> "call"
            InstructionType.RET -> "ret"
            else -> null
        }
    }

    override fun argPair(arg1: String, arg2: String): Pair<String, String> {
        return Pair(arg2, arg1)
    }

    override fun emitRegister(register: String): String {
        return register
    }

    override fun emitImmediate(value: String): String {
        return value
    }

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
        val empty = arg.value == 0
        builder
            .addLine("; PRINTING USING PRINTF")
            .addLine("lea", Pair("rdi", "[format]"), "Pass 1st argument in rdi")
            .addLine("mov", Pair("rsi", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]"), "Pass 2nd argument in rdi")
            .addLine("xor", Pair("rax", "rax"))
            .addLine("call", Pair("_printf", null), "Call function printf")

    }

    override fun emitIndirect(target: String): String {
        return "[$target]"
    }

    override fun emitIndirectRelative(target: String, offset: Int): String {
        return "[$target + ${ADDRESSING_OFFSET * offset}]"
    }

    override fun emitMainLabel(): String {
        return "_main"
    }
}