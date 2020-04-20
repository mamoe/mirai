/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import kotlin.jvm.JvmField
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 快速链接的两个消息 (避免构造新的 list).
 *
 * 不要直接构造 [CombinedMessage], 使用 [Message.plus]
 * 要连接多个 [Message], 使用 [buildMessageChain]
 *
 * @see Message.plus
 *
 * Left-biased list
 */
internal class CombinedMessage
internal constructor(
    @JvmField internal val left: Message, // 必须已经完成 constrain single
    @JvmField internal val tail: Message
) : Message, MessageChain {

    fun asSequence(): Sequence<SingleMessage> = sequence {
        yieldCombinedOrElementsFlatten(this@CombinedMessage)
    }

    override fun iterator(): Iterator<SingleMessage> {
        return asSequence().iterator()
    }

    override val size: Int
        get() = kotlin.run {
            var size = 0
            size += if (left is MessageChain) left.size else 1
            size += if (tail is MessageChain) tail.size else 1
            size
        }

    override fun equals(other: Any?): Boolean {
        return other is CombinedMessage && other.left == this.left && other.tail == this.tail
    }

    @OptIn(MiraiExperimentalAPI::class)
    override fun toString(): String {
        return left.toString() + tail.toString()
    }

    override fun contentToString(): String {
        return left.contentToString() + tail.contentToString()
    }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + tail.hashCode()
        return result
    }
}

@JvmSynthetic
// 不要把它用作 local function, 会编译错误
@OptIn(MiraiExperimentalAPI::class, MiraiInternalAPI::class)
private suspend fun SequenceScope<SingleMessage>.yieldCombinedOrElementsFlatten(message: Message) {
    when (message) {
        is CombinedMessage -> {
            // fast path
            yieldCombinedOrElementsFlatten(message.left)
            yieldCombinedOrElementsFlatten(message.tail)
        }
        is MessageChain -> {
            yieldAll(message)
        }
        else -> {
            check(message is SingleMessage) {
                "unsupported Message type: ${message::class}" +
                        "A Message must be a CombinedMessage, a Iterable<Message> or a SingleMessage"
            }
            yield(message)
        }
    }
}
