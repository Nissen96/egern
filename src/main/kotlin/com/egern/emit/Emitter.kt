package com.egern.emit

import com.egern.codegen.*
import java.lang.Exception

abstract class Emitter(
    private val instructions: List<Instruction>,
    protected val builder: AsmStringBuilder,
    private val syntax: SyntaxManager
) {
    abstract fun emitProgramPrologue()
    abstract fun emitProgramEpilogue()
    abstract fun emitPrint(arg: MetaOperationArg)
    abstract fun emitMainLabel(): String

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
        }
        emitProgramEpilogue()

        return builder.toFinalStr()
    }

    private fun emitInstruction(instruction: Instruction) {
        when (instruction.instructionType) {
            InstructionType.IDIV -> emitDivision(instruction)
            InstructionType.MOD -> emitModulo(instruction)
            InstructionType.NOT -> emitBooleanNot(instruction)
            InstructionType.LABEL -> emitLabel(instruction)
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

    private fun emitInstructionArg(argument: InstructionArg): String {
        val target = when (argument.instructionTarget) {
            is ImmediateValue -> syntax.immediate(argument.instructionTarget.value)
            is Memory -> argument.instructionTarget.address
            is Register -> when (argument.instructionTarget.register) {
                OpReg1 -> syntax.register("r12")
                OpReg2 -> syntax.register("r13")
                DataReg -> syntax.register("r14")
                is ParamReg -> syntax.register(CALLER_SAVE_REGISTERS[argument.instructionTarget.register.paramNum])
            }
            RBP -> syntax.register("rbp")
            RSP -> syntax.register("rsp")
            ReturnValue -> syntax.register("rax")
            StaticLink -> syntax.register("r15")
            MainLabel -> emitMainLabel()
        }

        return when (argument.addressingMode) {
            Direct -> target
            Indirect -> syntax.indirect(target)
            is IndirectRelative -> syntax.indirectRelative(target, ADDRESSING_OFFSET, argument.addressingMode.offset)
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
            .addLine("setz", arg, comment = "Set to 1 if zero etc.")
    }

    private fun emitAllocateStackSpace(arg: MetaOperationArg) {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${-VARIABLE_SIZE * arg.value}"), syntax.register("rsp"))
        builder.addLine(
            syntax.ops.getValue(InstructionType.ADD), arg1, arg2,
            "Move stack pointer to allocate space for local variables"
        )
    }

    private fun emitDeallocateStackSpace(arg: MetaOperationArg) {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("${VARIABLE_SIZE * arg.value}"), syntax.register("rsp"))
        builder.addLine(
            syntax.ops.getValue(InstructionType.ADD), arg1, arg2,
            "Move stack pointer to deallocate space for local variables"
        )
    }
}
