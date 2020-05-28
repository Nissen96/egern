package com.egern.symbols

import com.egern.ast.*
import com.egern.types.ExprType

class ClassDefinition(
    val className: String,
    val classDecl: ClassDecl,
    var superclass: ClassDefinition? = null,
    val superclassArgs: List<Expr>? = null,
    var interfaceDecl: InterfaceDecl? = null
) {
    var vTableOffset: Int = -1
    lateinit var symbolTable: SymbolTable
    lateinit var vTable: List<FuncDecl>

    fun setVTable() {
        // Get relevant methods for the VTable of the current class
        // Always references latest override of each method
        val methodsPerClass = getMethodsPerClass()
        val allMethods = methodsPerClass[0].toMutableList()

        // For every subclass, add all new methods and replace all overridden
        methodsPerClass.drop(1).forEach { methods ->
            val allMethodNames = allMethods.map { it.id }
            methods.forEach { method ->
                val overrideIndex = allMethodNames.indexOf(method.id)
                if (overrideIndex == -1) {
                    allMethods.add(method)
                } else {
                    allMethods[overrideIndex] = method
                }
            }
        }

        vTable = allMethods
    }

    fun getSuperclasses(): List<String> {
        // Insert interface before Base class
        if (interfaceDecl != null) {
            return listOf(className, interfaceDecl!!.id, "Base")
        }

        return listOf(className) + (superclass?.getSuperclasses() ?: emptyList())
    }

    private fun getMethodsPerClass(): List<List<FuncDecl>> {
        return (superclass?.getMethodsPerClass() ?: emptyList()) + listOf(classDecl.methods)
    }

    fun getConstructorFields(): List<Pair<String, ExprType>> {
        return classDecl.constructor
    }

    fun getLocalFields(): List<FieldDecl> {
        return classDecl.fieldDecls
    }

    private fun getFields(): List<Any> {
        return classDecl.fieldDecls + classDecl.constructor
    }

    private fun getNumLocalFields(): Int {
        return getFields().size
    }

    fun getAllLocalFields(): List<FieldDecl> {
        return (superclass?.getAllLocalFields() ?: emptyList()) + getLocalFields()
    }

    fun getAllFields(): List<Any> {
        return (superclass?.getAllFields() ?: emptyList()) + getFields()
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
        val (classWithField, fieldSymbol) = lookupField(fieldId, actualClass ?: className)!!
        val fieldOffset = fieldSymbol.info["fieldOffset"] as Int

        return (classWithField.superclass?.getNumFields() ?: 0) + fieldOffset
    }

    fun lookupField(id: String): Pair<String, FieldDecl?>? {
        // Find symbol recursively in class hierarchy
        val symbol = symbolTable.lookupCurrentScope(id) ?: return superclass?.lookupField(id)
        val fieldDecl = getLocalFields().find { symbol.id in it.ids }
        return Pair(className, fieldDecl)
    }

    fun lookupField(
        id: String,
        actualClass: String,
        actualClassReached: Boolean = false
    ): Pair<ClassDefinition, Symbol>? {
        // Find symbol in class hierarchy for class cast to some supertype
        val classReached = actualClass == className || actualClassReached
        val symbol = symbolTable.lookupCurrentScope(id) ?: return superclass?.lookupField(id, actualClass, classReached)
        val field = getLocalFields().find { symbol.id in it.ids }  // Null for constructor fields
        if (classReached || field == null || Modifier.OVERRIDE in field.modifiers) {
            return Pair(this, symbol)
        }
        return superclass?.lookupField(id, actualClass, classReached)
    }

    fun lookupMethod(id: String): Pair<String, FuncDecl>? {
        val funcDecl = classDecl.methods.find { id == it.id } ?: return superclass?.lookupMethod(id)
        return Pair(className, funcDecl)
    }

    fun getAllMethods(actualClass: String, actualClassReached: Boolean = false): List<FuncDecl> {
        var relevantMethods = classDecl.methods
        val classReached = actualClass == className || actualClassReached
        if (!classReached) relevantMethods = relevantMethods.filter { Modifier.OVERRIDE in it.modifiers }
        return (superclass?.getAllMethods(actualClass, classReached) ?: emptyList()) + relevantMethods
    }

    fun getInterface(): InterfaceDecl? {
        return interfaceDecl ?: superclass?.getInterface()
    }
}