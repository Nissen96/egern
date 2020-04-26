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

//    override fun emitProgramPrologue() {
//        builder
//            .addLine(".data")
//            .addLine("format_int:")
//            .addLine(".string \"%d\\n\"", comment = makeComment("Integer format string for C printf"))
//            .addLine("format_string:")
//            .addLine(".string \"%s\\n\"", comment = makeComment("String format string for C printf"))
//            .addLine("format_newline:")
//            .addLine(".string \"\\n\"", comment = makeComment("Empty format string for C printf"))
//            .newline()
//        emitDataSection()
//        builder
//            .addLine(".text")
//            .addLine(".globl", "main")
//            .newline()
//    }
//
//    override fun emitDataSection() {
//        staticStrings.forEach {
//            builder.addLine("${it.key}: .asciz \"${it.value}\"")
//        }
//        builder.newline()
//        builder
//            .addLine(".bss")
//            .addLine(".lcomm $HEAP_POINTER, 8")
//            .addLine(".lcomm $VTABLE_POINTER, 8")
//        dataFields.forEach {
//            builder.addLine(".lcomm $it, 8")
//        }
//        builder.newline()
//    }

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
