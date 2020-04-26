package com.egern.emit

import com.egern.codegen.*
import com.egern.types.ExprTypeEnum
import java.lang.Exception

class LinuxEmitter(
	instructions: List<Instruction>, 
	dataFields: MutableList<String>,
    staticStrings: Map<String, String>,
	syntax: SyntaxManager
) :
    Emitter(instructions, dataFields, staticStrings, AsmStringBuilder(), syntax) {

    override val paramPassingRegs: List<String> = listOf("rdi", "rsi", "rdx", "rcx", "r8", "r9")

    override fun emitProgramEpilogue() {
        // Empty epilogue
    }

    override fun emitAllocateProgramHeap() {
        emitAllocateProgramHeapBase()
    }

    override fun emitAllocateVTable() {
        emitAllocateVTableBase()
    }

    override fun emitPrint(type: Int) {
        emitPrintBase(type)
    }

    override fun emitMainLabel(): String {
        return "main"
    }
}
