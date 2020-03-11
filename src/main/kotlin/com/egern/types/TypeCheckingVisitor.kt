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
        functionStack.pop()
        if (!funcDecl.children.any { it is ReturnStmt }) {
            ErrorLogger.log(funcDecl, "No return statement found in function declaration")
        }
    }

    private fun lookupSymbol(id: String, validTypes: List<SymbolType>): Symbol {
        val sym = currentTable.lookup(id) ?: throw Exception("Symbol '$id' not defined")
        if (sym.type !in validTypes) {
            ErrorLogger.log(Exception("Symbol '$id' should be one of types $validTypes but is not"))
        }
        return sym
    }

    private fun getVariableType(id: String): ExprType {
        val symbol = currentTable.lookup(id)!!
        return when (symbol.type) {
            SymbolType.Variable -> deriveType(symbol.info["expr"] as Expr)
            SymbolType.Parameter -> symbol.info["type"] as ExprType
            else -> throw Exception("Can't derive type for IdExpr")
        }
    }

    private fun deriveType(expr: Expr): ExprType {
        return when (expr) {
            // Handle implicit returns of nothing (int=0)
            is IntExpr -> if (expr.isVoid) VOID else INT
            is BooleanExpr -> BOOLEAN
            is BooleanOpExpr -> BOOLEAN
            is CompExpr -> BOOLEAN
            is ArithExpr -> INT
            is IdExpr -> getVariableType(expr.id)
            is FuncCall -> (currentTable.lookup(expr.id)!!.info["funcDecl"] as FuncDecl).returnType
            is ParenExpr -> deriveType(expr.expr)
            is ArrayExpr -> ARRAY(deriveType(expr.entries[0]))
            else -> throw Exception("Can't derive type for expr!")
        }
    }

    private fun isMatchingType(expr1: Expr, expr2: Expr?): Boolean {
        return if (expr2 != null) (deriveType(expr1) == deriveType(expr2)) else true
    }

    override fun preVisit(funcCall: FuncCall) {
        val sym = lookupSymbol(funcCall.id, listOf(SymbolType.Function))
        val params = (sym.info["funcDecl"] as FuncDecl).params
        val nArgs = funcCall.args.size
        val nParams = params.size
        if (nArgs != nParams) {
            ErrorLogger.log(
                funcCall,
                "Wrong number of arguments to function ${funcCall.id} - $nArgs passed, $nParams expected"
            )
        }
        funcCall.args.forEachIndexed { index, arg ->
            val argType = deriveType(arg)
            val paramType = params[index].second
            if (argType != paramType) {
                ErrorLogger.log(arg, "Argument $index is of type $argType but $paramType was expected")
            }
        }
    }

    override fun preVisit(varAssign: VarAssign<*>) {
        varAssign.ids.map { lookupSymbol(it, listOf(SymbolType.Variable, SymbolType.Parameter)) }
        val type = deriveType(varAssign.expr)
        if (type == VOID) {
            ErrorLogger.log(varAssign, "Assigning to void is not valid.")
        }
        for (id in varAssign.ids) {
            if (type != getVariableType(id)) {
                ErrorLogger.log(varAssign, "Assignment to $type is invalid.")
            }
        }
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        val type = deriveType(varDecl.expr)
        if (type == VOID) {
            ErrorLogger.log(varDecl, "Declaring a variable of type void is not valid.")
        }
    }

    override fun preVisit(printStmt: PrintStmt) {
        if (printStmt.expr != null && deriveType(printStmt.expr) == VOID) {
            ErrorLogger.log(printStmt.expr, "Printing void is not valid.")
        }
    }

    override fun visit(idExpr: IdExpr) {
        lookupSymbol(idExpr.id, listOf(SymbolType.Variable, SymbolType.Parameter))
    }

    override fun postVisit(returnStmt: ReturnStmt) {
        if (functionStack.peek() != null && returnStmt.expr != null) {
            val returnType = functionStack.peek()!!.returnType
            val exprType = deriveType(returnStmt.expr)
            if (exprType != returnType) {
                ErrorLogger.log(returnStmt, "Invalid return type")
            }
        }
    }

    override fun postVisit(booleanOpExpr: BooleanOpExpr) {
        if (!isMatchingType(booleanOpExpr, booleanOpExpr.lhs) || !isMatchingType(booleanOpExpr, booleanOpExpr.rhs)) {
            ErrorLogger.log(booleanOpExpr, "Type mismatch on boolean operator")
        }
    }

    override fun postVisit(arithExpr: ArithExpr) {
        if (!isMatchingType(arithExpr, arithExpr.lhs) || !isMatchingType(arithExpr, arithExpr.rhs)) {
            ErrorLogger.log(arithExpr, "Type mismatch on arithmetic operator")
        }
    }

    override fun postVisit(compExpr: CompExpr) {
        val type = deriveType(compExpr.lhs)
        if (compExpr.op !in CompOp.validOperators(type)) {
            ErrorLogger.log(compExpr, "${compExpr.op.value} operation not defined on type $type")
        }
        if (!isMatchingType(compExpr.lhs, compExpr.rhs)) {
            ErrorLogger.log(compExpr, "Type mismatch on comparative expr operator")
        }
    }

    override fun postVisit(arrayExpr: ArrayExpr) {
        val type = deriveType(arrayExpr) as ARRAY
        for (entry in arrayExpr.entries) {
            if (type.type != deriveType(entry)) {
                ErrorLogger.log(entry, "Type mismatch in array")
            }
        }
    }
}