package com.egern.ast

import com.egern.visitor.Visitor

class InterfaceDecl(
    val id: String,
    val methodSignatures: List<MethodSignature>,
    lineNumber: Int? = null,
    charPosition: Int? = null
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        methodSignatures.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}