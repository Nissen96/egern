package com.egern.visitor

import com.egern.ast.*
import com.egern.util.forEach

class PrintProgramVisitor(private val indentation: Int = 4) : Visitor() {
    private var level = 0

    private fun printIndented(text: Any = "") {
        print(" ".repeat(indentation * level) + "$text")
    }

    override fun preStmtVisit() {
        printIndented()
    }

    override fun postStmtVisit() {
        println()
    }

    override fun midVisit(arithExpr: ArithExpr) {
        print(" ${arithExpr.op.value} ")
    }

    override fun preVisit(arrayExpr: ArrayExpr) {
        print("[")
    }

    override fun midVisit(arrayExpr: ArrayExpr) {
        print(", ")
    }

    override fun postVisit(arrayExpr: ArrayExpr) {
        print("]")
    }

    override fun preMidVisit(arrayIndexExpr: ArrayIndexExpr) {
        print("[")
    }

    override fun postMidVisit(arrayIndexExpr: ArrayIndexExpr) {
        print("]")
    }

    override fun preVisit(arrayOfSizeExpr: ArrayOfSizeExpr) {
        print("${typeString(arrayOfSizeExpr.type)}[")
    }

    override fun postVisit(arrayOfSizeExpr: ArrayOfSizeExpr) {
        print("]")
    }

    override fun preVisit(block: Block) {
        println("{")
        level++
    }

    override fun postVisit(block: Block) {
        level--
        printIndented("}")
    }

    override fun visit(booleanExpr: BooleanExpr) {
        print(booleanExpr.value)
    }

    override fun preVisit(booleanOpExpr: BooleanOpExpr) {
        if (booleanOpExpr.rhs == null) {
            print(booleanOpExpr.op.value)
        }
    }

    override fun midVisit(booleanOpExpr: BooleanOpExpr) {
        if (booleanOpExpr.rhs != null) {
            print(" ${booleanOpExpr.op.value} ")
        }
    }

    override fun visit(breakStmt: BreakStmt) {
        print("break")
    }

    override fun postVisit(castExpr: CastExpr) {
        print(" as ${typeString(castExpr.type)}")
    }

    override fun preVisit(classDecl: ClassDecl) {
        printIndented("class ${classDecl.id}")
        if (classDecl.constructor.isNotEmpty()) {
            print("(${classDecl.constructor.joinToString(", ") {
                "${if (it.modifier != null) it.modifier.modifier + " " else ""}${it.id}: ${typeString(it.type)}"
            }})")
        }
        print(": ${classDecl.superclass}(")
        classDecl.superclassArgs?.forEach({ it.accept(this) }, { print(", ") })
        println(") {")
        level++
    }

    override fun postVisit(classDecl: ClassDecl) {
        level--
        printIndented("}\n\n")
    }

    override fun visit(classField: ClassField) {
        print("${classField.objectId}.${classField.fieldId}")
    }

    override fun midVisit(compExpr: CompExpr) {
        print(" ${compExpr.op.value} ")
    }

    override fun visit(continueStmt: ContinueStmt) {
        print("continue")
    }

    override fun preVisit(fieldDecl: FieldDecl) {
        fieldDecl.modifiers.forEach { print("${it.modifier} ") }
        print("var ")
        fieldDecl.ids.forEach { print("$it = ") }
    }

    override fun preVisit(funcCall: FuncCall) {
        print("${funcCall.id}(")
    }

    override fun midVisit(funcCall: FuncCall) {
        print(", ")
    }

    override fun postVisit(funcCall: FuncCall) {
        print(")")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        printIndented()
        funcDecl.modifiers.forEach { print("${it.modifier} ") }
        print("func ${funcDecl.id}(")
        print(funcDecl.params.joinToString(", ") { "${it.first}: ${typeString(it.second)}" }) // Params
        println("): ${typeString(funcDecl.returnType)} {")
        level++
    }

    override fun postVisit(funcDecl: FuncDecl) {
        level--
        printIndented("}\n")
    }

    override fun visit(idExpr: IdExpr) {
        print(idExpr.id)
    }

    override fun preVisit(ifElse: IfElse) {
        print("if (")
    }

    override fun preMidVisit(ifElse: IfElse) {
        print(") ")
    }

    override fun postMidVisit(ifElse: IfElse) {
        if (ifElse.elseBlock != null) print(" else ")
    }

    override fun preVisit(interfaceDecl: InterfaceDecl) {
        printIndented("interface ${interfaceDecl.id} {\n")
        level++
    }

    override fun postVisit(interfaceDecl: InterfaceDecl) {
        level--
        printIndented("}\n\n")
    }

    override fun visit(intExpr: IntExpr) {
        print(intExpr.value)
    }

    override fun preVisit(lenExpr: LenExpr) {
        print("len(")
    }

    override fun postVisit(lenExpr: LenExpr) {
        print(")")
    }

    override fun preVisit(methodCall: MethodCall) {
        print("${methodCall.objectId}.${methodCall.methodId}(")
    }

    override fun midVisit(methodCall: MethodCall) {
        print(", ")
    }

    override fun postVisit(methodCall: MethodCall) {
        print(")")
    }

    override fun visit(methodSignature: MethodSignature) {
        printIndented("func ${methodSignature.id}(")
        print(methodSignature.params.joinToString(", ") { typeString(it) })  // Params
        println("): ${typeString(methodSignature.returnType)}")
    }

    override fun preVisit(objectInstantiation: ObjectInstantiation) {
        print("${objectInstantiation.classId}(")
    }

    override fun midVisit(objectInstantiation: ObjectInstantiation) {
        print(", ")
    }

    override fun postVisit(objectInstantiation: ObjectInstantiation) {
        print(")")
    }

    override fun preVisit(parenExpr: ParenExpr) {
        print("(")
    }

    override fun postVisit(parenExpr: ParenExpr) {
        print(")")
    }

    override fun preVisit(printStmt: PrintStmt) {
        print("print(")
    }

    override fun postVisit(printStmt: PrintStmt) {
        print(")")
        if (printStmt.expr == null) println()
    }

    override fun preVisit(program: Program) {
        println("Main Scope {")
        level++
    }

    override fun postVisit(program: Program) {
        level--
        printIndented("}")
        println()
    }

    override fun midVisit(rangeExpr: RangeExpr) {
        print(if (rangeExpr.inclusive) "..." else "..")
    }

    override fun preVisit(returnStmt: ReturnStmt) {
        print("return")
        if (returnStmt.expr != null) print(" ")
    }

    override fun visit(staticClassField: StaticClassField) {
        print("${staticClassField.classId}.${staticClassField.fieldId}")
    }

    override fun preVisit(staticMethodCall: StaticMethodCall) {
        print("${staticMethodCall.classId}.${staticMethodCall.methodId}(")
    }

    override fun midVisit(staticMethodCall: StaticMethodCall) {
        print(", ")
    }

    override fun postVisit(staticMethodCall: StaticMethodCall) {
        print(")")
    }

    override fun visit(stringExpr: StringExpr) {
        print("\"${stringExpr.value}\"")
    }

    override fun visit(thisExpr: ThisExpr) {
        print("this")
    }

    override fun preVisit(varAssign: VarAssign) {
        varAssign.ids.forEach { print("$it = ") }
    }

    override fun midVisit(varAssign: VarAssign) {
        print(" = ")
    }

    override fun preVisit(varDecl: VarDecl) {
        print("var ")
        varDecl.ids.forEach { print("$it = ") }
    }

    override fun preVisit(whileLoop: WhileLoop) {
        print("while (")
    }

    override fun midVisit(whileLoop: WhileLoop) {
        print(") ")
    }
}