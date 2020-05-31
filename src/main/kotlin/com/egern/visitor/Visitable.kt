package com.egern.visitor

interface Visitable {
    fun accept(visitor: Visitor)
}