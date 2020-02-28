package com.egern.codegen

import com.egern.ast.FuncDecl
import com.egern.ast.IfElse
import com.egern.ast.WhileLoop
import com.egern.visitor.Visitor

class PreCodeGenerationVisitor : Visitor {
    override fun preVisit(funcDecl: FuncDecl) {
        funcDecl.startLabel = LabelGenerator.nextLabel(funcDecl.id)
        funcDecl.endLabel = funcDecl.startLabel + "_end"
    }

    override fun preVisit(ifElse: IfElse) {
        ifElse.elseLabel = LabelGenerator.nextLabel("if_else")
        ifElse.endLabel = LabelGenerator.nextLabel("if_end")
    }

    override fun preVisit(whileLoop: WhileLoop) {
        whileLoop.startLabel = LabelGenerator.nextLabel("while_start")
        whileLoop.endLabel = LabelGenerator.nextLabel("while_end")
    }
}
