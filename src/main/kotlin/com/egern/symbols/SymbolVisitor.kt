package com.egern.symbols

import com.egern.ast.*
import com.egern.visitor.Visitor

class SymbolVisitor : Visitor {
    private var currentScopeLevel = 0
    var currentTable = SymbolTable(0, null)

    override fun preVisit(funcDecl: FuncDecl) {
        currentTable.insert(funcDecl.id, Symbol(funcDecl.id, SymbolType.Function, null))
        currentScopeLevel++
        currentTable = SymbolTable(currentScopeLevel, currentTable)
        for (param in funcDecl.params) {
            currentTable.insert(param, Symbol(param, SymbolType.Parameter, null))
        }
        funcDecl.symbolTable = currentTable
    }

    override fun postVisit(funcDecl: FuncDecl) {
        currentScopeLevel--
        currentTable = currentTable.parent!!
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        for (id in varDecl.ids) {
            currentTable.insert(id, Symbol(id, SymbolType.Variable, null))
        }
        varDecl.symbolTable = currentTable
    }
}