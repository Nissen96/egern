package com.egern.symbols

import com.egern.ast.*
import com.egern.util.*
import com.egern.visitor.Visitor

class SymbolVisitor : Visitor() {
    private var currentScopeLevel = 0
    private var varCountStack = stackOf(0)
    private var fieldOffset = 0
    var symbolTable = SymbolTable(0, null)
    val classDefinitions = mutableListOf<ClassDefinition>()
    val interfaces = mutableListOf<InterfaceDecl>()

    private fun returnToParentScope() {
        symbolTable = symbolTable.parent!!
    }

    private fun createNewScope() {
        symbolTable = SymbolTable(currentScopeLevel, symbolTable)
    }

    override fun postVisit(program: Program) {
        program.variableCount = varCountStack.pop()!!
    }

    override fun preVisit(block: Block) {
        createNewScope()
        block.symbolTable = symbolTable
    }

    override fun postVisit(block: Block) {
        returnToParentScope()
    }

    override fun preVisit(funcDecl: FuncDecl) {
        symbolTable.insert(
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
            symbolTable.insert(
                Symbol(
                    param.id,
                    SymbolType.Parameter,
                    currentScopeLevel,
                    mutableMapOf("paramOffset" to paramOffset, "type" to param.type)
                )
            )
        }
        funcDecl.symbolTable = symbolTable
        varCountStack.push(0)
    }

    override fun postVisit(funcDecl: FuncDecl) {
        currentScopeLevel--
        returnToParentScope()
        funcDecl.variableCount = varCountStack.pop()!!
    }

    override fun preVisit(varDecl: VarDecl) {
        varDecl.ids.forEach { id ->
            symbolTable.insert(
                Symbol(
                    id, SymbolType.Variable, currentScopeLevel, mutableMapOf(
                        "variableOffset" to varCountStack.peek(),
                        "expr" to varDecl.expr
                    )
                )
            )
            varCountStack.apply { it + 1 }
        }
        varDecl.symbolTable = symbolTable
    }

    override fun preVisit(varAssign: VarAssign) {
        varAssign.ids.forEach {
            symbolTable.lookup(it)?.info?.set("expr", varAssign.expr)
        }
    }

    override fun preVisit(fieldDecl: FieldDecl) {
        for (id in fieldDecl.ids) {
            symbolTable.insert(
                Symbol(
                    id, SymbolType.Field, currentScopeLevel, mutableMapOf(
                        "fieldOffset" to fieldOffset,
                        "expr" to fieldDecl.expr
                    )
                )
            )
            fieldOffset++
        }
        fieldDecl.symbolTable = symbolTable
    }

    override fun preVisit(classDecl: ClassDecl) {
        symbolTable.insert(
            Symbol(
                classDecl.id,
                SymbolType.Class,
                currentScopeLevel,
                mutableMapOf("classDecl" to classDecl)
            )
        )
        createNewScope()
        for ((index, field) in classDecl.constructor.withIndex()) {
            symbolTable.insert(
                Symbol(
                    field.id,
                    SymbolType.Field,
                    currentScopeLevel,
                    mutableMapOf("fieldOffset" to index, "type" to field.type)
                )
            )
        }
        val classDefinition = ClassDefinition(classDecl.id, classDecl)
        classDefinition.symbolTable = symbolTable
        classDefinitions.add(classDefinition)
        fieldOffset = classDecl.constructor.size
    }

    override fun postVisit(classDecl: ClassDecl) {
        returnToParentScope()
    }

    override fun preVisit(interfaceDecl: InterfaceDecl) {
        interfaces.add(interfaceDecl)
    }
}
