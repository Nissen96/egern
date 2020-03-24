package com.egern.emit

import com.egern.codegen.Instruction

class WindowsEmitter(instructions: List<Instruction>, dataFields: List<String>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder(";"), syntax) {

    override fun emitProgramPrologue() {
        builder
            .addLine("global", "main")
            .addLine("extern", "GetStdHandle")
            .addLine("extern", "WriteFile")
            .addLine("extern", "ExitProcess")
            .addLine("extern", "malloc")
            .addLine("extern", "free")
            .addLine("NULL EQU 0")
            .addLine("STD_HANDLE EQU -11")
        emitDataSection()
        builder.addLine("section .text")
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
        builder.addLine("call malloc")
    }

    override fun emitFreeProgramHeap() {
        builder.addLine("call free")
    }

    override fun emitPrint(isEmpty: Boolean) {
        // TODO: handle print empty
        builder
            .newline()
            .addLine("; Get handle")
            .addLine("sub", "rsp", "32")
            .addLine("mov", "ecx", "STD_HANDLE")
            .addLine("call", "GetStdHandle")
            .addLine("mov", "qword [REL Handle]", "rax")
            .addLine("add", "rsp", "32")
            .newline()
            .addLine("; Write to file")
            //.addLine("mov", "rdx", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size}]")
            .addLine("sub", "rsp", "40")
            .addLine("mov", "rcx", "qword [REL Handle]")

            .addLine("mov", "r15", "[rsp + ${8 * CALLER_SAVE_REGISTERS.size} + 40]")
            .addLine("add", "r15", "48")
            .addLine("push", "r15")
            .addLine("lea", "rdx", "[rsp]")
            .addLine("mov", "r8", "1")
            .addLine("lea", "r9", "[REL Written]")
            .addLine("mov", "qword [rsp + 4 * 8]", "NULL")
            .addLine("call", "WriteFile")
            .addLine("add", "rsp", "40")

    }

    override fun emitMainLabel(): String {
        return "main"
    }

}