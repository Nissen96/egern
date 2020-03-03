package com.egern.types

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.util.*
import com.egern.visitor.Visitor
import java.lang.Exception

class TypeCheckingVisitor(private var currentTable: SymbolTable) : Visitor {
    private val functionStack = stackOf<FuncDecl>()

    override fun preVisit(funcDecl: FuncDecl) {
        currentTable = funcDecl.symbolTable
        functionStack.push(funcDecl)
    }

    override fun postVisit(funcDecl: FuncDecl) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
        functionStack.pop();
    }

    private fun lookupSymbol(id: String, validTypes: List<SymbolType>): Symbol {
        val sym = currentTable.lookup(id) ?: throw Exception("Symbol '$id' not defined")
        if (sym.type !in validTypes) {
            ErrorLogger.log(Exception("Symbol '$id' should be one of types $validTypes but is not"))
        }
        return sym
    }

    private fun deriveType(expr: Expr): ExprType {
        return when (expr) {
            is IntExpr -> ExprType.INT
            is BooleanExpr -> ExprType.BOOLEAN
            is BooleanOpExpr -> ExprType.BOOLEAN
            is CompExpr -> ExprType.BOOLEAN
            is ArithExpr -> ExprType.INT
            is IdExpr -> {
                val symbol = currentTable.lookup(expr.id)!!
                return when {
                    symbol.type == SymbolType.Variable -> deriveType(symbol.info["expr"] as Expr)
                    symbol.type == SymbolType.Parameter -> symbol.info["type"] as ExprType
                    else -> throw Exception("Can't derive type for IdExpr")
                }
            }
            is FuncCall -> (currentTable.lookup(expr.id)!!.info["funcDecl"] as FuncDecl).returnType
            else -> throw Exception("Can't derive type for expr!")
        }
    }

    private fun isMatchingType(expr1: Expr, expr2: Expr?): Boolean {
        return deriveType(expr1) == (if (expr2 != null) deriveType(expr2) else true)
    }

    override fun preVisit(funcCall: FuncCall) {
        val sym = lookupSymbol(funcCall.id, listOf(SymbolType.Function))
        val nArgs = funcCall.args.size
        val nParams = (sym.info["funcDecl"] as FuncDecl).params.size
        if (nArgs != nParams) {
            ErrorLogger.log(Exception("Wrong number of arguments to function ${funcCall.id} - $nArgs passed, $nParams expected"))
        }
    }

    override fun preVisit(varAssign: VarAssign<*>) {
        varAssign.ids.map { lookupSymbol(it, listOf(SymbolType.Variable, SymbolType.Parameter)) }
    }

    override fun visit(idExpr: IdExpr) {
        lookupSymbol(idExpr.id, listOf(SymbolType.Variable, SymbolType.Parameter))
    }

    override fun postVisit(returnStmt: ReturnStmt) {
        if (functionStack.peek() != null && returnStmt.expr != null) {
            val returnType =
                (lookupSymbol(functionStack.peek()!!.id, emptyList()).info["funcDecl"] as FuncDecl).returnType
            val exprType = deriveType(returnStmt.expr)
            if (exprType != returnType) {
                ErrorLogger.log(Exception("Invalid return type"))
            }
        }
    }

    override fun postVisit(booleanOpExpr: BooleanOpExpr) {
        if (!isMatchingType(booleanOpExpr, booleanOpExpr.lhs) || !isMatchingType(
                booleanOpExpr.lhs,
                booleanOpExpr.rhs
            )
        ) {
            ErrorLogger.log(Exception("Type mismatch on boolean operator"))
        }
    }

    override fun postVisit(arithExpr: ArithExpr) {
        if (!isMatchingType(arithExpr, arithExpr.lhs) || !isMatchingType(arithExpr.lhs, arithExpr.rhs)) {
            ErrorLogger.log(Exception("Type mismatch on arithmetic operator"))
        }
    }

    override fun postVisit(compExpr: CompExpr) {
        if (!isMatchingType(compExpr.lhs, compExpr.rhs)) {
            ErrorLogger.log(Exception("Type mismatch on comperative expr operator"))
        }
    }
}