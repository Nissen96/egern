package com.egern.emit

import com.egern.codegen.*

class MacOSEmitter(
    instructions: List<Instruction>,
    dataFields: MutableList<String>,
    staticStrings: Map<String, String>,
    syntax: SyntaxManager
) : Emitter(instructions, dataFields, staticStrings, syntax) {

    override fun addPlatformPrefix(symbol: String): String {
        return "_$symbol"
    }

    override fun emitProgramEpilogue() {
        builder.addLine("format: db \"%d\", 10, 0")
    }

    override fun emitAllocateProgramHeap() {
        super.emitAllocateProgramHeap()
        TODO("Not yet implemented")
    }

    override fun emitAllocateVTable() {
        super.emitAllocateVTable()
        TODO("Not yet implemented")
    }

    override fun emitDeallocateInternalHeap(pointer: String) {
        super.emitDeallocateInternalHeap(pointer)
        TODO("Not yet implemented")
    }

    override fun emitPrint(typeValue: Int) {
        super.emitPrint(typeValue, 0)
        TODO("Not yet implemented")
    }

//    private var printfCounter = 0
//
//    override fun emitPrint(value: Int) {
//        // TODO: double check alignment (MacOS requires 16 byte)
//        // TODO: handle print empty
//        builder
//            .newline()
//            .addLine("; PRINTING USING PRINTF")
//            .addLine("lea", "rdi", "[format]", makeComment("Pass 1st argument in rdi"))
//            .addLine("mov", "rsi", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]", makeComment("Pass 2nd argument in rdi"))
//            .newline()
//            .addLine("; ALIGNMENT")
//            .addLine("mov", "rdx", "rsp")
//            .addLine("and", "rsp", "-16")
//            .addLine("xor", "rbx", "rbx")
//            .addLine("cmp", "rsp", "rdx")
//            .addLine("je", "was_aligned_${printfCounter}")
//            .addLine("inc", "rbx")
//            .addLine("was_aligned_${printfCounter}:")
//            .newline()
//            .addLine("xor", "rax", "rax")
//            .addLine("call", "_printf", comment = makeComment("Call function printf"))
//            .addLine("xor", "rcx", "rcx")
//            .addLine("cmp", "rbx", "rcx")
//            .addLine("je", "was_alinged_end_${printfCounter}")
//            .addLine("add", "rsp", "8")
//            .addLine("was_alinged_end_${printfCounter}:")
//
//        printfCounter++
//    }

    override fun emitMainLabel(): String {
        return "_main"
    }
}
