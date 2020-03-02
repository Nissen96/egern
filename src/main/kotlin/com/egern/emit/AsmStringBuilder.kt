package com.egern.emit

class AsmStringBuilder(private val commentSym: String) {
    private val builder = StringBuilder()

    companion object {
        const val OP_OFFSET = 10
        const val REGS_OFFSET = 28
    }

    fun add(s: String, pad: Int = 0): AsmStringBuilder {
        builder.append(s.padEnd(pad))
        return this
    }

    fun addLine(op: String? = null, regs: Pair<String, String?>? = null, comment: String? = null): AsmStringBuilder {
        if (op != null) {
            addOp(op)
        }

        if (regs != null) {
            addRegs(regs)
        }

        if (comment != null) {
            addComment(comment)
        }
        newline()
        return this
    }

    fun addOp(op: String): AsmStringBuilder {
        add(op, OP_OFFSET)
        return this
    }

    fun addRegs(regs: Pair<String, String?>): AsmStringBuilder {
        if (regs.second != null) {
            add("${regs.first}, ${regs.second}", REGS_OFFSET)
        } else {
            add(regs.first, REGS_OFFSET)
        }
        return this
    }

    fun newline(): AsmStringBuilder {
        builder.appendln()
        return this
    }

    fun addComment(comment: String): AsmStringBuilder {
        add("$commentSym $comment")
        return this
    }

    fun toFinalStr(): String {
        return builder.toString()
    }
}