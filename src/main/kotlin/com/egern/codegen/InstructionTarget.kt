package com.egern.codegen

sealed class InstructionTarget
data class ImmediateValue(val value : String) : InstructionTarget()
data class ImmediateLabel(val label : String) : InstructionTarget()
data class Memory(val address : String) : InstructionTarget()
data class Register(val register : String) : InstructionTarget()
object RBP : InstructionTarget()
object RSP : InstructionTarget()
object ReturnValue : InstructionTarget()
object StaticLink : InstructionTarget()
