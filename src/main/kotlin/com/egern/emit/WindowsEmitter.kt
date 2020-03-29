package com.egern.emit

import com.egern.codegen.Instruction
import com.egern.codegen.InstructionType
import com.sun.org.apache.xpath.internal.operations.Bool

class WindowsEmitter(instructions: List<Instruction>, dataFields: List<String>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder(), syntax) {

    override val paramPassingRegs: List<String> = listOf("rcx", "rdx", "r8", "r9")


    companion object {
        const val SHADOW_SPACE_SIZE = 32
    }

    override fun emitProgramPrologue() {
        builder
            .addLine("global", "_main")
            .addLine("extern", "printf")
            .addLine("extern", "malloc")
            .addLine("segment", ".data")
            .addLine("format_int: db \"%d\", 10, 0")
            .addLine("format_newline:  db \"\", 10, 0", comment = makeComment("Empty format string for C printf"))
            emitDataSection()
            builder.addLine("segment", ".text")
    }

	override fun emitDataSection() {
        builder
            .addLine("section .bss")
            .addLine("alignb", "8")
            .addLine("Handle", "resq 1")
            .addLine("Written", "resq 1")
            .newline()
    }

    override fun emitProgramEpilogue() {
        //builder.addLine("format: db \"%d\", 10, 0")
    }

    override fun emitRequestProgramHeap() {
        builder
            .addLine("sub", "rsp", "32")
            .addLine("call malloc")
            .addLine("add", "rsp", "32")
    }



    private fun deallocateShadowSpace() {
        val (arg1, arg2) = syntax.argOrder("$SHADOW_SPACE_SIZE", syntax.register("rsp"))
        builder.addLine(syntax.ops.getValue(InstructionType.ADD), arg1, arg2)
    }

    override fun emitPrint(isEmpty: Boolean) {
        allocateShadowSpace()
        emitPrintBase(isEmpty, SHADOW_SPACE_SIZE)
        deallocateShadowSpace()
    }

    override fun emitMainLabel(): String {
        return "_main"
    }

}
