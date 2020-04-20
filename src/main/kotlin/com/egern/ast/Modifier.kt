package com.egern.ast

enum class Modifier(val modifier: String) {
    OVERRIDE("override"),
    STATIC("static");

    companion object {
        private val map = values().associateBy(Modifier::modifier)
        fun fromString(type: String) = map[type]
    }
}