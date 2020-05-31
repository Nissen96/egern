package com.egern.weeding

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.visitor.Visitor

class WeedingVisitor : Visitor() {
    private var loopLevel = 0

    private fun allBranchesReturn(stmts: List<Statement>): Boolean {
        /**
         * Check all possible branches of execution end in a return statement
         */
        for (stmt in stmts) {
            when (stmt) {
                is ReturnStmt -> return true  // Return immediately if return is found
                is IfElse -> if (  // Check recursively both the if- and else-part has return
                    stmt.elseBlock != null &&  // No need to check if-block if else-block does not exist
                    allBranchesReturn(stmt.ifBlock.stmts.filterIsInstance<Statement>()) &&  // if-part
                    allBranchesReturn(
                        if (stmt.elseBlock is IfElse) listOf(stmt.elseBlock)  // else-if
                        else (stmt.elseBlock as Block).stmts.filterIsInstance<Statement>()  // else-part
                    )
                ) return true
            }
        }
        return false
    }

    override fun preVisit(funcDecl: FuncDecl) {
        // Check all branches of function returns
        if (!allBranchesReturn(funcDecl.stmts.filterIsInstance<Statement>())) {
            ErrorLogger.log(funcDecl, "Function must always return a value")
        }
    }

    override fun preVisit(whileLoop: WhileLoop) {
        loopLevel++
    }

    override fun postVisit(whileLoop: WhileLoop) {
        loopLevel--
    }

    override fun visit(continueStmt: ContinueStmt) {
        if (loopLevel <= 0) {
            ErrorLogger.log(continueStmt, "Continue invalid outside loop")
        }
    }

    override fun visit(breakStmt: BreakStmt) {
        if (loopLevel <= 0) {
            ErrorLogger.log(breakStmt, "Break invalid outside loop")
        }
    }
}
