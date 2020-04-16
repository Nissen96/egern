package com.egern.types

enum class ExprTypeEnum {
    INT,
    BOOLEAN,
    VOID,
    ARRAY,
    CLASS,
    STRING;

    companion object {
        fun fromInt(value: Int) = values().first { it.ordinal == value }
    }
}


sealed class ExprType(val type: ExprTypeEnum) {
    companion object {
        fun primitives() = mapOf("int" to INT, "boolean" to BOOLEAN, "string" to STRING)
    }
}

object INT : ExprType(ExprTypeEnum.INT)
object BOOLEAN : ExprType(ExprTypeEnum.BOOLEAN)
object STRING : ExprType(ExprTypeEnum.STRING)
object VOID : ExprType(ExprTypeEnum.VOID)
data class ARRAY(val depth: Int, val innerType: ExprType) : ExprType(ExprTypeEnum.ARRAY)
data class CLASS(val className: String, var castTo: String? = null) : ExprType(ExprTypeEnum.CLASS)