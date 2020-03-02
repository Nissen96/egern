package com.egern.emit

import com.egern.codegen.InstructionType

class IntelSyntax : SyntaxManager() {
    override fun argOrder(source: String, destination: String): Pair<String, String> {
        return Pair(destination, source)
    }

    override fun immediate(value: String): String {
        return value
    }

    override fun register(reg: String): String {
        return reg
    }

    override fun indirect(target: String): String {
        return "qword [$target]"
    }

    override fun indirectRelative(target: String, addressingOffset: Int, offset: Int): String {
        return "qword [$target + ${addressingOffset * offset}]"
    }

    override val ops = mapOf(
        InstructionType.MOV to "mov",
        InstructionType.ADD to "add",
        InstructionType.SUB to "sub",
        InstructionType.INC to "inc",
        InstructionType.DEC to "dec",
        InstructionType.IMUL to "imul",
        InstructionType.IDIV to "idiv",
        InstructionType.AND to "and",
        InstructionType.OR to "or",
        InstructionType.CMP to "cmp",
        InstructionType.JMP to "jmp",
        InstructionType.JNE to "jne",
        InstructionType.JE to "je",
        InstructionType.JG to "jg",
        InstructionType.JGE to "jge",
        InstructionType.JL to "jl",
        InstructionType.JLE to "jle",
        InstructionType.PUSH to "push",
        InstructionType.POP to "pop",
        InstructionType.CALL to "call",
        InstructionType.RET to "ret"
    )
}