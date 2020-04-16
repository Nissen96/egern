package com.egern.symbols

class Symbol(val id: String, val type: SymbolType, val scope: Int, val info: MutableMap<String, Any?>) {
    var isDeclared = false
}