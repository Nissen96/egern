package com.egern.ast

public class ArithExpr() : Expr() {
    constructor(lhs: ArithExpr, rhs: ArithExpr, op: String) : this()
    constructor(value: Int) : this()
    constructor(id: String) : this()
    constructor(expr: ArithExpr) : this()
}