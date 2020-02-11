package com.egern.ast

import com.egern.symbols.SymbolTable

interface Scopable {
    var symbolTable: SymbolTable
}