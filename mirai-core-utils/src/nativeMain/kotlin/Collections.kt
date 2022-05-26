/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.reflect.KClass

@Suppress("FunctionName")
actual fun <K : Any, V> ConcurrentHashMap(): MutableMap<K, V> {
    TODO("Not yet implemented")
}

@Suppress("FunctionName")
actual fun <E> ConcurrentLinkedDeque(): MutableDeque<E> {
    TODO("Not yet implemented")
}

@Suppress("FunctionName")
actual fun <E : Comparable<*>> PriorityQueue(): MutableQueue<E> {
    TODO("Not yet implemented")
}

@Suppress("FunctionName")
actual fun <E : Any> PriorityQueue(comparator: Comparator<E>): MutableCollection<E> {
    TODO("Not yet implemented")
}

@Suppress("FunctionName")
actual fun <K : Enum<K>, V> EnumMap(clazz: KClass<K>): MutableMap<K, V> = mutableMapOf()

@Suppress("FunctionName")
actual fun <E> ConcurrentSet(): MutableSet<E> {
    TODO("Not yet implemented")
}

actual class LinkedList<E> : MutableList<E>, AbstractMutableList<E>() {
    actual fun addLast(element: E) {
        TODO("Not yet implemented")
    }

    override fun add(index: Int, element: E) {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = TODO("Not yet implemented")

    override fun get(index: Int): E {
        TODO("Not yet implemented")
    }

    override fun removeAt(index: Int): E {
        TODO("Not yet implemented")
    }

    override fun set(index: Int, element: E): E {
        TODO("Not yet implemented")
    }
}

actual interface MutableDeque<E> : MutableQueue<E> {
    actual fun addFirst(element: E)
}

actual interface MutableQueue<E> : MutableCollection<E> {
    /**
     * Adds the specified element to the collection.
     *
     * @return `true` if the element has been added, `false` if the collection does not support duplicates
     * and the element is already contained in the collection.
     * @throws IllegalStateException if the queue is full.
     */
    actual override fun add(element: E): Boolean

    /**
     * Removes and returns the head of the queue, `null` otherwise.
     */
    actual fun poll(): E?

    /**
     * Adds an element into the queue.
     * @return `true` if the element has been added, `false` if queue is full.
     */
    actual fun offer(element: E): Boolean


}

/**
 * Returns a [List] that cannot be cast to [MutableList] to modify it.
 */
actual fun <T> List<T>.asImmutable(): List<T> = ImmutableList(this)
actual fun <T> Collection<T>.asImmutable(): Collection<T> = ImmutableCollection(this)
actual fun <T> Set<T>.asImmutable(): Set<T> = ImmutableSet(this)

internal class ImmutableList<T>(
    private val delegate: List<T>
) : List<T> by delegate

internal class ImmutableCollection<T>(
    private val delegate: Collection<T>
) : Collection<T> by delegate

internal class ImmutableSet<T>(
    private val delegate: Set<T>
) : Set<T> by delegate
