package com.egern.symbols

import com.egern.error.ErrorLogger
import java.lang.Exception

class SymbolTable(val scope: Int, val parent: SymbolTable?) {
    private val symbols: MutableMap<String, Symbol<*>> = mutableMapOf()

    fun insert(id: String, sym: Symbol<*>) {
        println("Insert $id of type ${sym.type}")
        if (id in symbols) {
            ErrorLogger.log(Exception("Symbol $id of type ${symbols[id]?.type} has already been declared!"))
        }
        symbols[id] = sym
    }

    fun lookup(id: String, checkDeclared: Boolean = false): Symbol<*>? {
        return if (id in symbols && (!checkDeclared || isDeclared(id))) {
            symbols[id]
        } else {
            parent?.lookup(id, checkDeclared)
        }
    }

    private fun isDeclared(id: String): Boolean {
        return (symbols[id]?.type == SymbolType.Variable && symbols[id]!!.isDeclared)
                || symbols[id]?.type != SymbolType.Variable
    }
}