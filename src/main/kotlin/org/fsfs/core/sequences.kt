package org.fsfs.core

import arrow.core.Try
import arrow.core.identity

fun <E> Sequence<E>.intersperse(separator: E): Sequence<E> {
    return InterspersedSequence(this, separator)
}

fun <E> Sequence<E>.log(prefix: String): Sequence<E> {
    return this.map { println("[$prefix] $it"); it }
}

fun <E> Sequence<E>.consume(): Unit {
    return this.forEach {  }
}

fun <E1, E2> Sequence<E1>.through(f: (Sequence<E1>) -> Sequence<E2>): Sequence<E2> {
    return f(this)
}

internal class InterspersedSequence<T>
(
    private val sequence: Sequence<T>,
    private val separator: T
) : Sequence<T> {
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var idx: Int = 0

        override fun next(): T {
            val next = if (idx % 2 == 1) separator else iterator.next()
            idx++
            return next
        }

        override fun hasNext(): Boolean {
            return iterator.hasNext()
        }
    }
}

class AutoClosingSequence<T>
(
    private val sequence: Sequence<T>,
    private val resource: AutoCloseable
) : Sequence<T> {
    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        override fun next(): T = iterator.next()
        override fun hasNext(): Boolean = Try { if (iterator.hasNext()) true else { resource.close(); false } }.fold({ false }, ::identity)
    }
}
