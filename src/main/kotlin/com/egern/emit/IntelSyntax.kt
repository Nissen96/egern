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

    override fun indirectRelative(target: String, offset: Int): String {
        return "qword [$target + $offset]"
    }

    override fun commentSym(): String {
        return ";"
    }

    override fun indirectFuncCall(): String {
        return ""
    }

    override fun emitPrologue(asmStringBuilder: AsmStringBuilder, mainLabel: String, platformPrefix: String, dataFields: List<String>,
                              staticStrings: Map<String, String>) {
        asmStringBuilder
            .addLine("global", mainLabel)
            .addLine("default", "rel")
            .addLine("extern", "${platformPrefix}printf")
            .addLine("extern", "${platformPrefix}malloc")
            .addLine("extern", "${platformPrefix}free")
        emitDataSection(asmStringBuilder, staticStrings)
        emitUninitializedDataSection(asmStringBuilder, dataFields)
        asmStringBuilder.addLine("segment", ".text")
    }

    private fun emitDataSection(asmStringBuilder: AsmStringBuilder, staticStrings: Map<String, String>) {
        asmStringBuilder.addLine("section", ".data")
        staticStrings.forEach {
            if (it.key != "format_string")
                asmStringBuilder.addLine("${it.key}: db \"${it.value}\", 10, 0")
            else
                asmStringBuilder.addLine("${it.key}: db \"${it.value}\", 0")

        }
        asmStringBuilder.newline()
    }

    private fun emitUninitializedDataSection(asmStringBuilder: AsmStringBuilder, dataFields: List<String>) {
        asmStringBuilder.addLine("section .bss")
        dataFields.forEach {
            asmStringBuilder.addLine(it, " resq 1")
        }
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
        InstructionType.RET to "ret",
        InstructionType.XOR to "xor"
    )
}