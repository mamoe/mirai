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
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * 链接的两个消息.
 *
 * 不要直接构造 [CombinedMessage], 使用 [Message.plus]
 * 要连接多个 [Message], 使用 [buildMessageChain]
 *
 * @see Message.plus
 *
 * Left-biased list
 */
class CombinedMessage
@Deprecated(message = "use Message.plus", level = DeprecationLevel.ERROR)
@MiraiInternalAPI("CombinedMessage 构造器可能会在将来被改动") constructor(
    @MiraiExperimentalAPI("CombinedMessage.left 可能会在将来被改动")
    val left: SingleMessage,
    @MiraiExperimentalAPI("CombinedMessage.tail 可能会在将来被改动")
    val tail: SingleMessage
) : Iterable<SingleMessage>, Message {

    /*
    // 不要把它用作 local function, 会编译错误
    @OptIn(MiraiExperimentalAPI::class)
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
    */

    @OptIn(MiraiExperimentalAPI::class)
    fun asSequence(): Sequence<SingleMessage> = sequence {
        yield(left)
        yield(tail)
    }

    override fun iterator(): Iterator<SingleMessage> {
        return asSequence().iterator()
    }

    @OptIn(MiraiExperimentalAPI::class)
    override fun toString(): String {
        return tail.toString() + left.toString()
    }

    override fun contentToString(): String {
        return toString()
    }
}