package com.egern.ast

import com.egern.visitor.Visitor

class ReturnStmt(val expr: Expr?) : Statement() {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr?.accept(visitor)
        visitor.postVisit(this)
    }
}