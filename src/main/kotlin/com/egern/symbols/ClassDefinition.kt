package com.egern.symbols

import com.egern.ast.*

class ClassDefinition(
    val className: String,
    val classDecl: ClassDecl,
    var superclass: ClassDefinition? = null,
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

    private fun getMethodsPerClass(): List<List<FuncDecl>> {
        return (superclass?.getMethodsPerClass() ?: emptyList()) + listOf(classDecl.methods)
    }

    fun getSuperclasses(includeInterface: Boolean = true): List<String> {
        // Optionally insert interface
        if (includeInterface && interfaceDecl != null) {
            return listOf(className, interfaceDecl!!.id)
        }

        return listOf(className) + (superclass?.getSuperclasses(includeInterface) ?: emptyList())
    }

    fun getInterface(): InterfaceDecl? {
        return interfaceDecl ?: superclass?.getInterface()
    }

    fun getConstructorFields(): List<Parameter> {
        return classDecl.constructor
    }

    fun getLocalFields(): List<FieldDecl> {
        return classDecl.fieldDecls
    }

    fun getInheritedFields(): List<Any> {
        return (superclass?.getInheritedFields() ?: emptyList()) + getConstructorFields() + getLocalFields()
    }

    fun getNumConstructorArgsPerClass(): List<Int> {
        return (superclass?.getNumConstructorArgsPerClass() ?: emptyList()) + listOf(classDecl.constructor.size)
    }

    fun getLocalFieldsPerClass(): List<List<FieldDecl>> {
        return (superclass?.getLocalFieldsPerClass() ?: emptyList()) + listOf(classDecl.fieldDecls)
    }

    fun getSuperclassArgs(): List<Expr>? {
        return classDecl.superclassArgs
    }

    fun getFieldOffset(fieldId: String, castToClass: String? = null): Int {
        val (classWithField, fieldSymbol) = lookupField(fieldId, castToClass ?: className) ?: throw Exception(
            "Field $fieldId does not exist in class ${castToClass ?: className}"
        )
        val fieldOffset = fieldSymbol.info["fieldOffset"] as Int

        // Return field offset in all fields inherited from superclasses
        return (classWithField.superclass?.getInheritedFields()?.size ?: 0) + fieldOffset
    }

    fun lookupLocalField(id: String): FieldDecl? {
        return getLocalFields().find { id in it.ids } ?: superclass?.lookupLocalField(id)
    }

    fun lookupConstructorField(id: String): Parameter? {
        return getConstructorFields().find { id == it.id } ?: superclass?.lookupConstructorField(id)
    }

    fun lookupField(
        id: String,
        castToClass: String,
        castToClassReached: Boolean = false
    ): Pair<ClassDefinition, Symbol>? {
        /** Find symbol in class hierarchy for class cast to some supertype - stop at first overridden field */
        // Check if class cast to or class cast from is reached
        val classReached = castToClass == className || castToClassReached
        // Find symbol in class - if null, recursively lookup superclass
        val symbol = symbolTable.lookupCurrentScope(id) ?: return superclass?.lookupField(id, castToClass, classReached)

        // Find corresponding local field or constructor field to check if it overrides
        val localField = getLocalFields().find { id in it.ids }
        val constructorField = getConstructorFields().find { id == it.id }
        val fieldOverrides = localField != null && Modifier.OVERRIDE in localField.modifiers ||
                constructorField != null && Modifier.OVERRIDE == constructorField.modifier

        // Result is found if class has been reached or if an overridden field is found first
        if (classReached || fieldOverrides) {
            return Pair(this, symbol)
        }
        return superclass?.lookupField(id, castToClass, classReached)
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
}