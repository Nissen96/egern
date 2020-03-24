package com.egern.symbols

import com.egern.ast.FieldDecl
import com.egern.ast.FuncDecl
import com.egern.error.ErrorLogger
import java.lang.Exception

class ClassDefinition(val className: String, var superclass: ClassDefinition?) {
    private val methods: MutableList<FuncDecl> = mutableListOf()
    private val localFields: MutableList<FieldDecl> = mutableListOf()
    var vTableOffset: Int = -1
    var numFields: Int = 0
    lateinit var symbolTable: SymbolTable

    fun insertMethod(methodDecl: FuncDecl) {
        // Add symbol if it is does not already exist
        if (methodDecl !in methods) {
            methods.add(methodDecl)
        } else {
            ErrorLogger.log(Exception("Method ${methodDecl.id} has already been declared in this class!"))
        }
    }

    fun insertField(fieldDecl: FieldDecl) {
        // Add symbol if it is does not already exist
        if (fieldDecl !in localFields) {
            localFields.add(fieldDecl)
        } else {
            ErrorLogger.log(Exception("Field has already been declared in this class!"))
        }
    }

    fun getMethods(): List<FuncDecl> {
        return (superclass?.getMethods() ?: emptyList()) + methods
    }

    fun getLocalFields(): List<FieldDecl> {
        return localFields
    }

    fun getFields(): List<FieldDecl> {
        return (superclass?.getFields() ?: emptyList()) + localFields
    }
}