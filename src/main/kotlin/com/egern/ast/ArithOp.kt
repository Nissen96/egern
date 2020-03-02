package com.egern.ast

enum class ArithOp(val value: String) {
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIVIDE("/"),
    MODULO("%");

    companion object {
        private val map = values().associateBy(ArithOp::value)
        fun fromString(type: String) = map[type]
        fun operators() = map.map { it.key }
    }
}