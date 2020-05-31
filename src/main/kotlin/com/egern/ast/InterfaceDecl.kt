package com.egern.ast

import com.egern.visitor.Visitor

class InterfaceDecl(
    val id: String,
    val methodSignatures: List<MethodSignature>,
    lineNumber: Int = -1,
    charPosition: Int = -1
) : ASTNode(lineNumber, charPosition) {
    override fun accept(visitor: Visitor) {
        visitor.preVisit(this)
        methodSignatures.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }
}