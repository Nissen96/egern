package com.egern.symbols

import com.egern.ast.*
import com.egern.visitor.Visitor

class SymbolVisitor : Visitor {
    private var currentScopeLevel = 0
    private var variableOffset = 0
    var currentTable = SymbolTable(0, null)

    private fun returnToParentScope() {
        currentScopeLevel--
        currentTable = currentTable.parent!!
    }

    private fun createNewScope() {
        currentScopeLevel++
        currentTable = SymbolTable(currentScopeLevel, currentTable)
    }

    override fun preVisit(block: Block) {
        createNewScope()
        block.symbolTable = currentTable
    }

    override fun postVisit(block: Block) {
        returnToParentScope()
    }

    override fun preVisit(funcDecl: FuncDecl) {
        currentTable.insert(funcDecl.id, Symbol(funcDecl.id, SymbolType.Function, currentScopeLevel, funcDecl))
        createNewScope()
        for (param in funcDecl.params) {
            currentTable.insert(param, Symbol(param, SymbolType.Parameter, currentScopeLevel, null))
        }
        funcDecl.symbolTable = currentTable
        variableOffset = 0
    }

    override fun postVisit(funcDecl: FuncDecl) {
        returnToParentScope()
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        for (id in varDecl.ids) {
            currentTable.insert(id, Symbol(id, SymbolType.Variable, currentScopeLevel, variableOffset))
        }
        varDecl.symbolTable = currentTable
        variableOffset++
    }
}