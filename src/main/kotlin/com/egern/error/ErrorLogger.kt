package com.egern.error

import java.lang.Exception

class ErrorLogger {
    companion object {
        private val errors = ArrayList<Exception>()

        fun hasErrors(): Boolean {
            return errors.isNotEmpty()
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