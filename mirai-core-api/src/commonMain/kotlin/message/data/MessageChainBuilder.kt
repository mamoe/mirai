/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("unused")

package net.mamoe.mirai.message.data

import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

/**
 * 构建一个 [MessageChain]. 用法查看 [MessageChainBuilder].
 *
 * @see MessageChainBuilder
 */
public inline fun buildMessageChain(block: MessageChainBuilder.() -> Unit): MessageChain {
    contract { callsInPlace(block, EXACTLY_ONCE) }
    return MessageChainBuilder().apply(block).asMessageChain()
}

/**
 * 使用特定的容器大小构建一个 [MessageChain]. 用法查看 [MessageChainBuilder].
 *
 * @see MessageChainBuilder
 */
public inline fun buildMessageChain(initialSize: Int, block: MessageChainBuilder.() -> Unit): MessageChain {
    contract { callsInPlace(block, EXACTLY_ONCE) }
    return MessageChainBuilder(initialSize).apply(block).asMessageChain()
}

/**
 * [MessageChain] 构建器.
 *
 * **注意:** 无并发安全性.
 *
 * ### 连续 String 优化
 *
 * 多个连续的 [String] 会被连接为单个 [PlainText] 以优化性能。
 *
 * ```java
 * MessageChain chain = new MessageChainBuilder()
 *     .append("Hello ")
 *     .append("mirai!")
 *     .build();
 *
 * // chain 将会只包含一个 [PlainText], 其内容为 "Hello mirai!".
 * ```
 *
 * ## Kotlin 示例
 *
 * ```
 * val chain = buildMessageChain {
 *     +PlainText("a")
 *     +AtAll
 *     +Image("/f8f1ab55-bf8e-4236-b55e-955848d7069f")
 *     add(At(123456))
 * }
 * ```
 *
 * 该示例中 `+` 是 [MessageChainBuilder.unaryPlus]. 使用 `+` 和使用 `add` 是相等的.
 *
 * ## Java 示例
 * ```java
 * MessageChain chain = new MessageChainBuilder()
 *     .append(new PlainText("string"))
 *     .append("string") // 会被构造成 PlainText 再添加, 相当于上一行
 *     .append(AtAll.INSTANCE)
 *     .append(Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png"))
 *     .build();
 * ```
 *
 * @see buildMessageChain 推荐使用
 * @see asMessageChain 完成构建
 */
public class MessageChainBuilder private constructor(
    private val container: MutableList<SingleMessage>
) : MutableList<SingleMessage> by container, Appendable {
    public constructor() : this(mutableListOf())
    public constructor(initialSize: Int) : this(ArrayList<SingleMessage>(initialSize))

    public override fun add(element: SingleMessage): Boolean {
        flushCache()
        return container.add(element)
    }

    public fun add(element: Message): Boolean {
        flushCache()
        @Suppress("UNCHECKED_CAST")
        return when (element) {
            // is ConstrainSingle -> container.add(element)
            is SingleMessage -> container.add(element) // no need to constrain
            is Iterable<*> -> this.addAll(element.toMessageChain().asSequence())
            else -> error("stub")
        }
    }

    public override fun addAll(elements: Collection<SingleMessage>): Boolean {
        flushCache()
        return addAll(elements.asSequence())
    }

    public fun addAll(elements: Iterable<SingleMessage>): Boolean {
        flushCache()
        return addAll(elements.asSequence())
    }

    @JvmName("addAllFlatten") // erased generic type cause declaration clash
    public fun addAll(elements: Iterable<Message>): Boolean {
        flushCache()
        return addAll(elements.toMessageChain().asSequence())
    }

    @JvmSynthetic
    public operator fun Message.unaryPlus() {
        flushCache()
        add(this)
    }


    @JvmSynthetic
    public operator fun String.unaryPlus() {
        add(this)
    }

    @JvmSynthetic // they should use add
    public operator fun plusAssign(plain: String) {
        withCache { append(plain) }
    }

    @JvmSynthetic // they should use add
    public operator fun plusAssign(message: Message) {
        flushCache()
        this.add(message)
    }

    @JvmSynthetic // they should use add
    public operator fun plusAssign(message: SingleMessage) { // avoid resolution ambiguity
        flushCache()
        this.add(message)
    }

    public fun add(plain: String) {
        withCache { append(plain) }
    }

    @JvmSynthetic // they should use add
    public operator fun plusAssign(charSequence: CharSequence) {
        withCache { append(charSequence) }
    }

    public override fun append(value: Char): MessageChainBuilder = withCache { append(value) }
    public override fun append(value: CharSequence?): MessageChainBuilder = withCache { append(value) }
    public override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): MessageChainBuilder =
        withCache { append(value, startIndex, endIndex) }

    public fun append(message: Message): MessageChainBuilder = apply { add(message) }
    public fun append(message: SingleMessage): MessageChainBuilder = apply { add(message) }

    // avoid resolution to extensions
    public fun asMessageChain(): MessageChain {
        this.flushCache()
        return createMessageChainImplOptimized(this.constrainSingleMessages())
    }

    /** 同 [asMessageChain] */
    public fun build(): MessageChain = asMessageChain()

    /**
     * 将所有已有元素引用复制到一个新的 [MessageChainBuilder]
     */
    public fun copy(): MessageChainBuilder {
        return MessageChainBuilder(container.toMutableList())
    }

    public override fun remove(element: SingleMessage): Boolean {
        return container.remove(element)
    }

    public override fun removeAll(elements: Collection<SingleMessage>): Boolean {
        return container.removeAll(elements)
    }

    public override fun removeAt(index: Int): SingleMessage {
        return container.removeAt(index)
    }

    public override fun clear() {
        return container.clear()
    }

    public override fun set(index: Int, element: SingleMessage): SingleMessage {
        return container.set(index, element)
    }


    /**
     * 缓存通过 `add(String)` 添加的字符串, 将连续的字符串连接为一个 [PlainText]
     */
    private val cache: StringBuilder = StringBuilder()
    private fun flushCache() {
        if (cache.isNotEmpty()) {
            container.add(PlainText(cache.toString()))
            cache.setLength(0)
        }
    }

    private inline fun withCache(block: StringBuilder.() -> Unit): MessageChainBuilder {
        contract { callsInPlace(block, EXACTLY_ONCE) }

        cache.apply(block)
        return this
    }

    private var firstConstrainSingleIndex = -1

    private fun addAndCheckConstrainSingle(element: SingleMessage): Boolean {
        return container.add(element)
        /*
        if (element is ConstrainSingle) {
            if (firstConstrainSingleIndex == -1) {
                firstConstrainSingleIndex = container.size
                return container.add(element)
            }
            val key = element.key

            val index = container.indexOfFirst(firstConstrainSingleIndex) { it is ConstrainSingle && it.key.isSubKeyOf(key) }
            if (index != -1) {
                container[index] = element
            } else {
                container.add(element)
            }

            return true
        } else {
            return container.add(element)
        }*/
    }
}