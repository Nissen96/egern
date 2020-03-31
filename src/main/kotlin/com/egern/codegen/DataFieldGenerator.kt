package com.egern.codegen

object DataFieldGenerator {
    private var counter = 0
    fun nextLabel(s: String): String {
        return "${s}_${counter++}"
    }
}