package com.egern.visitor

import com.egern.ast.*
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolType

class PrintSymbolTableVisitor : Visitor {
    override fun preVisit(program: Program) {
        println("Main Scope - local variables: ${program.variableCount}")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        val scope = funcDecl.symbolTable.scope
        val symbol = funcDecl.symbolTable.lookup(funcDecl.id)
        printSymbolLine(symbol, scope - 1, "local variables", funcDecl.variableCount)

        for (param in funcDecl.params) {
            val paramSymbol = funcDecl.symbolTable.lookup(param.first)
            printSymbolLine(paramSymbol, scope, "offset", paramSymbol?.info?.get("paramOffset") as Int)
        }
    }

    override fun preVisit(varDecl: VarDecl) {
        val scope = varDecl.symbolTable.scope
        for (id in varDecl.ids) {
            val symbol = varDecl.symbolTable.lookup(id)
            printSymbolLine(
                symbol, scope, "offset", symbol?.info?.get(
                    if (symbol.type == SymbolType.Variable) "variableOffset" else "fieldOffset"
                ) as? Int
            )
        }
    }

    private fun printSymbolLine(symbol: Symbol?, scope: Int, infoText: String, infoValue: Int?) {
        println("${symbol?.type} '${symbol?.id}' at scope $scope - $infoText: $infoValue")
    }
}