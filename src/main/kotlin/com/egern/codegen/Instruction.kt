package com.egern.codegen

import com.egern.ast.PrintStmt
import java.lang.Exception

class Instruction(val instructionType: InstructionType, vararg val args: Arg, val comment: String? = null)

fun buildPrintInstruction(printStmt: PrintStmt): Instruction {
	return Instruction(
                InstructionType.META,
                MetaOperation.Print,
                MetaOperationArg(if (printStmt.expr != null) 0 else 1)
            )
}

fun printInstructionIsEmpty(printArg: MetaOperationArg): Boolean {
    return when(printArg.value) {
        0 -> false
        1 -> true
        else -> throw Exception("Unexpected value for print instruction!")
    }
}