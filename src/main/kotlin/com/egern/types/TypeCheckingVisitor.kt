package com.egern.types

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.symbols.ClassDefinition
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.util.*
import com.egern.visitor.SymbolAwareVisitor
import java.lang.Exception

class TypeCheckingVisitor(
    symbolTable: SymbolTable,
    classDefinitions: List<ClassDefinition>,
    interfaces: List<InterfaceDecl>
) : SymbolAwareVisitor(symbolTable, classDefinitions, interfaces) {
    private val functionStack = stackOf<FuncDecl>()
    private var currentClass: ClassDefinition? = null

    override fun preVisit(block: Block) {
        symbolTable = block.symbolTable
    }

    override fun postVisit(block: Block) {
        symbolTable = symbolTable.parent ?: throw Exception("No more scopes -- please buy another")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        symbolTable = funcDecl.symbolTable
        functionStack.push(funcDecl)

        // Handle methods
        if (funcDecl.isMethod) {
            checkMethodDecl(funcDecl)
        }
    }

    override fun postVisit(funcDecl: FuncDecl) {
        symbolTable = symbolTable.parent ?: throw Exception("No more scopes -- please buy another")
        functionStack.pop()
    }

    private fun checkMethodDecl(methodDecl: FuncDecl) {
        // Check modifiers for the method itself
        val methodOverrides = Modifier.OVERRIDE in methodDecl.modifiers
        if (methodOverrides && Modifier.STATIC in methodDecl.modifiers) {
            ErrorLogger.log(methodDecl, "Static methods cannot be overridden")
        }

        // Check if method overrides any static methods or overrides without the override modifier
        val foundMethod = currentClass!!.superclass!!.lookupMethod(methodDecl.id)
        if (foundMethod == null) {
            // Check if method overrides from interface
            val foundInterfaceMethod = currentClass!!.getInterface()?.methodSignatures?.find { it.id == methodDecl.id }
            if (foundInterfaceMethod == null && methodOverrides) {
                ErrorLogger.log(methodDecl, "Method ${methodDecl.id} overrides nothing")
            }
            return
        }


        val (className, superMethod) = foundMethod
        if (Modifier.STATIC in superMethod.modifiers) {
            ErrorLogger.log(
                methodDecl,
                "Override in class ${currentClass!!.className} of static method ${methodDecl.id} from class $className"
            )
        } else if (!methodOverrides) {
            ErrorLogger.log(
                methodDecl,
                "Override in class ${currentClass!!.className} of method ${methodDecl.id} from class $className without override modifier"
            )
        }
    }

    override fun postVisit(funcCall: FuncCall) {
        val sym = lookupSymbol(funcCall.id, listOf(SymbolType.Function))
        val funcDecl = sym.info["funcDecl"] as? FuncDecl
        if (funcDecl == null) {
            ErrorLogger.log(funcCall, "Invalid function call")
            return
        }

        val nArgs = funcCall.args.size
        val nParams = funcDecl.params.size
        if (nArgs != nParams) {
            ErrorLogger.log(
                funcCall,
                "Wrong number of arguments to function ${funcCall.id} - $nArgs passed, $nParams expected"
            )
        }
        funcCall.args.take(nParams).forEachIndexed { index, arg ->
            val argType = deriveType(arg)
            val paramType = funcDecl.params[index].type
            if (paramType is CLASS) {
                val elementClass = (argType as CLASS).className
                val elementSuperclasses = classDefinitions.find { it.className == elementClass }!!.getSuperclasses()
                if (paramType.className !in elementSuperclasses) {
                    ErrorLogger.log(
                        arg,
                        "Argument ${index + 1} is of type ${typeString(argType)} is not a subclass " +
                                "of expected type ${typeString(paramType)}"
                    )
                }
            } else if (argType != paramType) {
                ErrorLogger.log(
                    arg,
                    "Argument ${index + 1} is of type ${typeString(argType)} but ${typeString(paramType)} was expected"
                )
            }

            if (arg !is IdExpr && (argType is ARRAY || argType is CLASS)) {
                ErrorLogger.log(
                    arg,
                    "Passing references directly is currently not supported"
                )
            }
        }
    }

    override fun preVisit(varAssign: VarAssign) {
        // Check variable ids
        val allIds = varAssign.ids
        allIds.forEach { lookupSymbol(it, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field)) }

        val exprType = deriveType(varAssign.expr)
        if (exprType == VOID) {
            ErrorLogger.log(varAssign, "Assigning void is invalid")
        }

        // For normal IDs, expression type must match declared variable
        varAssign.ids.forEach {
            val varType = getVariableType(it)
            if (varType != exprType) {
                ErrorLogger.log(
                    varAssign,
                    "Assigning expression of type ${typeString(exprType)} " +
                            "to variable of type ${typeString(varType)} is invalid"
                )
            }
        }

        // For inserting into arrays, expression type must match type of element at array index
        varAssign.indexExprs.forEach {
            val elementType = deriveType(it)
            if (elementType != exprType) {
                ErrorLogger.log(
                    varAssign,
                    "Assigning expression of type ${typeString(exprType)} " +
                            "to array element of type ${typeString(elementType)} is invalid"
                )
            }
        }

        // For class fields, expression type must match declared field
        varAssign.classFields.forEach { classField ->
            val fieldType = deriveType(classField)
            if (fieldType != exprType) {
                ErrorLogger.log(
                    varAssign,
                    "Assigning expression of type ${typeString(exprType)} " +
                            "to class field of type ${typeString(fieldType)} is invalid"
                )
            }
        }
    }

    override fun postVisit(varDecl: VarDecl) {
        val type = deriveType(varDecl.expr)
        if (type == VOID) {
            ErrorLogger.log(varDecl, "Declaring a variable of type void is invalid")
        }
    }

    override fun visit(classField: ClassField) {
        val callerClass = getObjectClass(classField.objectId)
        val classDefinition = classDefinitions.find { it.className == callerClass.className }
            ?: throw Exception("Class ${callerClass.className} not defined")

        // Check if field exists
        val fieldDecl = classDefinition.lookupLocalField(classField.fieldId)
        val constructorField = classDefinition.lookupConstructorField(classField.fieldId)

        if (fieldDecl == null && constructorField == null) {
            ErrorLogger.log(
                classField,
                "Field '${classField.fieldId}' not defined for instance '${classField.objectId}' " +
                        "of class ${callerClass.className}"
            )
            // Check no static field is referenced directly by instances - only classes
        } else if (classField is StaticClassField && fieldDecl != null && Modifier.STATIC in fieldDecl.modifiers) {
            ErrorLogger.log(
                classField,
                "Invalid reference of static class field by instance"
            )
        }
    }

    override fun postVisit(printStmt: PrintStmt) {
        if (printStmt.expr != null && deriveType(printStmt.expr) == VOID) {
            ErrorLogger.log(printStmt.expr, "Printing void is invalid")
        }
    }

    override fun postVisit(lenExpr: LenExpr) {
        val exprType = deriveType(lenExpr.expr)
        if (exprType !is ARRAY) {
            ErrorLogger.log(lenExpr.expr, "Len function is undefined for type ${typeString(exprType)}")
        }
    }

    override fun visit(idExpr: IdExpr) {
        val symbol = lookupSymbol(idExpr.id, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field))

        // No non-static fields allowed in static methods
        if (symbol.type == SymbolType.Field) {
            val method = functionStack.peek()!!
            val fieldDecl = currentClass!!.getLocalFields().find { idExpr.id in it.ids }

            if (Modifier.STATIC in method.modifiers && (fieldDecl == null || Modifier.STATIC !in fieldDecl.modifiers)) {
                ErrorLogger.log(idExpr, "Non-static field not allowed in static method")
            }
        }
    }

    override fun postVisit(returnStmt: ReturnStmt) {
        if (functionStack.peek() != null && returnStmt.expr != null) {
            val returnType = functionStack.peek()!!.returnType
            val exprType = deriveType(returnStmt.expr)
            if (exprType != returnType) {
                ErrorLogger.log(
                    returnStmt,
                    "Invalid return type: ${typeString(exprType)} - expected: ${typeString(returnType)}"
                )
            }
        }
    }

    override fun postVisit(booleanOpExpr: BooleanOpExpr) {
        val exprType = deriveType(booleanOpExpr)
        val lhsType = deriveType(booleanOpExpr.lhs)
        val rhsType = booleanOpExpr.rhs?.let { deriveType(it) }

        if (lhsType != exprType) {
            ErrorLogger.log(
                booleanOpExpr,
                "Type mismatch on boolean operator${if (rhsType != null) " - LHS" else ""}: ${typeString(lhsType)}"
            )
        }
        if (rhsType != null && rhsType != exprType) {
            ErrorLogger.log(booleanOpExpr, "Type mismatch on boolean operator - RHS: ${typeString(rhsType)}")
        }
    }

    override fun postVisit(arithExpr: ArithExpr) {
        val exprType = deriveType(arithExpr)
        val lhsType = deriveType(arithExpr.lhs)
        val rhsType = deriveType(arithExpr.rhs)

        if (lhsType != exprType) {
            ErrorLogger.log(arithExpr, "Type mismatch on arithmetic operator - LHS: ${typeString(lhsType)}")
        }
        if (rhsType != exprType) {
            ErrorLogger.log(arithExpr, "Type mismatch on arithmetic operator - RHS: ${typeString(rhsType)}")
        }
    }

    override fun postVisit(compExpr: CompExpr) {
        val lhsType = deriveType(compExpr.lhs)
        if (compExpr.op !in CompOp.validOperators(lhsType)) {
            ErrorLogger.log(compExpr.lhs, "${compExpr.op.value} operation not defined on type ${typeString(lhsType)}")
        }

        val rhsType = deriveType(compExpr.rhs)
        if (compExpr.op !in CompOp.validOperators(rhsType)) {
            ErrorLogger.log(compExpr.rhs, "${compExpr.op.value} operation not defined on type ${typeString(rhsType)}")
        }

        if (lhsType != rhsType) {
            ErrorLogger.log(
                compExpr,
                "Type mismatch on comparative operator - LHS: ${typeString(lhsType)}, RHS: ${typeString(rhsType)}"
            )
        }
    }

    override fun postVisit(rangeExpr: RangeExpr) {
        val exprType = deriveType(rangeExpr) as ARRAY
        val lhsType = deriveType(rangeExpr.lhs)
        val rhsType = deriveType(rangeExpr.rhs)

        if (lhsType != exprType.innerType) {
            ErrorLogger.log(rangeExpr, "Type mismatch on range operator - LHS: ${typeString(lhsType)}")
        }
        if (rhsType != exprType.innerType) {
            ErrorLogger.log(rangeExpr, "Type mismatch on range operator - RHS: ${typeString(rhsType)}")
        }
    }

    override fun postVisit(arrayIndexExpr: ArrayIndexExpr) {
        val arrayType = deriveType(arrayIndexExpr.id) as ARRAY
        if (arrayIndexExpr.indices.size > arrayType.depth) {
            ErrorLogger.log(arrayIndexExpr, "Indexing too deeply into array of ${arrayType.depth} dimensions")
        }

        arrayIndexExpr.indices.forEach {
            if (deriveType(it) !is INT) {
                ErrorLogger.log(it, "Index must be an integer value")
            }
        }
    }

    override fun postVisit(arrayExpr: ArrayExpr) {
        arrayExpr.entries.forEach {
            val elementType = deriveType(it)
            if (it !is IdExpr && (elementType is ARRAY || elementType is CLASS)) {
                ErrorLogger.log(
                    it,
                    "Instantiating reference elements directly in array is currently not supported"
                )
            }
        }

        // Ensure all array elements match the array type
        val arrayType = deriveType(arrayExpr) as ARRAY
        val arrayInnerType = arrayType.innerType
        arrayExpr.entries.forEachIndexed { index, element ->
            var logError = false
            var elementType = deriveType(element)

            // Nested arrays must match on depth
            if (arrayType.depth > 1) {
                if (elementType !is ARRAY || elementType.depth != arrayType.depth - 1) {
                    logError = true
                }
                elementType = (elementType as ARRAY).innerType
            }

            // Elements of object arrays must just share a common superclass
            if (arrayInnerType is CLASS) {
                val elementClass = (elementType as CLASS).className
                val elementSuperclasses = classDefinitions.find { it.className == elementClass }!!.getSuperclasses()
                if (arrayInnerType.className !in elementSuperclasses) {
                    logError = true
                }
            } else if (elementType != arrayInnerType && elementType != VOID && arrayInnerType != VOID) {
                // Element inner type and array inner type must match or one of them be void (empty list)
                logError = true
            }

            if (logError) {
                ErrorLogger.log(
                    element,
                    "Type mismatch in array at position $index - element type: ${typeString(elementType)}, " +
                            "expected type: ${typeString(arrayType.innerType)}"
                )
            }
        }
    }

    override fun postVisit(arrayOfSizeExpr: ArrayOfSizeExpr) {
        // Size must be integer
        if (deriveType(arrayOfSizeExpr.size) != INT) {
            ErrorLogger.log(
                arrayOfSizeExpr,
                "Array size must be an integer"
            )
        }
    }

    override fun preVisit(classDecl: ClassDecl) {
        currentClass = classDefinitions.find { classDecl.id == it.className }!!
        symbolTable = currentClass!!.symbolTable
    }

    override fun postVisit(classDecl: ClassDecl) {
        // Check constructor fields only has override modifier
        classDecl.constructor.forEach {
            if (it.modifier != null && it.modifier != Modifier.OVERRIDE) {
                ErrorLogger.log(
                    classDecl,
                    "Invalid modifier for constructor field. Only override allowed"
                )
            }
        }

        // Check class overrides all interface methods
        val classInterface = currentClass!!.interfaceDecl
        classInterface?.methodSignatures?.forEach {
            var foundMethod: FuncDecl? = null

            classDecl.methods.forEach { method ->
                if (it.id == method.id) {
                    foundMethod = method

                    // Check signature (parameter and return types)
                    val methodParams = method.params.drop(1).map { param -> param.type }

                    if (it.params.size != methodParams.size) {
                        ErrorLogger.log(
                            method,
                            "Overridden method has the wrong number of parameters: " +
                                    "${methodParams.size} - expected: ${it.params.size}"
                        )
                    } else {
                        it.params.zip(methodParams).forEach { param ->
                            if (param.first != param.second) {
                                ErrorLogger.log(
                                    method,
                                    "Overridden method parameter has wrong type: " +
                                            "${typeString(param.first)} - expected: ${typeString(param.second)}"
                                )
                            }
                        }
                    }

                    if (it.returnType != method.returnType) {
                        ErrorLogger.log(
                            method,
                            "Overridden method has invalid return type: " +
                                    "${typeString(method.returnType)} - expected: ${typeString(it.returnType)}"
                        )
                    }
                }
            }

            if (foundMethod == null || Modifier.OVERRIDE !in foundMethod!!.modifiers) {
                ErrorLogger.log(classDecl, "Method ${it.id} not implemented")
            }
        }

        symbolTable = symbolTable.parent ?: throw Exception("No more scopes -- please buy another")
    }

    override fun postVisit(fieldDecl: FieldDecl) {
        // Check modifiers for the field itself
        val fieldOverrides = Modifier.OVERRIDE in fieldDecl.modifiers
        if (fieldOverrides && Modifier.STATIC in fieldDecl.modifiers) {
            ErrorLogger.log(fieldDecl, "A field cannot be declared both static and overridden")
        }

        // For each field id, check if it overrides any static field or constructor param
        fieldDecl.ids.forEach {
            // Check if any superclass contains a field of the same name
            val superField = currentClass!!.superclass!!.lookupLocalField(it)
            if (superField == null && fieldOverrides) {
                // Attempt override of constructor field with local field
                ErrorLogger.log(
                    fieldDecl,
                    "Constructor field $it cannot be overridden"
                )
            } else if (superField != null && Modifier.STATIC in superField.modifiers) {
                // Attempt override of static super field
                ErrorLogger.log(
                    fieldDecl,
                    "Override in class ${currentClass!!.className} of static field $it"
                )
            }
        }
    }

    override fun postVisit(objectInstantiation: ObjectInstantiation) {
        // Check arguments fit the constructor parameters in number and types
        val nArgs = objectInstantiation.args.size
        val classDefinition = classDefinitions.find { it.className == objectInstantiation.classId }
        if (classDefinition == null) {
            ErrorLogger.log(
                objectInstantiation,
                "Class ${objectInstantiation.classId} is undefined"
            )
            return
        }
        val constructorFields = classDefinition.getConstructorFields()
        val nParams = constructorFields.size
        if (nArgs != nParams) {
            ErrorLogger.log(
                objectInstantiation,
                "Wrong number of arguments to constructor of class ${objectInstantiation.classId}" +
                        " - $nArgs passed, $nParams expected"
            )
        }

        objectInstantiation.args.take(nParams).forEachIndexed { index, arg ->
            val argType = deriveType(arg)
            val paramType = constructorFields[index].type
            if (argType != paramType) {
                ErrorLogger.log(
                    arg,
                    "Argument ${index + 1} is of type ${typeString(argType)} but ${typeString(paramType)} was expected"
                )
            }

            if (arg !is IdExpr && (argType is ARRAY || argType is CLASS)) {
                ErrorLogger.log(
                    arg,
                    "Passing references directly is currently not supported"
                )
            }
        }
    }

    override fun postVisit(castExpr: CastExpr) {
        val objectClass = castExpr.type as CLASS
        val classDefinition = classDefinitions.find { objectClass.className == it.className }!!
        val superclasses = classDefinition.getSuperclasses(includeInterface = false)
        if (objectClass.castTo !in superclasses) {
            ErrorLogger.log(
                castExpr,
                "Invalid cast! ${objectClass.castTo} is not a superclass of ${objectClass.className}"
            )
        }
    }
}