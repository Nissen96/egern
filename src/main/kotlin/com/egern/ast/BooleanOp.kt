package com.egern.ast

enum class BooleanOp(val value: String) {
    AND("&&"),
    OR("||"),
    NOT("!");

    companion object {
        private val map = values().associateBy(BooleanOp::value)
        fun fromString(type: String) = map[type]
        fun operators() = map.map { it.key }
    }
}