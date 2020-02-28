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
            InstructionType.IDIV -> null
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

        return builder.toFinalStr()
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

    private fun emitAllocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "addq", Pair("$${-VARIABLE_SIZE * arg.value}", "%rsp"),
            "Move stack pointer to allocate space for local variables"
        )
    }

    private fun emitDeallocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            "addq", Pair("$${VARIABLE_SIZE * arg.value}", "%rsp"),
            "Move stack pointer to deallocate space for local variables"
        )
    }

    private fun emitDivision(inst: Instruction) {
        add("movq ")
        add(emitArg(inst.args[1]))
        addLine(", %rax", "Setup dividend")
        addLine("cqo", "Sign extend into %rdx")
        add("idiv ")
        add(emitArg(inst.args[0]))
        addLine("", "Divide")
        add("movq %rax, ")
        add(emitArg(inst.args[1]))
        addLine("", "Move resulting quotient")
    }

    private fun emitCalleePrologue() {
        builder
            .addLine("# Callee Prologue")
            .addLine("pushq", Pair("%rbp", null), "save caller's base pointer")
            .addLine("movq", Pair("%rsp", "%rbp"), "make stack pointer new base pointer")
    }

    private fun emitCalleeEpilogue() {
        builder
            .addLine("# Callee Epilogue")
            .addLine("movq", Pair("%rbp", "%rsp"), "Restore stack pointer")
            .addLine("popq", Pair("%rbp", null), "Restore base pointer")
            .addLine("ret", comment = "Return from call")
    }

    private fun emitProgramPrologue() {
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

    private fun emitPrint(arg: MetaOperationArg) {
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


    private fun emitCallerCallee(restore: Boolean, registers: List<String>) {
        val op = if (restore) InstructionType.POP else InstructionType.PUSH
        builder
            .newline()
            .addLine("# Caller/Callee ${if (restore) "Restore" else "Save"}")
        for (register in if (restore) registers.reversed() else registers) {
            builder.addLine(mapInstructionType(op)!!, Pair("%$register", null))
        }
    }

    private fun emitLabel(instruction: Instruction) {
        builder
            .newline()
            .add(emitArg(instruction.args[0]) + ":", AsmStringBuilder.OP_OFFSET)
    }

    private fun emitSimpleInstruction(instruction: Instruction) {
        val instr = mapInstructionType(instruction.instructionType)
            ?: throw Exception("Assembly operation for ${instruction.instructionType} not defined")
        builder.add(instr, AsmStringBuilder.OP_OFFSET)
        emitArgs(instruction.args)
    }

    private fun emitArgs(arguments: Array<out Arg>) {
        when (arguments.size) {
            1 -> builder.add(emitArg(arguments[0]), AsmStringBuilder.REGS_OFFSET)
            2 -> builder.add(emitArg(arguments[0]) + ", " + emitArg(arguments[1]), AsmStringBuilder.REGS_OFFSET)
            else -> throw Exception("Unexpected number of arguments")
        }
    }

    private fun emitArg(argument: Arg): String {
        if (argument is InstructionArg) return emitInstructionArg(argument)
        else throw Exception("Trying to emit an argument that cant be emitted!")
    }

    private fun emitInstructionArg(argument: InstructionArg): String {
        val target = when (argument.instructionTarget) {
            is ImmediateValue -> "$${argument.instructionTarget.value}"
            is Memory -> argument.instructionTarget.address
            is Register -> when (argument.instructionTarget.register) {
                OpReg1 -> "%r12"
                OpReg2 -> "%r13"
                DataReg -> "%r14"
                is ParamReg -> "%${CALLER_SAVE_REGISTERS[argument.instructionTarget.register.paramNum]}"
            }
            RBP -> "%rbp"
            RSP -> "%rsp"
            ReturnValue -> "%rax"
            StaticLink -> "%r15"
            MainLabel -> "main"
        }

        return when (argument.addressingMode) {
            Direct -> target
            Indirect -> "($target)"
            is IndirectRelative -> "${ADDRESSING_OFFSET * argument.addressingMode.offset}($target)"
        }
    }
}
