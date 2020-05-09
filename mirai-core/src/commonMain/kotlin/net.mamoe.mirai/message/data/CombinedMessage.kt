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

import net.mamoe.mirai.utils.PlannedRemoval
import kotlin.jvm.JvmField
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

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
) : Message, MessageChain, List<SingleMessage> by (left.flatten() + tail.flatten()).toList() {

    @PlannedRemoval("1.2.0")
    @Deprecated(
        "use asSequence from stdlib",
        ReplaceWith("(this as List<SingleMessage>).asSequence()"),
        level = DeprecationLevel.HIDDEN
    )
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    @kotlin.internal.LowPriorityInOverloadResolution // resolve to extension from stdlib
    fun asSequence(): Sequence<SingleMessage> = (this as List<SingleMessage>).asSequence()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other::class != CombinedMessage::class) return false
        other as CombinedMessage
        return other.left == this.left && other.tail == this.tail
    }

    private var toStringCache: String? = null


    override fun toString(): String = toStringCache ?: (left.toString() + tail.toString()).also { toStringCache = it }

    private var contentToStringCache: String? = null
    override fun contentToString(): String =
        contentToStringCache ?: (left.contentToString() + tail.contentToString()).also { contentToStringCache = it }

    override fun hashCode(): Int {
        var result = left.hashCode()
        result = 31 * result + tail.hashCode()
        return result
    }
}

/*
@JvmSynthetic
// 不要把它用作 local function, 会编译错误

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
*/