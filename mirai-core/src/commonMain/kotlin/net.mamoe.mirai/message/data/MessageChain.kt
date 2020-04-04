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
@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.message.data.NullMessageChain.equals
import net.mamoe.mirai.message.data.NullMessageChain.toString
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai
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
 * @see asMessageChain 将单个 [Message] 转换为 [MessageChain]
 * @see asMessageChain 将 [Iterable] 或 [Sequence] 委托为 [MessageChain]
 *
 * @see foreachContent 遍历内容
 *
 * @see orNull 属性委托扩展
 * @see orElse 属性委托扩展
 * @see getValue 属性委托扩展
 * @see flatten 扁平化
 */
interface MessageChain : Message, Iterable<SingleMessage> {
    override operator fun contains(sub: String): Boolean

    /**
     * 得到易读的字符串
     */
    override fun toString(): String

    /**
     * 元素数量
     */
    @SinceMirai("0.31.1")
    val size: Int

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

    /**
     * 遍历每一个有内容的消息, 即 [At], [AtAll], [PlainText], [Image], [Face], [XmlMessage], [QuoteReply].
     * 仅供 `Java` 使用
     */
    @Suppress("FunctionName", "INAPPLICABLE_JVM_NAME")
    @JsName("forEachContent")
    @JvmName("forEachContent")
    @MiraiInternalAPI
    fun `__forEachContent for Java__`(block: (Message) -> Unit) {
        this.foreachContent(block)
    }

    /**
     * 遍历每一个消息, 即 [MessageSource] [At], [AtAll], [PlainText], [Image], [Face], [XmlMessage], [QuoteReply].
     * 仅供 `Java` 使用
     */
    @Suppress("FunctionName", "INAPPLICABLE_JVM_NAME")
    @JsName("forEach")
    @JvmName("forEach")
    @MiraiInternalAPI
    fun `__forEach for Java__`(block: (Message) -> Unit) {
        this.forEach(block)
    }
}

// region accessors

/**
 * 遍历每一个有内容的消息, 即 [At], [AtAll], [PlainText], [Image], [Face], [XmlMessage], [QuoteReply]
 */
@JvmSynthetic
inline fun MessageChain.foreachContent(block: (Message) -> Unit) {
    this.forEach {
        if (it !is MessageMetadata) block(it)
    }
}

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@JvmSynthetic
inline fun <reified M : Message?> MessageChain.firstOrNull(): M? = this.firstOrNull { it is M } as M?

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 * @throws [NoSuchElementException] 如果找不到该类型的实例
 */
@JvmSynthetic
inline fun <reified M : Message> MessageChain.first(): M = this.first { it is M } as M

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@JvmSynthetic
inline fun <reified M : Message> MessageChain.any(): Boolean = this.any { it is M }


/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@OptIn(MiraiExperimentalAPI::class)
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
fun <M : Message> MessageChain.firstOrNull(key: Message.Key<M>): M? = when (key) {
    At -> firstOrNull<At>()
    AtAll -> firstOrNull<AtAll>()
    PlainText -> firstOrNull<PlainText>()
    Image -> firstOrNull<Image>()
    OnlineImage -> firstOrNull<OnlineImage>()
    OfflineImage -> firstOrNull<OfflineImage>()
    GroupImage -> firstOrNull<GroupImage>()
    FriendImage -> firstOrNull<FriendImage>()
    Face -> firstOrNull<Face>()
    QuoteReply -> firstOrNull<QuoteReply>()
    MessageSource -> firstOrNull<MessageSource>()
    XmlMessage -> firstOrNull<XmlMessage>()
    JsonMessage -> firstOrNull<JsonMessage>()
    RichMessage -> firstOrNull<RichMessage>()
    LightApp -> firstOrNull<LightApp>()
    PokeMessage -> firstOrNull<PokeMessage>()
    HummerMessage -> firstOrNull<HummerMessage>()
    FlashImage -> firstOrNull<FlashImage>()
    GroupFlashImage -> firstOrNull<GroupFlashImage>()
    FriendFlashImage -> firstOrNull<FriendFlashImage>()
    else -> null
} as M?

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 * @throws [NoSuchElementException] 如果找不到该类型的实例
 */
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
inline fun <M : Message> MessageChain.first(key: Message.Key<M>): M =
    firstOrNull(key) ?: throw NoSuchElementException("no such element: $key")

/**
 * 获取第一个 [M] 类型的 [Message] 实例
 */
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
inline fun <M : Message> MessageChain.any(key: Message.Key<M>): Boolean = firstOrNull(key) != null

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
@JvmSynthetic
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
@JvmSynthetic
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
@Suppress("RemoveExplicitTypeArguments")
@JvmSynthetic
inline fun <reified T : Message?> MessageChain.orElse(
    lazyDefault: () -> T
): OrNullDelegate<T> =
    OrNullDelegate<T>(this.firstOrNull<T>() ?: lazyDefault())

// endregion delegate


// region converters
/**
 * 得到包含 [this] 的 [MessageChain].
 *
 * 若 [this] 为 [MessageChain] 将直接返回 this,
 * 若 [this] 为 [CombinedMessage] 将 [扁平化][flatten] 后委托为 [MessageChain],
 * 否则将调用 [asMessageChain]
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("UNCHECKED_CAST")
fun Message.asMessageChain(): MessageChain = when (this) {
    is MessageChain -> this
    is CombinedMessage -> (this as Iterable<Message>).asMessageChain()
    else -> SingleMessageChainImpl(this as SingleMessage)
}

/**
 * 直接将 [this] 委托为一个 [MessageChain]
 */
@JvmSynthetic
inline fun Collection<SingleMessage>.asMessageChain(): MessageChain = MessageChainImplByCollection(this)

/**
 * 将 [this] [扁平化后][flatten] 委托为一个 [MessageChain]
 */
@JvmName("newChain")
// @JsName("newChain")
inline fun Collection<Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

/**
 * 直接将 [this] 委托为一个 [MessageChain]
 */
@JvmSynthetic
inline fun Iterable<SingleMessage>.asMessageChain(): MessageChain = MessageChainImplByIterable(this)

@JvmSynthetic
inline fun MessageChain.asMessageChain(): MessageChain = this // 避免套娃

@JvmSynthetic
fun CombinedMessage.asMessageChain(): MessageChain {
    if (left is SingleMessage && this.tail is SingleMessage) {
        @Suppress("UNCHECKED_CAST")
        return (this as Iterable<SingleMessage>).asMessageChain()
    }
    return (this as Iterable<Message>).asMessageChain()
} // 避免套娃

/**
 * 将 [this] [扁平化后][flatten] 委托为一个 [MessageChain]
 */
@JvmName("newChain")
// @JsName("newChain")
inline fun Iterable<Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

/**
 * 直接将 [this] 委托为一个 [MessageChain]
 */
@JvmSynthetic
inline fun Sequence<SingleMessage>.asMessageChain(): MessageChain = MessageChainImplBySequence(this)

/**
 * 将 [this] [扁平化后][flatten] 委托为一个 [MessageChain]
 */
@JvmName("newChain")
// @JsName("newChain")
inline fun Sequence<Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

/**
 * 构造一个 [MessageChain]
 * 为提供更好的 Java API.
 */
@Suppress("FunctionName")
@JvmName("newChain")
fun _____newChain______(vararg messages: Message): MessageChain {
    return messages.asIterable().asMessageChain()
}

/**
 * 构造一个 [MessageChain]
 * 为提供更好的 Java API.
 */
@Suppress("FunctionName")
@JvmName("newChain")
fun _____newChain______(messages: String): MessageChain {
    return messages.toMessage().asMessageChain()
}

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
inline fun Iterable<Message>.flatten(): Sequence<SingleMessage> = asSequence().flatten()

// @JsName("flatten1")
@JvmName("flatten1")// avoid platform declare clash
@JvmSynthetic
inline fun Iterable<SingleMessage>.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path

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
inline fun Sequence<Message>.flatten(): Sequence<SingleMessage> = flatMap { it.flatten() }

@JsName("flatten1") // avoid platform declare clash
@JvmName("flatten1")
@JvmSynthetic
inline fun Sequence<SingleMessage>.flatten(): Sequence<SingleMessage> = this // fast path

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
        is CombinedMessage -> this.flatten()
        else -> sequenceOf(this as SingleMessage)
    }
}

fun CombinedMessage.flatten(): Sequence<SingleMessage> {
    if (this.isFlat()) {
        @Suppress("UNCHECKED_CAST")
        return (this as Iterable<SingleMessage>).asSequence()
    } else return this.asSequence().flatten()
}

inline fun MessageChain.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path

// endregion converters


/**
 * 不含任何元素的 [MessageChain]
 */
object EmptyMessageChain : MessageChain by MessageChainImplByCollection(emptyList())

/**
 * Null 的 [MessageChain].
 * 它不包含任何元素, 也没有创建任何 list.
 *
 * 除 [toString] 和 [equals] 外, 其他方法均 [error]
 */
object NullMessageChain : MessageChain {
    override fun toString(): String = "NullMessageChain"
    override val size: Int get() = 0
    override fun equals(other: Any?): Boolean = other === this
    override fun contains(sub: String): Boolean = error("accessing NullMessageChain")
    override fun followedBy(tail: Message): CombinedMessage = CombinedMessage(left = EmptyMessageChain, tail = tail)
    override fun iterator(): MutableIterator<SingleMessage> = error("accessing NullMessageChain")
}


// region implementations


/**
 * 使用 [Iterable] 作为委托的 [MessageChain]
 */
@PublishedApi
internal class MessageChainImplByIterable constructor(
    private val delegate: Iterable<SingleMessage>
) : Message, Iterable<SingleMessage>, MessageChain {
    override val size: Int by lazy { delegate.count() }
    override fun iterator(): Iterator<SingleMessage> = delegate.iterator()
    private var toStringTemp: String? = null
    override fun toString(): String =
        toStringTemp ?: this.delegate.joinToString("") { it.toString() }.also { toStringTemp = it }

    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
}

/**
 * 使用 [Collection] 作为委托的 [MessageChain]
 */
@PublishedApi
internal class MessageChainImplByCollection constructor(
    private val delegate: Collection<SingleMessage>
) : Message, Iterable<SingleMessage>, MessageChain {
    override val size: Int get() = delegate.size
    override fun iterator(): Iterator<SingleMessage> = delegate.iterator()
    private var toStringTemp: String? = null
    override fun toString(): String =
        toStringTemp ?: this.delegate.joinToString("") { it.toString() }.also { toStringTemp = it }

    override operator fun contains(sub: String): Boolean = delegate.any { it.contains(sub) }
}

/**
 * 使用 [Iterable] 作为委托的 [MessageChain]
 */
@PublishedApi
internal class MessageChainImplBySequence constructor(
    delegate: Sequence<SingleMessage>
) : Message, Iterable<SingleMessage>, MessageChain {
    override val size: Int by lazy { collected.size }

    /**
     * [Sequence] 可能只能消耗一遍, 因此需要先转为 [List]
     */
    private val collected: List<SingleMessage> by lazy { delegate.toList() }
    override fun iterator(): Iterator<SingleMessage> = collected.iterator()
    private var toStringTemp: String? = null
    override fun toString(): String =
        toStringTemp ?: this.collected.joinToString("") { it.toString() }.also { toStringTemp = it }

    override operator fun contains(sub: String): Boolean = collected.any { it.contains(sub) }
}

/**
 * 单个 [SingleMessage] 作为 [MessageChain]
 */
@PublishedApi
internal class SingleMessageChainImpl constructor(
    private val delegate: SingleMessage
) : Message, Iterable<SingleMessage>, MessageChain {
    override val size: Int get() = 1
    override fun toString(): String = this.delegate.toString()
    override fun iterator(): Iterator<SingleMessage> = iterator { yield(delegate) }
    override operator fun contains(sub: String): Boolean = sub in delegate
}

// endregion