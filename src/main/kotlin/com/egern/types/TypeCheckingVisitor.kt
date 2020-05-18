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
        if (!funcDecl.stmts.any { it is ReturnStmt }) {
            ErrorLogger.log(funcDecl, "No return statement found in function declaration")
        }
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
                "Override in class ${currentClass!!.className} of field ${methodDecl.id} from class $className without override modifier"
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
            val paramType = funcDecl.params[index].second
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

    override fun preVisit(varAssign: VarAssign) {
        // Check variable ids
        // TODO check types on varAssign classFields
        // TODO Fix type checking for arrays
        val allIds = varAssign.ids
        allIds.forEach { lookupSymbol(it, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field)) }

        val exprType = deriveType(varAssign.expr)
        if (exprType == VOID) {
            ErrorLogger.log(varAssign, "Assigning void is invalid")
        }

        // Expression type must match declared variable
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

        // Expression type must match type of element at array index
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
    }

    override fun postVisit(varDecl: VarDecl) {
        val type = deriveType(varDecl.expr)
        if (type == VOID) {
            ErrorLogger.log(varDecl, "Declaring a variable of type void is invalid")
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
                    "Invalid return type: ${typeString(returnType)} - expected: ${typeString(exprType)}"
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

        val arrayType = deriveType(arrayExpr) as ARRAY

        if (arrayType.depth > 1) {
            arrayExpr.entries.forEachIndexed { index, element ->
                val elementType = deriveType(element) as ARRAY
                if (elementType.depth != arrayType.depth - 1 ||
                    (elementType.innerType != arrayType.innerType && elementType.innerType != VOID && arrayType.innerType != VOID)
                ) {
                    ErrorLogger.log(
                        element,
                        "Type mismatch in array at position $index - element type: ${typeString(elementType)}, " +
                                "expected type: ${typeString(arrayType.innerType)}"
                    )
                }
            }
        } else {
            arrayExpr.entries.forEachIndexed { index, element ->
                val elementType = deriveType(element)
                if (elementType != arrayType.innerType) {
                    ErrorLogger.log(
                        element,
                        "Type mismatch in array at position $index - element type: ${typeString(elementType)}, " +
                                "expected type: ${typeString(arrayType.innerType)}"
                    )
                }
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
        // Check class overrides all interface methods
        val classInterface = currentClass!!.interfaceDecl
        classInterface?.methodSignatures?.forEach {
            var foundMethod: FuncDecl? = null

            classDecl.methods.forEach { method ->
                if (it.id == method.id) {
                    foundMethod = method

                    // Check signature (parameter and return types)
                    val methodParams = method.params.drop(1).map { param -> param.second }

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
            ErrorLogger.log(fieldDecl, "Static fields cannot be overridden")
        }

        // For each field id, check if it overrides any static field or constructor param
        // or overrides a field without the override modifier
        fieldDecl.ids.forEach {
            val foundField = currentClass!!.superclass!!.lookupField(it)
            if (foundField != null) {
                val (className, superField) = foundField
                if (superField != null) {
                    if (Modifier.STATIC in superField.modifiers) {
                        ErrorLogger.log(
                            fieldDecl,
                            "Override in class ${currentClass!!.className} of static field $it from class $className"
                        )
                    } else if (!fieldOverrides) {
                        ErrorLogger.log(
                            fieldDecl,
                            "Override in class ${currentClass!!.className} of field $it from class $className without override modifier"
                        )
                    }
                } else if (!fieldOverrides) {
                    ErrorLogger.log(
                        fieldDecl,
                        "Constructor parameter $it from class $className cannot be overridden"
                    )
                }
            }
        }
    }

    override fun postVisit(castExpr: CastExpr) {
        val castTo = (castExpr.type as CLASS).className
        val castFrom = (deriveType(castExpr.expr) as CLASS).className
        var classDefinition = classDefinitions.find { castFrom == it.className }
        while (classDefinition != null) {
            if (castTo == classDefinition.className) {
                return
            }
            classDefinition = classDefinition.superclass
        }
        ErrorLogger.log(castExpr, "Invalid cast!")
    }
}