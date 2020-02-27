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
            mapInstructionType(type) != null -> emitSimpleInstruction(instruction)
            type == InstructionType.LABEL -> emitLabel(instruction)
            type == InstructionType.META -> emitMetaOp(instruction)
            else -> throw Exception("Unsupported operation ${instruction.instructionType}")
        }
        // Add comment
        if (instruction.comment != null) {
            add(" # ${instruction.comment}")
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
        addLine(
            "addq $${-VARIABLE_SIZE * arg.value}, %rsp",
            "Move stack pointer to allocate space for local variables"
        )
    }

    private fun emitDeallocateStackSpace(arg: MetaOperationArg) {
        addLine(
            "addq $${VARIABLE_SIZE * arg.value}, %rsp",
            "Move stack pointer to deallocate space for local variables"
        )
    }

    private fun emitDivision(inst: Instruction) {
        add("movq ")
        emitArg(inst.args[1])
        addLine(", %rax", "Setup dividend")
        addLine("cqo", "Sign extend into %rdx")
        add("idiv ")
        emitArg(inst.args[0])
        addLine("", "Divide")
        add("movq %rax, ")
        emitArg(inst.args[1])
        addLine("", "Move resulting quotient")
    }

    private fun emitCalleePrologue() {
        addLine("", "Callee Prologue")
        addLine("pushq %rbp", "save caller's base pointer")
        addLine("movq %rsp, %rbp", "make stack pointer new base pointer")
    }

    private fun emitCalleeEpilogue() {
        addLine("", "Callee Epilogue")
        addLine("movq %rbp, %rsp", "Restore stack pointer")
        addLine("popq %rbp", "Restore base pointer")
        addLine("ret", "Return from call")
    }

    private fun emitProgramPrologue() {
        addLine(".data")
        addLine()
        addLine("format_int:")
        addLine(".string \"%d\\n\"", "integer format string for C printf")
        addLine("format_newline:")
        addLine(".string \"\\n\"", "empty format string for C printf")
        addLine()
        addLine(".text")
        addLine()
        addLine(".globl main")
        addLine()
    }

    private fun emitPrint(arg: MetaOperationArg) {
        val empty = arg.value == 0
        addLine("", "PRINTING USING PRINTF")
        addLine("movq \$format_${if (empty) "newline" else "int"}, %rdi", "pass 1. argument in %rdi")
        if (!empty) {
            addLine(
                "movq ${8 * CALLER_SAVE_REGISTERS.size}(%rsp), %rsi",
                "pass 2. argument in %rsi"
            )
        }
        addLine("movq $0, %rax", "no floating point registers used")
        addLine("call printf", "call function printf")
    }

    private fun emitCallerCallee(restore: Boolean, registers: List<String>) {
        val op = if (restore) mapInstructionType(InstructionType.POP)!! else mapInstructionType(InstructionType.PUSH)!!
        addLine("# Caller/Callee ${if (restore) "Restore" else "Save"}")
        for (register in if (restore) registers.reversed() else registers) {
            addLine("$op %$register")
        }
    }

    private fun emitLabel(instruction: Instruction) {
        emitArg(instruction.args[0])
        add(":")
    }

    private fun emitSimpleInstruction(instruction: Instruction) {
        add(mapInstructionType(instruction.instructionType)!!)
        emitArgs(instruction.args)
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
        if (argument is InstructionArg) emitInstructionArg(argument)
        else throw Exception("Trying to emit an argument that cant be emitted!")
    }

    private fun emitInstructionArg(argument: InstructionArg) {
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

        add(
            when (argument.addressingMode) {
                Direct -> target
                Indirect -> "($target)"
                is IndirectRelative -> "${ADDRESSING_OFFSET * argument.addressingMode.offset}($target)"
            }
        )
    }
}
