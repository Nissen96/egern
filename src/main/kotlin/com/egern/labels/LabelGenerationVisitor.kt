package com.egern.labels

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.util.*
import com.egern.visitor.Visitor

class LabelGenerationVisitor : Visitor() {
    private val loopStack = stackOf<WhileLoop>()

    override fun preVisit(funcDecl: FuncDecl) {
        funcDecl.startLabel = LabelGenerator.nextLabel(funcDecl.id)
        funcDecl.endLabel = funcDecl.startLabel + "_end"
    }

    override fun preVisit(ifElse: IfElse) {
        ifElse.elseLabel = LabelGenerator.nextLabel("if_else")
        ifElse.endLabel = LabelGenerator.nextLabel("if_end")
    }

    override fun preVisit(classDecl: ClassDecl) {
        classDecl.endLabel = LabelGenerator.nextLabel(classDecl.id + "_end")
    }

    override fun preVisit(whileLoop: WhileLoop) {
        whileLoop.startLabel = LabelGenerator.nextLabel("while_start")
        whileLoop.endLabel = LabelGenerator.nextLabel("while_end")
        loopStack.push(whileLoop)
    }

    override fun postVisit(whileLoop: WhileLoop) {
        loopStack.pop()
    }

    override fun visit(continueStmt: ContinueStmt) {
        if (loopStack.isNotEmpty()) {
            continueStmt.jumpLabel = loopStack.peek()!!.startLabel
        } else {
            ErrorLogger.log(continueStmt, "Continue invalid outside loop")
        }
    }

    override fun visit(breakStmt: BreakStmt) {
        if (loopStack.isNotEmpty()) {
            breakStmt.jumpLabel = loopStack.peek()!!.endLabel
        } else {
            ErrorLogger.log(breakStmt, "Break invalid outside loop")
        }
    }
}
