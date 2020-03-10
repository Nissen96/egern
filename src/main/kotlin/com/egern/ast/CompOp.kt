package com.egern.ast

import com.egern.types.BOOLEAN
import com.egern.types.ExprType
import com.egern.types.INT

enum class CompOp(val value: String, val validTypes: List<ExprType>) {
    EQ("==", listOf(BOOLEAN, INT)),
    NEQ("!=", listOf(BOOLEAN, INT)),
    LT("<", listOf(INT)),
    GT(">", listOf(INT)),
    LTE("<=", listOf(INT)),
    GTE(">=", listOf(INT));

    companion object {
        private val map = values().associateBy(CompOp::value)
        fun fromString(type: String) = map[type]
        fun operators() = map.map { it.key }
        fun validOperators(type: ExprType) = values().filter { type in it.validTypes }
    }
}