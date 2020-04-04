/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmName


/**
 * 只读联系人列表, lock-free 实现
 *
 * @see ContactList.asSequence
 */
@OptIn(MiraiInternalAPI::class)
@Suppress("unused")
class ContactList<C : Contact>(@MiraiInternalAPI val delegate: LockFreeLinkedList<C>) : Iterable<C> {
    operator fun get(id: Long): C = delegate.asSequence().first { it.id == id }
    fun getOrNull(id: Long): C? = delegate.getOrNull(id)

    val size: Int get() = delegate.size
    operator fun contains(element: C): Boolean = delegate.contains(element)
    operator fun contains(id: Long): Boolean = delegate.getOrNull(id) != null
    fun containsAll(elements: Collection<C>): Boolean = elements.all { contains(it) }
    fun isEmpty(): Boolean = delegate.isEmpty()

    override fun toString(): String =
        delegate.asSequence().joinToString(separator = ", ", prefix = "ContactList(", postfix = ")")

    override fun iterator(): Iterator<C> {
        return this.delegate.asSequence().iterator()
    }

    @PlannedRemoval("1.0.0")
    @Suppress("PropertyName")
    @get:JvmName("getIdContentString")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    val _idContentString: String
        get() = this.idContentString

    @PlannedRemoval("1.0.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    inline fun forEach(block: (C) -> Unit) = delegate.forEach(block)

    @PlannedRemoval("1.0.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    fun first(): C {
        forEach { return it }
        throw NoSuchElementException()
    }

    @PlannedRemoval("1.0.0")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    fun firstOrNull(): C? {
        forEach { return it }
        return null
    }
}

/**
 * ID 列表的字符串表示.
 * 如:
 * ```
 * [123456, 321654, 123654]
 * ```
 */
val ContactList<*>.idContentString: String
    get() = "[" + @OptIn(MiraiInternalAPI::class) buildString { delegate.forEach { append(it.id).append(", ") } }.dropLast(
        2
    ) + "]"


operator fun <C : Contact> LockFreeLinkedList<C>.get(id: Long): C {
    forEach { if (it.id == id) return it }
    throw NoSuchElementException("No such contact: $id")
}

fun <C : Contact> LockFreeLinkedList<C>.getOrNull(id: Long): C? {
    forEach { if (it.id == id) return it }
    return null
}

@PlannedRemoval("1.0.0")
@Deprecated(
    "use firstOrNull from stdlib",
    replaceWith = ReplaceWith("this.asSequence().firstOrNull(filter)"),
    level = DeprecationLevel.ERROR
)
inline fun <C : Contact> LockFreeLinkedList<C>.firstOrNull(filter: (C) -> Boolean): C? {
    forEach { if (filter(it)) return it }
    return null
}

@PlannedRemoval("1.0.0")
@Deprecated(
    "use firstOrNull from stdlib",
    replaceWith = ReplaceWith("firstOrNull(filter)"),
    level = DeprecationLevel.ERROR
)
inline fun <C : Contact> LockFreeLinkedList<C>.filteringGetOrNull(filter: (C) -> Boolean): C? =
    this.asSequence().firstOrNull(filter)

@PlannedRemoval("1.0.0")
@Deprecated("use Iterator.toList from stdlib", level = DeprecationLevel.HIDDEN)
fun <E : Contact> ContactList<E>.toList(): List<E> = toMutableList()

@PlannedRemoval("1.0.0")
@Deprecated("use Iterator.toMutableList from stdlib", level = DeprecationLevel.HIDDEN)
@OptIn(MiraiInternalAPI::class)
fun <E : Contact> ContactList<E>.toMutableList(): MutableList<E> = this.delegate.toMutableList()

@PlannedRemoval("1.0.0")
@Deprecated("use Iterator.toSet from stdlib", level = DeprecationLevel.HIDDEN)
fun <E : Contact> ContactList<E>.toSet(): Set<E> = toMutableSet()

@PlannedRemoval("1.0.0")
@Deprecated("use Iterator.toMutableSet from stdlib", level = DeprecationLevel.HIDDEN)
@OptIn(MiraiInternalAPI::class)
fun <E : Contact> ContactList<E>.toMutableSet(): MutableSet<E> = this.delegate.toMutableSet()

@PlannedRemoval("1.0.0")
@Deprecated("use Iterator.asSequence from stdlib", level = DeprecationLevel.HIDDEN)
@OptIn(MiraiInternalAPI::class)
fun <E : Contact> ContactList<E>.asSequence(): Sequence<E> = this.delegate.asSequence()