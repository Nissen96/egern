package com.egern.symbols

class Symbol<T>(val id: String, val type: SymbolType, val scope: Int, val info: T) {
    var isDeclared = false
}