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
import kotlin.reflect.KProperty

/**
 * 消息链. 即 MutableList<Message>.
 * 它的一般实现为 [MessageChainImpl], `null` 实现为 [NullMessageChain]
 *
 * 有关 [MessageChain] 的创建和连接:
 * - 当任意两个不是 [MessageChain] 的 [Message] 相连接后, 将会产生一个 [MessageChain].
 * - 若两个 [MessageChain] 连接, 后一个将会被合并到第一个内.
 * - 若一个 [MessageChain] 与一个其他 [Message] 连接, [Message] 将会被添加入 [MessageChain].
 * - 若一个 [Message] 与一个 [MessageChain] 连接, [Message] 将会被添加入 [MessageChain].
 *
 * 要获取更多信息, 请查看 [Message]
 *
 * @see buildMessageChain
 */
interface MessageChain : Message, List<Message> {
    // region Message override
    override operator fun contains(sub: String): Boolean
    // endregion

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
    this.forEachIndexed { index: Int, message: Message ->
        if (message is At) {
            if (index == 0 || this[index - 1] !is QuoteReply) {
                block(message)
            }
        } else if (message.hasContent()) {
            block(message)
        }
    }
}

/**
 * 判断这个 [Message] 是否含有内容, 即是否为 [At], [AtAll], [PlainText], [Image], [Face], [XMLMessage]
 */
fun Message.hasContent(): Boolean {
    return when (this) {
        is At,
        is AtAll,
        is PlainText,
        is Image,
        is Face,
        is XMLMessage -> true
        else -> false
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
    else MessageChainImpl(messages.toMutableList())

/**
 * 构造 [MessageChain] 的快速途径 (无 [Array] 创建)
 * 若仅提供一个参数, 请考虑使用 [Message.toChain] 以优化性能
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(message: Message): MessageChain =
    MessageChainImpl(listOf(message))

/**
 * 构造 [MessageChain]
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(messages: Iterable<Message>): MessageChain =
    MessageChainImpl(messages.toList())

/**
 * 构造 [MessageChain]
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("FunctionName")
fun MessageChain(messages: List<Message>): MessageChain =
    MessageChainImpl(messages)


/**
 * 得到包含 [this] 的 [MessageChain].
 * 若 [this] 为 [MessageChain] 将直接返回 this
 * 否则将调用 [MessageChain] 构造一个 [MessageChainImpl]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Message.toChain(): MessageChain = if (this is MessageChain) this else MessageChain(this)

/**
 * 构造 [MessageChain]
 */
@Suppress("unused", "NOTHING_TO_INLINE")
inline fun List<Message>.asMessageChain(): MessageChain = MessageChain(this)

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

object EmptyMessageChain : MessageChain by {
    MessageChainImpl(emptyList())
}()

/**
 * Null 的 [MessageChain].
 * 它不包含任何元素, 也没有创建任何 list.
 *
 * 除 [toString] 外, 其他方法均 [error]
 */
object NullMessageChain : MessageChain {
    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Message> = error("accessing NullMessageChain")

    override fun toString(): String = "null"

    override fun contains(sub: String): Boolean = error("accessing NullMessageChain")
    override fun contains(element: Message): Boolean = error("accessing NullMessageChain")
    override fun followedBy(tail: Message): CombinedMessage = CombinedMessage(left = EmptyMessageChain, element = tail)

    override val size: Int get() = error("accessing NullMessageChain")
    override fun containsAll(elements: Collection<Message>): Boolean = error("accessing NullMessageChain")
    override fun get(index: Int): Message = error("accessing NullMessageChain")
    override fun indexOf(element: Message): Int = error("accessing NullMessageChain")
    override fun isEmpty(): Boolean = error("accessing NullMessageChain")
    override fun iterator(): MutableIterator<Message> = error("accessing NullMessageChain")

    override fun lastIndexOf(element: Message): Int = error("accessing NullMessageChain")
    override fun listIterator(): MutableListIterator<Message> = error("accessing NullMessageChain")

    override fun listIterator(index: Int): MutableListIterator<Message> = error("accessing NullMessageChain")
}

/**
 * [MessageChain] 实现
 * 它是一个特殊的 [Message], 实现 [MutableList] 接口, 但将所有的接口调用都转到内部维护的另一个 [MutableList].
 */
internal class MessageChainImpl constructor(
    /**
     * Elements will not be instances of [MessageChain]
     */
    private val delegate: List<Message>
) : Message, List<Message> by delegate, MessageChain {
    override fun toString(): String = this.delegate.joinToString("") { it.toString() }

    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
    override fun followedBy(tail: Message): CombinedMessage {
        require(tail !is SingleOnly) { "SingleOnly Message cannot follow another message" }
        // if (tail is MessageChain) tail.forEach { child -> this.followedBy(child) }
        // else this.delegate.add(tail)
        return CombinedMessage(tail, this)
    }
}

