/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.code.MiraiCode.parseMiraiCode
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast
import java.util.stream.Stream
import kotlin.reflect.KProperty
import kotlin.streams.asSequence

/**
 * 消息链. 空的实现为 [EmptyMessageChain]
 *
 * 要获取更多消息相关的信息, 查看 [Message]
 *
 * ### 构造消息链
 * - [buildMessageChain][buildMessageChain]: 使用构建器
 * - [Message.plus][Message.plus]: 将两个消息相连成为一个消息链
 * - [toMessageChain][toMessageChain] 将 [Iterable], [Array] 等类型转换为 [MessageChain]
 * - [messageChainOf][messageChainOf] 类似 [listOf], 将多个 [Message] 构造为 [MessageChain]
 *
 * @see get 获取消息链中一个类型的元素, 不存在时返回 `null`
 * @see getOrFail 获取消息链中一个类型的元素, 不存在时抛出异常 [NoSuchElementException]
 * @see MessageSource.quote 引用这条消息
 * @see MessageSource.recall 撤回这条消息 (仅限来自 [MessageEvent] 的消息)
 *
 * @see buildMessageChain 构造一个 [MessageChain]
 * @see Message.toMessageChain 将单个 [Message] 转换为 [MessageChain]
 * @see toMessageChain 将 [Iterable] 或 [Sequence] 委托为 [MessageChain]
 *
 * @see forEachContent 遍历内容
 * @see allContent 判断是否每一个 [MessageContent] 都满足条件
 * @see noneContent 判断是否每一个 [MessageContent] 都不满足条件
 *
 * @see orNull 属性委托扩展
 * @see orElse 属性委托扩展
 * @see getValue 属性委托扩展
 * @see flatten 扁平化
 *
 * @see MiraiCode mirai 码
 */
@Serializable(MessageChain.Serializer::class)
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

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        forEach { it.safeCast<CodableMessage>()?.appendMiraiCodeTo(builder) }
    }

    @kotlinx.serialization.Serializer(MessageChain::class)
    public object Serializer : KSerializer<MessageChain> {
        @Suppress("DEPRECATION_ERROR")
        private val delegate = ListSerializer(PolymorphicSerializer(SingleMessage::class))
        override val descriptor: SerialDescriptor = delegate.descriptor
        override fun deserialize(decoder: Decoder): MessageChain = delegate.deserialize(decoder).toMessageChain()
        override fun serialize(encoder: Encoder, value: MessageChain): Unit = delegate.serialize(encoder, value)
    }

    @Suppress("DEPRECATION_ERROR")
    public companion object {
        private fun getDefaultJson() = Json {
            serializersModule =
                MessageSerializers.serializersModule // don't convert to property, serializersModule is volatile.
            ignoreUnknownKeys = true
        }

        /**
         * 从 JSON 字符串解析 [MessageChain]
         * @param json 需要包含 [MessageSerializers.serializersModule]
         * @see serializeToJsonString
         */
        @JvmOverloads
        @JvmStatic
        public fun deserializeFromJsonString(
            string: String,
            json: Json = getDefaultJson()
        ): MessageChain {
            return json.decodeFromString(Serializer, string)
        }

        /**
         * 从 JSON 字符串解析 [MessageChain]
         * @param json 需要包含 [MessageSerializers.serializersModule]
         * @see deserializeFromJsonString
         * @see serializeToJsonString
         */
        @JvmSynthetic
        @JvmStatic
        public inline fun String.deserializeJsonToMessageChain(json: Json): MessageChain =
            deserializeFromJsonString(this, json)

        /**
         * 从 JSON 字符串解析 [MessageChain]
         * @see serializeToJsonString
         * @see deserializeFromJsonString
         */
        @JvmSynthetic
        @JvmStatic
        public inline fun String.deserializeJsonToMessageChain(): MessageChain = deserializeFromJsonString(this)

        /**
         * 将 [MessageChain] 序列化为 JSON 字符串.
         * @see deserializeFromJsonString
         */
        @JvmOverloads
        @JvmStatic
        public fun MessageChain.serializeToJsonString(
            json: Json = getDefaultJson()
        ): String = json.encodeToString(Serializer, this)

        /**
         * 将 [MessageChain] 序列化为指定格式的字符串.
         *
         * @see serializeToJsonString
         * @see StringFormat.encodeToString
         */
        @ExperimentalSerializationApi
        @JvmStatic
        public fun MessageChain.serializeToString(format: StringFormat): String =
            format.encodeToString(Serializer, this)

        /**
         * 解析形如 "[mirai:]" 的 mirai 码, 即 [CodableMessage.toMiraiCode] 返回的内容.
         * @see MiraiCode.parseMiraiCode
         */
        @JvmStatic
        public fun MessageChain.parseFromMiraiCode(miraiCode: String, contact: Contact? = null): MessageChain =
            miraiCode.parseMiraiCode(contact)
    }
}

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

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
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


// region toMessageChain

/**
 * 返回一个包含 [messages] 所有元素的消息链, 保留顺序.
 * @see buildMessageChain
 */
@JvmName("newChain")
public inline fun messageChainOf(vararg messages: Message): MessageChain = messages.toMessageChain()

/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmName("newChain")
public fun Sequence<Message>.toMessageChain(): MessageChain = MessageChainImpl(this.constrainSingleMessages())

/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmName("newChain")
public fun Stream<Message>.toMessageChain(): MessageChain = this.asSequence().toMessageChain()

/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmName("newChain")
public suspend fun Flow<Message>.toMessageChain(): MessageChain =
    buildMessageChain { collect(::add) }

/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmName("newChain")
public inline fun Iterable<Message>.toMessageChain(): MessageChain = this.asSequence().toMessageChain()

/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmName("newChain")
public inline fun Iterator<Message>.toMessageChain(): MessageChain = this.asSequence().toMessageChain()

/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmSynthetic
// no JvmName because 'fun messageChainOf(vararg messages: Message)'
public inline fun Array<out Message>.toMessageChain(): MessageChain = this.asSequence().toMessageChain()

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "UNCHECKED_CAST")
@kotlin.internal.LowPriorityInOverloadResolution // prefer Iterable<Message>.toMessageChain() for MessageChain
@JvmName("newChain")
public fun Message.toMessageChain(): MessageChain = when (this) {
    is MessageChain -> (this as List<SingleMessage>).toMessageChain()
    else -> MessageChainImpl(
        listOf(
            this as? SingleMessage ?: error("Message is either MessageChain nor SingleMessage: $this")
        )
    )
}


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
