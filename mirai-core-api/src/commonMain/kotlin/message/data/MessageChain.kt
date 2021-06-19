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
import net.mamoe.mirai.message.code.MiraiCode.deserializeMiraiCode
import net.mamoe.mirai.message.data.MessageChain.Companion.deserializeFromMiraiCode
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.MessageSource.Key.recallIn
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast
import java.util.stream.Stream
import kotlin.reflect.KProperty
import kotlin.streams.asSequence

/**
 * 消息链, `List<SingleMessage>`, 即 [单个消息元素][SingleMessage] 的有序集合.
 *
 * [MessageChain] 代表一条完整的聊天中的消息, 可包含 [带内容的消息 `MessageContent`][MessageContent]
 * 和 [不带内容的元数据 `MessageMetadata`][MessageMetadata].
 *
 * # 元素类型
 *
 * [MessageContent] 如 [纯文字][PlainText], [图片][Image], [语音][Voice], 是能被用户看到的内容.
 *
 *
 * [MessageMetadata] 是用来形容这条消息的状态的数据, 因此称为 *元数据 (metadata)*.
 * 元数据目前只分为 [消息来源 `MessageSource`][MessageSource] 和 [引用回复 `QuoteReply`][QuoteReply].
 *
 * [MessageSource] 存储这条消息的发送人, 接收人, 识别 ID (服务器提供), 发送时间等信息.
 * **[MessageSource] 是精确的**. 凭 [MessageSource] 就可以在服务器上定位一条消息, 因此可以用来 [撤回消息][MessageSource.recall].
 *
 * [QuoteReply] 是一个标记, 表示这条消息引用了另一条消息 (在官方客户端中可通过 "回复" 功能发起引用). [QuoteReply.source] 则指代那条被引用的消息.
 * 由于 [MessageSource] 是精确的, 如果对 [QuoteReply.source] 使用 [MessageSource.recall], 则可以撤回那条被引用的消息.
 *
 *
 * # 获得消息链
 *
 * 在消息事件中可以获得消息内容作为 [MessageChain]: [MessageEvent.message]
 *
 * 在主动发送消息时, 可使用如下方案.
 *
 * ## 在 Kotlin 构造消息链
 * - 获取不包含任何元素的消息链: [EmptyMessageChain]
 * - [messageChainOf][messageChainOf]: 类似 [listOf], 将多个 [Message] 构造为 [MessageChain]:
 *   ```
 *   val chain = messageChainOf(PlainText("..."), Image("..."), ...)
 *   ```
 * - [buildMessageChain][buildMessageChain]: 使用 DSL 构建器.
 *   ```
 *   val chain = buildMessageChain {
 *      +"你想要的图片是:"
 *      +Image("...")
 *   }
 *   ```
 * - [Message.plus][Message.plus]: 将两个消息相连成为一个消息链:
 *   ```
 *   val chain = PlainText("Hello ") + PlainText("Mirai!") // chain: MessageChain
 *   ```
 * - [toMessageChain][toMessageChain] 将 [Iterable], [Array], [Sequence], [Iterator], [Flow], [Stream] 转换为 [MessageChain].
 *   相关定义为:
 *   ```
 *   public fun Sequence<Message>.toMessageChain(): MessageChain
 *   public fun Iterable<Message>.toMessageChain(): MessageChain
 *   public fun Iterator<Message>.toMessageChain(): MessageChain
 *   public fun Stream<Message>.toMessageChain(): MessageChain
 *   public fun Flow<Message>.toMessageChain(): MessageChain
 *   public fun Array<Message>.toMessageChain(): MessageChain
 *   ```
 * - [Message.toMessageChain][Message.toMessageChain] 将单个 [Message] 包装成一个单元素的 [MessageChain]
 *
 * ## 在 Java 构造消息链
 * - `MessageUtils.newChain`: 有多个重载, 相关定义如下:
 *   ```java
 *   public static MessageChain newChain(Message messages...)
 *   public static MessageChain newChain(Iterable<Message> iterable)
 *   public static MessageChain newChain(Iterator<Message> iterator)
 *   public static MessageChain newChain(Stream<Message> stream)
 *   public static MessageChain newChain(Message[] array)
 *   ```
 * - [Message.plus][Message.plus]: 将两个消息相连成为一个消息链:
 *   ```java
 *   MessageChain chain = new PlainText("Hello ").plus(new PlainText("Mirai!"))
 *   ```
 * - [MessageChainBuilder][MessageChainBuilder]:
 *   ```java
 *   MessageChainBuilder builder = MessageChainBuilder.create();
 *   builder.append(new PlainText("Hello "));
 *   builder.append(new PlainText(" Mirai!"));
 *   MessageChain chain = builder.build();
 *   ```
 *
 * # 元素唯一性
 *
 * 部分消息类型如 [语音][Voice], [小程序][LightApp] 在官方客户端限制中只允许单独存在于一条消息. 在创建 [MessageChain] 时这种限制会被体现.
 *
 * 当添加只允许单独存在的消息元素到一个消息链时, 已有的元素可能会被删除或替换. 详见 [AbstractPolymorphicMessageKey] 和 [ConstrainSingle].
 *
 * # 操作消息链
 *
 * [MessageChain] 继承 `List<SingleMessage>`. 可以以 [List] 的方式处理 [MessageChain].
 *
 * 额外地, 若要获取一个 [ConstrainSingle] 的元素, 可以通过 [ConstrainSingle.key]:
 * ```
 * val quote = chain[QuoteReply] // Kotlin
 *
 * QuoteReply quote = chain.get(QuoteReply.Key) // Java
 * ```
 *
 * 相关地还可以使用 [MessageChain.contains] 和 [MessageChain.getOrFail]
 *
 * ## 直接索引访问
 *
 * [MessageChain] 实现接口 [List], 可以通过索引 `get(index)` 来访问. 由于 [MessageChain] 是稳定的, 这种访问操作也是稳定的.
 *
 * 但在处理来自服务器的 [MessageChain] 时, 请尽量避免这种直接索引访问. 来自服务器的消息的组成有可能会变化, 可能会有新的 [MessageMetadata] 加入.
 * 例如用户发送了两条内容相同的消息, 但其中一条带有引用回复而另一条没有, 则两条消息的索引可能有变化 (当然内容顺序不会改变, 只是 [QuoteReply] 的位置有可能会变化).
 * 因此在使用直接索引访问时要格外注意兼容性, 故不推荐这种访问方案.
 *
 * ## 撤回和引用
 *
 * 要撤回消息, 查看 [MessageSource]
 *
 * - [MessageSource.quote]
 * - [MessageSource.recall]
 * - [MessageSource.recallIn]
 * - `MessageChain.quote`
 * - `MessageChain.recall`
 * - `MessageChain.recallIn`
 *
 * ## Kotlin 扩展
 *
 * ### 属性委托
 * ```
 * val at: At? by chain.orNull()
 * val at: At by chain.orElse { /* 返回一个 At */ }
 * val at: At by chain
 * ```
 *
 * ### 筛选得到 [Sequence] 与 [List]
 * - [MessageChain.contentsSequence]
 * - [MessageChain.metadataSequence]
 * - [MessageChain.contentsList]
 * - [MessageChain.metadataList]
 *
 * # 序列化
 *
 * ## kotlinx-serialization 序列化
 *
 * - 使用 [MessageChain.serializeToJsonString] 将 [MessageChain] 序列化为 JSON [String].
 * - 使用 [MessageChain.deserializeFromJsonString] 将 JSON [String] 反序列化为 [MessageChain].
 *
 * ## Mirai Code 序列化
 *
 * 详见 [MiraiCode]
 *
 * - 使用 [MessageChain.serializeToMiraiCode] 将 [MessageChain] 序列化为 Mirai Code [String].
 * - 使用 [MessageChain.deserializeFromMiraiCode] 将 Mirai Code [String] 反序列化为 [MessageChain].
 *
 */
@Serializable(MessageChain.Serializer::class)
public sealed interface MessageChain :
    Message, List<SingleMessage>, RandomAccess, CodableMessage {

    /**
     * 获取第一个类型为 [key] 的 [Message] 实例. 若不存在此实例, 返回 `null`.
     *
     * 此方法目前仅适用于 [ConstrainSingle] 的消息类型, 如 [MessageSource].
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

    /**
     * 当存在 [ConstrainSingle.key] 为 [key] 的 [SingleMessage] 实例时返回 `true`.
     *
     * 此方法目前仅适用于 [ConstrainSingle] 的消息类型, 如 [MessageSource].
     *
     * ### Kotlin 使用方法
     * ```
     * val chain: MessageChain = ...
     *
     * if (chain.contains(QuoteReply)) {
     *     // 包含引用回复
     * }
     * ```
     *
     * ### Java 使用方法
     * ```java
     * MessageChain chain = ...
     * if (chain.contains(QuoteReply.Key)) {
     *     // 包含引用回复
     * }
     * ```
     *
     * @param key 由各个类型消息的伴生对象持有. 如 [MessageSource.Key]
     *
     * @see MessageChain.getOrFail 在找不到此类型的元素时抛出 [NoSuchElementException]
     */
    public operator fun <M : SingleMessage> contains(key: MessageKey<M>): Boolean =
        asSequence().any { key.safeCast.invoke(it) != null }

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        forEach { it.safeCast<CodableMessage>()?.appendMiraiCodeTo(builder) }
    }

    /**
     * 将 [MessageChain] 作为 `List<SingleMessage>` 序列化. 使用 [多态序列化][Polymorphic].
     *
     * 在实践时请提供 [MessageSerializers.serializersModule] 到指定 [SerialFormat].
     *
     * @see ListSerializer
     * @see MessageSerializers
     */
    public object Serializer : KSerializer<MessageChain> {
        @Suppress("DEPRECATION_ERROR")
        private val delegate = ListSerializer(PolymorphicSerializer(SingleMessage::class))
        override val descriptor: SerialDescriptor = delegate.descriptor
        override fun deserialize(decoder: Decoder): MessageChain = delegate.deserialize(decoder).toMessageChain()
        override fun serialize(encoder: Encoder, value: MessageChain): Unit = delegate.serialize(encoder, value)
    }

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
         * 解析形如 "[mirai:]" 的 mirai 码, 即 [CodableMessage.serializeToMiraiCode] 返回的内容.
         * @see MiraiCode.deserializeMiraiCode
         */
        @JvmStatic
        public fun MessageChain.deserializeFromMiraiCode(miraiCode: String, contact: Contact? = null): MessageChain =
            miraiCode.deserializeMiraiCode(contact)
    }
}

/**
 * 不含任何元素的 [MessageChain].
 */
@Serializable(MessageChain.Serializer::class)
public object EmptyMessageChain : MessageChain, List<SingleMessage> by emptyList() {
    override val size: Int get() = 0

    override fun toString(): String = ""
    override fun contentToString(): String = ""
    override fun serializeToMiraiCode(): String = ""

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
    }

    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = 1

    override fun iterator(): Iterator<SingleMessage> = EmptyMessageChainIterator

    private object EmptyMessageChainIterator : Iterator<SingleMessage> {
        override fun hasNext(): Boolean = false
        override fun next(): Nothing = throw NoSuchElementException("EmptyMessageChain is empty.")
    }
}

// region accessors

/**
 * 获取第一个类型为 [key] 的 [Message] 实例, 在找不到此类型的元素时抛出 [NoSuchElementException]
 *
 * @param key 由各个类型消息的伴生对象持有. 如 [MessageSource.Key]
 */
@JvmSynthetic
public inline fun <M : SingleMessage> MessageChain.getOrFail(
    key: MessageKey<M>,
    crossinline lazyMessage: (key: MessageKey<M>) -> String = { key.toString() }
): M = get(key) ?: throw NoSuchElementException(lazyMessage(key))

/**
 * 获取 `Sequence<MessageContent>`
 * 相当于 `this.asSequence().filterIsInstance<MessageContent>()`
 */
@JvmSynthetic
public fun MessageChain.contentsSequence(): Sequence<MessageContent> =
    this.asSequence().filterIsInstance<MessageContent>()

/**
 * 获取 `Sequence<MessageMetadata>`
 * 相当于 `this.asSequence().filterIsInstance<MessageMetadata>()`
 */
@JvmSynthetic
public fun MessageChain.metadataSequence(): Sequence<MessageMetadata> =
    this.asSequence().filterIsInstance<MessageMetadata>()

/**
 * 筛选 [MessageMetadata]
 */
public fun MessageChain.metadataList(): List<MessageMetadata> = this.filterIsInstance<MessageMetadata>()

/**
 * 筛选 [MessageContent]
 */
public fun MessageChain.contentsList(): List<MessageContent> = this.filterIsInstance<MessageContent>()


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
 *
 * ```
 * val chain = messageChainOf(messageChainOf(AtAll, new PlainText("")), messageChainOf(Image(""), QuoteReply()))
 * ```
 * 将会得到 `chain` 为 `[AtAll, PlainText, Image, QuoteReply]`
 *
 * @see buildMessageChain
 */
@JvmName("newChain")
public inline fun messageChainOf(vararg messages: Message): MessageChain = messages.toMessageChain()

/**
 * 扁平化 [this] 并创建一个 [MessageChain].
 */
@JvmName("newChain")
public fun Sequence<Message>.toMessageChain(): MessageChain =
    createMessageChainImplOptimized(this.constrainSingleMessages())

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
 * ```
 */
@JvmSynthetic
public inline operator fun <reified T : SingleMessage> MessageChain.getValue(thisRef: Any?, property: KProperty<*>): T =
    this.firstIsInstance()

/**
 * 可空的委托
 * @see orNull
 */
@JvmInline
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
public value class OrNullDelegate<out R> @PublishedApi internal constructor(@JvmField @PublishedApi internal val value: Any?) {
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