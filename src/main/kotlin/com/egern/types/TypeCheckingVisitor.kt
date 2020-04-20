package com.egern.types

import com.egern.ast.*
import com.egern.error.ErrorLogger
import com.egern.symbols.ClassDefinition
import com.egern.symbols.Symbol
import com.egern.symbols.SymbolTable
import com.egern.symbols.SymbolType
import com.egern.util.*
import com.egern.visitor.Visitor
import java.lang.Exception

class TypeCheckingVisitor(
    private var currentTable: SymbolTable, private val classDefinitions: List<ClassDefinition>
) : Visitor() {
    private val functionStack = stackOf<FuncDecl>()
    private var currentClass: ClassDefinition? = null

    override fun preVisit(block: Block) {
        currentTable = block.symbolTable
    }

    override fun postVisit(block: Block) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
    }

    override fun preVisit(funcDecl: FuncDecl) {
        currentTable = funcDecl.symbolTable
        functionStack.push(funcDecl)

        // Handle methods
        if (funcDecl.isMethod) {
            checkMethodDecl(funcDecl)
        }
    }

    override fun postVisit(funcDecl: FuncDecl) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
        functionStack.pop()
        if (!funcDecl.stmts.any { it is ReturnStmt }) {
            ErrorLogger.log(funcDecl, "No return statement found in function declaration")
        }
    }

    fun checkMethodDecl(methodDecl: FuncDecl) {
        // Check modifiers for the method itself
        val methodOverrides = Modifier.OVERRIDE in methodDecl.modifiers
        if (methodOverrides && Modifier.STATIC in methodDecl.modifiers) {
            ErrorLogger.log(methodDecl, "Static methods cannot be overridden")
        }

        // Check if method overrides any static methods or overrides without the override modifier
        val foundMethod = currentClass!!.superclass!!.lookupMethod(methodDecl.id) ?: return
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

    private fun lookupSymbol(id: String, validTypes: List<SymbolType>): Symbol {
        val sym = currentTable.lookup(id) ?: throw Exception("Symbol '$id' not defined")
        if (sym.type !in validTypes) {
            ErrorLogger.log(Exception("Symbol '$id' should be one of types $validTypes but is not"))
        }
        return sym
    }

    private fun deriveType(expr: Expr): ExprType {
        return deriveType(expr, currentTable, classDefinitions)
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
        }
    }

    override fun preVisit(varAssign: VarAssign) {
        // Check variable ids
        // TODO check types on varAssign classFields
        val allIds = varAssign.ids + varAssign.indexExprs.map { it.id }
        allIds.forEach { lookupSymbol(it, listOf(SymbolType.Variable, SymbolType.Parameter, SymbolType.Field)) }

        val exprType = deriveType(varAssign.expr)
        if (exprType == VOID) {
            ErrorLogger.log(varAssign, "Assigning void is invalid")
        }

        // Expression type must match declared variable
        varAssign.ids.forEach {
            val varType = getVariableType(it, currentTable, classDefinitions)
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
        val arrayType = getVariableType(arrayIndexExpr.id, currentTable, classDefinitions) as ARRAY
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

    override fun preVisit(classDecl: ClassDecl) {
        currentClass = classDefinitions.find { classDecl.id == it.className }!!
        currentTable = currentClass!!.symbolTable
    }

    override fun postVisit(classDecl: ClassDecl) {
        currentTable = currentTable.parent ?: throw Exception("No more scopes -- please buy another")
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