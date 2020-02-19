package com.egern.codegen

sealed class RegisterKind
object OpReg1 : RegisterKind()
object OpReg2 : RegisterKind()
object DataReg : RegisterKind()
data class ParamReg(val paramNum: Int) : RegisterKind()