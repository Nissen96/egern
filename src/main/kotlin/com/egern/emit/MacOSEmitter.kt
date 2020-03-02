package com.egern.emit

import com.egern.codegen.*

class MacOSEmitter(instructions: List<Instruction>, syntax: SyntaxManager) : Emitter(instructions, AsmStringBuilder(";"), syntax) {
    override val instructionMap = mapOf(
        InstructionType.MOV to "mov",
        InstructionType.ADD to "add",
        InstructionType.SUB to "sub",
        InstructionType.INC to "inc",
        InstructionType.DEC to "dec",
        InstructionType.IMUL to "imul",
        InstructionType.IDIV to "idiv",
        InstructionType.CMP to "cmp",
        InstructionType.JMP to "jmp",
        InstructionType.JNE to "jne",
        InstructionType.JE to "je",
        InstructionType.JG to "jg",
        InstructionType.JGE to "jge",
        InstructionType.JL to "jl",
        InstructionType.JLE to "jle",
        InstructionType.PUSH to "push",
        InstructionType.POP to "pop",
        InstructionType.CALL to "call",
        InstructionType.RET to "ret"
    )

//    override fun argPair(arg1: String, arg2: String): Pair<String, String> {
//        return Pair(arg2, arg1)
//    }
//
//    override fun emitRegister(register: String): String {
//        return register
//    }
//
//    override fun emitImmediate(value: String): String {
//        return value
//    }

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

//    override fun emitIndirect(target: String): String {
//        return "qword [$target]"
//    }
//
//    override fun emitIndirectRelative(target: String, offset: Int): String {
//        return "qword [$target + ${ADDRESSING_OFFSET * offset}]"
//    }

    override fun emitMainLabel(): String {
        return "_main"
    }
}