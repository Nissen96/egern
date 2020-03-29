package com.egern.emit

import com.egern.codegen.*

class MacOSEmitter(instructions: List<Instruction>, private val dataFields: List<String>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder(), syntax) {

    override val paramPassingRegs: List<String> = listOf("rdi", "rsi", "rdx", "rcx", "r8", "r9")
    override fun addPlatformPrefix(symbol: String): String {
        return "_$symbol"
    }


    override fun emitProgramPrologue() {
        builder
            .addLine("global", "_main")
            .addLine("extern", "_printf")
            .addLine("default rel")
            .newline()
        emitDataSection()
        builder.addLine("section .text")
    }

    override fun emitDataSection() {
        builder
            .addLine(".bss")
            .addLine("$HEAP_POINTER: resq 1")
            .addLine("$VTABLE_POINTER: resq 1")
        dataFields.forEach {
            builder.addLine("$it: resq 1")
        }
        builder.newline()
    }

    override fun emitProgramEpilogue() {
        builder.addLine("format: db \"%d\", 10, 0")
    }

    override fun emitAllocateProgramHeap(heapSize: Int) {
        // TODO: align to 16 byte boundary
        emitAllocateProgramHeapBase(heapSize)
    }

    override fun emitFreeProgramHeap() {
        builder.addLine("call free")
    }

    private var printfCounter = 0

    override fun emitPrint(value: Int) {
        // TODO: double check alignment (MacOS requires 16 byte)
        // TODO: handle print empty
        builder
            .newline()
            .addLine("; PRINTING USING PRINTF")
            .addLine("lea", "rdi", "[format]", makeComment("Pass 1st argument in rdi"))
            .addLine("mov", "rsi", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]", makeComment("Pass 2nd argument in rdi"))
            .newline()
            .addLine("; ALIGNMENT")
            .addLine("mov", "rdx", "rsp")
            .addLine("and", "rsp", "-16")
            .addLine("xor", "rbx", "rbx")
            .addLine("cmp", "rsp", "rdx")
            .addLine("je", "was_aligned_${printfCounter}")
            .addLine("inc", "rbx")
            .addLine("was_aligned_${printfCounter}:")
            .newline()
            .addLine("xor", "rax", "rax")
            .addLine("call", "_printf", comment = makeComment("Call function printf"))
            .addLine("xor", "rcx", "rcx")
            .addLine("cmp", "rbx", "rcx")
            .addLine("je", "was_alinged_end_${printfCounter}")
            .addLine("add", "rsp", "8")
            .addLine("was_alinged_end_${printfCounter}:")

        printfCounter++
    }

    override fun emitMainLabel(): String {
        return "_main"
    }
}
