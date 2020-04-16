package com.egern.visitor

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.symbols.ClassDefinition
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.types.*
import java.lang.Exception

abstract class Visitor {
    // Utility functions
    fun typeString(type: ExprType): String {
        return when (type) {
            INT -> "int"
            BOOLEAN -> "boolean"
            STRING -> "string"
            VOID -> "void"
            is ARRAY -> "[".repeat(type.depth) + typeString(type.innerType) + "]".repeat(type.depth)
            is CLASS -> type.className
        }
    }

    fun getObjectClass(objectId: String, symbolTable: SymbolTable, classDefinitions: List<ClassDefinition>): CLASS {
        val symbol = symbolTable.lookup(objectId)!!
        return if (symbol.type == SymbolType.Variable) {
            var instance = symbolTable.lookup(objectId)!!.info["expr"]
            while (instance is IdExpr) {
                instance = symbolTable.lookup(instance.id)!!.info["expr"]
            }
            return when (instance) {
                is ObjectInstantiation -> CLASS(instance.classId)
                is CastExpr -> CLASS((deriveType(instance.expr, symbolTable, classDefinitions) as CLASS).className, (instance.type as CLASS).className)
                else -> throw Error("Invalid instance type")
            }
        } else {
            symbol.info["type"] as CLASS
        }
    }

    // Type functions
    fun getVariableType(id: String, symbolTable: SymbolTable, classDefinitions: List<ClassDefinition>): ExprType {
        val symbol = lookupSymbol(id, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field), symbolTable)
        return when (symbol.type) {
            SymbolType.Variable -> deriveType(symbol.info["expr"] as Expr, symbolTable, classDefinitions)
            SymbolType.Parameter -> symbol.info["type"] as ExprType

            // Get type directly for constructor parameters, derive if local field
            SymbolType.Field -> symbol.info["type"] as? ExprType ?: deriveType(
                symbol.info["expr"] as Expr,
                symbolTable,
                classDefinitions
            )
            else -> throw Exception("Can't derive type for IdExpr")
        }
    }

    private fun lookupSymbol(id: String, validTypes: List<SymbolType>, currentTable: SymbolTable): Symbol {
        val sym = currentTable.lookup(id) ?: throw Exception("Symbol '$id' not defined")
        if (sym.type !in validTypes) {
            ErrorLogger.log(Exception("Symbol '$id' should be one of types $validTypes but is not"))
        }
        return sym
    }

    fun deriveType(
        expr: Expr,
        currentTable: SymbolTable,
        classDefinitions: List<ClassDefinition>
    ): ExprType {
        return when (expr) {
            // Handle implicit returns of nothing (int=0)
            is IntExpr -> if (expr.isVoid) VOID else INT
            is BooleanExpr -> BOOLEAN
            is StringExpr -> STRING
            is BooleanOpExpr -> BOOLEAN
            is CompExpr -> BOOLEAN
            is ArithExpr -> INT
            is IdExpr -> getVariableType(expr.id, currentTable, classDefinitions)
            is FuncCall -> (currentTable.lookup(expr.id)!!.info["funcDecl"] as FuncDecl).returnType
            is ParenExpr -> deriveType(expr.expr, currentTable, classDefinitions)
            is LenExpr -> INT
            is ArrayExpr -> deriveArrayType(expr, currentTable, classDefinitions)
            is ArrayIndexExpr -> {
                val array = getVariableType(expr.id, currentTable, classDefinitions) as ARRAY
                if (array.depth - expr.indices.size > 0) {
                    ARRAY(array.depth - expr.indices.size, array.innerType)
                } else {
                    array.innerType
                }
            }
            is ObjectInstantiation -> CLASS(expr.classId)
            is MethodCall -> deriveMethodCallType(expr, currentTable, classDefinitions)
            is ClassField -> deriveClassFieldType(expr, currentTable, classDefinitions)
            is CastExpr -> expr.type
            else -> throw Exception("Can't derive type for expr!")
        }
    }

    private fun deriveMethodCallType(
        methodCall: MethodCall,
        currentTable: SymbolTable,
        classDefinitions: List<ClassDefinition>
    ): ExprType {
        val objectClass = getObjectClass(methodCall.objectId, currentTable, classDefinitions)
        val classDefinition = classDefinitions.find { it.className == objectClass.className }!!
        val methods = classDefinition.getMethods(objectClass.castTo ?: objectClass.className)
        return methods.find { it.id == methodCall.methodId }!!.returnType
    }

    private fun deriveClassFieldType(
        classField: ClassField,
        currentTable: SymbolTable,
        classDefinitions: List<ClassDefinition>
    ): ExprType {
        val objectClass = getObjectClass(classField.objectId, currentTable, classDefinitions)
        val classDefinition = classDefinitions.find { it.className == objectClass.className }!!
        val field = classDefinition.lookup(classField.fieldId, objectClass.castTo ?: objectClass.className)!!
        return if (field.second.info.containsKey("expr")) {
            deriveType(field.second.info["expr"] as Expr, currentTable, classDefinitions)
        } else {
            field.second.info["type"] as ExprType
        }
    }

    private fun deriveArrayType(
        arrayExpr: ArrayExpr,
        currentTable: SymbolTable,
        classDefinitions: List<ClassDefinition>
    ): ExprType {
        var depth = 0
        var expr: Expr = arrayExpr
        while (expr is ArrayExpr) {
            depth++
            expr = if (expr.entries.isNotEmpty()) expr.entries[0] else IntExpr(0, isVoid = true)
        }

        var innerType = deriveType(expr, currentTable, classDefinitions)
        if (innerType is ARRAY) {
            depth += innerType.depth
            innerType = innerType.innerType
        }

        return ARRAY(depth, innerType)
    }

    // General visits
    open fun preStmtVisit() {}
    open fun postStmtVisit() {}

    // AST node visits
    open fun midVisit(arithExpr: ArithExpr) {}
    open fun postVisit(arithExpr: ArithExpr) {}

    open fun preVisit(arrayExpr: ArrayExpr) {}
    open fun midVisit(arrayExpr: ArrayExpr) {}
    open fun postVisit(arrayExpr: ArrayExpr) {}

    open fun preVisit(arrayIndexExpr: ArrayIndexExpr) {}
    open fun preMidVisit(arrayIndexExpr: ArrayIndexExpr) {}
    open fun postMidVisit(arrayIndexExpr: ArrayIndexExpr) {}
    open fun postVisit(arrayIndexExpr: ArrayIndexExpr) {}

    open fun preVisit(block: Block) {}
    open fun postVisit(block: Block) {}

    open fun visit(booleanExpr: BooleanExpr) {}

    open fun preVisit(booleanOpExpr: BooleanOpExpr) {}
    open fun midVisit(booleanOpExpr: BooleanOpExpr) {}
    open fun postVisit(booleanOpExpr: BooleanOpExpr) {}

    open fun postVisit(castExpr: CastExpr) {}

    open fun preVisit(classDecl: ClassDecl) {}
    open fun midVisit(classDecl: ClassDecl) {}
    open fun postVisit(classDecl: ClassDecl) {}

    open fun visit(classField: ClassField) {}

    open fun midVisit(compExpr: CompExpr) {}
    open fun postVisit(compExpr: CompExpr) {}

    open fun preVisit(fieldDecl: FieldDecl) {}
    open fun postVisit(fieldDecl: FieldDecl) {}

    open fun preVisit(funcCall: FuncCall) {}
    open fun midVisit(funcCall: FuncCall) {}
    open fun postVisit(funcCall: FuncCall) {}

    open fun preVisit(funcDecl: FuncDecl) {}
    open fun postVisit(funcDecl: FuncDecl) {}

    open fun visit(idExpr: IdExpr) {}

    open fun preVisit(ifElse: IfElse) {}
    open fun preMidVisit(ifElse: IfElse) {}
    open fun postMidVisit(ifElse: IfElse) {}
    open fun postVisit(ifElse: IfElse) {}

    open fun visit(intExpr: IntExpr) {}

    open fun preVisit(lenExpr: LenExpr) {}
    open fun postVisit(lenExpr: LenExpr) {}

    open fun preVisit(methodCall: MethodCall) {}
    open fun midVisit(methodCall: MethodCall) {}
    open fun postVisit(methodCall: MethodCall) {}

    open fun preVisit(objectInstantiation: ObjectInstantiation) {}
    open fun midVisit(objectInstantiation: ObjectInstantiation) {}
    open fun postVisit(objectInstantiation: ObjectInstantiation) {}

    open fun preVisit(parenExpr: ParenExpr) {}
    open fun postVisit(parenExpr: ParenExpr) {}

    open fun preVisit(printStmt: PrintStmt) {}
    open fun postVisit(printStmt: PrintStmt) {}

    open fun preVisit(program: Program) {}
    open fun midVisit(program: Program) {}
    open fun postVisit(program: Program) {}

    open fun preVisit(returnStmt: ReturnStmt) {}
    open fun postVisit(returnStmt: ReturnStmt) {}

    open fun visit(stringExpr: StringExpr) {}

    open fun visit(thisExpr: ThisExpr) {}

    open fun preVisit(varAssign: VarAssign) {}
    open fun midVisit(varAssign: VarAssign) {}
    open fun postVisit(varAssign: VarAssign) {}

    open fun preVisit(varDecl: VarDecl) {}
    open fun postVisit(varDecl: VarDecl) {}

    open fun preVisit(whileLoop: WhileLoop) {}
    open fun midVisit(whileLoop: WhileLoop) {}
    open fun postVisit(whileLoop: WhileLoop) {}
}