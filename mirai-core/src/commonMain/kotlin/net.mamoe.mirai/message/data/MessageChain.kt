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
@file:Suppress("unused")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.message.data.NullMessageChain.equals
import net.mamoe.mirai.message.data.NullMessageChain.toString
import kotlin.js.JsName
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.reflect.KProperty

/**
 * 消息链.
 * 它的一般实现为 [MessageChainImplByIterable] 或 [MessageChainImplBySequence],
 * 替代 `null` 情况的实现为 [NullMessageChain],
 * 空的实现为 [EmptyMessageChain]
 *
 * 要获取更多信息, 请查看 [Message]
 *
 * @see buildMessageChain 构造一个 [MessageChain]
 * @see toChain 将单个 [Message] 转换为 [MessageChain]
 * @see asMessageChain 将 [Iterable] 或 [Sequence] 委托为 [MessageChain]
 *
 * @see orNull 属性委托扩展
 * @see orElse 属性委托扩展
 * @see getValue 属性委托扩展
 * @see flatten 扁平化
 */
interface MessageChain : Message, Iterable<SingleMessage> {
    override operator fun contains(sub: String): Boolean
    override fun toString(): String

    /**
     * 获取第一个类型为 [key] 的 [Message] 实例
     *
     * @param key 由各个类型消息的伴生对象持有. 如 [PlainText.Key]
     * @throws NoSuchElementException 当找不到这个类型的 [Message] 时
     */
    operator fun <M : Message> get(key: Message.Key<M>): M = first(key)

    /**
     * 获取第一个类型为 [key] 的 [Message] 实例, 找不到则返回 `null`
     *
     * @param key 由各个类型消息的伴生对象持有. 如 [PlainText.Key]
     */
    fun <M : Message> getOrNull(key: Message.Key<M>): M? = firstOrNull(key)
}

// region accessors

/**
 * 遍历每一个有内容的消息, 即 [At], [AtAll], [PlainText], [Image], [Face], [XMLMessage]
 */
inline fun MessageChain.foreachContent(block: (Message) -> Unit) {
    var last: Message? = null
    this.forEach { message: Message ->
        if (message is At) { // 筛除因 QuoteReply 导致的多余的 At
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
 * 获取第一个 [M] 类型的 [Message] 实例
 */
inline fun <reified M : Message?> MessageChain.firstOrNull(): M? = this.firstOrNull { it is M } as M?

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
fun <M : Message> MessageChain.first(key: Message.Key<M>): M =
    firstOrNull(key) ?: throw NoSuchElementException("no such element: $key")

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.any(key: Message.Key<M>): Boolean = firstOrNull(key) != null

// endregion accessors


// region delegate

/**
 * 提供一个类型的值的委托. 若不存在则会抛出异常 [NoSuchElementException]
 *
 * 用法:
 * ```
 * val message: MessageChain
 *
 * val at: At by message
 * val image: Image by message
 */
inline operator fun <reified T : Message> MessageChain.getValue(thisRef: Any?, property: KProperty<*>): T = this.first()

/**
 * 可空的委托
 * @see orNull
 */
inline class OrNullDelegate<out R : Message?>(private val value: Any?) {
    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: Any?, property: KProperty<*>): R = value as R
}

/**
 * 提供一个类型的 [Message] 的委托, 若不存在这个类型的 [Message] 则委托会提供 `null`
 *
 * 用法:
 * ```
 * val message: MessageChain
 *
 * val at: At? by message.orNull()
 * ```
 * @see orNull 提供一个不存在则 null 的委托
 * @see orElse 提供一个不存在则使用默认值的委托
 */
inline fun <reified T : Message> MessageChain.orNull(): OrNullDelegate<T?> = OrNullDelegate(this.firstOrNull<T>())

/**
 * 提供一个类型的 [Message] 的委托, 若不存在这个类型的 [Message] 则委托会提供 `null`
 *
 * 用法:
 * ```
 * val message: MessageChain
 *
 * val at: At by message.orElse { /* 返回一个 At */  }
 * val atNullable: At? by message.orElse { /* 返回一个 At? */  }
 * ```
 * @see orNull 提供一个不存在则 null 的委托
 */
inline fun <reified T : Message?> MessageChain.orElse(
    lazyDefault: () -> T
): OrNullDelegate<T> =
    OrNullDelegate<T>(this.firstOrNull<T>() ?: lazyDefault())

// endregion delegate


// region converters

/**
 * 返回 [EmptyMessageChain]
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
    else MessageChainImplBySequence(messages.asSequence().flatten())

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
        else -> MessageChainImplBySequence(message.flatten())
    }

/**
 * 构造 [MessageChain]
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(messages: Iterable<Message>): MessageChain =
    MessageChainImplBySequence(messages.flatten())

/**
 * [扁平化][flatten] [messages] 然后构造 [MessageChain]
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(messages: List<Message>): MessageChain =
    MessageChainImplByIterable(messages.flatten().asIterable())


/**
 * 得到包含 [this] 的 [MessageChain].
 *
 * 若 [this] 为 [MessageChain] 将直接返回 this,
 * 若 [this] 为 [CombinedMessage] 将 [扁平化][flatten] 后委托为 [MessageChain],
 * 否则将调用 [MessageChain] 构造一个 [MessageChainImplByIterable]
 */
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
@JvmSynthetic
fun Message.toChain(): MessageChain = when (this) {
    is MessageChain -> this
    is CombinedMessage -> MessageChainImplByIterable((this as Iterable<Message>).flatten().asIterable())
    else -> SingleMessageChainImpl(this as SingleMessage)
}

/**
 * 直接将 [this] 委托为一个 [MessageChain]
 */
@JvmName("asMessageChain1")
@JvmSynthetic
@Suppress("unused", "NOTHING_TO_INLINE")
fun Iterable<SingleMessage>.asMessageChain(): MessageChain = MessageChainImplByIterable(this)

/**
 * 将 [this] [扁平化后][flatten] 委托为一个 [MessageChain]
 */
@JvmSynthetic
@Suppress("unused", "NOTHING_TO_INLINE")
fun Iterable<Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

/**
 * 扁平化消息序列.
 *
 * 原 [this]:
 * ```
 * A <- CombinedMessage(B, C) <- D <- MessageChain(E, F, G)
 * ```
 * 结果 [Sequence]:
 * ```
 * A <- B <- C <- D <- E <- F <- G
 * ```
 */
fun Iterable<Message>.flatten(): Sequence<SingleMessage> = asSequence().flatten()

/**
 * 扁平化消息序列.
 *
 * 原 [this]:
 * ```
 * A <- CombinedMessage(B, C) <- D <- MessageChain(E, F, G)
 * ```
 * 结果 [Sequence]:
 * ```
 * A <- B <- C <- D <- E <- F <- G
 * ```
 */
fun Sequence<Message>.flatten(): Sequence<SingleMessage> = flatMap { it.flatten() }

/**
 * 扁平化 [Message]
 *
 * 对于不同类型的接收者（receiver）:
 * - `CombinedMessage(A, B)` 返回 `A <- B`
 * - `MessageChain(E, F, G)` 返回 `E <- F <- G`
 * - 其他: 返回 `sequenceOf(this)`
 */
fun Message.flatten(): Sequence<SingleMessage> {
    return when (this) {
        is MessageChain -> this.asSequence()
        is CombinedMessage -> this.asSequence().flatten()
        else -> sequenceOf(this as SingleMessage)
    }
}

// endregion converters

// region implementations

/**
 * 不含任何元素的 [MessageChain]
 */
object EmptyMessageChain : MessageChain by MessageChainImplByIterable(emptyList())

/**
 * Null 的 [MessageChain].
 * 它不包含任何元素, 也没有创建任何 list.
 *
 * 除 [toString] 和 [equals] 外, 其他方法均 [error]
 */
object NullMessageChain : MessageChain {
    override fun toString(): String = "NullMessageChain"
    override fun equals(other: Any?): Boolean = other == null
    override fun contains(sub: String): Boolean = error("accessing NullMessageChain")
    override fun followedBy(tail: Message): CombinedMessage = CombinedMessage(left = EmptyMessageChain, element = tail)
    override fun iterator(): MutableIterator<SingleMessage> = error("accessing NullMessageChain")
}

/**
 * 使用 [Iterable] 作为委托的 [MessageChain]
 */
@PublishedApi
internal inline class MessageChainImplByIterable constructor(
    private val delegate: Iterable<SingleMessage>
) : Message, Iterable<SingleMessage>, MessageChain {
    override fun iterator(): Iterator<SingleMessage> = delegate.iterator()
    override fun toString(): String = this.delegate.joinToString("") { it.toString() }
    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
}

/**
 * 使用 [Iterable] 作为委托的 [MessageChain]
 */
@PublishedApi
internal inline class MessageChainImplBySequence constructor(
    private val delegate: Sequence<SingleMessage>
) : Message, Iterable<SingleMessage>, MessageChain {
    override fun iterator(): Iterator<SingleMessage> = delegate.iterator()
    override fun toString(): String = this.delegate.joinToString("") { it.toString() }
    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
}

/**
 * 单个 [SingleMessage] 作为 [MessageChain]
 */
@PublishedApi
internal inline class SingleMessageChainImpl constructor(
    private val delegate: SingleMessage
) : Message, Iterable<SingleMessage>, MessageChain {
    override fun toString(): String = this.delegate.toString()
    override fun iterator(): Iterator<SingleMessage> = iterator { yield(delegate) }
    override operator fun contains(sub: String): Boolean = sub in delegate
}

// endregion