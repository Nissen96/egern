package com.egern.codegen

sealed class InstructionTarget
data class ImmediateValue(val value: String) : InstructionTarget()
data class Memory(val address: String) : InstructionTarget()
data class Register(val register: RegisterKind) : InstructionTarget()
object MainLabel : InstructionTarget()
object RBP : InstructionTarget()
object RSP : InstructionTarget()
object RHP : InstructionTarget()
object VTable : InstructionTarget()
object Heap : InstructionTarget()
object ReturnValue : InstructionTarget()
object StaticLink : InstructionTarget()
