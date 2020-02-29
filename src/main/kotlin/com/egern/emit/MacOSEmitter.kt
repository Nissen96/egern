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

    override fun emitAllocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "add", Pair("rsp", "${-VARIABLE_SIZE * arg.value}"),
            "Move stack pointer to allocate space for local variables"
        )
    }

    override fun emitDeallocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "add", Pair("rsp", "${VARIABLE_SIZE * arg.value}"),
            "Move stack pointer to deallocate space for local variables"
        )
    }

    override fun emitPerformDivision(inst: Instruction, resultReg: String) {
        /*
        // TODO: fix order
        add("mov ")
        emitArg(inst.args[1])
        addLine(", %rax", "Setup dividend")
        addLine("cqo", "Sign extend into %rdx")
        add("idiv ")
        emitArg(inst.args[0])
        addLine("", "Divide")
        add("mov %rax, ")
        emitArg(inst.args[1])
        addLine("", "Move resulting quotient")
         */
    }

    override fun emitCalleePrologue() {
        builder
            .addLine("; Callee Prologue")
            .addLine("push", Pair("rbp", null), "save caller's base pointer")
            .addLine("mov", Pair("rbp", "rsp"), "make stack pointer new base pointer")
    }

    override fun emitCalleeEpilogue() {
        builder
            .addLine("; Callee Epilogue")
            .addLine("mov", Pair("rsp", "rbp"), "Restore stack pointer")
            .addLine("pop", Pair("rbp", null), "Restore base pointer")
            .addLine("ret", comment = "Return from call")
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
            .addLine("call", Pair("_printf", null), "call function printf")

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