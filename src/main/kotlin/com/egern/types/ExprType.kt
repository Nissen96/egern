package com.egern.types

sealed class ExprType {
    companion object {
        fun primitives() = mapOf("int" to INT, "boolean" to BOOLEAN)
    }
}

object INT : ExprType()
object BOOLEAN : ExprType()
object VOID : ExprType()
data class ARRAY(val depth: Int, val innerExpr: ExprType) : ExprType()