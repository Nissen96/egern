package com.egern.emit

import com.egern.codegen.*

class LinuxEmitter(instructions: List<Instruction>, syntax: SyntaxManager) : Emitter(instructions, AsmStringBuilder("#"), syntax) {
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

//    override fun emitIndirect(target: String): String {
//        return "($target)"
//    }
//
//    override fun emitIndirectRelative(target: String, offset: Int): String {
//        return "${ADDRESSING_OFFSET * offset}($target)"
//    }

    override fun emitMainLabel(): String {
        return "main"
    }
}
