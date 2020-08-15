/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("unused")

package net.mamoe.mirai.message.data

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 构建一个 [MessageChain]
 *
 * @see MessageChainBuilder
 */
@JvmSynthetic
inline fun buildMessageChain(block: MessageChainBuilder.() -> Unit): MessageChain {
    return MessageChainBuilder().apply(block).asMessageChain()
}

/**
 * 使用特定的容器大小构建一个 [MessageChain]
 *
 * @see MessageChainBuilder
 */
@JvmSynthetic
inline fun buildMessageChain(initialSize: Int, block: MessageChainBuilder.() -> Unit): MessageChain {
    return MessageChainBuilder(initialSize).apply(block).asMessageChain()
}

/**
 * [MessageChain] 构建器.
 * 多个连续的 [String] 会被连接为单个 [PlainText] 以优化性能.
 *
 *
 * **注意:** 无并发安全性.
 *
 * @see buildMessageChain 推荐使用
 * @see asMessageChain 完成构建
 */
open class MessageChainBuilder private constructor(
    private val container: MutableList<SingleMessage>
) : MutableList<SingleMessage> by container, Appendable {
    constructor() : this(mutableListOf())
    constructor(initialSize: Int) : this(ArrayList<SingleMessage>(initialSize))

    final override fun add(element: SingleMessage): Boolean {
        checkBuilt()
        flushCache()
        return addAndCheckConstrainSingle(element)
    }

    fun add(element: Message): Boolean {
        checkBuilt()
        flushCache()
        @Suppress("UNCHECKED_CAST")
        return when (element) {
            is ConstrainSingle<*> -> addAndCheckConstrainSingle(element)
            is SingleMessage -> container.add(element) // no need to constrain
            is Iterable<*> -> this.addAll(element.flatten())
            else -> error("stub")
        }
    }

    final override fun addAll(elements: Collection<SingleMessage>): Boolean {
        checkBuilt()
        flushCache()
        return addAll(elements.flatten())
    }

    fun addAll(elements: Iterable<SingleMessage>): Boolean {
        checkBuilt()
        flushCache()
        return addAll(elements.flatten())
    }

    @JvmName("addAllFlatten") // erased generic type cause declaration clash
    fun addAll(elements: Iterable<Message>): Boolean {
        checkBuilt()
        flushCache()
        return addAll(elements.flatten())
    }

    @JvmSynthetic
    operator fun Message.unaryPlus() {
        checkBuilt()
        flushCache()
        add(this)
    }


    @JvmSynthetic
    operator fun String.unaryPlus() {
        checkBuilt()
        add(this)
    }

    @JvmSynthetic // they should use add
    operator fun plusAssign(plain: String) {
        checkBuilt()
        withCache { append(plain) }
    }

    @JvmSynthetic // they should use add
    operator fun plusAssign(message: Message) {
        checkBuilt()
        flushCache()
        this.add(message)
    }

    @JvmSynthetic // they should use add
    operator fun plusAssign(message: SingleMessage) { // avoid resolution ambiguity
        checkBuilt()
        flushCache()
        this.add(message)
    }

    fun add(plain: String) {
        checkBuilt()
        withCache { append(plain) }
    }

    @JvmSynthetic // they should use add
    operator fun plusAssign(charSequence: CharSequence) {
        checkBuilt()
        withCache { append(charSequence) }
    }

    final override fun append(value: Char): MessageChainBuilder = withCache { append(value) }
    final override fun append(value: CharSequence?): MessageChainBuilder = withCache { append(value) }
    final override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): MessageChainBuilder =
        withCache { append(value, startIndex, endIndex) }

    fun append(message: Message): MessageChainBuilder = apply { add(message) }
    fun append(message: SingleMessage): MessageChainBuilder = apply { add(message) }

    // avoid resolution to extensions
    fun asMessageChain(): MessageChain {
        built = true
        this.flushCache()
        return MessageChainImplByCollection(this) // fast-path, no need to constrain
    }

    /** 同 [asMessageChain] */
    fun build(): MessageChain = asMessageChain()

    /**
     * 将所有已有元素引用复制到一个新的 [MessageChainBuilder]
     */
    fun copy(): MessageChainBuilder {
        return MessageChainBuilder(container.toMutableList())
    }

    ///////
    // FOR IMMUTABLE SAFETY

    final override fun remove(element: SingleMessage): Boolean {
        checkBuilt()
        return container.remove(element)
    }

    final override fun removeAll(elements: Collection<SingleMessage>): Boolean {
        checkBuilt()
        return container.removeAll(elements)
    }

    final override fun removeAt(index: Int): SingleMessage {
        checkBuilt()
        return container.removeAt(index)
    }

    final override fun clear() {
        checkBuilt()
        return container.clear()
    }

    final override fun set(index: Int, element: SingleMessage): SingleMessage {
        checkBuilt()
        return container.set(index, element)
    }

    ///////
    // IMPLEMENTATION
    private var cache: StringBuilder? = null
    private fun flushCache() {
        cache?.let {
            container.add(it.toString().toMessage())
        }
        cache = null
    }

    private inline fun withCache(block: StringBuilder.() -> Unit): MessageChainBuilder {
        checkBuilt()
        if (cache == null) {
            cache = StringBuilder().apply(block)
        } else {
            cache!!.apply(block)
        }
        return this
    }

    private var built = false
    private fun checkBuilt() = check(!built) { "MessageChainBuilder is already built therefore can't modify" }

    private var firstConstrainSingleIndex = -1

    private fun addAndCheckConstrainSingle(element: SingleMessage): Boolean {
        if (element is ConstrainSingle<*>) {
            if (firstConstrainSingleIndex == -1) {
                firstConstrainSingleIndex = container.size
                return container.add(element)
            }
            val key = element.key

            val index = container.indexOfFirst(firstConstrainSingleIndex) { it is ConstrainSingle<*> && it.key == key }
            if (index != -1) {
                container[index] = element
            } else {
                container.add(element)
            }

            return true
        } else {
            return container.add(element)
        }
    }
}