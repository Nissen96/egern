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

    fun getNumLocalFields(): Int {
        return classDecl.fieldDecls.size + classDecl.constructor.size
    }

    fun getNumFields(): Int {
        return getNumLocalFields() + (superclass?.getNumFields() ?: 0)
    }

    fun getNumConstructorArgsPerClass(): List<Int> {
        return (superclass?.getNumConstructorArgsPerClass() ?: emptyList()) + listOf(classDecl.constructor.size)
    }

    fun getLocalFieldsPerClass(): List<List<FieldDecl>> {
        return (superclass?.getLocalFieldsPerClass() ?: emptyList()) + listOf(classDecl.fieldDecls)
    }

    fun getFieldOffset(fieldId: String): Int {
        val (classWithField, fieldSymbol) = lookup(fieldId)!!
        val fieldOffset = fieldSymbol.info["fieldOffset"] as Int

        return (classWithField.superclass?.getNumFields() ?: 0) + fieldOffset
    }

    fun lookup(id: String): Pair<ClassDefinition, Symbol>? {
        // Find symbol recursively in class hierarchy
        val symbol = symbolTable.lookupCurrentScope(id) ?: return superclass?.lookup(id)
        return Pair(this, symbol)
    }
}