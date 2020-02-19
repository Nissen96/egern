package com.egern.visitor

import com.egern.ast.*
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolType

class PrintSymbolTableVisitor : Visitor {
    override fun preVisit(funcDecl: FuncDecl) {
        val scope = funcDecl.symbolTable.scope
        val symbol = funcDecl.symbolTable.lookup(funcDecl.id)
        printSymbolLine(symbol, scope - 1, "Local variables", funcDecl.variableCount)

        for (param in funcDecl.params) {
            val paramSymbol = funcDecl.symbolTable.lookup(param)
            printSymbolLine(paramSymbol, scope, "Offset", paramSymbol?.info as Int)
        }
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        val scope = varDecl.symbolTable.scope
        for (id in varDecl.ids) {
            val symbol = varDecl.symbolTable.lookup(id)
            printSymbolLine(symbol, scope, "Offset", symbol?.info as Int)
        }
    }

    private fun printSymbolLine(symbol: Symbol<*>?, scope: Int, infoText: String, infoValue: Int) {
        println("${symbol?.type} at scope $scope: ${symbol?.id}\n    $infoText: $infoValue")
    }
}