package com.egern.classes

import com.egern.ast.*
import com.egern.symbols.ClassDefinition
import com.egern.symbols.SymbolTable
import com.egern.types.CLASS
import com.egern.visitor.SymbolAwareVisitor

class ClassVisitor(
    symbolTable: SymbolTable,
    classDefinitions: List<ClassDefinition>,
    interfaces: List<InterfaceDecl>
) : SymbolAwareVisitor(symbolTable, classDefinitions, interfaces) {
    override fun preVisit(classDecl: ClassDecl) {
        val currentClass = classDefinitions.find { it.className == classDecl.id }!!
        val superclass = classDefinitions.find { it.className == classDecl.superclass }
        if (superclass != null) currentClass.superclass = superclass
        currentClass.interfaceDecl = interfaces.find { it.id == classDecl.superclass }
    }

    override fun postVisit(castExpr: CastExpr) {
        // Set cast from and to types of cast expression
        castExpr.type as CLASS
        castExpr.type.castTo = castExpr.type.className
        castExpr.type.className = (deriveType(castExpr.expr) as CLASS).className
    }
}
