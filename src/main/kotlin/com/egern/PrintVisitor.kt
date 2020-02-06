package com.egern

import MainBaseVisitor

class PrintVisitor : MainBaseVisitor<Void>() {
    override fun visitExpr(ctx: MainParser.ExprContext): Void? {
        println(ctx.INT())
        if(ctx.op != null) {
            println(ctx.op.text)
        }
        visitChildren(ctx);
        return null
    }
}