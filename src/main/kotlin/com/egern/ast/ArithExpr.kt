package com.egern.ast

// TODO: REFACTOR PLEASE!
public class ArithExpr() : Expr() {
    var lhs: ArithExpr? = null
    var rhs: ArithExpr? = null
    var op: String? = null

    var value: Int? = null
    var id: String? = null
    var expr: ArithExpr? = null

    constructor(lhs: ArithExpr, rhs: ArithExpr, op: String) : this() {
        this.lhs = lhs
        this.rhs = rhs
        this.op = op
    }

    constructor(value: Int) : this() {
        this.value = value
    }

    constructor(id: String) : this() {
        this.id = id
    }

    constructor(expr: ArithExpr) : this() {
        this.expr = expr
    }
}