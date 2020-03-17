package com.egern.types

enum class ExprTypeEnum {
    INT,
    BOOLEAN,
    VOID,
    ARRAY,
    CLASS
}


sealed class ExprType(val type: ExprTypeEnum) {
    companion object {
        fun primitives() = mapOf("int" to INT, "boolean" to BOOLEAN)
    }
}

object INT : ExprType(ExprTypeEnum.INT)
object BOOLEAN : ExprType(ExprTypeEnum.BOOLEAN)
object VOID : ExprType(ExprTypeEnum.VOID)
data class ARRAY(val depth: Int, val innerExpr: ExprType) : ExprType(ExprTypeEnum.ARRAY)
data class CLASS(val className: String) : ExprType(ExprTypeEnum.CLASS)