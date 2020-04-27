package com.egern.emit

import com.egern.codegen.*
import com.egern.types.ExprTypeEnum

abstract class Emitter(
    private val instructions: List<Instruction>,
    protected val dataFields: MutableList<String>,
    protected val staticStrings: Map<String, String>,
    protected val builder: AsmStringBuilder,
    protected val syntax: SyntaxManager
) {
    abstract fun emitProgramEpilogue()
    abstract fun emitAllocateProgramHeap()
    abstract fun emitAllocateVTable()
    abstract fun emitDeallocateInternalHeap(pointer: String)
    abstract fun emitPrint(type: Int)
    abstract fun emitMainLabel(): String
    abstract val paramPassingRegs: List<String>

    // Defaults to nothing; can be overwritten
    open fun addPlatformPrefix(symbol: String): String {
        return symbol
    }

    protected companion object {
        const val VARIABLE_SIZE = 8
        const val ADDRESSING_OFFSET = -8

        val CALLER_SAVE_REGISTERS = listOf("rcx", "rdx", "rsi", "rdi", "r8", "r9", "r10", "r11")
        val CALLEE_SAVE_REGISTERS = listOf("rbx", "r12", "r13", "r14", "r15")
        const val HEAP_POINTER: String = "heap_pointer"
        const val HEAP_SIZE: Int = 1024
        const val VTABLE_POINTER: String = "vtable_pointer"
        const val VTABLE_SIZE: Int = 1024
    }

    fun emit(): String {
        emitProgramPrologue()
        for (instruction in instructions) {
            emitInstruction(instruction)
        }
        emitProgramEpilogue()

        return builder.toFinalStr()
    }

    protected fun makeComment(text: String): Comment {
        return Comment(syntax.commentSym(), text)
    }

    fun emitProgramPrologue() {
        dataFields.add(HEAP_POINTER)
        dataFields.add(VTABLE_POINTER)
        syntax.emitPrologue(builder,  emitMainLabel(), addPlatformPrefix(""), dataFields, staticStrings)
    }

    protected fun emitPrintBase(value: Int, additionalOffset: Int = 0) {
        val enumType = ExprTypeEnum.fromInt(value)
        val type = when (enumType) {
            ExprTypeEnum.VOID -> "newline"
            ExprTypeEnum.STRING -> "string"
            ExprTypeEnum.INT -> "int"
            ExprTypeEnum.BOOLEAN -> "string"
            else -> throw Exception("Printing $enumType is invalid")
        }

        val (arg1, arg2) = syntax.argOrder(
            syntax.immediate("format_$type"),
            syntax.register(paramPassingRegs[0])
        )

        builder
            .newline()
            .addComment(comment = makeComment("PRINTING USING PRINTF"))
            .newline()
            .addLine(
                syntax.ops.getValue(InstructionType.MOV),
                arg1, arg2,
                makeComment("Pass formatting as 1st argument in ${paramPassingRegs[0]}")
            )
        if (enumType != ExprTypeEnum.VOID) {
            val (arg3, arg4) = syntax.argOrder(
                syntax.indirectRelative(syntax.register("rsp"), -ADDRESSING_OFFSET * CALLER_SAVE_REGISTERS.size + additionalOffset),
                syntax.register(paramPassingRegs[1])
            )
            builder.addLine(
                syntax.ops.getValue(InstructionType.MOV), arg3, arg4,
                makeComment("Pass possible value to print as 2nd argument in ${paramPassingRegs[1]}")
            )
        }
        builder
            .addLine(
                syntax.ops.getValue(InstructionType.XOR),
                syntax.register("rax"),
                syntax.register("rax"),
                makeComment("No floating point registers used")
            )
            .addLine("call", addPlatformPrefix("printf"), comment = makeComment("Call function printf"))
    }

    private fun emitAllocateInternalHeaps() {
        emitAllocateProgramHeap()
        emitAllocateVTable()
    }

    protected fun emitAllocateProgramHeapBase() {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * HEAP_SIZE}"), syntax.register(paramPassingRegs[0]))
        val (arg3, arg4) = syntax.argOrder(emitInstructionTarget(ReturnValue), emitInstructionTarget(RHP))
        val (arg5, arg6) = syntax.argOrder(emitInstructionTarget(ReturnValue), syntax.indirect(HEAP_POINTER))
        builder
            .addLine(
                syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
                makeComment("Move argument into parameter register for malloc call")
            )
            .addLine("call ${addPlatformPrefix("malloc")}")
            .addLine(
                syntax.ops.getValue(InstructionType.MOV), arg3, arg4,
                makeComment("Move returned heap pointer to fixed heap pointer register")
            )
            .addLine(
                syntax.ops.getValue(InstructionType.MOV), arg5, arg6,
                makeComment("Save start of heap pointer globally")
            )

    }

    protected fun emitAllocateVTableBase() {

        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * VTABLE_SIZE}"), syntax.register(paramPassingRegs[0]))
        val (arg3, arg4) = syntax.argOrder(emitInstructionTarget(ReturnValue), syntax.indirect(VTABLE_POINTER))
        builder
            .addLine(
                syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
                makeComment("Move argument into parameter register for malloc call")
             )
            .addLine("call ${addPlatformPrefix("malloc")}")
            .addLine(
                syntax.ops.getValue(InstructionType.MOV), arg3, arg4,
                makeComment("Save start of vtable pointer globally")
            )
    }

    private fun emitDeallocateInternalHeaps() {
        emitDeallocateInternalHeap(VTABLE_POINTER)
        emitDeallocateInternalHeap(HEAP_POINTER)
    }

    protected fun emitDeallocateInternalHeapBase(pointer: String) {
        val (arg1, arg2) = syntax.argOrder(pointer, syntax.register(paramPassingRegs[0]))
        builder
            .addLine(
                syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
                makeComment("Move argument into parameter register for free call")
            )
            .addLine("call ${addPlatformPrefix("free")}")
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
            builder.addComment(makeComment(instruction.comment))
        }
        builder.newline()
    }

    private fun emitCall(instruction: Instruction) {
        val instr = syntax.ops.getValue(instruction.instructionType)
        builder.addOp(instr)

        // Check for indirect function call
        val prefix = if ((instruction.args[0] as InstructionArg).instructionTarget is Register) syntax.indirectFuncCall() else ""
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
            MetaOperation.DeallocateHeapSpace -> emitDeallocateHeapSpace(value)
        }
    }

    private fun emitCallerCallee(restore: Boolean, registers: List<String>) {
        val op = if (restore) InstructionType.POP else InstructionType.PUSH
        builder
            .newline()
            .addComment(makeComment("Caller/Callee ${if (restore) "Restore" else "Save"}"))
            .newline()
        for (register in (if (restore) registers.reversed() else registers)) {
            builder.addLine(syntax.ops.getValue(op), syntax.register(register))
        }
        builder.newline()
    }

    private fun emitCalleePrologue() {
        val (reg1, reg2) = syntax.argOrder(syntax.register("rsp"), syntax.register("rbp"))
        builder
            .addComment(makeComment("Callee Prologue"))
            .newline()
            .addLine(
                syntax.ops.getValue(InstructionType.PUSH),
                syntax.register("rbp"),
                comment = makeComment("Save caller's base pointer")
            )
            .addLine(syntax.ops.getValue(InstructionType.MOV), reg1, reg2, makeComment("Make stack pointer new base pointer"))
    }

    private fun emitCalleeEpilogue() {
        val (reg1, reg2) = syntax.argOrder(syntax.register("rbp"), syntax.register("rsp"))
        builder
            .addComment(makeComment("Callee Epilogue"))
            .newline()
            .addLine(syntax.ops.getValue(InstructionType.MOV), reg1, reg2, makeComment("Restore stack pointer"))
            .addLine(syntax.ops.getValue(InstructionType.POP), syntax.register("rbp"), comment = makeComment("Restore base pointer"))
            .addLine(syntax.ops.getValue(InstructionType.RET), comment = makeComment("Return from call"))
    }

    private fun emitLabel(instruction: Instruction) {
        builder
            .newline()
            .addLabel(emitArg(instruction.args[0]))
            .newline()
    }

    private fun emitPerformDivision(inst: Instruction) {
        val (arg1, arg2) = syntax.argOrder(emitArg(inst.args[1]), syntax.register("rax"))
        builder
            .addLine(syntax.ops.getValue(InstructionType.MOV), arg1, arg2, makeComment("Setup dividend"))
            .addLine("cqo", comment = makeComment("Sign extend into rdx"))
            .addLine(syntax.ops.getValue(InstructionType.IDIV), emitArg(inst.args[0]), comment = makeComment("Divide"))
    }

    private fun emitDivision(inst: Instruction) {
        emitPerformDivision(inst)
        val (arg1, arg2) = syntax.argOrder(syntax.register("rax"), emitArg(inst.args[1]))
        builder.addLine(syntax.ops.getValue(InstructionType.MOV), arg1, arg2, makeComment("Move resulting quotient"))
    }

    private fun emitModulo(inst: Instruction) {
        emitPerformDivision(inst)
        val (arg1, arg2) = syntax.argOrder(syntax.register("rdx"), emitArg(inst.args[1]))
        builder.addLine(syntax.ops.getValue(InstructionType.MOV), arg1, arg2, makeComment("Move resulting remainder"))
    }

    private fun emitBooleanNot(inst: Instruction) {
        val arg = emitArg(inst.args[0])
        builder
            .addLine("test", arg, arg, makeComment("Test argument with itself"))
            .addLine("setz", "${arg}b", comment = makeComment("Set first byte to 1 if zero etc."))
    }

    private fun emitAllocateHeapSpace(size: Int) {
        val (arg1, arg2) = syntax.argOrder(emitInstructionTarget(RHP), emitInstructionTarget(ReturnValue))
        val (arg3, arg4) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * size}"), emitInstructionTarget(RHP))
        builder.addLine(
            syntax.ops.getValue(InstructionType.MOV), arg1, arg2,
            makeComment("Move pointer to return value")
        ).addLine(
            syntax.ops.getValue(InstructionType.ADD), arg3, arg4,
            makeComment("Offset pointer by allocated bytes")
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
            makeComment("Move stack pointer to allocate space for local variables")
        )
    }

    private fun emitDeallocateStackSpace(numVariables: Int) {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * numVariables}"), syntax.register("rsp"))
        builder.addLine(
            syntax.ops.getValue(InstructionType.ADD), arg1, arg2,
            makeComment("Move stack pointer to deallocate space for local variables")
        )
    }
}
