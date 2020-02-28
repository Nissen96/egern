package com.egern.emit

import com.egern.codegen.*
import java.lang.Exception

abstract class Emitter(protected val instructions: List<Instruction>, protected val builder: AsmStringBuilder) {
    abstract fun emit(): String
    abstract fun mapInstructionType(type: InstructionType): String?

    protected companion object {
        const val VARIABLE_SIZE = 8
        const val ADDRESSING_OFFSET = -8

        val CALLER_SAVE_REGISTERS = listOf("rcx", "rdx", "rsi", "rdi", "r8", "r9", "r10", "r11")
        val CALLEE_SAVE_REGISTERS = listOf("rbx", "r12", "r13", "r14", "r15")
    }
}