package com.egern.codegen

object LabelGenerator {
    var counter = 0
    fun nextLabel(s: String): String {
        return "L${counter++}_$s"
    }
}