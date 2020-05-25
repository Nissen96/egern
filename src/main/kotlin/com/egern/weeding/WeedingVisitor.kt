package com.egern.weeding

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.visitor.Visitor

class WeedingVisitor : Visitor() {
    data class FunctionNode(
        val funcDecl: FuncDecl?,
        val parent: FunctionNode?,
        val children: MutableList<FunctionNode> = mutableListOf(),
        var calledBy: MutableSet<FunctionNode> = mutableSetOf()
    ) {
        override fun equals(other: Any?): Boolean {
            if (other !is FunctionNode) return false
            return funcDecl == other.funcDecl
        }

        override fun hashCode(): Int {
            return funcDecl.hashCode()
        }
    }

    private var functionTree = FunctionNode(null, null)  // Function hierarchy with caller info
    private var loopLevel = 0

    private fun allBranchesReturn(stmts: List<Statement>): Boolean {
        /**
         * Check all possible branches of execution contain a return statement
         */
        for (stmt in stmts) {
            when (stmt) {
                is ReturnStmt -> return true  // Return immediately if return is found
                is IfElse -> if (  // Check recursively both the if- and else-part has return
                    stmt.elseBlock != null &&
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

    private fun buildFunctionTree(funcDecls: List<FuncDecl>) {
        /**
         * Build hierarchy of nested functions
         */
        funcDecls.forEach {
            functionTree = FunctionNode(it, functionTree)
            functionTree.parent?.children?.add(functionTree)
            buildFunctionTree(it.funcDecls)
            functionTree = functionTree.parent!!
        }
    }


    private fun sweepUnusedFunctions(node: ASTNode) {
        /**
         * Recursively remove all unused functions in each scope, starting from the outermost
         * For each function, check it is part of a call chain starting from a previously confirmed used function
         * If not, remove it immediately to prevent it being visited, automatically removing its nested functions
         */
        // Remove unsed functions
        val usedFunctions = functionTree.children.filter { functionIsUsed(it) }
        if (node is Program) {
            node.funcDecls = usedFunctions.map { it.funcDecl!! }
        } else if (node is FuncDecl) {
            node.funcDecls = usedFunctions.map { it.funcDecl!! }
        }

        // Recursively check nested functions
        usedFunctions.forEach {
            functionTree = it
            sweepUnusedFunctions(it.funcDecl!!)
            functionTree = functionTree.parent!!
        }
    }

    private fun functionIsUsed(func: FunctionNode): Boolean {
        /**
         * Checks if the current function is the direct caller of the input function or any function in its call chain
         * The functionTree is the current scope being checked and the input function one of its children (nested)
         * The current function is already confirmed called, so if it starts a call chain containing the input function,
         * this function is surely at some point called
         */
        return func.funcDecl == functionTree.funcDecl || func.calledBy.any { functionIsUsed(it) }
    }

    override fun preVisit(program: Program) {
        buildFunctionTree(program.funcDecls)
    }

    override fun postVisit(program: Program) {
        sweepUnusedFunctions(program)
    }

    override fun preVisit(funcDecl: FuncDecl) {
        if (!funcDecl.isMethod) {
            // Find and set function node corresponding to this function
            functionTree = functionTree.children.find { it.funcDecl == funcDecl }!!
        }

        // Check all branches of function returns
        if (!allBranchesReturn(funcDecl.stmts.filterIsInstance<Statement>())) {
            ErrorLogger.log(funcDecl, "Function must always return a value")
        }
    }

    override fun postVisit(funcDecl: FuncDecl) {
        if (!funcDecl.isMethod) functionTree = functionTree.parent!!
    }

    override fun postVisit(funcCall: FuncCall) {
        // Find the corresponding function in the function hierarchy
        var currentFunc: FunctionNode? = functionTree
        while (currentFunc != null) {
            currentFunc.children.forEach {
                if (it.funcDecl?.id == funcCall.id) {
                    // Add the current function as a caller of the called, building a call tree
                    it.calledBy.add(functionTree)
                    return
                }
            }

            currentFunc = currentFunc.parent
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
