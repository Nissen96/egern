package com.egern.classes

import com.egern.ast.*
import com.egern.symbols.ClassDefinition
import com.egern.visitor.Visitor

class ClassVisitor(
    val classDefinitions: List<ClassDefinition>,
    val interfaces: List<InterfaceDecl>
) : Visitor() {
    override fun preVisit(classDecl: ClassDecl) {
        val currentClass = classDefinitions.find { it.className == classDecl.id }!!
        val superclass = classDefinitions.find { it.className == classDecl.superclass }
        if (superclass != null) currentClass.superclass = superclass
        currentClass.interfaceDecl = interfaces.find { it.id == classDecl.superclass }
    }
}
