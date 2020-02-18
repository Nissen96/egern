package com.egern.visitor

import com.egern.ast.*
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable

class PrintSymbolTableVisitor(val rootTable: SymbolTable) : Visitor {
    override fun preVisit(program: Program) {
        val scope = rootTable.scope
        val symbol = rootTable.lookup("main")
        printSymbolLine(symbol, scope - 1)
    }

    override fun preVisit(funcDecl: FuncDecl) {
        val scope = funcDecl.symbolTable.scope
        val symbol = funcDecl.symbolTable.lookup(funcDecl.id)
        printSymbolLine(symbol, scope - 1)

        for (param in funcDecl.params) {
            val paramSymbol = funcDecl.symbolTable.lookup(param)
            printSymbolLine(paramSymbol, scope)
        }
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        val scope = varDecl.symbolTable.scope
        for (id in varDecl.ids) {
            val symbol = varDecl.symbolTable.lookup(id)
            printSymbolLine(symbol, scope)
        }
    }

    private fun printSymbolLine(symbol: Symbol<*>?, scope: Int) {
        println("${symbol?.type}\t at scope $scope: ${symbol?.id}")
    }
}