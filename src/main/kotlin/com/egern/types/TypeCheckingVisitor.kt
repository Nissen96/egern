package com.egern.types

import com.egern.ast.FuncCall
import com.egern.ast.FuncDecl
import com.egern.ast.IdExpr
import com.egern.ast.VarAssign
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.visitor.Visitor
import java.lang.Exception
import java.util.*

class TypeCheckingVisitor(private var currentTable: SymbolTable) : Visitor {
    override fun preVisit(funcDecl: FuncDecl) {
        currentTable = funcDecl.symbolTable
    }

    override fun postVisit(funcDecl: FuncDecl) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
    }

    private fun lookupSymbol(id: String, validTypes: List<SymbolType>): Symbol<*> {
        val sym = currentTable.lookup(id) ?: throw Exception("Symbol '$id' not defined")
        if (sym.type !in validTypes) {
            throw Exception("Symbol '$id' should be one of types $validTypes but is not")
        }
        return sym
    }

    override fun preVisit(funcCall: FuncCall) {
        val sym = lookupSymbol(funcCall.id, listOf(SymbolType.Function))
        val nArgs = funcCall.args.size
        val nParams = (sym.info as FuncDecl).params.size
        if (nArgs != nParams) {
            throw Exception("Wrong number of arguments to function ${funcCall.id} - $nArgs passed, $nParams expected")
        }
    }

    override fun preVisit(varAssign: VarAssign<*>) {
        varAssign.ids.map { lookupSymbol(it, listOf(SymbolType.Variable, SymbolType.Parameter)) }
    }

    override fun preVisit(idExpr: IdExpr) {
        lookupSymbol(idExpr.id, listOf(SymbolType.Variable, SymbolType.Parameter))
    }
}