package com.egern.emit

import com.egern.codegen.*

    class MacOSEmitter(instructions: List<Instruction>): Emitter(instructions, AsmStringBuilder(";")) {

    override fun mapInstructionType(type: InstructionType): String? {
        return when (type) {
            InstructionType.MOV -> "mov"
            InstructionType.ADD -> "add"
            InstructionType.SUB -> "sub"
            InstructionType.INC -> "inc"
            InstructionType.DEC -> "dec"
            InstructionType.IMUL -> "imul"
            InstructionType.IDIV -> null
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
            InstructionType.LABEL -> null
            InstructionType.META -> null
        }
    }

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

    private fun emitAllocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "add", Pair("rsp", "${-VARIABLE_SIZE * arg.value}"),
            "Move stack pointer to allocate space for local variables"
        )
    }

    private fun emitDeallocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "add", Pair("rsp", "${VARIABLE_SIZE * arg.value}"),
            "Move stack pointer to deallocate space for local variables"
        )
    }

    private fun emitDivision(inst: Instruction) {
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

    private fun emitCalleePrologue() {
        builder
            .addLine("; Callee Prologue")
            .addLine("push", Pair("rbp", null), "save caller's base pointer")
            .addLine("mov", Pair("rbp", "rsp"), "make stack pointer new base pointer")
    }

    private fun emitCalleeEpilogue() {
        builder
            .addLine("; Callee Epilogue")
            .addLine("mov", Pair("rsp", "rbp"), "Restore stack pointer")
            .addLine("pop", Pair("rbp", null), "Restore base pointer")
            .addLine("ret", comment="Return from call")
    }

    private fun emitProgramPrologue() {
        builder
            .addLine("global", Pair("_main", null))
            .addLine("extern", Pair("_printf",null))
            .addLine("default rel")
            .addLine("section .text")

    }

    private fun emitProgramEpilogue() {
        builder.addLine("format: db \"num: %d\", 10, 0")
    }

    private fun emitPrint() {
        // TODO: double check alignment (MacOS requires 16 byte)
        builder
            .addLine("; PRINTING USING PRINTF")
            .addLine("lea", Pair("rdi", "[format]"))
            .addLine("mov", Pair("rsi", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]"))
            .addLine("xor", Pair("rax", "rax"))
            .addLine("call", Pair("_printf", null), "call function printf")

    }

    private fun emitCallerCallee(restore: Boolean, registers: List<String>) {
        val op = if (restore) InstructionType.POP else InstructionType.PUSH
        builder.addLine("; Caller/Callee ${if (restore) "Restore" else "Save"}")
        for (register in if (restore) registers.reversed() else registers) {
            builder.addLine(mapInstructionType(op)!!, Pair(register, null))
        }
    }

    private fun emitLabel(instruction: Instruction) {
        builder.add(emitArg(instruction.args[0]) + ":", AsmStringBuilder.OP_OFFSET)
    }

    private fun emitSimpleInstruction(instruction: Instruction) {
        val instr = mapInstructionType(instruction.instructionType) ?: throw Exception("Assembly operation for ${instruction.instructionType} not defined")
        builder.add(instr, AsmStringBuilder.OP_OFFSET)
        emitArgs(instruction.args)
    }

    private fun emitArgs(arguments: Array<out Arg>) {
        when (arguments.size) {
            1 -> builder.add(emitArg(arguments[0]), AsmStringBuilder.REGS_OFFSET)
            2 -> builder.add(emitArg(arguments[0]) + ", " + emitArg(arguments[1]), AsmStringBuilder.REGS_OFFSET)
            else -> throw Exception("Unexpected number of arguments")
        }
        /*
        if (arguments.isNotEmpty()) {
            //builder.add("")
            emitArg(arguments[0])
        }
        if (arguments.size > 1) {
            for (arg in arguments.slice(1 until arguments.size)) {
                builder.add(", ")
                emitArg(arg)
            }
        } */
    }

    private fun emitArg(argument: Arg): String { // TODO: look
        if (argument is InstructionArg) return emitInstructionArg(argument)
        else throw Exception("Trying to emit an argument that cant be emitted!")
    }

    private fun emitInstructionArg(argument: InstructionArg): String {
        val target = when (argument.instructionTarget) {
            is ImmediateValue -> argument.instructionTarget.value
            is Memory -> argument.instructionTarget.address
            is Register -> when (argument.instructionTarget.register) {
                OpReg1 -> "r12"
                OpReg2 -> "r13"
                DataReg -> "r14"
                is ParamReg -> CALLER_SAVE_REGISTERS[argument.instructionTarget.register.paramNum]
            }
            RBP -> "rbp"
            RSP -> "rsp"
            ReturnValue -> "rax"
            StaticLink -> "r15"
        }
        return when (argument.addressingMode) {
            Direct -> target
            Indirect -> "[$target]"
            is IndirectRelative -> "[$target + ${ADDRESSING_OFFSET * argument.addressingMode.offset}]"
        }
    }

}