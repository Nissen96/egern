package com.egern.labels

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.visitor.Visitor

class LabelGenerationVisitor : Visitor() {
    var currentLoop: WhileLoop? = null

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
        currentLoop = whileLoop
    }

    override fun postVisit(whileLoop: WhileLoop) {
        currentLoop = null
    }

    override fun visit(continueStmt: ContinueStmt) {
        if (currentLoop != null) {
            continueStmt.jumpLabel = currentLoop!!.startLabel
        } else {
            ErrorLogger.log(continueStmt, "Continue invalid outside loop")
        }
    }

    override fun visit(breakStmt: BreakStmt) {
        if (currentLoop != null) {
            breakStmt.jumpLabel = currentLoop!!.endLabel
        } else {
            ErrorLogger.log(breakStmt, "Break invalid outside loop")
        }
    }
}
