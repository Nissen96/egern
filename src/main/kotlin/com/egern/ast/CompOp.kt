package com.egern.ast

import com.egern.types.ExprType

enum class CompOp(val value: String, val validTypes: List<ExprType>) {
    EQ("==", listOf(ExprType.BOOLEAN, ExprType.INT)),
    NEQ("!=", listOf(ExprType.BOOLEAN, ExprType.INT)),
    LT("<", listOf(ExprType.INT)),
    GT(">", listOf(ExprType.INT)),
    LTE("<=", listOf(ExprType.INT)),
    GTE(">=", listOf(ExprType.INT));

    companion object {
        private val map = values().associateBy(CompOp::value)
        fun fromString(type: String) = map[type]
        fun operators() = map.map { it.key }
        fun validOperators(type: ExprType) = values().filter { type in it.validTypes }
    }
}