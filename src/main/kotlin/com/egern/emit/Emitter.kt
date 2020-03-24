package com.egern.emit

import com.egern.codegen.*

abstract class Emitter(
    private val instructions: List<Instruction>,
    protected val builder: AsmStringBuilder,
    private val syntax: SyntaxManager
) {
    abstract fun emitProgramPrologue()
    abstract fun emitDataSection()
    abstract fun emitProgramEpilogue()
    abstract fun emitRequestProgramHeap()
    abstract fun emitFreeProgramHeap()
    abstract fun emitPrint(isEmpty: Boolean)
    abstract fun emitMainLabel(): String

    protected companion object {
        const val VARIABLE_SIZE = 8
        const val ADDRESSING_OFFSET = -8

        val CALLER_SAVE_REGISTERS = listOf("rcx", "rdx", "rsi", "rdi", "r8", "r9", "r10", "r11")
        val CALLEE_SAVE_REGISTERS = listOf("rbx", "r12", "r13", "r14", "r15")
        const val HEAP_POINTER: String = "heap_pointer"
        const val HEAP_SIZE: Int = 256
        const val VTABLE_POINTER: String = "vtable_pointer"
        const val VTABLE_SIZE: Int = 256
    }

    fun emit(): String {
        emitProgramPrologue()
        for (instruction in instructions) {
            emitInstruction(instruction)
        }
        emitProgramEpilogue()

        return builder.toFinalStr()
    }

    private fun emitAllocateInternalHeaps() {
        emitAllocateProgramHeap()
        emitAllocateVTable()
    }

    private fun emitAllocateProgramHeap() {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * HEAP_SIZE}"), syntax.register("rdi"))
        val (arg3, arg4) = syntax.argOrder(emitInstructionTarget(ReturnValue), emitInstructionTarget(RHP))
        val (arg5, arg6) = syntax.argOrder(arg3, HEAP_POINTER)
        builder.addLine(
            syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
            "Move argument into parameter register for malloc call"
        )
        emitRequestProgramHeap()
        builder.addLine(
            syntax.ops.getValue(InstructionType.MOV), arg3, arg4,
            "Move returned heap pointer to fixed heap pointer register"
        )
        builder.addLine(
            syntax.ops.getValue(InstructionType.MOV), arg5, arg6,
            "Save start of heap pointer globally"
        )
    }

    private fun emitAllocateVTable() {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * VTABLE_SIZE}"), syntax.register("rdi"))
        val (arg3, arg4) = syntax.argOrder(emitInstructionTarget(ReturnValue), VTABLE_POINTER)
        builder.addLine(
            syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
            "Move argument into parameter register for malloc call"
        )
        emitRequestProgramHeap()
        builder.addLine(
            syntax.ops.getValue(InstructionType.MOV), arg3, arg4,
            "Save start of vtable pointer globally"
        )
    }

    private fun emitDeallocateInternalHeaps() {
        emitDeallocateInternalHeap(VTABLE_POINTER)
        emitDeallocateInternalHeap(HEAP_POINTER)

    }

    private fun emitDeallocateInternalHeap(pointer: String) {
        val (arg1, arg2) = syntax.argOrder(pointer, syntax.register("rdi"))
        builder
            .addLine(
                syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
                "Move argument into parameter register for free call"
            )
        emitFreeProgramHeap()
    }

    private fun emitInstruction(instruction: Instruction) {
        when (instruction.instructionType) {
            InstructionType.IDIV -> emitDivision(instruction)
            InstructionType.MOD -> emitModulo(instruction)
            InstructionType.NOT -> emitBooleanNot(instruction)
            InstructionType.LABEL -> emitLabel(instruction)
            InstructionType.CALL -> emitCall(instruction)
            InstructionType.META -> emitMetaOp(instruction)
            in syntax.ops -> emitSimpleInstruction(instruction)
            else -> throw Exception("Unsupported operation ${instruction.instructionType}")
        }
        // Add comment
        if (instruction.comment != null) {
            builder.addComment(instruction.comment)
        }
        builder.newline()
    }

    private fun emitCall(instruction: Instruction) {
        val instr = syntax.ops.getValue(instruction.instructionType)
        builder.addOp(instr)

        // Check for indirect function call
        val prefix = if ((instruction.args[0] as InstructionArg).instructionTarget is Register) "*" else ""
        builder.addRegs("$prefix${emitArg(instruction.args[0])}")
    }

    private fun emitSimpleInstruction(instruction: Instruction) {
        val instr = syntax.ops[instruction.instructionType]
            ?: throw Exception("Assembly operation for ${instruction.instructionType} not defined")
        builder.addOp(instr)
        emitArgs(instruction.args)
    }

    private fun emitArgs(arguments: Array<out Arg>) {
        when (arguments.size) {
            1 -> builder.addRegs(emitArg(arguments[0]))
            2 -> {
                val (arg1, arg2) = syntax.argOrder(emitArg(arguments[0]), emitArg(arguments[1]))
                builder.addRegs(arg1, arg2)
            }
            else -> throw Exception("Unexpected number of arguments")
        }
    }

    private fun emitArg(argument: Arg): String {
        if (argument is InstructionArg) return emitInstructionArg(argument)
        else throw Exception("Trying to emit an argument that cant be emitted!")
    }

    private fun emitInstructionTarget(target: InstructionTarget): String {
        return when (target) {
            is ImmediateValue -> syntax.immediate(target.value)
            is Memory -> target.address
            is Register -> when (target.register) {
                OpReg1 -> syntax.register("r12")
                OpReg2 -> syntax.register("r13")
                is ParamReg -> syntax.register(CALLER_SAVE_REGISTERS[target.register.paramNum])
            }
            RBP -> syntax.register("rbp")
            RSP -> syntax.register("rsp")
            RHP -> syntax.register("rbx")
            VTable -> VTABLE_POINTER
            ReturnValue -> syntax.register("rax")
            StaticLink -> syntax.register("r15")
            MainLabel -> emitMainLabel()
        }
    }

    private fun emitInstructionArg(argument: InstructionArg): String {
        val target = emitInstructionTarget(argument.instructionTarget)

        return when (argument.addressingMode) {
            Direct -> target
            Indirect -> syntax.indirect(target)
            is IndirectRelative -> syntax.indirectRelative(target, ADDRESSING_OFFSET, argument.addressingMode.offset)
        }
    }

    private fun emitMetaOp(instruction: Instruction) {
        val value = (instruction.args.getOrElse(1) { MetaOperationArg(-1) } as MetaOperationArg).value
        when (instruction.args[0]) {
            // Meta operation without arguments
            MetaOperation.CallerSave -> emitCallerCallee(false, CALLER_SAVE_REGISTERS)
            MetaOperation.CallerRestore -> emitCallerCallee(true, CALLER_SAVE_REGISTERS)
            MetaOperation.CalleeSave -> emitCallerCallee(false, CALLEE_SAVE_REGISTERS)
            MetaOperation.CalleeRestore -> emitCallerCallee(true, CALLEE_SAVE_REGISTERS)
            MetaOperation.CalleePrologue -> emitCalleePrologue()
            MetaOperation.CalleeEpilogue -> emitCalleeEpilogue()
            MetaOperation.AllocateInternalHeap -> emitAllocateInternalHeaps()
            MetaOperation.DeallocateInternalHeap -> emitDeallocateInternalHeaps()

            // Meta operation with an argument
            MetaOperation.Print -> emitPrint(value == 0)
            MetaOperation.AllocateStackSpace -> emitAllocateStackSpace(value)
            MetaOperation.DeallocateStackSpace -> emitDeallocateStackSpace(value)
            MetaOperation.AllocateHeapSpace -> emitAllocateHeapSpace(value)
            MetaOperation.DeallocateHeapSpace -> emitDeallocateHeapSpace(value)
        }
    }

    private fun emitCallerCallee(restore: Boolean, registers: List<String>) {
        val op = if (restore) InstructionType.POP else InstructionType.PUSH
        builder
            .newline()
            .addComment("Caller/Callee ${if (restore) "Restore" else "Save"}")
            .newline()
        for (register in (if (restore) registers.reversed() else registers)) {
            builder.addLine(syntax.ops.getValue(op), syntax.register(register))
        }
        builder.newline()
    }

    private fun emitCalleePrologue() {
        val (reg1, reg2) = syntax.argOrder(syntax.register("rsp"), syntax.register("rbp"))
        builder
            .addComment("Callee Prologue")
            .newline()
            .addLine(
                syntax.ops.getValue(InstructionType.PUSH),
                syntax.register("rbp"),
                comment = "Save caller's base pointer"
            )
            .addLine(syntax.ops.getValue(InstructionType.MOV), reg1, reg2, "Make stack pointer new base pointer")
    }

    private fun emitCalleeEpilogue() {
        val (reg1, reg2) = syntax.argOrder(syntax.register("rbp"), syntax.register("rsp"))
        builder
            .addComment("Callee Epilogue")
            .newline()
            .addLine(syntax.ops.getValue(InstructionType.MOV), reg1, reg2, "Restore stack pointer")
            .addLine(syntax.ops.getValue(InstructionType.POP), syntax.register("rbp"), comment = "Restore base pointer")
            .addLine(syntax.ops.getValue(InstructionType.RET), comment = "Return from call")
    }

    private fun emitLabel(instruction: Instruction) {
        builder
            .newline()
            .addOp(emitArg(instruction.args[0]) + ":")
            .newline()
    }

    private fun emitPerformDivision(inst: Instruction) {
        val (arg1, arg2) = syntax.argOrder(emitArg(inst.args[1]), syntax.register("rax"))
        builder
            .addLine(syntax.ops.getValue(InstructionType.MOV), arg1, arg2, "Setup dividend")
            .addLine("cqo", comment = "Sign extend into rdx")
            .addLine(syntax.ops.getValue(InstructionType.IDIV), emitArg(inst.args[0]), comment = "Divide")
    }

    private fun emitDivision(inst: Instruction) {
        emitPerformDivision(inst)
        val (arg1, arg2) = syntax.argOrder(syntax.register("rax"), emitArg(inst.args[1]))
        builder.addLine(syntax.ops.getValue(InstructionType.MOV), arg1, arg2, "Move resulting quotient")
    }

    private fun emitModulo(inst: Instruction) {
        emitPerformDivision(inst)
        val (arg1, arg2) = syntax.argOrder(syntax.register("rdx"), emitArg(inst.args[1]))
        builder.addLine(syntax.ops.getValue(InstructionType.MOV), arg1, arg2, "Move resulting remainder")
    }

    private fun emitBooleanNot(inst: Instruction) {
        val arg = emitArg(inst.args[0])
        builder
            .addLine("test", arg, arg, "Test argument with itself")
            .addLine("setz", "${arg}b", comment = "Set first byte to 1 if zero etc.")
    }

    private fun emitAllocateHeapSpace(size: Int) {
        val (arg1, arg2) = syntax.argOrder(emitInstructionTarget(RHP), emitInstructionTarget(ReturnValue))
        val (arg3, arg4) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * size}"), emitInstructionTarget(RHP))
        builder.addLine(
            syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
            "Move heap pointer to return value"
        ).addLine(
            syntax.ops.getValue(InstructionType.ADD), arg3, arg4,
            "Offset heap pointer by allocated bytes"
        )
    }

    private fun emitDeallocateHeapSpace(size: Int) {
        // TODO()
        /**
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * arg.value}"), syntax.register("rdi"))
        builder
        .addLine(
        syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
        "Move argument into parameter register for free call"
        )
        .addLine("call free")
         **/
    }

    private fun emitAllocateStackSpace(numVariables: Int) {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${-VARIABLE_SIZE * numVariables}"), syntax.register("rsp"))
        builder.addLine(
            syntax.ops.getValue(InstructionType.ADD), arg1, arg2,
            "Move stack pointer to allocate space for local variables"
        )
    }

    private fun emitDeallocateStackSpace(numVariables: Int) {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * numVariables}"), syntax.register("rsp"))
        builder.addLine(
            syntax.ops.getValue(InstructionType.ADD), arg1, arg2,
            "Move stack pointer to deallocate space for local variables/parameters"
        )
    }
}
