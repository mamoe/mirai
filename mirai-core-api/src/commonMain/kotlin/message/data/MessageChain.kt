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
@file:Suppress("unused", "NOTHING_TO_INLINE", "INAPPLICABLE_JVM_NAME")

package net.mamoe.mirai.message.data

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.safeCast
import kotlin.js.JsName
import kotlin.reflect.KProperty

/**
 * 消息链. 空的实现为 [EmptyMessageChain]
 *
 * 要获取更多消息相关的信息, 查看 [Message]
 *
 * ### 构造消息链
 * - [buildMessageChain][buildMessageChain]: 使用构建器
 * - [Message.plus][Message.plus]: 将两个消息相连成为一个消息链
 * - [asMessageChain][asMessageChain] 将 [Iterable], [Array] 等类型消息转换为 [MessageChain]
 * - [messageChainOf][messageChainOf] 类似 [listOf], 将多个 [Message] 构造为 [MessageChain]
 *
 * @see get 获取消息链中一个类型的元素, 不存在时返回 `null`
 * @see getOrFail 获取消息链中一个类型的元素, 不存在时抛出异常 [NoSuchElementException]
 * @see MessageSource.quote 引用这条消息
 * @see MessageSource.recall 撤回这条消息 (仅限来自 [MessageEvent] 的消息)
 *
 * @see buildMessageChain 构造一个 [MessageChain]
 * @see asMessageChain 将单个 [Message] 转换为 [MessageChain]
 * @see asMessageChain 将 [Iterable] 或 [Sequence] 委托为 [MessageChain]
 *
 * @see forEachContent 遍历内容
 * @see allContent 判断是否每一个 [MessageContent] 都满足条件
 * @see noneContent 判断是否每一个 [MessageContent] 都不满足条件
 *
 * @see orNull 属性委托扩展
 * @see orElse 属性委托扩展
 * @see getValue 属性委托扩展
 * @see flatten 扁平化
 */
@Serializable(MessageChain.Serializer::class)
@Suppress("FunctionName", "DeprecatedCallableAddReplaceWith", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public interface MessageChain : Message, List<SingleMessage>, RandomAccess, CodableMessage {
    /**
     * 元素数量. [EmptyMessageChain] 不参加计数.
     */
    public override val size: Int

    /**
     * 获取第一个类型为 [key] 的 [Message] 实例. 若不存在此实例, 返回 `null`.
     *
     * 此方法仅适用于 [ConstrainSingle] 的消息类型, 如 [MessageSource]
     *
     * ### Kotlin 使用方法
     * ```
     * val chain: MessageChain = ...
     *
     * val source = chain[MessageSource] // MessageSource 为伴生对象
     * ```
     *
     * ### Java 使用方法
     * ```java
     * MessageChain chain = ...
     * chain.get(MessageSource.Key)
     * ```
     *
     * @param key 由各个类型消息的伴生对象持有. 如 [MessageSource.Key]
     *
     * @see MessageChain.getOrFail 在找不到此类型的元素时抛出 [NoSuchElementException]
     */
    public operator fun <M : SingleMessage> get(key: MessageKey<M>): M? =
        asSequence().mapNotNull { key.safeCast.invoke(it) }.firstOrNull()

    public operator fun <M : SingleMessage> contains(key: MessageKey<M>): Boolean =
        asSequence().any { key.safeCast.invoke(it) != null }

    public object Serializer : KSerializer<MessageChain> {
        @Suppress("DEPRECATION_ERROR")
        private val delegate = ListSerializer<Message>(Message.Serializer)
        override val descriptor: SerialDescriptor = delegate.descriptor
        override fun deserialize(decoder: Decoder): MessageChain = delegate.deserialize(decoder).asMessageChain()
        override fun serialize(encoder: Encoder, value: MessageChain): Unit = delegate.serialize(encoder, value)
    }

    override fun appendMiraiCode(builder: StringBuilder) {
        forEach { it.safeCast<CodableMessage>()?.appendMiraiCode(builder) }
    }

    @Suppress("DEPRECATION_ERROR")
    public companion object {
        /**
         * 从 JSON 字符串解析 [MessageChain]
         * @see serializeToJsonString
         */
        @Deprecated("消息序列化仍未稳定，请在 2.0-RC 再使用", level = DeprecationLevel.HIDDEN)
        @JvmOverloads
        @JvmStatic
        public fun deserializeFromJsonString(
            string: String,
            json: Json = Json { serializersModule = Message.Serializer.serializersModule }
        ): MessageChain {
            return json.decodeFromString(Serializer, string)
        }

        /**
         * 将 [MessageChain] 序列化为 JSON 字符串.
         * @see deserializeFromJsonString
         */
        @Deprecated("消息序列化仍未稳定，请在 2.0-RC 再使用", level = DeprecationLevel.HIDDEN)
        @JvmOverloads
        @JvmStatic
        public fun MessageChain.serializeToJsonString(
            json: Json = Json { serializersModule = Message.Serializer.serializersModule }
        ): String = json.encodeToString(Message.Serializer, this)

        /**
         * 将 [MessageChain] 序列化为指定格式的字符串.
         *
         * @see serializeToJsonString
         * @see StringFormat.encodeToString
         */
        @Deprecated("消息序列化仍未稳定，请在 2.0-RC 再使用", level = DeprecationLevel.HIDDEN)
        @ExperimentalSerializationApi
        @JvmStatic
        public fun MessageChain.serializeToString(format: StringFormat): String =
            format.encodeToString(Serializer, this)
    }
}

// region accessors

/**
 * 获取第一个类型为 [key] 的 [Message] 实例, 在找不到此类型的元素时抛出 [NoSuchElementException]
 *
 * @param key 由各个类型消息的伴生对象持有. 如 [MessageSource.Key]
 */
@JvmOverloads
public inline fun <M : SingleMessage> MessageChain.getOrFail(
    key: MessageKey<M>,
    crossinline lazyMessage: (key: MessageKey<M>) -> String = { key.toString() }
): M = get(key) ?: throw NoSuchElementException(lazyMessage(key))


/**
 * 遍历每一个 [消息内容][MessageContent]
 */
@JvmSynthetic
public inline fun MessageChain.forEachContent(block: (MessageContent) -> Unit) {
    for (element in this) {
        if (element !is MessageMetadata) {
            check(element is MessageContent) { "internal error: Message must be either MessageMetadata or MessageContent" }
            block(element)
        }
    }
}

/**
 * 如果每一个 [消息内容][MessageContent] 都满足 [block], 返回 `true`
 */
@JvmSynthetic
public inline fun MessageChain.allContent(block: (MessageContent) -> Boolean): Boolean {
    this.forEach {
        if (it !is MessageMetadata) {
            check(it is MessageContent) { "internal error: Message must be either MessageMetadata or MessageContent" }
            if (!block(it)) return false
        }
    }
    return true
}

/**
 * 如果每一个 [消息内容][MessageContent] 都不满足 [block], 返回 `true`
 */
@JvmSynthetic
public inline fun MessageChain.noneContent(block: (MessageContent) -> Boolean): Boolean {
    this.forEach {
        if (it !is MessageMetadata) {
            check(it is MessageContent) { "internal error: Message must be either MessageMetadata or MessageContent" }
            if (block(it)) return false
        }
    }
    return true
}


/**
 * 获取第一个 [M] 实例. 在不存在时返回 `null`.
 */
@JvmSynthetic
public inline fun <reified M : SingleMessage?> MessageChain.findIsInstance(): M? =
    this.find { it is M } as M?

/**
 * 获取第一个 [M] 实例. 在不存在时返回 `null`.
 * @see findIsInstance
 */
@JvmSynthetic
public inline fun <reified M : SingleMessage?> MessageChain.firstIsInstanceOrNull(): M? =
    this.find { it is M } as M?

/**
 * 获取第一个 [M] 实例. 在不存在时抛出 [NoSuchElementException].
 * @see findIsInstance
 */
@JvmSynthetic
public inline fun <reified M : SingleMessage> MessageChain.firstIsInstance(): M = this.first { it is M } as M

/**
 * 当 [this] 中存在 [M] 的实例时返回 `true`.
 */
@JvmSynthetic
public inline fun <reified M : SingleMessage> MessageChain.anyIsInstance(): Boolean = this.any { it is M }

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
public inline operator fun <reified T : SingleMessage> MessageChain.getValue(thisRef: Any?, property: KProperty<*>): T =
    this.firstIsInstance()

/**
 * 可空的委托
 * @see orNull
 */
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
public inline class OrNullDelegate<out R> @PublishedApi internal constructor(@JvmField @PublishedApi internal val value: Any?) {
    @Suppress("UNCHECKED_CAST") // don't inline, IC error
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): R = value as R
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
public inline fun <reified T : SingleMessage> MessageChain.orNull(): OrNullDelegate<T?> =
    OrNullDelegate(this.firstIsInstanceOrNull<T>())

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
public inline fun <reified T : R, R : SingleMessage?> MessageChain.orElse(
    lazyDefault: () -> R
): OrNullDelegate<R> = OrNullDelegate<R>(this.firstIsInstanceOrNull<T>() ?: lazyDefault())

// endregion delegate


// region asMessageChain

/**
 * 返回一个包含 [messages] 所有元素的消息链, 保留顺序.
 */
@JvmName("newChain")
public inline fun messageChainOf(vararg messages: Message): MessageChain = messages.asMessageChain()

/**
 * 得到包含 [this] 的 [MessageChain].
 */
@JvmName("newChain")
@JsName("newChain")
@Suppress("UNCHECKED_CAST")
public fun Message.asMessageChain(): MessageChain = when (this) {
    is MessageChain -> this
    else -> SingleMessageChainImpl(this as SingleMessage)
}

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
public fun SingleMessage.asMessageChain(): MessageChain = SingleMessageChainImpl(this)

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
public fun Collection<SingleMessage>.asMessageChain(): MessageChain =
    MessageChainImpl(this.constrainSingleMessages())

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
@JvmSynthetic
@JvmName("newChain1")
// @JsName("newChain")
public fun Array<out Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

@JvmSynthetic
@JvmName("newChain2")
public fun Array<out SingleMessage>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.asSequence())

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
@JvmName("newChain")
// @JsName("newChain")
public fun Collection<Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
public fun Iterable<SingleMessage>.asMessageChain(): MessageChain =
    MessageChainImpl(this.constrainSingleMessages())

@JvmSynthetic
public inline fun MessageChain.asMessageChain(): MessageChain = this // 避免套娃

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
@JvmName("newChain")
// @JsName("newChain")
public fun Iterable<Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

/**
 * 直接将 [this] 构建为一个 [MessageChain]
 */
@JvmSynthetic
public fun Sequence<SingleMessage>.asMessageChain(): MessageChain = MessageChainImplBySequence(this)

/**
 * 将 [this] [扁平化后][flatten] 构建为一个 [MessageChain]
 */
@JvmName("newChain")
// @JsName("newChain")
public fun Sequence<Message>.asMessageChain(): MessageChain = MessageChainImplBySequence(this.flatten())

/**
 * 扁平化消息序列.
 *
 * 原 [this]:
 * ```
 * A <- MessageChain(B, C) <- D <- MessageChain(E, F, G)
 * ```
 * 结果 [Sequence]:
 * ```
 * A <- B <- C <- D <- E <- F <- G
 * ```
 */
public inline fun Iterable<Message>.flatten(): Sequence<SingleMessage> = asSequence().flatten()

// @JsName("flatten1")
@JvmName("flatten1")// avoid platform declare clash
@JvmSynthetic
public inline fun Iterable<SingleMessage>.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path

/**
 * 扁平化消息序列.
 *
 * 原 [this]:
 * ```
 * A <- MessageChain(B, C) <- D <- MessageChain(E, F, G)
 * ```
 * 结果 [Sequence]:
 * ```
 * A <- B <- C <- D <- E <- F <- G
 * ```
 */
public inline fun Sequence<Message>.flatten(): Sequence<SingleMessage> = flatMap { it.flatten() }

@JsName("flatten1") // avoid platform declare clash
@JvmName("flatten1")
@JvmSynthetic
public inline fun Sequence<SingleMessage>.flatten(): Sequence<SingleMessage> = this // fast path

public inline fun Array<out Message>.flatten(): Sequence<SingleMessage> = this.asSequence().flatten()

public inline fun Array<out SingleMessage>.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path

/**
 * 返回 [MessageChain.asSequence] 或 `sequenceOf(this as SingleMessage)`
 */
public fun Message.flatten(): Sequence<SingleMessage> {
    return when (this) {
        is MessageChain -> this.asSequence()
        else -> sequenceOf(this as SingleMessage)
    }
}

@JvmSynthetic // make Java user happier with less methods
public inline fun MessageChain.flatten(): Sequence<SingleMessage> = this.asSequence() // fast path

// endregion converters


/**
 * 不含任何元素的 [MessageChain].
 */
public object EmptyMessageChain : MessageChain, Iterator<SingleMessage>, List<SingleMessage> by emptyList() {

    public override val size: Int get() = 0
    public override fun toString(): String = ""
    public override fun contentToString(): String = ""
    public override fun equals(other: Any?): Boolean = other === this

    public override fun iterator(): Iterator<SingleMessage> = this
    public override fun hasNext(): Boolean = false
    public override fun next(): SingleMessage = throw NoSuchElementException("EmptyMessageChain is empty.")
    override fun toMiraiCode(): String = ""
    override fun appendMiraiCode(builder: StringBuilder) {}
}