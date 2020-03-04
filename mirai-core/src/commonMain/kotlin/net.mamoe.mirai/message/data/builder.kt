/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlin.jvm.JvmOverloads

/**
 * 构建一个 [MessageChain]
 *
 * @see MessageChainBuilder
 */
inline fun buildMessageChain(block: MessageChainBuilder.() -> Unit): MessageChain {
    return MessageChainBuilder().apply(block).asMessageChain()
}

/**
 * 使用特定的容器大小构建一个 [MessageChain]
 *
 * @see MessageChainBuilder
 */
inline fun buildMessageChain(initialSize: Int, block: MessageChainBuilder.() -> Unit): MessageChain {
    return MessageChainBuilder(initialSize).apply(block).asMessageChain()
}

/**
 * 使用特定的容器构建一个 [MessageChain]
 *
 * @see MessageChainBuilder
 */
inline fun buildMessageChain(
    container: MutableCollection<Message>,
    block: MessageChainBuilder.() -> Unit
): MessageChain {
    return MessageChainBuilder(container).apply(block).asMessageChain()
}

/**
 * [MessageChain] 构建器.
 *
 * @see buildMessageChain 推荐使用
 * @see asMessageChain 完成构建
 */
class MessageChainBuilder
@JvmOverloads constructor(
    private val container: MutableCollection<Message> = mutableListOf()
) : MutableCollection<Message> by container, Appendable {
    constructor(initialSize: Int) : this(ArrayList<Message>(initialSize))

    operator fun Message.unaryPlus() {
        add(this)
    }

    operator fun String.unaryPlus() {
        add(this.toMessage())
    }

    operator fun plusAssign(plain: String) {
        this.add(plain.toMessage())
    }

    operator fun plusAssign(message: Message) {
        this.add(message)
    }

    fun add(plain: String) {
        this.add(plain.toMessage())
    }

    operator fun plusAssign(charSequence: CharSequence) {
        this.add(PlainText(charSequence))
    }

    override fun append(value: Char): Appendable = apply {
        this.add(PlainText(value.toString()))
    }

    override fun append(value: CharSequence?): Appendable = apply {
        when {
            value == null -> this.add(PlainText.Null)
            value.isEmpty() -> this.add(PlainText.Empty)
            else -> this.add(PlainText(value))
        }
    }

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable = apply {
        when {
            value == null -> this.add(PlainText.Null)
            value.isEmpty() -> this.add(PlainText.Empty)
            else -> this.add(PlainText(value.substring(startIndex, endIndex)))
        }
    }
}