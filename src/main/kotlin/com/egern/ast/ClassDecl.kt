package com.egern.ast

import com.egern.types.ExprType
import com.egern.visitor.Visitor

class ClassDecl(
    val id: String,
    val constructor: List<Pair<String, ExprType>>,
    val superclass: String?,
    val superclassArgs: List<Expr>?,
    val fieldDecls: List<FieldDecl>,
    val methods: List<FuncDecl>,
    lineNumber: Int? = null, charPosition: Int? = null
) : ASTNode(lineNumber, charPosition) {
    lateinit var endLabel: String

    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        /*if (superclassArgs.isNotEmpty()) {
            superclassArgs.dropLast(1).map {
                it.accept(visitor)
                visitor.midSuperclassArgVisit(this)
            }
            superclassArgs.last().accept(visitor)
        }
        visitor.preMidVisit(this)
         */
        fieldDecls.forEach {
            it.accept(visitor)
        }
        visitor.postMidVisit(this)
        methods.forEach {
            it.accept(visitor)
        }
        visitor.postVisit(this)
    }
}