/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.reflect.KClass


public actual fun <T> Collection<T>.asImmutable(): Collection<T> {
    return Collections.unmodifiableCollection(this)
}

@Suppress("NOTHING_TO_INLINE")
public actual inline fun <T> List<T>.asImmutable(): List<T> {
    return Collections.unmodifiableList(this)
}

@Suppress("NOTHING_TO_INLINE")
public actual inline fun <T> Set<T>.asImmutable(): Set<T> {
    return Collections.unmodifiableSet(this)
}

@Suppress("NOTHING_TO_INLINE")
public inline fun <K, V> Map<K, V>.asImmutable(): Map<K, V> {
    return Collections.unmodifiableMap(this)
}

@Suppress("FunctionName")
public actual fun <K : Any, V> ConcurrentHashMap(): MutableMap<K, V> {
    return java.util.concurrent.ConcurrentHashMap()
}

public actual typealias LinkedList<E> = java.util.LinkedList<E>


public actual typealias MutableDeque<E> = java.util.Deque<E>

public actual typealias MutableQueue<E> = java.util.Queue<E>


@Suppress("FunctionName")
public actual fun <K : Enum<K>, V> EnumMap(clazz: KClass<K>): MutableMap<K, V> {
    return EnumMap(clazz.java)
}

@Suppress("FunctionName")
public actual fun <E> ConcurrentSet(): MutableSet<E> {
    return CopyOnWriteArraySet()
}

/**
 * Same as [MutableCollection.addAll].
 *
 * Adds all the elements of the specified enumeration to this collection.
 * @return true if any of the specified elements was added to the collection, false if the collection was not modified.
 */
public fun <T> MutableCollection<T>.addAll(enumeration: Enumeration<T>): Boolean {
    var addResult = false
    while (enumeration.hasMoreElements()) {
        addResult = this.add(enumeration.nextElement())
    }
    return addResult
}