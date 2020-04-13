package com.egern.ast

enum class Keyword(val keyword: String) {
    OVERRIDE("override");

    companion object {
        private val map = values().associateBy(Keyword::keyword)
        fun fromString(type: String) = map[type]
    }
}