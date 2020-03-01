package com.egern.emit

import com.egern.codegen.*

class MacOSEmitter(instructions: List<Instruction>) : Emitter(instructions, AsmStringBuilder(";")) {
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

    override fun emit(): String {
        emitProgramPrologue()
        for (instruction in instructions) {
            emitInstruction(instruction)
            builder.newline()
        }
        emitProgramEpilogue()

        return builder.toFinalStr()
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
            builder.add("; ${instruction.comment}")
        }
    }

    private fun emitMetaOp(instruction: Instruction) {
        when (instruction.args[0]) {
            MetaOperation.CallerSave -> emitCallerCallee(false, CALLER_SAVE_REGISTERS)
            MetaOperation.CallerRestore -> emitCallerCallee(true, CALLER_SAVE_REGISTERS)
            MetaOperation.CalleeSave -> emitCallerCallee(false, CALLEE_SAVE_REGISTERS)
            MetaOperation.CalleeRestore -> emitCallerCallee(true, CALLEE_SAVE_REGISTERS)
            MetaOperation.Print -> emitPrint()
            MetaOperation.CalleePrologue -> emitCalleePrologue()
            MetaOperation.CalleeEpilogue -> emitCalleeEpilogue()
            MetaOperation.AllocateStackSpace -> emitAllocateStackSpace(instruction.args[1] as MetaOperationArg)
            MetaOperation.DeallocateStackSpace -> emitDeallocateStackSpace(instruction.args[1] as MetaOperationArg)
        }
    }

    override fun emitAllocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "add", Pair("rsp", "${-VARIABLE_SIZE * arg.value}"),
            "Move stack pointer to allocate space for local variables"
        )
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
        //val empty = arg.value == 0
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