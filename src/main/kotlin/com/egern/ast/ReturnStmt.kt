package com.egern.ast

import com.egern.visitor.Visitor

class ReturnStmt(val expr: Expr?, lineNumber: Int = -1, charPosition: Int = -1) : Statement(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr?.accept(visitor)
        visitor.postVisit(this)
    }
}