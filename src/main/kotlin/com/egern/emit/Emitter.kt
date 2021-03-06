package com.egern.emit

import com.egern.codegen.*
import com.egern.types.ExprTypeEnum

abstract class Emitter(
    private val instructions: List<Instruction>,
    private val dataFields: MutableList<String>,
    private val staticStrings: Map<String, String>,
    private val vTableSize: Int,
    protected val syntax: SyntaxManager
) {
    protected val builder = AsmStringBuilder(syntax.commentSymbol)
    open val paramPassingRegs: List<String> = listOf("rdi", "rsi", "rdx", "rcx", "r8", "r9")

    abstract fun emitProgramEpilogue()
    abstract fun emitMainLabel(): String

    // Defaults to nothing; can be overwritten
    open fun addPlatformPrefix(symbol: String): String {
        return symbol
    }

    companion object {
        const val VARIABLE_SIZE = 8
        const val ADDRESSING_OFFSET = -8

        val CALLER_SAVE_REGISTERS = listOf("rdi", "rsi", "rdx", "rcx", "r8", "r9", "r10", "r11")
        val CALLEE_SAVE_REGISTERS = listOf("rbx", "r12", "r13", "r14", "r15")
        const val VTABLE_POINTER = "vtable_pointer"
        const val HEAP_POINTER = "heap_pointer"
        const val FROM_SPACE = "fromspace"
        const val TO_SPACE = "tospace"
        const val CURRENT_HEAP_POINTER = "current_heap_pointer"
        const val HEAP_SIZE = 1024
        const val ALLOCATE_HEAP_ROUTINE = "allocate_heap"
    }

    fun emit(): String {
        emitProgramPrologue()
        for (instruction in instructions) {
            emitInstruction(instruction)
        }
        emitProgramEpilogue()
        emitRuntime()

        return builder.toFinalStr()
    }

    private fun emitProgramPrologue() {
        dataFields.add(HEAP_POINTER)
        dataFields.add(FROM_SPACE)  // Starting points of each heap half for garbage collection
        dataFields.add(TO_SPACE)
        dataFields.add(CURRENT_HEAP_POINTER)
        dataFields.add("current_tospace_pointer")
        dataFields.add("scan")
        dataFields.add(VTABLE_POINTER)
        syntax.emitPrologue(builder, emitMainLabel(), addPlatformPrefix(""), dataFields, staticStrings)
    }

    private fun emitRuntime() {
        syntax.emitRuntime(builder)
    }

    open fun emitPrint(typeValue: Int) {
        emitPrint(typeValue, 0)
    }

    fun emitPrint(typeValue: Int, additionalOffset: Int) {
        val enumType = ExprTypeEnum.fromInt(typeValue)
        val type = when (enumType) {
            ExprTypeEnum.VOID -> "newline"
            ExprTypeEnum.STRING -> "string"
            ExprTypeEnum.INT -> "int"
            ExprTypeEnum.BOOLEAN -> "string"
            else -> throw Exception("Printing $enumType is invalid")
        }

        builder.newline().addComment("PRINTING WITH PRINTF").newline()
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ImmediateValue("format_$type"), Direct),
                InstructionArg(Register(ParamReg(0)), Direct),
                comment = "Pass formatting as 1st argument in ${paramPassingRegs[0]}"
            )
        )

        if (enumType != ExprTypeEnum.VOID) {
            emitInstruction(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(
                        RSP,
                        IndirectRelative(-(CALLER_SAVE_REGISTERS.size + additionalOffset / VARIABLE_SIZE))
                    ),
                    InstructionArg(Register(ParamReg(1)), Direct),
                    comment = "Pass possible value to print as 2nd argument in ${paramPassingRegs[1]}"
                )
            )
        }
        emitInstruction(
            Instruction(
                InstructionType.XOR,
                InstructionArg(ReturnValue, Direct),
                InstructionArg(ReturnValue, Direct),
                comment = "No floating point registers used"
            )
        )

        emitInstruction(
            Instruction(
                InstructionType.CALL,
                InstructionArg(Memory("printf"), Direct),
                comment = "Call function printf"
            )
        )
    }

    private fun emitAllocateInternalHeaps() {
        emitAllocateProgramHeap()
        emitAllocateVTable()
    }

    open fun emitAllocateProgramHeap() {
        // Add double heap size for garbage collection
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ImmediateValue("${VARIABLE_SIZE * HEAP_SIZE * 2}"), Direct),
                InstructionArg(Register(ParamReg(0)), Direct),
                comment = "Move argument into parameter register for malloc call"
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.CALL,
                InstructionArg(Memory("malloc"), Direct)
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ReturnValue, Direct),
                InstructionArg(RHP, Direct),
                comment = "Move returned heap pointer to fixed heap pointer register"
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ReturnValue, Direct),
                InstructionArg(Heap, Direct),
                comment = "Save start of heap pointer globally"
            )
        )
    }

    open fun emitAllocateVTable() {
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ImmediateValue("${VARIABLE_SIZE * vTableSize}"), Direct),
                InstructionArg(Register(ParamReg(0)), Direct),
                comment = "Move argument into parameter register for malloc call"
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.CALL,
                InstructionArg(Memory("malloc"), Direct)
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(ReturnValue, Direct),
                InstructionArg(VTable, Direct),
                comment = "Save start of vtable pointer globally"
            )
        )
    }

    private fun emitDeallocateInternalHeaps() {
        emitDeallocateInternalHeap(VTABLE_POINTER)
        emitDeallocateInternalHeap(HEAP_POINTER)
    }

    open fun emitDeallocateInternalHeap(pointer: String) {
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(Memory(pointer), Direct),
                InstructionArg(Register(ParamReg(0)), Direct),
                comment = "Move argument into parameter register for free call"
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.CALL,
                InstructionArg(Memory("free"), Direct)
            )
        )
    }

    private fun emitInstruction(instruction: Instruction) {
        when (instruction.instructionType) {
            InstructionType.IDIV -> emitDivision(instruction)
            InstructionType.MOD -> emitModulo(instruction)
            InstructionType.NOT -> emitBooleanNot(instruction)
            InstructionType.LABEL -> emitLabel(instruction)
            InstructionType.CALL -> emitCall(instruction)
            InstructionType.RET -> emitReturn(instruction)
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
        val prefix =
            if ((instruction.args[0] as InstructionArg).instructionTarget is Register) syntax.indirectFuncCall() else ""
        builder.addRegs("$prefix${emitArg(instruction.args[0])}")
    }

    private fun emitReturn(instruction: Instruction) {
        val instr = syntax.ops.getValue(instruction.instructionType)
        builder.addLine(instr, comment = instruction.comment)
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
                is ParamReg -> syntax.register(paramPassingRegs[target.register.paramNum])
            }
            RBP -> syntax.register("rbp")
            RSP -> syntax.register("rsp")
            RHP -> syntax.indirect(CURRENT_HEAP_POINTER)
            Heap -> syntax.indirect(HEAP_POINTER)
            FromSpace -> syntax.indirect(FROM_SPACE)
            ToSpace -> syntax.indirect(TO_SPACE)
            VTable -> syntax.indirect(VTABLE_POINTER)
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
            is IndirectRelative -> syntax.indirectRelative(target, ADDRESSING_OFFSET * argument.addressingMode.offset)
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
            MetaOperation.Print -> emitPrint(value)
            MetaOperation.AllocateStackSpace -> emitAllocateStackSpace(value)
            MetaOperation.DeallocateStackSpace -> emitDeallocateStackSpace(value)
            MetaOperation.AllocateHeapSpace -> emitAllocateHeapSpace(value)
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
        builder.newline().addComment("Callee Prologue").newline()
        emitInstruction(
            Instruction(
                InstructionType.PUSH,
                InstructionArg(RBP, Direct),
                comment = "Save caller's base pointer"
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(RSP, Direct),
                InstructionArg(RBP, Direct),
                comment = "Make stack pointer new base pointer"
            )
        )
    }

    private fun emitCalleeEpilogue() {
        builder.newline().addComment("Callee Epilogue").newline()
        emitInstruction(
            Instruction(
                InstructionType.MOV,
                InstructionArg(RBP, Direct),
                InstructionArg(RSP, Direct),
                comment = "Restore stack pointer"
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.POP,
                InstructionArg(RBP, Direct),
                comment = "Restore base pointer"
            )
        )
        emitInstruction(
            Instruction(
                InstructionType.RET,
                comment = "Return from call"
            )
        )
    }

    private fun emitLabel(instruction: Instruction) {
        builder
            .newline()
            .addLabel(emitArg(instruction.args[0]))
    }

    private fun emitPerformDivision(inst: Instruction) {
        val (arg1, arg2) = syntax.argOrder(emitArg(inst.args[1]), emitInstructionTarget(ReturnValue))
        builder
            .addLine(syntax.ops.getValue(InstructionType.MOV), arg1, arg2, "Setup dividend")
            .addLine("cqo", comment = "Sign extend into rdx")
            .addLine(syntax.ops.getValue(InstructionType.IDIV), emitArg(inst.args[0]), comment = "Divide")
    }

    private fun emitDivision(inst: Instruction) {
        emitPerformDivision(inst)
        val (arg1, arg2) = syntax.argOrder(emitInstructionTarget(ReturnValue), emitArg(inst.args[1]))
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
        // If size is set to -1, it is on stack top - move to register
        if (size == -1) {
            emitInstruction(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(RSP, Indirect),
                    InstructionArg(Register(OpReg1), Direct),
                    comment = "Get array size from stack top"
                )
            )
        }

        emitCallerCallee(false, CALLER_SAVE_REGISTERS)

        // Move arguments to param registers: size, base pointer, and stack pointer
        val args = listOf(
            if (size == -1) Register(OpReg1) else ImmediateValue("$size"),
            RBP, RSP
        )
        args.forEachIndexed { index, arg ->
            emitInstruction(
                Instruction(
                    InstructionType.MOV,
                    InstructionArg(arg, Direct),
                    InstructionArg(Register(ParamReg(index)), Direct),
                    comment = "Move argument to parameter register $index"
                )
            )
        }

        emitInstruction(
            Instruction(
                InstructionType.CALL,
                InstructionArg(Memory(ALLOCATE_HEAP_ROUTINE), Direct),
                comment = "Call heap allocator routine"
            )
        )

        emitCallerCallee(true, CALLER_SAVE_REGISTERS)
    }

    private fun emitAllocateStackSpace(numVariables: Int) {
        emitInstruction(
            Instruction(
                InstructionType.ADD,
                InstructionArg(ImmediateValue("${-VARIABLE_SIZE * numVariables}"), Direct),
                InstructionArg(RSP, Direct),
                comment = "Move stack pointer to allocate space for local variables"
            )
        )
    }

    private fun emitDeallocateStackSpace(numVariables: Int) {
        emitInstruction(
            Instruction(
                InstructionType.ADD,
                InstructionArg(ImmediateValue("${VARIABLE_SIZE * numVariables}"), Direct),
                InstructionArg(RSP, Direct),
                comment = "Move stack pointer to deallocate space for local variables"
            )
        )
    }
}
