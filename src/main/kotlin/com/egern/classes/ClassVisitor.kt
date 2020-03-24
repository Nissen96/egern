package com.egern.classes

import com.egern.ast.ClassDecl
import com.egern.ast.FuncDecl
import com.egern.ast.VarDecl
import com.egern.symbols.ClassDefinition
import com.egern.visitor.Visitor

class ClassVisitor(val classDefinitions: List<ClassDefinition>) : Visitor {
    var currentClass = classDefinitions[0]

    override fun preVisit(classDecl: ClassDecl) {
        currentClass = classDefinitions.find { it.className == classDecl.id }!!
        currentClass.superclass = classDefinitions.find { it.className == classDecl.superclass }!!
    }

    override fun preVisit(funcDecl: FuncDecl) {
        if (funcDecl.classId != null) {
            currentClass.insertMethod(funcDecl)
        }
    }

    override fun preVisit(varDecl: VarDecl<*>) {
        if (varDecl.classId != null) {
            currentClass.insertField(varDecl)
        }
    }
}