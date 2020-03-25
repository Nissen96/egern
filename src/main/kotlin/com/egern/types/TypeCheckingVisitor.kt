package com.egern.types

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.symbols.ClassDefinition
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.util.*
import com.egern.visitor.Visitor
import java.lang.Exception

class TypeCheckingVisitor(private var currentTable: SymbolTable, private val classDefinitions: List<ClassDefinition>) :
    Visitor {
    private val functionStack = stackOf<FuncDecl>()

    private fun typeString(type: ExprType): String {
        return when (type) {
            INT -> "int"
            BOOLEAN -> "boolean"
            VOID -> "void"
            is ARRAY -> "[".repeat(type.depth) + typeString(type.innerType) + "]".repeat(type.depth)
            is CLASS -> type.className
        }
    }

    override fun preVisit(block: Block) {
        currentTable = block.symbolTable
    }

    override fun postVisit(block: Block) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        currentTable = funcDecl.symbolTable
        functionStack.push(funcDecl)
    }

    override fun postVisit(funcDecl: FuncDecl) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
        functionStack.pop()
        if (!funcDecl.stmts.any { it is ReturnStmt }) {
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

            // Get type directly for constructor parameters, derive if local field
            SymbolType.Field -> symbol.info["type"] as? ExprType ?: deriveType(symbol.info["expr"] as Expr)
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
            is LenExpr -> INT
            is ArrayExpr -> deriveArrayType(expr)
            is ArrayIndexExpr -> {
                val array = getVariableType(expr.id) as ARRAY
                if (array.depth - expr.indices.size > 0) {
                    ARRAY(array.depth - expr.indices.size, array.innerType)
                } else {
                    array.innerType
                }
            }
            is ObjectInstantiation -> CLASS(expr.classId)
            is MethodCall -> INT //TODO() // VTABLE LOOKUP
            is ClassField -> INT //TODO()
            else -> throw Exception("Can't derive type for expr!")
        }
    }

    private fun deriveArrayType(arrayExpr: ArrayExpr): ExprType {
        var depth = 0
        var expr: Expr = arrayExpr
        while (expr is ArrayExpr) {
            depth++
            expr = if (expr.entries.isNotEmpty()) expr.entries[0] else IntExpr(0, isVoid = true)
        }

        var innerExpr = deriveType(expr)
        if (innerExpr is ARRAY) {
            depth += innerExpr.depth
            innerExpr = innerExpr.innerType
        }

        return ARRAY(depth, innerExpr)
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
                ErrorLogger.log(
                    arg,
                    "Argument $index is of type ${typeString(argType)} but ${typeString(paramType)} was expected"
                )
            }
        }
    }

    override fun preVisit(varAssign: VarAssign) {
        // Check variable ids
        val allIds = varAssign.ids + varAssign.indexExprs.map { it.id } + varAssign.classFields.map { it.fieldId }
        allIds.forEach { lookupSymbol(it, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field)) }

        val exprType = deriveType(varAssign.expr)
        if (exprType == VOID) {
            ErrorLogger.log(varAssign, "Assigning void is invalid")
        }

        // Expression type must match declared variable
        varAssign.ids.forEach {
            val varType = getVariableType(it)
            if (varType != exprType) {
                ErrorLogger.log(
                    varAssign,
                    "Assigning expression of type ${typeString(exprType)} " +
                            "to variable of type ${typeString(varType)} is invalid"
                )
            }
        }

        // Expression type must match type of element at array index
        varAssign.indexExprs.forEach {
            val elementType = deriveType(it)
            if (elementType != exprType) {
                ErrorLogger.log(
                    varAssign,
                    "Assigning expression of type ${typeString(exprType)} " +
                            "to array element of type ${typeString(elementType)} is invalid"
                )
            }
        }
    }

    override fun preVisit(varDecl: VarDecl) {
        val type = deriveType(varDecl.expr)
        if (type == VOID) {
            ErrorLogger.log(varDecl, "Declaring a variable of type void is not valid")
        }
    }

    override fun preVisit(printStmt: PrintStmt) {
        if (printStmt.expr != null && deriveType(printStmt.expr) == VOID) {
            ErrorLogger.log(printStmt.expr, "Printing void is not valid")
        }
    }

    override fun preVisit(lenExpr: LenExpr) {
        val exprType = deriveType(lenExpr.expr)
        if (exprType !is ARRAY) {
            ErrorLogger.log(lenExpr.expr, "Len function is undefined for type ${typeString(exprType)}")
        }
    }

    override fun visit(idExpr: IdExpr) {
        lookupSymbol(idExpr.id, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field))
    }

    override fun postVisit(returnStmt: ReturnStmt) {
        if (functionStack.peek() != null && returnStmt.expr != null) {
            val returnType = functionStack.peek()!!.returnType
            val exprType = deriveType(returnStmt.expr)
            if (exprType != returnType) {
                ErrorLogger.log(
                    returnStmt,
                    "Invalid return type: ${typeString(returnType)} - expected: ${typeString(exprType)}"
                )
            }
        }
    }

    override fun postVisit(booleanOpExpr: BooleanOpExpr) {
        val exprType = deriveType(booleanOpExpr)
        val lhsType = deriveType(booleanOpExpr.lhs)
        val rhsType = booleanOpExpr.rhs?.let { deriveType(it) }

        if (lhsType != exprType) {
            ErrorLogger.log(
                booleanOpExpr,
                "Type mismatch on boolean operator${if (rhsType != null) " - LHS" else ""}: ${typeString(lhsType)}"
            )
        }
        if (rhsType != null && rhsType != exprType) {
            ErrorLogger.log(booleanOpExpr, "Type mismatch on boolean operator - RHS: ${typeString(rhsType)}")
        }
    }

    override fun postVisit(arithExpr: ArithExpr) {
        val exprType = deriveType(arithExpr)
        val lhsType = deriveType(arithExpr.lhs)
        val rhsType = deriveType(arithExpr.rhs)

        if (lhsType != exprType) {
            ErrorLogger.log(arithExpr, "Type mismatch on arithmetic operator - LHS: ${typeString(lhsType)}")
        }
        if (rhsType != exprType) {
            ErrorLogger.log(arithExpr, "Type mismatch on arithmetic operator - RHS: ${typeString(rhsType)}")
        }
    }

    override fun postVisit(compExpr: CompExpr) {
        val lhsType = deriveType(compExpr.lhs)
        if (compExpr.op !in CompOp.validOperators(lhsType)) {
            ErrorLogger.log(compExpr.lhs, "${compExpr.op.value} operation not defined on type ${typeString(lhsType)}")
        }

        val rhsType = deriveType(compExpr.rhs)
        if (compExpr.op !in CompOp.validOperators(rhsType)) {
            ErrorLogger.log(compExpr.rhs, "${compExpr.op.value} operation not defined on type ${typeString(rhsType)}")
        }

        if (lhsType != rhsType) {
            ErrorLogger.log(
                compExpr,
                "Type mismatch on comparative operator - LHS: ${typeString(lhsType)}, RHS: ${typeString(rhsType)}"
            )
        }
    }

    override fun postVisit(arrayIndexExpr: ArrayIndexExpr) {
        val arrayType = getVariableType(arrayIndexExpr.id) as ARRAY
        if (arrayIndexExpr.indices.size > arrayType.depth) {
            ErrorLogger.log(arrayIndexExpr, "Indexing too deeply into array of ${arrayType.depth} dimensions")
        }

        arrayIndexExpr.indices.forEach {
            if (deriveType(it) !is INT) {
                ErrorLogger.log(it, "Index must be an integer value")
            }
        }
    }

    override fun postVisit(arrayExpr: ArrayExpr) {
        val arrayType = deriveType(arrayExpr) as ARRAY

        if (arrayType.depth > 1) {
            arrayExpr.entries.forEachIndexed { index, element ->
                val elementType = deriveType(element) as ARRAY
                if (elementType.depth != arrayType.depth - 1 ||
                    (elementType.innerType != arrayType.innerType && elementType.innerType != VOID && arrayType.innerType != VOID)
                ) {
                    ErrorLogger.log(
                        element,
                        "Type mismatch in array at position $index - element type: ${typeString(elementType)}, " +
                                "expected type: ${typeString(arrayType.innerType)}"
                    )
                }
            }
        } else {
            arrayExpr.entries.forEachIndexed { index, element ->
                val elementType = deriveType(element)
                if (elementType != arrayType.innerType) {
                    ErrorLogger.log(
                        element,
                        "Type mismatch in array at position $index - element type: ${typeString(elementType)}, " +
                                "expected type: ${typeString(arrayType.innerType)}"
                    )
                }
            }
        }
    }

    override fun preVisit(classDecl: ClassDecl) {
        currentTable = classDefinitions.find { classDecl.id == it.className }!!.symbolTable
    }

    override fun postVisit(classDecl: ClassDecl) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
    }
}