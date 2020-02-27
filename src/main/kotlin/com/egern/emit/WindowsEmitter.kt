package com.egern.emit

import com.egern.codegen.Instruction
import com.egern.codegen.InstructionType

class WindowsEmitter(instructions: List<Instruction>): Emitter(instructions, AsmStringBuilder(";")) {
    override fun emit(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mapInstructionType(type: InstructionType): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}