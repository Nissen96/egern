package com.egern.ast

import com.egern.types.ExprType
import com.egern.visitor.Visitor

class MethodSignature(
    val id: String,
    val params: List<ExprType>,
    val returnType: ExprType,
    lineNumber: Int, charPosition: Int
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.visit(this)
    }
}