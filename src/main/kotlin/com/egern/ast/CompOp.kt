package com.egern.ast

import com.egern.types.*

enum class CompOp(val value: String, val validTypes: List<ExprTypeEnum>) {
    EQ("==", listOf(ExprTypeEnum.BOOLEAN, ExprTypeEnum.INT, ExprTypeEnum.ARRAY)),
    NEQ("!=", listOf(ExprTypeEnum.BOOLEAN, ExprTypeEnum.INT, ExprTypeEnum.ARRAY)),
    LT("<", listOf(ExprTypeEnum.INT)),
    GT(">", listOf(ExprTypeEnum.INT)),
    LTE("<=", listOf(ExprTypeEnum.INT)),
    GTE(">=", listOf(ExprTypeEnum.INT));

    companion object {
        private val map = values().associateBy(CompOp::value)
        fun fromString(type: String) = map[type]
        fun operators() = map.map { it.key }
        fun validOperators(exprType: ExprType) = values().filter { exprType.type in it.validTypes }
    }
}