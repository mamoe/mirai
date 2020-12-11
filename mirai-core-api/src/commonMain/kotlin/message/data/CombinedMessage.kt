/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

//
///**
// * 快速链接的两个消息 (避免构造新的 list).
// *
// * 不要直接构造 [CombinedMessage], 使用 [Message.plus]
// * 要连接多个 [Message], 使用 [buildMessageChain]
// *
// * @see Message.plus
// *
// * Left-biased list
// */
//@Serializable
//internal data class CombinedMessage
//internal constructor(
//    @JvmField internal val left: Message, // 必须已经完成 constrain single
//    @JvmField internal val tail: Message
//) : Message, MessageChain, List<SingleMessage> by (left.flatten() + tail.flatten()).toList() {
//    private val toStringCache: String by lazy(NONE) { left.contentToString() + tail.contentToString() }
//    override fun toString(): String = toStringCache
//
//    private val contentToStringCache: String by lazy(NONE) { left.contentToString() + tail.contentToString() }
//    override fun contentToString(): String = contentToStringCache
//}

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