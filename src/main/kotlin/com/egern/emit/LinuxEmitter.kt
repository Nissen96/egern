package com.egern.emit

import com.egern.codegen.*
import com.egern.types.ExprTypeEnum
import java.lang.Exception

class LinuxEmitter(
	instructions: List<Instruction>, 
	private val dataFields: List<String>,
    private val staticStrings: Map<String, String>,
	syntax: SyntaxManager
) :
    Emitter(instructions, AsmStringBuilder(), syntax) {

    override val paramPassingRegs: List<String> = listOf("rdi", "rsi", "rdx", "rcx", "r8", "r9")



    override fun emitProgramPrologue() {
        builder
            .addLine(".data")
            .addLine("format_int:")
            .addLine(".string \"%d\\n\"", comment = makeComment("Integer format string for C printf"))
            .addLine("format_string:")
            .addLine(".string \"%s\\n\"", comment = makeComment("String format string for C printf"))
            .addLine("format_newline:")
            .addLine(".string \"\\n\"", comment = makeComment("Empty format string for C printf"))
            .newline()
        emitDataSection()
        builder
            .addLine(".text")
            .addLine(".globl", "main")
            .newline()
    }

    override fun emitDataSection() {
        staticStrings.forEach {
            builder.addLine("${it.key}: .asciz \"${it.value}\"")
        }
        builder.newline()
        builder
            .addLine(".bss")
            .addLine(".lcomm $HEAP_POINTER, 8")
            .addLine(".lcomm $VTABLE_POINTER, 8")
        dataFields.forEach {
            builder.addLine(".lcomm $it, 8")
        }
        builder.newline()
    }

    override fun emitProgramEpilogue() {
        // Empty epilogue
    }

    override fun emitAllocateProgramHeap(heapSize: Int) {
        emitAllocateProgramHeapBase(heapSize)
    }

    override fun emitFreeProgramHeap() {
        builder.addLine("call free")
    }

    override fun emitPrint(value: Int) {
        val enumType = ExprTypeEnum.fromInt(value)
        val type = when (enumType) {
            ExprTypeEnum.VOID -> "newline"
            ExprTypeEnum.STRING -> "string"
            ExprTypeEnum.INT -> "int"
            ExprTypeEnum.BOOLEAN -> "string"
            else -> throw Exception("Printing $enumType is invalid")
        }
        builder
            .newline()
            .addLine("# PRINTING USING PRINTF")
            .addLine(
                "movq", "\$format_$type", "%rdi",
                "Pass 1st argument in %rdi"
            )
        if (enumType != ExprTypeEnum.VOID) {
            builder.addLine(
                "movq", "${8 * CALLER_SAVE_REGISTERS.size}(%rsp)", "%rsi",
                makeComment("Pass 2nd argument in %rsi")
            )
        }
        builder
            .addLine("xor", "%rax", "%rax", makeComment("No floating point registers used"))
            .addLine("call", "printf", comment = makeComment("Call function printf"))
    }

    override fun emitMainLabel(): String {
        return "main"
    }
}
