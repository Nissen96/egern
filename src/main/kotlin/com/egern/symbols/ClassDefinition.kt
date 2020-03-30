package com.egern.symbols

import com.egern.ast.ClassDecl
import com.egern.ast.Expr
import com.egern.ast.FieldDecl
import com.egern.ast.FuncDecl
import com.egern.error.ErrorLogger
import com.egern.types.ExprType
import java.lang.Exception

class ClassDefinition(
    val className: String,
    val classDecl: ClassDecl,
    var superclass: ClassDefinition? = null,
    val superclassArgs: List<Expr>? = null
) {
    var vTableOffset: Int = -1
    var numFields: Int = 0
    lateinit var symbolTable: SymbolTable

    fun getMethods(): List<FuncDecl> {
        return (superclass?.getMethods() ?: emptyList()) + classDecl.methods
    }

    fun getConstructor(): List<Pair<String, ExprType>> {
        return classDecl.constructor
    }

    fun getLocalFields(): List<FieldDecl> {
        return classDecl.fieldDecls
    }

    fun getFields(): List<FieldDecl> {
        return (superclass?.getFields() ?: emptyList()) + getLocalFields()
    }

    fun getNumConstructorArgsPerClass(): List<Int> {
        return listOf(classDecl.constructor.size) + (superclass?.getNumConstructorArgsPerClass() ?: emptyList())
    }

    fun lookup(id: String): Symbol? {
        // Find symbol recursively in class hierarchy
        return symbolTable.lookupCurrentScope(id) ?: superclass?.lookup(id)
    }
}