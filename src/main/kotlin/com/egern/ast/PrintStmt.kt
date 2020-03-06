package com.egern.ast

import com.egern.visitor.Visitor

class PrintStmt(val expr: Expr?, lineNumber: Int, charPosition: Int) : Statement(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr?.accept(visitor)
        visitor.postVisit(this)
    }
}