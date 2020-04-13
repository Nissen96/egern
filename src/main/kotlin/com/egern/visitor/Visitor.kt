package com.egern.visitor

import com.egern.ast.*
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.types.*

abstract class Visitor {
    // Utility functions
    fun typeString(type: ExprType): String {
        return when (type) {
            INT -> "int"
            BOOLEAN -> "boolean"
            VOID -> "void"
            is ARRAY -> "[".repeat(type.depth) + typeString(type.innerType) + "]".repeat(type.depth)
            is CLASS -> type.className
        }
    }

    fun getObjectClass(objectId: String, symbolTable: SymbolTable): String {
        val symbol = symbolTable.lookup(objectId)!!
        return if (symbol.type == SymbolType.Variable) {
            var instance = symbolTable.lookup(objectId)!!.info["expr"]
            while (instance is IdExpr) {
                instance = symbolTable.lookup(instance.id)!!.info["expr"]
            }
            return when (instance) {
                is ObjectInstantiation -> instance.classId
                is CastExpr -> (instance.type as CLASS).className
                else -> throw Error("Invalid instance type")
            }
        } else {
            (symbol.info["type"] as CLASS).className
        }
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

    open fun preVisit(castExpr: CastExpr) {}
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