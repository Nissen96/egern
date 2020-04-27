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

    override fun emitDeallocateInternalHeap(pointer: String) {
        allocateShadowSpace()
        emitDeallocateInternalHeapBase(pointer)
        deallocateShadowSpace()
    }

    override fun emitPrint(type: Int) {
        allocateShadowSpace()
        emitPrintBase(type, SHADOW_SPACE_SIZE)
        deallocateShadowSpace()
    }

    private fun allocateShadowSpace() {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("$SHADOW_SPACE_SIZE"), syntax.register("rsp"))
        builder.addLine(syntax.ops.getValue(InstructionType.SUB), arg1, arg2, makeComment("Allocate shadow space"))
    }

    private fun deallocateShadowSpace() {
        val (arg1, arg2) = syntax.argOrder(syntax.immediate("$SHADOW_SPACE_SIZE"), syntax.register("rsp"))
        builder.addLine(syntax.ops.getValue(InstructionType.ADD), arg1, arg2, makeComment("Deallocate shadow space"))
    }

    override fun emitMainLabel(): String {
        return "_main"
    }

}
