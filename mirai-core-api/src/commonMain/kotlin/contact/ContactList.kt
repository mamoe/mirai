/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.MiraiInternalApi
import java.util.concurrent.ConcurrentLinkedQueue


/**
 * 只读联系人列表. 元素列表仍可能会被 mirai 内部修改.
 *
 * @see ContactList.asSequence
 */
@Suppress("unused")
public class ContactList<C : Contact>
@MiraiInternalApi public constructor(@JvmField @MiraiInternalApi public val delegate: ConcurrentLinkedQueue<C>) :
    Collection<C> by delegate {
    @MiraiInternalApi
    public constructor(collection: Collection<C>) : this(ConcurrentLinkedQueue(collection))

    @MiraiInternalApi
    public constructor() : this(ConcurrentLinkedQueue())

    /**
     * 获取一个 [Contact.id] 为 [id] 的元素. 在不存在时返回 `null`.
     */
    public operator fun get(id: Long): C? = delegate.firstOrNull { it.id == id }

    /**
     * 获取一个 [Contact.id] 为 [id] 的元素. 在不存在时抛出 [NoSuchElementException].
     */
    public fun getOrFail(id: Long): C = get(id) ?: throw NoSuchElementException("Contact $id not found.")

    /**
     * 删除 [Contact.id] 为 [id] 的元素.
     */
    public fun remove(id: Long): Boolean = delegate.removeAll { it.id == id }

    /**
     * 当存在 [Contact.id] 为 [id] 的元素时返回 `true`.
     */
    public operator fun contains(id: Long): Boolean = get(id) != null

    override fun toString(): String = delegate.joinToString(separator = ", ", prefix = "ContactList(", postfix = ")")
    override fun equals(other: Any?): Boolean = other is ContactList<*> && delegate == other.delegate
    override fun hashCode(): Int = delegate.hashCode()
}