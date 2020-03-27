package com.egern.weeding

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.visitor.Visitor

class WeedingVisitor : Visitor {
    data class FunctionNode(
        val funcDecl: FuncDecl?,
        val parent: FunctionNode?,
        val children: MutableList<FunctionNode> = mutableListOf(),
        var isCalled: Boolean = false
    ) {
        override fun toString(): String {
            return "${funcDecl?.id}: $isCalled"
        }
    }

    private var functionTree = FunctionNode(null, null)

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

    private fun markUsedFunctions(nodes: List<ASTNode>) {
        /**
         * Follow every function call to the corresponding function, marking it as used
         */
        val funcCalls = nodes.filterIsInstance<FuncCall>()

        // For each function call, mark the corresponding function as used
        funcCalls.forEach { funcCall ->
            var currentFunc: FunctionNode? = functionTree
            var calledFuncNode: FunctionNode? = null
            while (currentFunc != null) {
                for (func in currentFunc.children) {
                    // Search for corresponding function declaration - skip already called functions
                    if (func.funcDecl?.id == funcCall.id && !func.isCalled) {
                        func.isCalled = true
                        calledFuncNode = func
                        break
                    }
                }

                // Recursively visit called function
                if (calledFuncNode != null) {
                    functionTree = calledFuncNode
                    markUsedFunctions(calledFuncNode.funcDecl!!.children)
                    functionTree = calledFuncNode.parent!!
                }

                currentFunc = currentFunc.parent
            }
        }
    }

    private fun sweepUnusedFunctions(node: ASTNode) {
        /**
         * Remove all functions which have note been marked as used in the function tree
         */
        val usedFunctions = functionTree.children.filter { it.isCalled }

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

    override fun preVisit(program: Program) {
        // Mark and sweep unused functions
        println(program.children)
        buildFunctionTree(program.children.filterIsInstance<FuncDecl>())
        println(functionTree.children)
        markUsedFunctions(program.children)
        println(functionTree.children)
        sweepUnusedFunctions(program)
        println(program.children)
    }

    override fun preVisit(funcDecl: FuncDecl) {
        if (!allBranchesReturn(funcDecl.children.filterIsInstance<Statement>())) {
            ErrorLogger.log(funcDecl, "Function must always return a value")
        }
    }
}