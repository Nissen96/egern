package com.egern.ast

import com.egern.visitor.Visitor

class PrintStmt() : Statement() {
    var expr: Expr? = null
    var funcCall: FuncCall? = null

    constructor(expr: Expr?) : this() {
        this.expr = expr
    }

    constructor(funcCall: FuncCall?) : this() {
        this.funcCall = funcCall
    }

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        expr?.accept(visitor)
        funcCall?.accept(visitor)
    }
}