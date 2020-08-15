/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.asSequence
import kotlin.jvm.JvmField


/**
 * 只读联系人列表, 无锁链表实现
 *
 * @see ContactList.asSequence
 */
@Suppress("unused")
class ContactList<C : Contact>
internal constructor(@JvmField internal val delegate: LockFreeLinkedList<C>) : Collection<C> {

    operator fun get(id: Long): C =
        delegate.asSequence().firstOrNull { it.id == id } ?: throw NoSuchElementException("Contact id $id")

    fun getOrNull(id: Long): C? = delegate.getOrNull(id)

    override val size: Int get() = delegate.size
    override operator fun contains(element: C): Boolean = delegate.contains(element)
    operator fun contains(id: Long): Boolean = delegate.getOrNull(id) != null
    override fun containsAll(elements: Collection<C>): Boolean = elements.all { contains(it) }
    override fun isEmpty(): Boolean = delegate.isEmpty()

    override fun toString(): String =
        delegate.asSequence().joinToString(separator = ", ", prefix = "ContactList(", postfix = ")")

    override fun iterator(): Iterator<C> {
        return this.delegate.asSequence().iterator()
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
    get() = "[" + buildString { delegate.forEach { append(it.id).append(", ") } }.dropLast(
        2
    ) + "]"


internal operator fun <C : Contact> LockFreeLinkedList<C>.get(id: Long): C {
    forEach { if (it.id == id) return it }
    throw NoSuchElementException("No such contact: $id")
}

internal fun <C : Contact> LockFreeLinkedList<C>.getOrNull(id: Long): C? {
    forEach { if (it.id == id) return it }
    return null
}