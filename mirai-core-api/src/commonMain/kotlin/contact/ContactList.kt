/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalApi
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * 只读联系人列表, 无锁链表实现
 *
 * @see ContactList.asSequence
 */
@Suppress("unused")
public class ContactList<C : Contact>
internal constructor(@JvmField @MiraiInternalApi public val delegate: ConcurrentLinkedQueue<C>) :
    Collection<C> by delegate {
    internal constructor(collection: Collection<C>) : this(ConcurrentLinkedQueue(collection))
    internal constructor() : this(ConcurrentLinkedQueue())

    public operator fun get(id: Long): C? = delegate.firstOrNull { it.id == id }
    public fun getOrFail(id: Long): C = get(id) ?: throw NoSuchElementException("Contact $id not found.")
    public fun remove(id: Long): Boolean = delegate.removeAll { it.id == id }
    public operator fun contains(id: Long): Boolean = get(id) != null

    override fun toString(): String = delegate.joinToString(separator = ", ", prefix = "ContactList(", postfix = ")")
    override fun equals(other: Any?): Boolean = other is ContactList<*> && delegate == other.delegate
    override fun hashCode(): Int = delegate.hashCode()
}

/**
 * ID 列表的字符串表示.
 * 如:
 * ```
 * [123456, 321654, 123654]
 * ```
 */
public val ContactList<*>.idContentString: String
    get() = "[" + delegate.joinToString { it.id.toString() } + "]"


internal operator fun <C : Contact> LockFreeLinkedList<C>.get(id: Long): C {
    forEach { if (it.id == id) return it }
    throw NoSuchElementException("No such contact: $id")
}

internal fun <C : Contact> LockFreeLinkedList<C>.getOrNull(id: Long): C? {
    forEach { if (it.id == id) return it }
    return null
}