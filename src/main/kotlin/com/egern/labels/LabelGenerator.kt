package com.egern.labels

object LabelGenerator {
    private var counter = 0
    fun nextLabel(s: String): String {
        return "L${counter++}_$s"
    }
}