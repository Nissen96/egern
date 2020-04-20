package com.egern.symbols

import com.egern.ast.*
import com.egern.types.ExprType

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

    fun getFieldOffset(fieldId: String, actualClass: String? = null): Int {
        val (classWithField, fieldSymbol) = lookup(fieldId, actualClass ?: className)!!
        val fieldOffset = fieldSymbol.info["fieldOffset"] as Int

        return (classWithField.superclass?.getNumFields() ?: 0) + fieldOffset
    }

    fun lookup(id: String, actualClass: String, actualClassReached: Boolean = false): Pair<ClassDefinition, Symbol>? {
        // Find symbol recursively in class hierarchy
        val classReached = actualClass == className || actualClassReached
        val symbol = symbolTable.lookupCurrentScope(id) ?: return superclass?.lookup(id, actualClass, classReached)
        val field = getLocalFields().find { symbol.id in it.ids }
        if (classReached || field == null || Modifier.OVERRIDE in field.modifiers) {
            return Pair(this, symbol)
        }
        return superclass?.lookup(id, actualClass, classReached)
    }

    fun getMethods(actualClass: String, actualClassReached: Boolean = false): List<FuncDecl> {
        var relevantMethods = classDecl.methods
        val classReached = actualClass == className || actualClassReached
        if (!classReached) relevantMethods = relevantMethods.filter { Modifier.OVERRIDE in it.modifiers }
        return (superclass?.getMethods(actualClass, classReached) ?: emptyList()) + relevantMethods
    }
}