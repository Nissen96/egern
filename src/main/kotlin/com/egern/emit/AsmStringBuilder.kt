package com.egern.emit

import com.egern.emit.AsmStringBuilder.Companion.OP_OFFSET
import com.egern.emit.AsmStringBuilder.Companion.REGS_OFFSET

class AsmStringBuilder(val commentSym: String) {
    val strings = mutableListOf<String>()

    init {
        strings.add("")
    }

    fun toFinalStr(): String {
        return strings.joinToString(separator = "\n")
    }

    companion object {
        const val OP_OFFSET = 10
        const val REGS_OFFSET = 28
    }
}

fun <T : AsmStringBuilder> T.add(s: String, pad: Int = 0): T {
    strings[strings.lastIndex] = strings.last() + s.padEnd(pad)
    return this
}

fun <T : AsmStringBuilder> T.addLine(op: String, regs: Pair<String, String?>? = null, comment: String? = null): T {
    add(op, OP_OFFSET)

    if (regs != null) {
        if (regs.second != null) {
            add("${regs.first}, ${regs.second}", REGS_OFFSET)
        } else {
            add(regs.first, REGS_OFFSET)
        }
    } else {
        add("")
    }
    if (comment != null) {
        add("$commentSym $comment")
    }
    newline()
    return this
}

fun <T : AsmStringBuilder> T.newline(): T {
    strings.add("")
    return this
}