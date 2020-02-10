package com.egern.ast

import com.egern.visitor.Visitor

class FuncCall(val id: String, val args: List<String>) : Statement() {
    override fun accept(visitor: Visitor) {
        return visitor.visit(this)
    }
}