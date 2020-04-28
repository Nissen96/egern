package com.egern.ast

import com.egern.visitor.Visitor

class ContinueStmt(lineNumber: Int, charPosition: Int) : Statement(lineNumber, charPosition) {
    lateinit var jumpLabel: String

    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}