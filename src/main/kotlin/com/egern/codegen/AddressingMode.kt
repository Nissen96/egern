package com.egern.codegen

sealed class AddressingMode
object Direct : AddressingMode()
object Indirect : AddressingMode()
data class IndirectRelative(val offset: String) : AddressingMode()