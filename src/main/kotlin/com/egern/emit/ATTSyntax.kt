package com.egern.emit

import com.egern.codegen.InstructionType

class ATTSyntax : SyntaxManager() {
    override val commentSymbol = "#"

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

    override fun indirectRelative(target: String, offset: Int): String {
        return "$offset($target)"
    }

    override fun indirectFuncCall(): String {
        return "*"
    }

    override fun emitPrologue(
        asmStringBuilder: AsmStringBuilder,
        mainLabel: String,
        platformPrefix: String,
        dataFields: List<String>,
        staticStrings: Map<String, String>
    ) {
        asmStringBuilder.addLine(".globl", mainLabel)
        emitDataSection(asmStringBuilder, staticStrings)
        emitUninitializedDataSection(asmStringBuilder, dataFields)
        asmStringBuilder
            .addLine(".text")
            .newline()
    }

    private fun emitDataSection(asmStringBuilder: AsmStringBuilder, staticStrings: Map<String, String>) {
        asmStringBuilder.addLine(".data")
        staticStrings.forEach {
            asmStringBuilder.addLine(
                "${it.key}: .asciz \"${it.value}${if (it.key.startsWith("format_")) "\\n" else ""}\""
            )
        }
        asmStringBuilder.newline()
    }

    private fun emitUninitializedDataSection(asmStringBuilder: AsmStringBuilder, dataFields: List<String>) {
        asmStringBuilder.addLine(".bss")
        dataFields.forEach {
            asmStringBuilder.addLine(".lcomm $it, 8")
        }
        asmStringBuilder.newline()
    }

    override val ops = mapOf(
        InstructionType.MOV to "movq",
        InstructionType.ADD to "addq",
        InstructionType.SUB to "subq",
        InstructionType.INC to "incq",
        InstructionType.DEC to "decq",
        InstructionType.IMUL to "imulq",
        InstructionType.IDIV to "idiv",
        InstructionType.AND to "andq",
        InstructionType.OR to "orq",
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
        InstructionType.RET to "ret",
        InstructionType.XOR to "xor"
    )
}