package com.egern.util

inline fun <T> Iterable<T>.forEach(action: (T) -> Unit, doBetween: () -> Unit) {
    val iterator = this.iterator()
    var hasNext = iterator.hasNext()
    while (hasNext) {
        action(iterator.next())
        hasNext = iterator.hasNext()
        if (hasNext) {
            doBetween()
        }
    }
}