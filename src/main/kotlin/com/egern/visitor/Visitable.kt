package com.egern.visitor

import com.egern.visitor.Visitor

interface Visitable {
    fun accept(visitor: Visitor)
}