package com.egern.emit

import com.egern.codegen.*
import java.lang.Exception

class Emitter(private val instructions: List<Instruction>) {
    private val builder = StringBuilder()

    companion object {
        const val ADDRESSING_OFFSET = -8

        val CALLER_SAVE_REGISTERS = listOf("rcx", "rdx", "rsi", "rdi", "r8", "r9", "r10", "r11")
        val CALLEE_SAVE_REGISTERS = listOf("rbx", "r12", "r13", "r14", "r15")
    }

    fun emit(): StringBuilder {
        for (instruction in instructions) {
            emitInstruction(instruction)
            builder.appendln()
        }
        return builder
    }

    private fun add(s: String) {
        builder.append(s)
    }

    private fun addLine(s: String) {
        builder.appendln(s)
    }

    private fun emitInstruction(instruction: Instruction) {
        when {
            instruction.instructionType.instruction != null -> emitSimpleInstruction(instruction)
            instruction.instructionType == InstructionType.LABEL -> emitLabel(instruction)
            instruction.instructionType == InstructionType.META -> emitMeta(instruction)
            else -> throw Exception("Unsupported operation ${instruction.instructionType}")
        }
        // Add comment
        if (instruction.comment != null) {
            add(" # ${instruction.comment}")
        }
    }

    private fun emitMeta(instruction: Instruction) {
        when {
            instruction.args[0] is MetaOperation -> emitMetaOp(instruction.args[0] as MetaOperation)
        }
    }

    private fun emitMetaOp(operation: MetaOperation) {
        when (operation) {
            MetaOperation.CallerSave -> emitCallerCallee(false, CALLER_SAVE_REGISTERS)
            MetaOperation.CallerRestore -> emitCallerCallee(true, CALLER_SAVE_REGISTERS)
            MetaOperation.CalleeSave -> emitCallerCallee(false, CALLEE_SAVE_REGISTERS)
            MetaOperation.CalleeRestore -> emitCallerCallee(true, CALLEE_SAVE_REGISTERS)
        }
    }

    private fun emitCallerCallee(restore: Boolean, registers: List<String>) {
        val op = if (restore) InstructionType.POP.instruction!! else InstructionType.PUSH.instruction!!
        addLine("# Caller/Callee Save/Restore")
        for (register in if (restore) registers.reversed() else registers) {
            addLine("$op %$register")
        }
    }

    private fun emitLabel(instruction: Instruction) {
        emitArgs(instruction.args)
        add(":")
    }

    private fun emitSimpleInstruction(instruction: Instruction) {
        add(instruction.instructionType.instruction!!)
        emitArgs(instruction.args);
    }

    private fun emitArgs(arguments: Array<out Arg>) {
        if (arguments.isNotEmpty()) {
            add(" ")
            emitArg(arguments[0])
        }
        if (arguments.size > 1) {
            for (arg in arguments.slice(1 until arguments.size)) {
                add(", ")
                emitArg(arg)
            }
        }
    }

    private fun emitArg(argument: Arg) {
        when (argument) {
            is InstructionArg -> emitInstructionArg(argument)
            else -> throw Exception("Trying to emit an argument that cant be emitted!")
        }
    }

    private fun emitInstructionArg(argument: InstructionArg) {
        val target = when (argument.instructionTarget) {
            is ImmediateValue -> "$${argument.instructionTarget.value}"
            is Memory -> argument.instructionTarget.address
            is Register -> when (argument.instructionTarget.register) {
                RegisterKind.OpReg1 -> "%r12"
                RegisterKind.OpReg2 -> "%r13"
                RegisterKind.DataReg -> "%r14"
            }
            RBP -> "%rbp"
            RSP -> "%rsp"
            ReturnValue -> "%rax"
            StaticLink -> "%rdx"
        }

        add(
            when (argument.addressingMode) {
                Direct -> target
                Indirect -> "($target)"
                is IndirectRelative -> "${ADDRESSING_OFFSET * argument.addressingMode.offset}($target)"
            }
        )
    }
}