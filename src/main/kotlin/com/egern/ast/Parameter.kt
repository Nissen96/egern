package com.egern.ast

import com.egern.types.ExprType

data class Parameter(
    val id: String,
    val type: ExprType,
    val modifier: Modifier? = null
)