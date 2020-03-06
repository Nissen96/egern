package com.egern.error

import com.egern.ast.ASTNode
import java.lang.Exception

class ErrorLogger {
    companion object {
        private val errors = ArrayList<Exception>()

        fun hasErrors(): Boolean {
            return errors.isNotEmpty()
        }

        fun log(node: ASTNode, message: String) {
            log(Exception("$message at line ${node.lineNumber} char ${node.charPosition}"))
        }

        fun log(exception: Exception) {
            errors.add(exception)
        }

        fun print() {
            for (error in errors) {
                println(error.toString())
            }
        }
    }
}