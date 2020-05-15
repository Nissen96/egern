package com.egern.ast

import com.egern.visitor.Visitable

abstract class ASTNode(val lineNumber: Int, val charPosition: Int) : Visitable