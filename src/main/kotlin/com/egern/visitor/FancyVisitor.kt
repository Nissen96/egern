package com.egern.visitor

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.symbols.ClassDefinition
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.types.*
import java.lang.Exception

abstract class FancyVisitor(
    var symbolTable: SymbolTable,
    val classDefinitions: MutableList<ClassDefinition>
) : Visitor() {

    // Utility functions
    fun getObjectClass(objectId: String): CLASS {
        val symbol = lookupSymbol(objectId, listOf(SymbolType.Variable, SymbolType.Parameter))
        return when (symbol.type) {
            SymbolType.Variable -> {
                var instance = symbolTable.lookup(objectId)!!.info["expr"]
                while (instance is IdExpr) {
                    instance = symbolTable.lookup(instance.id)!!.info["expr"]
                }
                return when (instance) {
                    is ObjectInstantiation -> CLASS(instance.classId)
                    is CastExpr -> CLASS(
                        (deriveType(instance.expr) as CLASS).className,
                        (instance.type as CLASS).className
                    )
                    else -> throw Error("Invalid instance type")
                }
            }
            SymbolType.Parameter -> symbol.info["type"] as CLASS
            else -> throw Exception("Id does not match a class")
        }
    }

    // Type functions
    fun getVariableType(id: String): ExprType {
        val symbol = lookupSymbol(id, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field))
        return when (symbol.type) {
            SymbolType.Variable -> deriveType(symbol.info["expr"] as Expr)
            SymbolType.Parameter -> symbol.info["type"] as ExprType

            // Get type directly for constructor parameters, derive if local field
            SymbolType.Field -> symbol.info["type"] as? ExprType ?: deriveType(
                symbol.info["expr"] as Expr
            )
            else -> throw Exception("Can't derive type for IdExpr")
        }
    }

    fun lookupSymbol(id: String, validTypes: List<SymbolType>): Symbol {
        val sym = symbolTable.lookup(id) ?: throw Exception("Symbol '$id' not defined")
        if (sym.type !in validTypes) {
            ErrorLogger.log(Exception("Symbol '$id' should be one of types $validTypes but is not"))
        }
        return sym
    }

    fun deriveType(
        expr: Expr
    ): ExprType {
        return when (expr) {
            // Handle implicit returns of nothing (int=0)
            is IntExpr -> if (expr.isVoid) VOID else INT
            is BooleanExpr -> BOOLEAN
            is StringExpr -> STRING
            is BooleanOpExpr -> BOOLEAN
            is CompExpr -> BOOLEAN
            is ArithExpr -> INT
            is IdExpr -> getVariableType(expr.id)
            is FuncCall -> (symbolTable.lookup(expr.id)!!.info["funcDecl"] as FuncDecl).returnType
            is ParenExpr -> deriveType(expr.expr)
            is LenExpr -> INT
            is ArrayExpr -> deriveArrayType(expr)
            is ArrayIndexExpr -> {
                val array = deriveType(expr.id) as ARRAY
                if (array.depth - expr.indices.size > 0) {
                    ARRAY(array.depth - expr.indices.size, array.innerType)
                } else {
                    array.innerType
                }
            }
            is ObjectInstantiation -> CLASS(expr.classId)
            is MethodCall -> deriveMethodCallType(expr)
            is ClassField -> deriveClassFieldType(expr)
            is CastExpr -> expr.type
            else -> throw Exception("Can't derive type for expr!")
        }
    }

    private fun deriveMethodCallType(
        methodCall: MethodCall
    ): ExprType {
        val callerClass = if (methodCall is StaticMethodCall) CLASS(methodCall.classId) else getObjectClass(
            methodCall.objectId
        )
        val classDefinition = classDefinitions.find { it.className == callerClass.className }!!
        val methods = classDefinition.getMethods(callerClass.castTo ?: callerClass.className)
        return methods.find { it.id == methodCall.methodId }!!.returnType
    }

    private fun deriveClassFieldType(
        classField: ClassField
    ): ExprType {
        val callerClass = if (classField is StaticClassField) CLASS(classField.classId) else getObjectClass(
            classField.objectId
        )
        val classDefinition = classDefinitions.find { it.className == callerClass.className }
            ?: throw Exception("Class ${callerClass.className} not defined")
        val field = classDefinition.lookupField(classField.fieldId, callerClass.castTo ?: callerClass.className)!!
        return if (field.second.info.containsKey("expr")) {
            deriveType(field.second.info["expr"] as Expr)
        } else {
            field.second.info["type"] as ExprType
        }
    }

    private fun deriveArrayType(
        arrayExpr: ArrayExpr
    ): ExprType {
        var depth = 0
        var expr: Expr = arrayExpr
        while (expr is ArrayExpr) {
            depth++
            expr = if (expr.entries.isNotEmpty()) expr.entries[0] else IntExpr(0, isVoid = true)
        }

        var innerType = deriveType(expr)
        if (innerType is ARRAY) {
            depth += innerType.depth
            innerType = innerType.innerType
        }

        return ARRAY(depth, innerType)
    }
}