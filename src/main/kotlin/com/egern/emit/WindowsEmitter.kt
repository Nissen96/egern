package com.egern.emit

import com.egern.codegen.Instruction
import com.egern.codegen.InstructionType

abstract class WindowsEmitter(instructions: List<Instruction>) : Emitter(instructions, AsmStringBuilder(";")) {
    
}