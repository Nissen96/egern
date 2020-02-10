package com.egern.ast

class IfElse(val expression: Expr, val ifBlock: Block, val elseBlock: Block?) : ASTNode()