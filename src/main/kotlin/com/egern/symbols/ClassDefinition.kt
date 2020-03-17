package com.egern.symbols

import com.egern.error.ErrorLogger
import java.lang.Exception

class ClassDefinition(val className: String, val parent: ClassDefinition?) {
    private val fields: MutableList<String> = mutableListOf()
    private val methods: MutableList<String> = mutableListOf()

    fun insertMethod(id: String) {
        // Add symbol if it is does not already exist
        if (id !in methods) {
            methods.add(id)
        } else {
            ErrorLogger.log(Exception("Method $id has already been declared in this class!"))
        }
    }

    fun lookupMethod(id: String): Boolean? {
        // Find symbol in this scope or any parent's
        val foundSymbol = id in methods
        return if (foundSymbol) true else parent?.lookupMethod(id)
    }
}