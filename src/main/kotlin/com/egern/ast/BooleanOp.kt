package com.egern.ast

enum class BooleanOp(val value: String) {
    AND("&&"),
    OR("||");

    companion object {
        private val map = BooleanOp.values().associateBy(BooleanOp::value)
        fun fromString(type: String) = map[type]
        fun operators() = map.map { it.key }
    }
}