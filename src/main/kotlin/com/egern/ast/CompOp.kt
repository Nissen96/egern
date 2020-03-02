package com.egern.ast

enum class CompOp(val value: String) {
    EQ("=="),
    NEQ("!="),
    LT("<"),
    GT(">"),
    LTE("<="),
    GTE(">=");

    companion object {
        private val map = values().associateBy(CompOp::value)
        fun fromString(type: String) = map[type]
        fun operators() = map.map { it.key }
    }
}