package com.egern.emit

import com.egern.codegen.Instruction
import com.egern.codegen.InstructionType

class WindowsEmitter(
    instructions: List<Instruction>,
    dataFields: MutableList<String>,
    staticStrings: Map<String, String>,
    syntax: SyntaxManager
) : Emitter(instructions, dataFields, staticStrings, AsmStringBuilder(), syntax) {

    override val paramPassingRegs: List<String> = listOf("rcx", "rdx", "r8", "r9")

    companion object {
        const val SHADOW_SPACE_SIZE = 32
    }

//    override fun emitProgramPrologue() {
//        builder
//            .addLine("global", "_main")
//            .addLine("default", "rel")
//            .addLine("extern", "printf")
//            .addLine("extern", "malloc")
//            .addLine("extern", "free")
//        emitDataSection()
//        emitUninitializedDataSection()
//        builder.addLine("segment", ".text")
//    }
//
//    override fun emitDataSection() {
//        builder
//            .addLine("segment", ".data")
//        staticStrings.forEach {
//            if (it.key != "format_string")
//                builder.addLine("${it.key}: db \"${it.value}\", 10, 0")
//            else
//                builder.addLine("${it.key}: db \"${it.value}\", 0")
//
//        }
//        builder.newline()
//    }
//
//    private fun emitUninitializedDataSection() {
//        builder
//            .addLine("section .bss")
//            .addLine(HEAP_POINTER, " resq 1")
//            .addLine(VTABLE_POINTER, " resq 1")
//        dataFields.forEach {
//            builder.addLine(it, " resq 1")
//        }
//    }

    override fun emitProgramEpilogue() {
        //builder.addLine("format: db \"%d\", 10, 0")
    }

    private fun wrapCallInShadowSpace(call: () -> (Unit)) {
        allocateShadowSpace()
        call()
        deallocateShadowSpace()
    }

    override fun emitAllocateProgramHeap() {
        wrapCallInShadowSpace { emitAllocateProgramHeapBase() }
    }

    override fun emitAllocateVTable() {
        wrapCallInShadowSpace { emitAllocateVTableBase() }
    }

    override fun emitPrint(type: Int) {
        allocateShadowSpace()
        emitPrintBase(type, SHADOW_SPACE_SIZE)
        deallocateShadowSpace()
    }

    private fun allocateShadowSpace() {
        val (arg1, arg2) = syntax.argOrder("$SHADOW_SPACE_SIZE", syntax.register("rsp"))
        builder.addLine(syntax.ops.getValue(InstructionType.SUB), arg1, arg2, makeComment("Allocate shadow space"))
    }

    private fun deallocateShadowSpace() {
        val (arg1, arg2) = syntax.argOrder("$SHADOW_SPACE_SIZE", syntax.register("rsp"))
        builder.addLine(syntax.ops.getValue(InstructionType.ADD), arg1, arg2, makeComment("Deallocate shadow space"))
    }

    override fun emitMainLabel(): String {
        return "_main"
    }

}
