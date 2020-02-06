package com.egern

import MainVisitor
import org.antlr.v4.runtime.tree.ErrorNode
import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.RuleNode
import org.antlr.v4.runtime.tree.TerminalNode

class PrintVisitor : MainVisitor<Void> {
    override fun visitExpr(ctx: MainParser.ExprContext?): Void? {
        println(ctx.toString());
        return null;
    }

    override fun visitChildren(node: RuleNode?): Void? {
        println(node.toString());
        return null;
    }

    override fun visitErrorNode(node: ErrorNode?): Void? {
        println(node.toString());
        return null;
    }

    override fun visitProg(ctx: MainParser.ProgContext?): Void? {
        println("yes sir");
        if (ctx != null) {
            println(ctx.toStringTree())
        };
        return null;
    }

    override fun visit(tree: ParseTree?): Void? {
        println(tree.toString());
        return null;
    }

    override fun visitTerminal(node: TerminalNode?): Void? {
        println(node.toString());
        return null;
    }

}