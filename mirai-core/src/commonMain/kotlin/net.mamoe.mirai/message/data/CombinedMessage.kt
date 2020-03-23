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

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * 链接的两个消息.
 *
 * @see Message.plus
 *
 * Left-biased list
 */
class CombinedMessage(
    val left: Message,
    val tail: Message
) : Iterable<Message>, Message {

    // 不要把它用作 local function, 会编译错误
    private suspend fun SequenceScope<Message>.yieldCombinedOrElements(message: Message) {
        when (message) {
            is CombinedMessage -> {
                // fast path, 避免创建新的 iterator, 也不会挂起协程
                yieldCombinedOrElements(message.left)
                yieldCombinedOrElements(message.tail)
            }
            is Iterable<*> -> {
                // 更好的性能, 因为协程不会挂起.
                // 这可能会导致爆栈 (十万个元素), 但作为消息序列足够了.
                message.forEach {
                    yieldCombinedOrElements(
                        it as? Message ?: error(
                            "A Message implementing Iterable must implement Iterable<Message>, " +
                                    "whereas got ${it!!::class.simpleName}"
                        )
                    )
                }
            }
            else -> {
                check(message is SingleMessage) {
                    "unsupported Message type. " +
                            "A Message must be a CombinedMessage, a Iterable<Message> or a SingleMessage"
                }
                yield(message)
            }
        }
    }

    fun asSequence(): Sequence<Message> = sequence {
        yieldCombinedOrElements(this@CombinedMessage)
    }

    override fun iterator(): Iterator<Message> {
        return asSequence().iterator()
    }

    override fun toString(): String {
        return tail.toString() + left.toString()
    }

    fun isFlat(): Boolean {
        return tail is SingleMessage && left is SingleMessage
    }
}