package com.egern.symbols

import com.egern.error.ErrorLogger
import java.lang.Exception

class SymbolTable(val scope: Int, val parent: SymbolTable?) {
    private val symbols: MutableMap<Pair<String, SymbolType>, Symbol> = mutableMapOf()

    fun insert(sym: Symbol) {
        // Symbol is inserted in table with (id, type) key - allowing shadowing of same id in same scope
        val key = Pair(sym.id, sym.type)

        // Check if any symbol with the same id exists
        val checkKeys = SymbolType.values().map { Pair(sym.id, it) }
        val foundSymbol = checkKeys.asSequence().map { symbols[it] }.firstOrNull { it != null }

        // Add symbol if none already exists or if symbol is a variable shadowing a parameter
        if (foundSymbol == null || (sym.type == SymbolType.Variable && foundSymbol.type == SymbolType.Parameter)) {
            symbols[key] = sym
        } else {
            ErrorLogger.log(Exception("Symbol ${sym.id} of type ${foundSymbol.type} has already been declared!"))
        }
    }

    fun lookupCurrentScope(id: String, checkDeclared: Boolean = false): Symbol? {
        // For each symbol type, lookup a corresponding id in this scope
        val checkKeys = SymbolType.values().map { Pair(id, it) }
        return checkKeys.asSequence().map { lookupType(it, checkDeclared) }.firstOrNull { it != null }
    }

    fun lookup(id: String, checkDeclared: Boolean = false): Symbol? {
        // If none was found, check parent scope
        return lookupCurrentScope(id, checkDeclared) ?: parent?.lookup(id, checkDeclared)
    }

    private fun lookupType(key: Pair<String, SymbolType>, checkDeclared: Boolean = false): Symbol? {
        // Lookup symbol - and check if symbol has been declared yet if specified
        return if (key in symbols && (!checkDeclared || isDeclared(key))) symbols[key]
        else null
    }

    private fun isDeclared(key: Pair<String, SymbolType>): Boolean {
        // Check if a variable has been declared - no checks needed for other symbols
        return (symbols[key]?.type == SymbolType.Variable && symbols[key]!!.isDeclared)
                || symbols[key]?.type != SymbolType.Variable
    }
}