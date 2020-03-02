package com.egern.emit

import com.egern.codegen.Instruction

abstract class WindowsEmitter(instructions: List<Instruction>, syntax: SyntaxManager) :
    Emitter(instructions, AsmStringBuilder(";"), syntax) {

}