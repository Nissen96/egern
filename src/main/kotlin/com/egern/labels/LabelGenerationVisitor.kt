package com.egern.labels

import com.egern.ast.*
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
        continueStmt.jumpLabel = currentLoop?.startLabel ?: throw Exception("Continue outside loop not allowed")
    }

    override fun visit(breakStmt: BreakStmt) {
        breakStmt.jumpLabel = currentLoop?.endLabel ?: throw Exception("Continue outside loop not allowed")
    }
}
