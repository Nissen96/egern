package com.egern.classes

import com.egern.ast.*
import com.egern.symbols.ClassDefinition
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolType
import com.egern.visitor.Visitor

class ClassVisitor(val classDefinitions: List<ClassDefinition>) : Visitor {
    override fun preVisit(classDecl: ClassDecl) {
        val currentClass = classDefinitions.find { it.className == classDecl.id }!!
        currentClass.superclass = classDefinitions.find { it.className == classDecl.superclass }!!
    }
}
