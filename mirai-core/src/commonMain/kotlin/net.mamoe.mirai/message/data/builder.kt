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
 * 构造一个 [MessageChain]
 *
 * @see MessageChainBuilder
 */
inline fun buildMessageChain(block: MessageChainBuilder.() -> Unit): MessageChain {
    return MessageChainBuilder().apply(block).asMessageChain()
}

class MessageChainBuilder @JvmOverloads constructor(
    private val container: MutableList<Message> = mutableListOf()
) : MutableList<Message> by container, Appendable {
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

    override fun append(c: Char): Appendable = apply {
        this.add(PlainText(c.toString()))
    }

    override fun append(csq: CharSequence?): Appendable = apply {
        when {
            csq == null -> this.add(PlainText.Null)
            csq.isEmpty() -> this.add(PlainText.Empty)
            else -> this.add(PlainText(csq))
        }
    }

    override fun append(csq: CharSequence?, start: Int, end: Int): Appendable = apply {
        when {
            csq == null -> this.add(PlainText.Null)
            csq.isEmpty() -> this.add(PlainText.Empty)
            else -> this.add(PlainText(csq.substring(start, end)))
        }
    }
}