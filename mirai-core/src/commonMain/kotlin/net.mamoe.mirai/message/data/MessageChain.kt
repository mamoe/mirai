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

import net.mamoe.mirai.message.data.NullMessageChain.toString
import kotlin.js.JsName
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.reflect.KProperty

/**
 * 消息链.
 * 它的一般实现为 [MessageChainImpl], `null` 实现为 [NullMessageChain]
 *
 * 要获取更多信息, 请查看 [Message]
 *
 * @see buildMessageChain 构造一个 [MessageChain]
 */
interface MessageChain : Message, Iterable<SingleMessage> {
    override operator fun contains(sub: String): Boolean
    override fun toString(): String

    /**
     * 获取第一个类型为 [key] 的 [Message] 实例
     *
     * @param key 由各个类型消息的伴生对象持有. 如 [PlainText.Key]
     */
    operator fun <M : Message> get(key: Message.Key<M>): M = first(key)
}

/**
 * 遍历每一个有内容的消息, 即 [At], [AtAll], [PlainText], [Image], [Face], [XMLMessage]
 */
inline fun MessageChain.foreachContent(block: (Message) -> Unit) {
    var last: Message? = null
    this.forEach { message: Message ->
        if (message is At) {
            if (last != null || last !is QuoteReply) {
                block(message)
            }
        } else if (message is MessageContent) {
            block(message)
        }
        last = message
    }
}

/**
 * 提供一个类型的值. 若不存在则会抛出异常 [NoSuchElementException]
 */
inline operator fun <reified T : Message> MessageChain.getValue(thisRef: Any?, property: KProperty<*>): T = this.first()

/**
 * 构造无初始元素的可修改的 [MessageChain]. 初始大小将会被设定为 8
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(): MessageChain = EmptyMessageChain

/**
 * 构造 [MessageChain]
 * 若仅提供一个参数, 请考虑使用 [Message.toChain] 以优化性能
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(vararg messages: Message): MessageChain =
    if (messages.isEmpty()) EmptyMessageChain
    else MessageChainImpl(messages.asSequence().flatten())

/**
 * 构造 [MessageChain] 的快速途径 (无 [Array] 创建)
 * 若仅提供一个参数, 请考虑使用 [Message.toChain] 以优化性能
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(message: Message): MessageChain =
    when (message) {
        is SingleMessage -> SingleMessageChainImpl(message)
        else -> MessageChainImpl(message.flatten().asIterable())
    }

/**
 * 构造 [MessageChain]
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(messages: Iterable<Message>): MessageChain =
    MessageChainImpl(messages.flatten().asIterable())

/**
 * 构造 [MessageChain]
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(messages: List<Message>): MessageChain =
    MessageChainImpl(messages.flatten().asIterable())


/**
 * 得到包含 [this] 的 [MessageChain].
 * 若 [this] 为 [MessageChain] 将直接返回 this
 * 否则将调用 [MessageChain] 构造一个 [MessageChainImpl]
 */
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
@JvmSynthetic
fun Message.toChain(): MessageChain = when (this) {
    is MessageChain -> this
    is CombinedMessage -> MessageChainImpl((this as Iterable<Message>).flatten().asIterable())
    else -> SingleMessageChainImpl(this as SingleMessage)
}

@JvmName("asMessageChain1")
@JvmSynthetic
@Suppress("unused", "NOTHING_TO_INLINE")
fun Iterable<SingleMessage>.asMessageChain(): MessageChain = MessageChainImpl(this)

@JvmSynthetic
@Suppress("unused", "NOTHING_TO_INLINE")
fun Iterable<Message>.asMessageChain(): MessageChain = MessageChainImpl(this.flatten())

fun Iterable<Message>.flatten(): Sequence<SingleMessage> = asSequence().flatten()

fun Sequence<Message>.flatten(): Sequence<SingleMessage> = flatMap { it.flatten() }

fun Message.flatten(): Sequence<SingleMessage> {
    return when (this) {
        is MessageChain -> this.asSequence()
        is CombinedMessage -> this.asSequence().flatten()
        else -> sequenceOf(this as SingleMessage)
    }
}

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
inline fun <reified M : Message> MessageChain.firstOrNull(): M? = this.firstOrNull { it is M } as M?

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 * @throws [NoSuchElementException] 如果找不到该类型的实例
 */
inline fun <reified M : Message> MessageChain.first(): M = this.first { it is M } as M

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
inline fun <reified M : Message> MessageChain.any(): Boolean = this.any { it is M }


/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.firstOrNull(key: Message.Key<M>): M? = when (key) {
    At -> first<At>()
    AtAll -> first<AtAll>()
    PlainText -> first<PlainText>()
    Image -> first<Image>()
    Face -> first<Face>()
    QuoteReply -> first<QuoteReply>()
    MessageSource -> first<MessageSource>()
    else -> null
} as M?

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 * @throws [NoSuchElementException] 如果找不到该类型的实例
 */
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.first(key: Message.Key<M>): M = firstOrNull(key) ?: throw NoSuchElementException("no such element: $key")

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.any(key: Message.Key<M>): Boolean = firstOrNull(key) != null

object EmptyMessageChain : MessageChain by MessageChainImpl(emptyList())

/**
 * Null 的 [MessageChain].
 * 它不包含任何元素, 也没有创建任何 list.
 *
 * 除 [toString] 外, 其他方法均 [error]
 */
object NullMessageChain : MessageChain {
    override fun toString(): String = "null"
    override fun contains(sub: String): Boolean = error("accessing NullMessageChain")
    override fun followedBy(tail: Message): CombinedMessage = CombinedMessage(left = EmptyMessageChain, element = tail)
    override fun iterator(): MutableIterator<SingleMessage> = error("accessing NullMessageChain")
}

@PublishedApi
internal class MessageChainImpl constructor(
    private val delegate: Iterable<SingleMessage>
) : Message, Iterable<SingleMessage> by delegate, MessageChain {
    constructor(delegate: Sequence<SingleMessage>) : this(delegate.asIterable())

    override fun toString(): String = this.delegate.joinToString("") { it.toString() }
    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
}

@PublishedApi
internal class SingleMessageChainImpl constructor(
    private val delegate: SingleMessage
) : Message, Iterable<SingleMessage> by listOf(delegate), MessageChain {
    override fun toString(): String = this.delegate.toString()

    override operator fun contains(sub: String): Boolean = sub in delegate
}