package com.egern.ast

import com.egern.visitor.Visitor

class ReturnStmt(val expr: Expr?) : Statement() {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
        expr?.accept(visitor)
    }
}