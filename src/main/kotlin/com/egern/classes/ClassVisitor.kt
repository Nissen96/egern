package com.egern.classes

import com.egern.ast.*
import com.egern.symbols.ClassDefinition
import com.egern.symbols.SymbolTable
import com.egern.visitor.SymbolAwareVisitor

class ClassVisitor(
    symbolTable: SymbolTable,
    classDefinitions: MutableList<ClassDefinition>,
    val interfaces: List<InterfaceDecl>
) : SymbolAwareVisitor(symbolTable, classDefinitions) {
    override fun preVisit(classDecl: ClassDecl) {
        val currentClass = classDefinitions.find { it.className == classDecl.id }!!
        val superclass = classDefinitions.find { it.className == classDecl.superclass }
        if (superclass != null) currentClass.superclass = superclass
        currentClass.interfaceDecl = interfaces.find { it.id == classDecl.superclass }
    }
}
