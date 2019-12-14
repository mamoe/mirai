@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.joinToString


/**
 * 只读联系人列表, lock-free 实现
 */
@UseExperimental(MiraiInternalAPI::class)
@Suppress("unused")
class ContactList<C : Contact>(@PublishedApi internal val delegate: MutableContactList<C>) {
    /**
     * ID 列表的字符串表示.
     * 如:
     * ```
     * [123456, 321654, 123654]
     * ```
     */
    val idContentString: String get() = "[" + buildString { delegate.forEach { append(it.id).append(", ") } }.dropLast(2) + "]"

    operator fun get(id: UInt): C = delegate[id]
    fun getOrNull(id: UInt): C? = delegate.getOrNull(id)
    fun containsId(id: UInt): Boolean = delegate.getOrNull(id) != null

    val size: Int get() = delegate.size
    operator fun contains(element: C): Boolean = delegate.contains(element)
    fun containsAll(elements: Collection<C>): Boolean = elements.all { contains(it) }
    fun isEmpty(): Boolean = delegate.isEmpty()
    inline fun forEach(block: (C) -> Unit) = delegate.forEach(block)

    override fun toString(): String = delegate.joinToString(separator = ", ", prefix = "ContactList(", postfix = ")")
}

/**
 * 可修改联系人列表. 只会在内部使用.
 */
@MiraiInternalAPI
class MutableContactList<C : Contact> : LockFreeLinkedList<C>() {
    override fun toString(): String = joinToString(separator = ", ", prefix = "MutableContactList(", postfix = ")")

    operator fun get(id: UInt): C {
        forEach { if (it.id == id) return it }
        throw NoSuchElementException()
    }

    fun getOrNull(id: UInt): C? {
        forEach { if (it.id == id) return it }
        return null
    }

    fun getOrAdd(id: UInt, supplier: () -> C): C = super.filteringGetOrAdd({it.id == id}, supplier)
}