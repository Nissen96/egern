package com.egern.emit

import com.egern.codegen.InstructionType

class ATTSyntax : SyntaxManager() {
    override fun argOrder(source: String, destination: String): Pair<String, String> {
        return Pair(source, destination)
    }

    override fun immediate(value: String): String {
        return "$$value"
    }

    override fun register(reg: String): String {
        return "%$reg"
    }

    override fun indirect(target: String): String {
        return "($target)"
    }

    override fun indirectRelative(target: String, addressingOffset: Int, offset: Int): String {
        return "${addressingOffset * offset}($target)"
    }

    override val ops = mapOf(
        InstructionType.MOV to "movq",
        InstructionType.ADD to "addq",
        InstructionType.SUB to "subq",
        InstructionType.INC to "incq",
        InstructionType.DEC to "decq",
        InstructionType.IMUL to "imulq",
        InstructionType.IDIV to "idiv",
        InstructionType.CMP to "cmpq",
        InstructionType.JMP to "jmp",
        InstructionType.JNE to "jne",
        InstructionType.JE to "je",
        InstructionType.JG to "jg",
        InstructionType.JGE to "jge",
        InstructionType.JL to "jl",
        InstructionType.JLE to "jle",
        InstructionType.PUSH to "pushq",
        InstructionType.POP to "popq",
        InstructionType.CALL to "call",
        InstructionType.RET to "ret"
    )
}