package com.egern.symbols

import com.egern.ast.*
import com.egern.util.*
import com.egern.visitor.Visitor

class SymbolVisitor : Visitor {
    private var currentScopeLevel = 0
    private var varCountStack = stackOf(0)
    var currentTable = SymbolTable(0, null)
    var classDefinition = ClassDefinition("Base", null)

    private fun returnToParentScope() {
        currentTable = currentTable.parent!!
    }

    private fun createNewScope() {
        currentTable = SymbolTable(currentScopeLevel, currentTable)
    }

    override fun postVisit(program: Program) {
        program.variableCount = varCountStack.pop()!!
    }

    override fun preVisit(block: Block) {
        createNewScope()
        block.symbolTable = currentTable
    }

    override fun postVisit(block: Block) {
        returnToParentScope()
    }

    override fun preVisit(funcDecl: FuncDecl) {
        currentTable.insert(Symbol(funcDecl.id, SymbolType.Function, currentScopeLevel, mapOf("funcDecl" to funcDecl)))
        currentScopeLevel++
        createNewScope()
        for ((paramOffset, param) in funcDecl.params.withIndex()) {
            currentTable.insert(
                Symbol(
                    param.first,
                    SymbolType.Parameter,
                    currentScopeLevel,
                    mapOf("paramOffset" to paramOffset, "type" to param.second)
                )
            )
        }
        funcDecl.symbolTable = currentTable
        varCountStack.push(0)
    }

    override fun postVisit(funcDecl: FuncDecl) {
        currentScopeLevel--
        returnToParentScope()
        funcDecl.variableCount = varCountStack.pop()!!
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        for (id in varDecl.ids) {
            currentTable.insert(
                Symbol(
                    id,
                    SymbolType.Variable,
                    currentScopeLevel,
                    mapOf("variableOffset" to varCountStack.peek(), "expr" to varDecl.expr)
                )
            )
            varCountStack.apply { it + 1 }
        }
        varDecl.symbolTable = currentTable
    }

    override fun preVisit(classDecl: ClassDecl) {
        createNewScope()
        val newClassDefinition = ClassDefinition(classDecl.id, classDefinition)
        classDefinition = newClassDefinition
    }

    override fun postVisit(classDecl: ClassDecl) {
        classDefinition = classDefinition.parent!!
        returnToParentScope()
    }

    override fun preVisit(methodCall: MethodCall) {
        classDefinition.insertMethod(methodCall.methodId)
    }
}