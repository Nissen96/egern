package com.egern.ast

class CompExpr(val lhs: ArithExpr, val rhs: ArithExpr, val op: String) : Expr()