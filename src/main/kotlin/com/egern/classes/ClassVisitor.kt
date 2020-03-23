package com.egern.classes

import com.egern.ast.ClassDecl
import com.egern.ast.MethodCall
import com.egern.symbols.ClassDefinition
import com.egern.visitor.Visitor

class ClassVisitor(private val classDefinitions: List<ClassDefinition>) : Visitor {
    var currentClass = classDefinitions[0]

    override fun preVisit(classDecl: ClassDecl) {
        currentClass = classDefinitions.find { it.className == classDecl.id }!!
        currentClass.superclass = classDefinitions.find { it.className == classDecl.superclass }!!

    }

    override fun preVisit(methodCall: MethodCall) {
        currentClass.insertMethod(methodCall.methodId)
    }
}