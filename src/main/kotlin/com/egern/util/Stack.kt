package com.egern.util

typealias Stack<T> = MutableList<T>

fun <T> stackOf(): Stack<T> = mutableListOf()
fun <T> stackOf(vararg elements: T): Stack<T> = mutableListOf(*elements)
fun <T> Stack<T>.push(item: T?) = item?.let { this.add(this.count(), it) }
fun <T> Stack<T>.pop(): T? = if (this.count() > 0) this.removeAt(this.count() - 1) else null
fun <T> Stack<T>.peek(): T? = if (this.count() > 0) this[this.count() - 1] else null
fun <T> Stack<T>.peek(offset: Int): T? = if (this.count() > offset) this[this.count() - offset - 1] else null
fun <T> Stack<T>.apply(func: (T) -> T) = if (this.count() > 0) this.push(func(this.pop()!!)) else null