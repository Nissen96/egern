package com.egern.symbols

import com.egern.ast.*
import com.egern.util.*
import com.egern.visitor.Visitor

class SymbolVisitor : Visitor() {
    private var currentScopeLevel = 0
    private var varCountStack = stackOf(0)
    var currentTable = SymbolTable(0, null)
    private val baseClass = ClassDefinition(
        "Base",
        ClassDecl("Base", emptyList(), null, null, emptyList(), emptyList())
    )
    val classDefinitions = mutableListOf(baseClass)

    private fun returnToParentScope() {
        currentTable = currentTable.parent!!
    }

    private fun createNewScope() {
        currentTable = SymbolTable(currentScopeLevel, currentTable)
    }

    override fun preVisit(program: Program) {
        baseClass.symbolTable = currentTable
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
        currentTable.insert(
            Symbol(
                funcDecl.id,
                SymbolType.Function,
                currentScopeLevel,
                mutableMapOf("funcDecl" to funcDecl)
            )
        )
        currentScopeLevel++
        createNewScope()
        for ((paramOffset, param) in funcDecl.params.withIndex()) {
            currentTable.insert(
                Symbol(
                    param.first,
                    SymbolType.Parameter,
                    currentScopeLevel,
                    mutableMapOf("paramOffset" to paramOffset, "type" to param.second)
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
        varDecl.ids.forEach { id ->
            currentTable.insert(
                Symbol(
                    id, SymbolType.Variable, currentScopeLevel, mutableMapOf(
                        "variableOffset" to varCountStack.peek(),
                        "expr" to varDecl.expr
                    )
                )
            )
            varCountStack.apply { it + 1 }
        }
        varDecl.symbolTable = currentTable
    }

    override fun preVisit(varAssign: VarAssign) {
        varAssign.ids.forEach {
            currentTable.lookup(it)?.info?.set("expr", varAssign.expr)
        }
    }

    override fun preVisit(fieldDecl: FieldDecl) {
        for (id in fieldDecl.ids) {
            currentTable.insert(
                Symbol(
                    id, SymbolType.Field, currentScopeLevel, mutableMapOf(
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
        currentTable.insert(
            Symbol(
                classDecl.id,
                SymbolType.Class,
                currentScopeLevel,
                mutableMapOf("classDecl" to classDecl)
            )
        )
        createNewScope()
        for ((index, field) in classDecl.constructor.withIndex()) {
            currentTable.insert(
                Symbol(
                    field.first,
                    SymbolType.Field,
                    currentScopeLevel,
                    mutableMapOf("fieldOffset" to index, "type" to field.second)
                )
            )
        }
        val classDefinition = ClassDefinition(classDecl.id, classDecl, baseClass, classDecl.superclassArgs)
        classDefinition.symbolTable = currentTable
        classDefinitions.add(classDefinition)
        varCountStack.push(classDecl.constructor.size)
    }

    override fun postVisit(classDecl: ClassDecl) {
        returnToParentScope()
    }
}
