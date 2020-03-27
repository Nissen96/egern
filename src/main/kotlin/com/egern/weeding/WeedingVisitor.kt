package com.egern.weeding

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.visitor.Visitor

class WeedingVisitor : Visitor {
    data class FunctionNode(
        val funcDecl: FuncDecl?,
        val parent: FunctionNode?,
        val children: MutableList<FunctionNode> = mutableListOf(),
        var calledBy: MutableSet<FunctionNode> = mutableSetOf()
    ) {
        override fun toString(): String {
            return "Function ${funcDecl?.id}: $calledBy"
        }

        override fun equals(other: Any?): Boolean {
            if (other !is FunctionNode) return false
            return funcDecl == other.funcDecl
        }

        override fun hashCode(): Int {
            return funcDecl.hashCode()
        }
    }

    private var functionTree = FunctionNode(null, null)  // Function hierarchy with caller info
    private val confirmedCalled: MutableSet<FunctionNode> = mutableSetOf(functionTree)  // Memoize called functions

    private fun allBranchesReturn(stmts: List<Statement>): Boolean {
        /**
         * Check all possible branches of execution contain a return statement
         */
        for (stmt in stmts) {
            when (stmt) {
                is ReturnStmt -> return true  // Return immediately if return is found
                is IfElse -> if (  // Check recursively both the if- and else-part has return
                    stmt.elseBlock != null &&
                    allBranchesReturn(stmt.ifBlock.children.filterIsInstance<Statement>()) &&  // if-part
                    allBranchesReturn(
                        // Handle if-else as a singular list of the if-else statement (simplest)
                        if (stmt.elseBlock is IfElse) listOf(stmt.elseBlock)
                        else (stmt.elseBlock as Block).children.filterIsInstance<Statement>()  // else-part
                    )
                ) return true
            }
        }
        return false
    }

    private fun buildFunctionTree(funcDecls: List<FuncDecl>) {
        /**
         * Build a hierarchy of functions for keeping track of which is called
         */
        funcDecls.forEach {
            functionTree = FunctionNode(it, functionTree)
            functionTree.parent?.children?.add(functionTree)
            buildFunctionTree(it.children.filterIsInstance<FuncDecl>())
            functionTree = functionTree.parent!!
        }
    }


    private fun sweepUnusedFunctions(node: ASTNode) {
        /**
         * Recursively remove all unused functions in each scope, starting from the outermost
         * For each function, check it is part of a call chain starting from a previously confirmed used function
         * If not, remove it immediately to prevent it being visited, automatically removing its nested functions
         */
        val usedFunctions = functionTree.children.filter { functionIsUsed(it) }

        if (node is Program) {
            val nonFunctions = node.children.filterNot { it is FuncDecl }
            node.children = nonFunctions + usedFunctions.map { it.funcDecl!! } as List<ASTNode>
        } else if (node is FuncDecl) {
            val nonFunctions = node.children.filterNot { it is FuncDecl }
            node.children = nonFunctions + usedFunctions.map { it.funcDecl!! } as List<ASTNode>
        }

        usedFunctions.forEach {
            functionTree = it
            sweepUnusedFunctions(it.funcDecl!!)
            functionTree = functionTree.parent!!
        }
    }

    private fun functionIsUsed(func: FunctionNode): Boolean {
        // Function is used if any call chain involving it contains a previously confirmed used function
        return if (func in confirmedCalled) {
            confirmedCalled.add(func)
            true
        } else {
            func.calledBy.any { functionIsUsed(it) }
        }
    }

    override fun preVisit(program: Program) {
        buildFunctionTree(program.children.filterIsInstance<FuncDecl>())
    }

    override fun postVisit(program: Program) {
        sweepUnusedFunctions(program)
    }

    override fun preVisit(funcDecl: FuncDecl) {
        functionTree = functionTree.children.find { it.funcDecl == funcDecl }!!
        if (!allBranchesReturn(funcDecl.children.filterIsInstance<Statement>())) {
            ErrorLogger.log(funcDecl, "Function must always return a value")
        }
    }

    override fun postVisit(funcDecl: FuncDecl) {
        functionTree = functionTree.parent!!
    }

    override fun postVisit(funcCall: FuncCall) {
        // Find the corresponding function in the function hierarchy
        var currentFunc: FunctionNode? = functionTree
        while (currentFunc != null) {
            currentFunc.children.forEach {
                if (it.funcDecl?.id == funcCall.id) {
                    it.calledBy.add(functionTree)
                    return
                }
            }

            currentFunc = currentFunc.parent
        }
    }
}