package com.egern.ast

class VarAssign<T>(ids: List<String>, expr: T) : Statement() {
}

typealias VarDecl<T> = VarAssign<T>