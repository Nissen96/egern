package com.egern.ast

public class IfElse(val expression: Expr, val ifBlock: Block, val elseBlock: Block?) : ASTNode() {
}