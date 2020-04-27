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
        wrapCallInShadowSpace { super.emitAllocateProgramHeap() }
    }

    override fun emitAllocateVTable() {
        wrapCallInShadowSpace { super.emitAllocateVTable() }
    }

    override fun emitDeallocateInternalHeap(pointer: String) {
        wrapCallInShadowSpace { super.emitDeallocateInternalHeap(pointer) }
    }

    override fun emitPrint(typeValue: Int) {
        wrapCallInShadowSpace { super.emitPrint(typeValue, SHADOW_SPACE_SIZE) }
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
