package com.egern.emit

import com.egern.codegen.*
import java.lang.Exception

class LinuxEmitter(instructions: List<Instruction>) : Emitter(instructions, AsmStringBuilder("#", "%", "$")) {

    override fun mapInstructionType(type: InstructionType): String? {
        return when (type) {
            InstructionType.MOV -> "movq"
            InstructionType.ADD -> "addq"
            InstructionType.SUB -> "subq"
            InstructionType.INC -> "incq"
            InstructionType.DEC -> "decq"
            InstructionType.IMUL -> "imulq"
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

    private fun add(s: String) {
        builder.add(s)
    }

    private fun addLine(s: String = "", comment: String? = null) {
        builder.add(s)
        if (comment != null) {
            builder.add("\t# ")
            builder.add(comment)
        }
        builder.newline()
    }

    private fun emitInstruction(instruction: Instruction) {
        val type = instruction.instructionType
        when {
            type == InstructionType.IDIV -> emitDivision(instruction)
            mapInstructionType(type) != null -> emitSimpleInstruction(instruction) // TODO: fix double work
            type == InstructionType.LABEL -> emitLabel(instruction)
            type == InstructionType.META -> emitMetaOp(instruction)
            else -> throw Exception("Unsupported operation ${instruction.instructionType}")
        }
        // Add comment
        if (instruction.comment != null) {
            builder.add("# ${instruction.comment}")
        }
    }

    private fun emitMetaOp(instruction: Instruction) {
        when (instruction.args[0]) {
            MetaOperation.CallerSave -> emitCallerCallee(false, CALLER_SAVE_REGISTERS)
            MetaOperation.CallerRestore -> emitCallerCallee(true, CALLER_SAVE_REGISTERS)
            MetaOperation.CalleeSave -> emitCallerCallee(false, CALLEE_SAVE_REGISTERS)
            MetaOperation.CalleeRestore -> emitCallerCallee(true, CALLEE_SAVE_REGISTERS)
            MetaOperation.Print -> emitPrint(instruction.args[1] as MetaOperationArg)
            MetaOperation.CalleePrologue -> emitCalleePrologue()
            MetaOperation.CalleeEpilogue -> emitCalleeEpilogue()
            MetaOperation.AllocateStackSpace -> emitAllocateStackSpace(instruction.args[1] as MetaOperationArg)
            MetaOperation.DeallocateStackSpace -> emitDeallocateStackSpace(instruction.args[1] as MetaOperationArg)
        }
    }

    override fun emitAllocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "addq", Pair("$${-VARIABLE_SIZE * arg.value}", "%rsp"),
            "Move stack pointer to allocate space for local variables"
        )
    }

    override fun emitDeallocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "addq", Pair("$${VARIABLE_SIZE * arg.value}", "%rsp"),
            "Move stack pointer to deallocate space for local variables"
        )
    }

    override fun emitPerformDivision(inst: Instruction, resultReg: String) {
        // TODO use builder
        add("movq ")
        add(emitArg(inst.args[1]))
        addLine(", %rax", "Setup dividend")
        addLine("cqo", "Sign extend into %rdx")
        add("idiv ")
        add(emitArg(inst.args[0]))
        addLine("", "Divide")
        add("movq $resultReg, ")
        add(emitArg(inst.args[1]))
    }

    override fun emitCalleePrologue() {
        builder
            .addLine("# Callee Prologue")
            .addLine("pushq", Pair("%rbp", null), "save caller's base pointer")
            .addLine("movq", Pair("%rsp", "%rbp"), "make stack pointer new base pointer")
    }

    override fun emitCalleeEpilogue() {
        builder
            .addLine("# Callee Epilogue")
            .addLine("movq", Pair("%rbp", "%rsp"), "Restore stack pointer")
            .addLine("popq", Pair("%rbp", null), "Restore base pointer")
            .addLine("ret", comment = "Return from call")
    }

    override fun emitProgramPrologue() {
        builder
            .addLine(".data")
            .addLine("format_int:")
            .addLine(".string \"%d\\n\"", comment = "integer format string for C printf")
            .addLine("format_newline:")
            .addLine(".string \"\\n\"", comment = "empty format string for C printf")
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
