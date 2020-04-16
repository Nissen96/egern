package com.egern.ast

import com.egern.visitor.Visitable
import kotlin.reflect.full.*

abstract class ASTNode(val lineNumber: Int, val charPosition: Int) : Visitable {
    override fun hashCode(): Int {
        var result = 0

        // Calculate hash from hash of each (non-lateinit) field
        this::class.declaredMemberProperties.forEach {
            if (!it.isLateinit) {
                result = 31 * result + it.getter.call(this).hashCode()
            }
        }

        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ASTNode) return false

        // Check equals for each
        this::class.declaredMemberProperties.forEach {
            if (!it.isLateinit && it.getter.call(this) != it.getter.call(other))
                return false
        }

        return true
    }
}