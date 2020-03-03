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

    private fun lookupSymbol(id: String, validTypes: List<SymbolType>): Symbol<*> {
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
            is IdExpr -> TODO()
            is FuncCall -> TODO()
            else -> throw Exception("Can't derive type for expr!")
        }
    }

    private fun isMatchingType(expr1: Expr, expr2: Expr?): Boolean {
        return deriveType(expr1) == (if (expr2 != null) deriveType(expr2) else true)
    }

    override fun preVisit(funcCall: FuncCall) {
        val sym = lookupSymbol(funcCall.id, listOf(SymbolType.Function))
        val nArgs = funcCall.args.size
        val nParams = (sym.info as FuncDecl).params.size
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

    override fun preVisit(returnStmt: ReturnStmt) {
        /**if (returnStmt.expr != null && functionStack.peek() != null) {
            val type = deriveType(returnStmt.expr)
            val funcDecl = (lookupSymbol(functionStack.peek()!!.id, emptyList()).info as FuncDecl)
            if (funcDecl.returnType == null) {
                funcDecl.returnType = type
            } else if (funcDecl.returnType != type) {
                // TODO: ERROR
            }
        }**/
    }

    override fun postVisit(booleanOpExpr: BooleanOpExpr) {
        if (deriveType(booleanOpExpr) != deriveType(booleanOpExpr.lhs) || !isMatchingType(
                booleanOpExpr.lhs,
                booleanOpExpr.rhs
            )
        ) {
            // TODO: ERROR
        }
    }

    override fun postVisit(arithExpr: ArithExpr) {
        if (deriveType(arithExpr) != deriveType(arithExpr.lhs) || !isMatchingType(arithExpr.lhs, arithExpr.rhs)) {
            // TODO: ERROR
        }
    }

    override fun postVisit(compExpr: CompExpr) {
        if (!isMatchingType(compExpr.lhs, compExpr.rhs)) {
            // TODO: ERROR
        }
    }
}