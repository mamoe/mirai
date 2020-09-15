/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

// We target JVM and Android only.
internal expect class LinkedList<E>() : List<E>, Queue<E>, Deque<E>

internal interface Queue<E> : MutableCollection<E> {
    override fun add(element: E): Boolean
    fun offer(element: E): Boolean
    fun remove(): E
    fun poll(): E
    fun element(): E
    fun peek(): E
}

internal interface Deque<E> : Queue<E> {
    fun addFirst(e: E)
    fun addLast(e: E)
    fun offerFirst(e: E): Boolean
    fun offerLast(e: E): Boolean
    fun removeFirst(): E
    fun removeLast(): E
    fun pollFirst(): E
    fun pollLast(): E
    val first: E
    val last: E
    fun peekFirst(): E
    fun peekLast(): E
    fun removeFirstOccurrence(o: E): Boolean
    fun removeLastOccurrence(o: E): Boolean
    fun push(e: E)
    fun pop(): E
    fun descendingIterator(): Iterator<E>
}