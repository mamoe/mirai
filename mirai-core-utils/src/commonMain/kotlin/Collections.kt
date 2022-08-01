/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("CollectionsKt_common")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmName
import kotlin.reflect.KClass

@Suppress("FunctionName")
public expect fun <K : Any, V> ConcurrentHashMap(): MutableMap<K, V>

@Suppress("FunctionName")
public expect fun <E> ConcurrentLinkedDeque(): MutableDeque<E>

@Suppress("FunctionName")
public fun <E> ConcurrentLinkedQueue(): MutableQueue<E> = ConcurrentLinkedDeque()

public expect class LinkedList<E> constructor() : MutableList<E> {
    public fun addLast(element: E)
}

public expect interface MutableQueue<E> : MutableCollection<E> {
    /**
     * Adds the specified element to the collection.
     *
     * @return `true` if the element has been added, `false` if the collection does not support duplicates
     * and the element is already contained in the collection.
     * @throws IllegalStateException if the queue is full.
     */
    public override fun add(element: E): Boolean

    /**
     * Removes and returns the head of the queue, `null` otherwise.
     */
    public fun poll(): E?


    /**
     * Adds an element into the queue.
     * @return `true` if the element has been added, `false` if queue is full.
     */
    public fun offer(element: E): Boolean
}

public expect interface MutableDeque<E> : MutableQueue<E> {
    public fun addFirst(element: E)
}

@Suppress("FunctionName")
public expect fun <K : Enum<K>, V> EnumMap(clazz: KClass<K>): MutableMap<K, V>

@Suppress("FunctionName")
public expect fun <E> ConcurrentSet(): MutableSet<E>

@Deprecated("", ReplaceWith("getOrElse(key) { default }"))
public fun <K, V : R, R> Map<K, V>.getOrDefault(key: K, default: R): R = getOrElse(key) { default }

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") // JDK 1.8
@Deprecated("", ReplaceWith("getOrPut(key) { value }"))
public fun <K, V> MutableMap<K, V>.putIfAbsent(key: K, value: V): V = getOrPut(key) { value }

/**
 * Returns a [List] that cannot be cast to [MutableList] to modify it.
 */
public expect fun <T> List<T>.asImmutable(): List<T>

/**
 * Returns a [Collection] that cannot be cast to [MutableCollection] to modify it.
 */
public expect fun <T> Collection<T>.asImmutable(): Collection<T>

public expect fun <T> Set<T>.asImmutable(): Set<T>