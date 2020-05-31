package com.egern.visitor

import com.egern.ast.*
import com.egern.types.*

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

    open fun preVisit(arrayOfSizeExpr: ArrayOfSizeExpr) {}
    open fun postVisit(arrayOfSizeExpr: ArrayOfSizeExpr) {}

    open fun preVisit(block: Block) {}
    open fun postVisit(block: Block) {}

    open fun visit(booleanExpr: BooleanExpr) {}

    open fun preVisit(booleanOpExpr: BooleanOpExpr) {}
    open fun midVisit(booleanOpExpr: BooleanOpExpr) {}
    open fun postVisit(booleanOpExpr: BooleanOpExpr) {}

    open fun visit(breakStmt: BreakStmt) {}

    open fun postVisit(castExpr: CastExpr) {}

    open fun preVisit(classDecl: ClassDecl) {}
    open fun midVisit(classDecl: ClassDecl) {}
    open fun postVisit(classDecl: ClassDecl) {}

    open fun visit(classField: ClassField) {}

    open fun midVisit(compExpr: CompExpr) {}
    open fun postVisit(compExpr: CompExpr) {}

    open fun visit(continueStmt: ContinueStmt) {}

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

    open fun preVisit(interfaceDecl: InterfaceDecl) {}
    open fun postVisit(interfaceDecl: InterfaceDecl) {}

    open fun visit(intExpr: IntExpr) {}

    open fun preVisit(lenExpr: LenExpr) {}
    open fun postVisit(lenExpr: LenExpr) {}

    open fun preVisit(methodCall: MethodCall) {}
    open fun midVisit(methodCall: MethodCall) {}
    open fun postVisit(methodCall: MethodCall) {}

    open fun visit(methodSignature: MethodSignature) {}

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

    open fun midVisit(rangeExpr: RangeExpr) {}
    open fun postVisit(rangeExpr: RangeExpr) {}

    open fun preVisit(returnStmt: ReturnStmt) {}
    open fun postVisit(returnStmt: ReturnStmt) {}

    open fun visit(staticClassField: StaticClassField) {}

    open fun preVisit(staticMethodCall: StaticMethodCall) {}
    open fun midVisit(staticMethodCall: StaticMethodCall) {}
    open fun postVisit(staticMethodCall: StaticMethodCall) {}

    open fun visit(stringExpr: StringExpr) {}

    open fun visit(thisExpr: ThisExpr) {}

    open fun preVisit(varAssign: VarAssign) {}
    open fun midVisit(varAssign: VarAssign) {}
    open fun postVisit(varAssign: VarAssign) {}

    open fun preVisit(varDecl: VarDecl) {}
    open fun postVisit(varDecl: VarDecl) {}

    open fun visit(voidExpr: VoidExpr) {}

    open fun preVisit(whileLoop: WhileLoop) {}
    open fun midVisit(whileLoop: WhileLoop) {}
    open fun postVisit(whileLoop: WhileLoop) {}
}