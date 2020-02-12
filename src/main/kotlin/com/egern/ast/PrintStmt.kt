package com.egern.ast

import com.egern.visitor.Visitor

class PrintStmt(val expr: Expr?) : Statement() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr?.accept(visitor)
    }
}