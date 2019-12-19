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
class ContactList<C : Contact>(@MiraiInternalAPI val delegate: LockFreeLinkedList<C>) {
    /**
     * ID 列表的字符串表示.
     * 如:
     * ```
     * [123456, 321654, 123654]
     * ```
     */
    val idContentString: String get() = "[" + buildString { delegate.forEach { append(it.id).append(", ") } }.dropLast(2) + "]"

    operator fun get(id: Long): C = delegate[id]
    fun getOrNull(id: Long): C? = delegate.getOrNull(id)
    fun containsId(id: Long): Boolean = delegate.getOrNull(id) != null

    val size: Int get() = delegate.size
    operator fun contains(element: C): Boolean = delegate.contains(element)
    fun containsAll(elements: Collection<C>): Boolean = elements.all { contains(it) }
    fun isEmpty(): Boolean = delegate.isEmpty()
    inline fun forEach(block: (C) -> Unit) = delegate.forEach(block)

    override fun toString(): String = delegate.joinToString(separator = ", ", prefix = "ContactList(", postfix = ")")
}

operator fun <C : Contact> LockFreeLinkedList<C>.get(id: Long): C {
    forEach { if (it.id == id) return it }
    throw NoSuchElementException()
}

fun <C : Contact> LockFreeLinkedList<C>.getOrNull(id: Long): C? {
    forEach { if (it.id == id) return it }
    return null
}

fun <C : Contact> LockFreeLinkedList<C>.getOrAdd(id: Long, supplier: () -> C): C = filteringGetOrAdd({ it.id == id }, supplier)
