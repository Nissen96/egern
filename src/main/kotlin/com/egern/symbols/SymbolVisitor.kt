package com.egern.symbols

import com.egern.ast.*
import com.egern.util.*
import com.egern.visitor.Visitor

class SymbolVisitor : Visitor {
    private var currentScopeLevel = 0
    private var varCountStack = stackOf(0)
    var currentTable = SymbolTable(0, null)
    private val baseClass = ClassDefinition(
        "Base",
        ClassDecl("Base", emptyList(), null, null, emptyList(), emptyList())
    )
    val classDefinitions = mutableListOf(baseClass)
    private var isInsideClass = false

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

    override fun preVisit(varDecl: VarDecl) {
        for (id in varDecl.ids) {
            currentTable.insert(
                Symbol(
                    id, SymbolType.Variable, currentScopeLevel, mapOf(
                        "variableOffset" to varCountStack.peek(),
                        "expr" to varDecl.expr
                    )
                )
            )
            varCountStack.apply { it + 1 }
        }
        varDecl.symbolTable = currentTable
    }

    override fun preVisit(fieldDecl: FieldDecl) {
        for (id in fieldDecl.ids) {
            currentTable.insert(
                Symbol(
                    id, SymbolType.Field, currentScopeLevel, mapOf(
                        "fieldOffset" to varCountStack.peek(),
                        "expr" to fieldDecl.expr
                    )
                )
            )
            varCountStack.apply { it + 1 }
        }
        fieldDecl.symbolTable = currentTable
    }

    override fun preVisit(classDecl: ClassDecl) {
        createNewScope()
        for ((index, field) in classDecl.constructor.withIndex()) {
            currentTable.insert(
                Symbol(
                    field.first,
                    SymbolType.Field,
                    currentScopeLevel,
                    mapOf("fieldOffset" to index, "type" to field.second)
                )
            )
        }
        val classDefinition = ClassDefinition(classDecl.id, classDecl, baseClass, classDecl.superclassArgs)
        classDefinition.symbolTable = currentTable
        classDefinitions.add(classDefinition)
        varCountStack.push(classDecl.constructor.size)
        isInsideClass = true
    }

    override fun postVisit(classDecl: ClassDecl) {
        isInsideClass = false
        returnToParentScope()
    }
}