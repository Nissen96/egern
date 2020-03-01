package com.egern.emit

import com.egern.codegen.*
import java.lang.Exception

abstract class Emitter(private val instructions: List<Instruction>, protected val builder: AsmStringBuilder) {
    abstract fun emitProgramPrologue()
    abstract fun emitProgramEpilogue()
    abstract fun emitRegister(register: String): String
    abstract fun emitImmediate(value: String): String
    abstract fun emitIndirect(target: String): String
    abstract fun emitIndirectRelative(target: String, offset: Int): String
    abstract fun emitPrint(arg: MetaOperationArg)
    abstract fun emitMainLabel(): String
    abstract fun argPair(arg1: String, arg2: String): Pair<String, String>

    abstract val instructionMap: Map<InstructionType, String>

    protected companion object {
        const val VARIABLE_SIZE = 8
        const val ADDRESSING_OFFSET = -8

        val CALLER_SAVE_REGISTERS = listOf("rcx", "rdx", "rsi", "rdi", "r8", "r9", "r10", "r11")
        val CALLEE_SAVE_REGISTERS = listOf("rbx", "r12", "r13", "r14", "r15")
    }

    fun emit(): String {
        emitProgramPrologue()
        for (instruction in instructions) {
            emitInstruction(instruction)
            builder.newline()
        }
        emitProgramEpilogue()

        return builder.toFinalStr()
    }

    private fun emitInstruction(instruction: Instruction) {
        when (instruction.instructionType) {
            InstructionType.IDIV -> emitDivision(instruction)
            InstructionType.MOD -> emitModulo(instruction)
            InstructionType.LABEL -> emitLabel(instruction)
            InstructionType.META -> emitMetaOp(instruction)
            in instructionMap -> emitSimpleInstruction(instruction)
            else -> throw Exception("Unsupported operation ${instruction.instructionType}")
        }
        // Add comment
        if (instruction.comment != null) {
            builder.addComment(instruction.comment)
        }
    }

    private fun emitSimpleInstruction(instruction: Instruction) {
        val instr = instructionMap[instruction.instructionType]
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
            is ImmediateValue -> emitImmediate(argument.instructionTarget.value)
            is Memory -> argument.instructionTarget.address
            is Register -> when (argument.instructionTarget.register) {
                OpReg1 -> emitRegister("r12")
                OpReg2 -> emitRegister("r13")
                DataReg -> emitRegister("r14")
                is ParamReg -> emitRegister(CALLER_SAVE_REGISTERS[argument.instructionTarget.register.paramNum])
            }
            RBP -> emitRegister("rbp")
            RSP -> emitRegister("rsp")
            ReturnValue -> emitRegister("rax")
            StaticLink -> emitRegister("r15")
            MainLabel -> emitMainLabel()
        }

        return when (argument.addressingMode) {
            Direct -> target
            Indirect -> emitIndirect(target)
            is IndirectRelative -> emitIndirectRelative(target, argument.addressingMode.offset)
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

    private fun emitCallerCallee(restore: Boolean, registers: List<String>) {
        val op = if (restore) InstructionType.POP else InstructionType.PUSH
        builder
            .newline()
            .addComment("Caller/Callee ${if (restore) "Restore" else "Save"}")
        for (register in (if (restore) registers.reversed() else registers)) {
            builder.addLine(instructionMap.getValue(op), Pair(emitRegister(register), null))
        }
    }

    private fun emitCalleePrologue() {
        builder
            .addComment("Callee Prologue")
            .addLine(
                instructionMap.getValue(InstructionType.PUSH),
                Pair(emitRegister("rbp"), null),
                "Save caller's base pointer"
            )
            .addLine(
                instructionMap.getValue(InstructionType.MOV),
                argPair(emitRegister("rsp"), emitRegister("rbp")),
                "Make stack pointer new base pointer"
            )
    }

    private fun emitCalleeEpilogue() {
        builder
            .addComment("Callee Epilogue")
            .addLine(
                instructionMap.getValue(InstructionType.MOV),
                argPair(emitRegister("rbp"), emitRegister("rsp")),
                "Restore stack pointer"
            )
            .addLine(
                instructionMap.getValue(InstructionType.POP),
                Pair(emitRegister("rbp"), null),
                "Restore base pointer"
            )
            .addLine(instructionMap.getValue(InstructionType.RET), comment = "Return from call")
    }

    private fun emitLabel(instruction: Instruction) {
        builder
            .newline()
            .add(emitArg(instruction.args[0]) + ":", AsmStringBuilder.OP_OFFSET)
    }

    private fun emitPerformDivision(inst: Instruction) {
        builder
            .addLine(
                instructionMap.getValue(InstructionType.MOV),
                argPair(emitArg(inst.args[1]), emitRegister("rax")),
                "Setup dividend"
            )
            .addLine("cqo", comment = "Sign extend into rdx")
            .addLine(
                instructionMap.getValue(InstructionType.IDIV),
                Pair(emitArg(inst.args[0]), null),
                "Divide"
            )
    }

    private fun emitDivision(inst: Instruction) {
        emitPerformDivision(inst)
        builder.addLine(
            instructionMap.getValue(InstructionType.MOV),
            argPair(emitRegister("rax"), emitArg(inst.args[1])),
            "Move resulting quotient"
        )
    }

    private fun emitModulo(inst: Instruction) {
        emitPerformDivision(inst)
        builder.addLine(
            instructionMap.getValue(InstructionType.MOV),
            argPair(emitRegister("rdx"), emitArg(inst.args[1])),
            "Move resulting remainder"
        )
    }

    private fun emitAllocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            instructionMap.getValue(InstructionType.ADD),
            argPair(emitImmediate("${-VARIABLE_SIZE * arg.value}"), emitRegister("rsp")),
            "Move stack pointer to allocate space for local variables"
        )
    }

    private fun emitDeallocateStackSpace(arg: MetaOperationArg) {
        builder.addLine(
            instructionMap.getValue(InstructionType.ADD),
            argPair(emitImmediate("${VARIABLE_SIZE * arg.value}"), emitRegister("rsp")),
            "Move stack pointer to deallocate space for local variables"
        )
    }
}