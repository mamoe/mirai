/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("RedundantVisibilityModifier")

package net.mamoe.mirai.utils

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.reflect.KClass

@Suppress("FunctionName")
public actual fun <K : Any, V> ConcurrentHashMap(): MutableMap<K, V> {
    return LockedConcurrentHashMap(reentrantLock())
}

private class LockedConcurrentHashSet<E>(
    private val lock: ReentrantLock,
    private val delegate: MutableSet<E> = mutableSetOf()
) : MutableSet<E> {
    override fun add(element: E): Boolean = lock.withLock {
        return delegate.add(element)
    }

    override fun addAll(elements: Collection<E>): Boolean = lock.withLock {
        return delegate.addAll(elements)
    }

    override val size: Int get() = lock.withLock { delegate.size }

    override fun clear() = lock.withLock { delegate.clear() }

    override fun isEmpty(): Boolean = lock.withLock { delegate.isEmpty() }
    override fun containsAll(elements: Collection<E>): Boolean = lock.withLock { delegate.containsAll(elements) }
    override fun contains(element: E): Boolean = lock.withLock { delegate.contains(element) }

    override fun iterator(): MutableIterator<E> = delegate.iterator() // no effect for locking

    @Suppress("ConvertArgumentToSet")
    override fun retainAll(elements: Collection<E>): Boolean = lock.withLock { delegate.retainAll(elements) }

    @Suppress("ConvertArgumentToSet")
    override fun removeAll(elements: Collection<E>): Boolean = lock.withLock { delegate.removeAll(elements) }

    override fun remove(element: E): Boolean = lock.withLock { delegate.remove(element) }


    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()
}

private class LockedConcurrentCollection<E>(
    private val lock: ReentrantLock,
    private val delegate: MutableCollection<E>
) : MutableCollection<E> {
    override fun add(element: E): Boolean = lock.withLock {
        return delegate.add(element)
    }

    override fun addAll(elements: Collection<E>): Boolean = lock.withLock {
        return delegate.addAll(elements)
    }

    override val size: Int get() = lock.withLock { delegate.size }

    override fun clear() = lock.withLock { delegate.clear() }

    override fun isEmpty(): Boolean = lock.withLock { delegate.isEmpty() }
    override fun containsAll(elements: Collection<E>): Boolean = lock.withLock { delegate.containsAll(elements) }
    override fun contains(element: E): Boolean = lock.withLock { delegate.contains(element) }

    override fun iterator(): MutableIterator<E> = delegate.iterator() // no effect for locking

    @Suppress("ConvertArgumentToSet")
    override fun retainAll(elements: Collection<E>): Boolean = lock.withLock { delegate.retainAll(elements) }

    @Suppress("ConvertArgumentToSet")
    override fun removeAll(elements: Collection<E>): Boolean = lock.withLock { delegate.removeAll(elements) }

    override fun remove(element: E): Boolean = lock.withLock { delegate.remove(element) }

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()
}

private class LockedConcurrentHashMap<K : Any, V>(
    private val lock: ReentrantLock,
    private val delegate: MutableMap<K, V> = mutableMapOf()
) : MutableMap<K, V> {
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = lock.withLock { LockedConcurrentHashSet(lock, delegate.entries) }

    override val keys: MutableSet<K> get() = lock.withLock { LockedConcurrentHashSet(lock, delegate.keys) }
    override val size: Int get() = lock.withLock { delegate.size }
    override val values: MutableCollection<V>
        get() = lock.withLock { LockedConcurrentCollection(lock, delegate.values) }

    override fun clear() = lock.withLock { delegate.clear() }
    override fun isEmpty(): Boolean = lock.withLock { delegate.isEmpty() }
    override fun remove(key: K): V? = lock.withLock { delegate.remove(key) }
    override fun putAll(from: Map<out K, V>) = lock.withLock { delegate.putAll(from) }
    override fun put(key: K, value: V): V? = lock.withLock { delegate.put(key, value) }
    override fun get(key: K): V? = lock.withLock { delegate.get(key) }
    override fun containsValue(value: V): Boolean = lock.withLock { delegate.containsValue(value) }
    override fun containsKey(key: K): Boolean = lock.withLock { delegate.containsKey(key) }

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()
}

@Suppress("FunctionName")
public actual fun <E> ConcurrentLinkedDeque(): MutableDeque<E> {
    return LockedConcurrentArrayDeque(reentrantLock())
}

private class LockedConcurrentArrayDeque<E>(
    private val lock: ReentrantLock,
    private val delegate: ArrayDeque<E> = ArrayDeque()
) : MutableDeque<E> {
    override fun addFirst(element: E) = lock.withLock { delegate.addFirst(element) }

    override fun add(element: E): Boolean {
        lock.withLock { delegate.add(element) }
        return true
    }

    override fun poll(): E? = lock.withLock { delegate.removeFirstOrNull() }

    override fun offer(element: E): Boolean {
        lock.withLock { delegate.addLast(element) }
        return true
    }

    override val size: Int
        get() = lock.withLock { delegate.size }

    override fun clear() = lock.withLock { delegate.clear() }
    override fun addAll(elements: Collection<E>): Boolean = lock.withLock { delegate.addAll(elements) }
    override fun isEmpty(): Boolean = lock.withLock { delegate.isEmpty() }
    override fun iterator(): MutableIterator<E> = delegate.iterator()
    override fun retainAll(elements: Collection<E>): Boolean = lock.withLock { delegate.retainAll(elements) }
    override fun removeAll(elements: Collection<E>): Boolean = lock.withLock { delegate.removeAll(elements) }
    override fun remove(element: E): Boolean = lock.withLock { delegate.remove(element) }
    override fun containsAll(elements: Collection<E>): Boolean = lock.withLock { delegate.containsAll(elements) }
    override fun contains(element: E): Boolean = lock.withLock { delegate.contains(element) }

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()
}

internal class ArrayDequeAsMutableDeque<E>(
    private val delegate: ArrayDeque<E> = ArrayDeque()
) : MutableDeque<E> {
    override fun addFirst(element: E) = delegate.addFirst(element)

    override fun add(element: E): Boolean {
        delegate.add(element)
        return true
    }

    override fun poll(): E? = delegate.removeFirstOrNull()

    override fun offer(element: E): Boolean {
        delegate.addLast(element)
        return true
    }

    override val size: Int
        get() = delegate.size

    override fun clear() = delegate.clear()
    override fun addAll(elements: Collection<E>): Boolean = delegate.addAll(elements)
    override fun isEmpty(): Boolean = delegate.isEmpty()
    override fun iterator(): MutableIterator<E> = delegate.iterator()
    override fun retainAll(elements: Collection<E>): Boolean = delegate.retainAll(elements)
    override fun removeAll(elements: Collection<E>): Boolean = delegate.removeAll(elements)
    override fun remove(element: E): Boolean = delegate.remove(element)
    override fun containsAll(elements: Collection<E>): Boolean = delegate.containsAll(elements)
    override fun contains(element: E): Boolean = delegate.contains(element)

    override fun equals(other: Any?): Boolean = delegate == other
    override fun hashCode(): Int = delegate.hashCode()
    override fun toString(): String = delegate.toString()
}

@Suppress("FunctionName")
public actual fun <K : Enum<K>, V> EnumMap(clazz: KClass<K>): MutableMap<K, V> = mutableMapOf()

@Suppress("FunctionName")
public actual fun <E> ConcurrentSet(): MutableSet<E> {
    return LockedConcurrentHashSet(reentrantLock())
}

public actual class LinkedList<E>(
    private val delegate: ArrayDeque<E>
) : MutableList<E> by delegate {
    public actual constructor() : this(ArrayDeque())

    public actual fun addLast(element: E) {
        return delegate.addLast(element)
    }
}

public actual interface MutableDeque<E> : MutableQueue<E> {
    public actual fun addFirst(element: E)
}

public actual interface MutableQueue<E> : MutableCollection<E> {
    /**
     * Adds the specified element to the collection.
     *
     * @return `true` if the element has been added, `false` if the collection does not support duplicates
     * and the element is already contained in the collection.
     * @throws IllegalStateException if the queue is full.
     */
    public actual override fun add(element: E): Boolean

    /**
     * Removes and returns the head of the queue, `null` otherwise.
     */
    public actual fun poll(): E?

    /**
     * Adds an element into the queue.
     * @return `true` if the element has been added, `false` if queue is full.
     */
    public actual fun offer(element: E): Boolean


}

/**
 * Returns a [List] that cannot be cast to [MutableList] to modify it.
 */
public actual fun <T> List<T>.asImmutable(): List<T> = ImmutableList(this)
public actual fun <T> Collection<T>.asImmutable(): Collection<T> = ImmutableCollection(this)
public actual fun <T> Set<T>.asImmutable(): Set<T> = ImmutableSet(this)

internal class ImmutableList<T>(
    private val delegate: List<T>
) : List<T> by delegate

internal class ImmutableCollection<T>(
    private val delegate: Collection<T>
) : Collection<T> by delegate

internal class ImmutableSet<T>(
    private val delegate: Set<T>
) : Set<T> by delegate
