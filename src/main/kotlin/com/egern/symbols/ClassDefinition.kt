package com.egern.symbols

import com.egern.ast.FuncDecl
import com.egern.ast.VarDecl
import com.egern.error.ErrorLogger
import java.lang.Exception

class ClassDefinition(val className: String, var superclass: ClassDefinition?) {
    private val methods: MutableList<FuncDecl> = mutableListOf()
    private val localFields: MutableList<VarDecl<*>> = mutableListOf()
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

    fun insertField(varDecl: VarDecl<*>) {
        // Add symbol if it is does not already exist
        if (varDecl !in localFields) {
            localFields.add(varDecl)
        } else {
            ErrorLogger.log(Exception("Field has already been declared in this class!"))
        }
    }

    fun getMethods(): List<FuncDecl> {
        return (superclass?.getMethods() ?: emptyList()) + methods
    }

    fun getFields(): List<VarDecl<*>> {
        return (superclass?.getFields() ?: emptyList()) + localFields
    }
}