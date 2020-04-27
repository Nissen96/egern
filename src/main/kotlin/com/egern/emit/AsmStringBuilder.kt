package com.egern.emit

class AsmStringBuilder {
    private val builder = StringBuilder()

    companion object {
        const val OP_OFFSET = 10
        const val REGS_OFFSET = 28
        const val LABEL_INDENTATION = 8
    }

    fun add(s: String, pad: Int = 0): AsmStringBuilder {
        builder.append(s.padEnd(pad))
        return this
    }

    fun addLine(
        op: String? = null,
        reg1: String? = null,
        reg2: String? = null,
        comment: String? = null
    ): AsmStringBuilder {

        if (op != null) {
            addOp(op)
        }

        if (reg1 != null) {
            addRegs(reg1, reg2)
        } else {
            add("", REGS_OFFSET)
        }

        if (comment != null) {
            addComment(comment)
        }
        newline()
        return this
    }

    fun addLabel(label: String): AsmStringBuilder {
        builder
            .append("$label:")
            .appendln()
        return this
    }

    fun addOp(op: String): AsmStringBuilder {
        add("", LABEL_INDENTATION)
        add(op, OP_OFFSET)
        return this
    }

    fun addRegs(reg1: String, reg2: String? = null): AsmStringBuilder {
        add("$reg1${if (reg2 != null) ", $reg2" else ""}", REGS_OFFSET)
        return this
    }

    fun newline(): AsmStringBuilder {
        builder.appendln()
        return this
    }

    fun addComment(comment: String): AsmStringBuilder {
        add(comment)
        return this
    }

    fun toFinalStr(): String {
        return builder.toString()
    }
}