package com.egern.symbols

import com.egern.ast.FuncDecl
import com.egern.error.ErrorLogger
import java.lang.Exception

class ClassDefinition(val className: String, var superclass: ClassDefinition?) {
    private val methods: MutableList<FuncDecl> = mutableListOf()
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

    fun getMethods(): List<FuncDecl> {
        return (superclass?.getMethods() ?: emptyList()) + methods
    }
}