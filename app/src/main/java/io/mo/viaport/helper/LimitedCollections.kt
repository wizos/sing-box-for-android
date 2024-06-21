/*
 * Copyright (c) 2023 Wizos
 * Project: Loread
 * Email: wizos@qq.com
 * Create Time: 2023-10-17 10:39:13
 */

package io.mo.viaport.helper

import java.util.LinkedList

fun <E> limitedListOf(limit: Int): LimitedList<E> = LimitedList(limit)
class LimitedList<E>(private val limit: Int) : LinkedList<E>() {
    override fun add(element: E): Boolean {
        val overflow = size - limit + 1
        if (overflow > 0) {
            for (i in 1..overflow) {
                removeFirst()
            }
        }
        return super.add(element)
    }
    override fun addLast(element: E) {
        val overflow = size - limit + 1
        if (overflow > 0) {
            for (i in 1..overflow) {
                removeFirst()
            }
        }
        super.addLast(element)
    }
    operator fun plus(element: E): LimitedList<E> {
        addLast(element)
        return this
    }
    operator fun plus(elements: List<E>): LimitedList<E> {
        addAll(elements)
        return this
    }
}

public fun <E> limitedSetOf(limit: Int): LimitedSet<E> = LimitedSet(limit)
class LimitedSet<E>(private val limit: Int) : LinkedHashSet<E>() {
    override fun add(element: E): Boolean {
        val overflow = size - limit + 1
        if (overflow > 0) {
            for (i in 1..overflow) {
                remove(first())
            }
        }
        return super.add(element)
    }
}

public fun <K, V> limitedMapOf(limit: Int): LinkedHashMap<K, V> = LinkedHashMap(limit)
class LimitedMap<K, V>(private val limit: Int) : LinkedHashMap<K, V>() {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > limit
    }
}