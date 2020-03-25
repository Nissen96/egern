package com.egern.weeding

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.visitor.Visitor

class WeedingVisitor : Visitor {
    private fun allBranchesReturn(stmts: List<Statement>): Boolean {
        /**
         * Check all possible branches of execution contain a return statement
         */
        var returnFound = false
        for (stmt in stmts) {
            when (stmt) {
                is ReturnStmt -> return true  // Return immediately if return is found
                is IfElse -> returnFound = (  // Check recursively both the if- and else-part has return
                        stmt.elseBlock != null &&
                                allBranchesReturn(stmt.ifBlock.children.filterIsInstance<Statement>()) &&  // if-part
                                allBranchesReturn(
                                    // Handle if-else as a singular list of the if-else statement (simplest)
                                    if (stmt.elseBlock is IfElse) listOf(stmt.elseBlock)
                                    else (stmt.elseBlock as Block).children.filterIsInstance<Statement>()  // else-part
                                )
                        )
            }
            if (returnFound) {
                return true
            }
        }
        return false
    }

    override fun preVisit(funcDecl: FuncDecl) {
        if (!allBranchesReturn(funcDecl.children.filterIsInstance<Statement>())) {
            ErrorLogger.log(funcDecl, "Function must always return a value")
        }
    }
}