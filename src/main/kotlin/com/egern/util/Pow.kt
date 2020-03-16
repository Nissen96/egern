package com.egern.util

import kotlin.math.pow

fun pow(a: Int, b: Int): Int {
    return a.toDouble().pow(b).toInt();
}