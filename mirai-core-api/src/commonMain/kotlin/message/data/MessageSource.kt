/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")
@file:Suppress("unused", "INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "UnUsedImport")

package net.mamoe.mirai.message.data

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonConfiguration
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.internal.message.MessageSourceSerializerImpl
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.action.AsyncRecallResult
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.safeCast
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 表示聊天中的一条消息的定位信息, 即消息源.
 *
 * 一个[消息源][MessageSource]可用于定位一条存在于服务器中的消息, 因此可用来[撤回][recall]或[引用][quote]该消息
 *
 * 消息源可存在于 [MessageChain] 中, 用于表示这个消息的来源, 也可以用来分辨 [MessageChain].
 *
 * 消息源分为在线消息源 [OnlineMessageSource] 和离线消息源 [OfflineMessageSource].
 *
 * ## 属性组成
 * [MessageSource] 由以下属性组成:
 * - 三个*定位属性* [ids], [internalId], [time]
 * - 发送人 ID [fromId]
 * - 收信人 ID [targetId]
 * - 原消息内容 [originalMessage]
 *
 * 官方客户端通过这三个*定位属性*来准确定位消息, 撤回和引用回复都是如此 (有这三个属性才可以精确撤回和引用某个消息).
 *
 * 即使三个*定位属性*就可以知道原消息是哪一条, 但服务器和官方客户端都实现为读取 [originalMessage] 的内容.
 * 也就是说, 如果[引用][quote]一个 [MessageSource], *定位属性*只会被用来支持跳转到原消息, 引用中显示的被引用消息内容只取决于 [originalMessage].
 * 可以通过修改 [originalMessage] 来达到显示的内容与跳转内容不符合的效果. 但一般没有必要这么做.
 *
 * ## 获取消息源实例
 * - 来自 [MessageEvent.message] 的 [MessageChain] 总是包含在线消息源 [OnlineMessageSource]. 可通过 [MessageChain.get] 获取 [MessageSource]:
 *    ```kotlin
 *    // Kotlin
 *    val source: MessageSource? = chain[MessageSource]
 *    val notNull: MessageSource = chain.source // 可能抛出 NoSuchElementException
 *    ```
 *    ```java
 *    // Java
 *    MessageSource source = chain.get(MessageSource.Key);
 *    ```
 * - 构造离线消息源: [IMirai.constructMessageSource]
 * - 使用构建器构造: [MessageSourceBuilder]
 *
 * 参阅 [OnlineMessageSource] 或 [OfflineMessageSource] 可获得更详细的获取实例的方式.
 *
 * ### "修改" 一个 [MessageSource]
 * [MessageSource] 是不可变的. 因此不能修改其中属性, 但可以通过 [MessageSource.copyAmend] 或者 [MessageSourceBuilder.allFrom] 来复制一个.
 * ```java
 * MessageSource newSource = new MessageSourceBuilder()
 *     .allFrom(source) // 从 source 继承所有数据
 *     .message(new PlainText("aaa")) // 覆盖消息
 *     .build();
 * ```
 *
 * ## 使用
 *
 * 消息源可用于 [引用回复][MessageSource.quote] 或 [撤回][MessageSource.recall].
 *
 * 对于来自 [MessageEvent.message] 的 [MessageChain], 总是包含 [MessageSource].
 * 因此也可以对这样的 [MessageChain] 进行 [引用回复][MessageChain.quote] 或 [撤回][MessageChain.recall].
 *
 * ### 获取有关 [Bot] 实例
 *
 * 调用 [MessageSource.bot] 或 [MessageSource.botOrNull] 来获取有关 [Bot] 实例.
 *
 * ### Kotlin 示例
 * ```kotlin
 * val source: MessageSource = ...
 * source.recall() // 通过 MessageSource 撤回
 *
 * val event: MessageEvent = ...
 * event.message.recall() // 也可以通过来自服务器的 [MessageChain] 撤回, 因为这些 chain 包含 [MessageSource]
 * ```
 *
 * ### Java 示例
 * ```java
 * MessageSource source = ...
 * MessageSource.recall(source); // 通过 MessageSource 撤回
 *
 * MessageEvent event = ...
 * MessageSource.recall(event.message); // 也可以通过来自服务器的 [MessageChain] 撤回, 因为这些 chain 包含 [MessageSource]
 * ```
 *
 *
 * @see MessageSource.quote 引用这条消息, 创建 [MessageChain]
 *
 * @see OnlineMessageSource 在线消息的 [MessageSource]
 * @see OfflineMessageSource 离线消息的 [MessageSource]
 *
 * @see buildMessageSource 构建一个 [OfflineMessageSource]
 */
@Suppress("DEPRECATION")
@Serializable(MessageSource.Serializer::class)
public sealed class MessageSource : Message, MessageMetadata, ConstrainSingle {
    public final override val key: MessageKey<MessageSource>
        get() = Key

    /**
     * 所属 [Bot.id]
     */
    public abstract val botId: Long

    /**
     * 消息 ids (序列号). 在获取失败时 (概率很低) 为空数组.
     *
     * ### 顺序
     * 群消息的 id 由服务器维护. 好友消息的 id 由 mirai 维护.
     * 此 id 不一定从 0 开始.
     *
     * - 在同一个群的消息中此值随每条消息递增 1, 但此行为由服务器决定, mirai 不保证自增顺序.
     * - 在好友消息中无法保证每次都递增 1. 也可能会产生大幅跳过的情况.
     *
     * ### 多 ID 情况
     * 对于单条消息, [ids] 为单元素数组. 对于分片 (一种长消息处理机制) 消息, [ids] 将包含多元素.
     *
     * [internalIds] 与 [ids] 以数组下标对应.
     */
    public abstract val ids: IntArray

    /**
     * 内部 ids. **仅用于协议模块使用**
     *
     * 值没有顺序, 也可能为 0, 取决于服务器是否提供.
     *
     * 在事件中和在引用中无法保证同一条消息的 [internalIds] 相同.
     *
     * [internalIds] 与 [ids] 以数组下标对应.
     *
     * @see ids
     */
    public abstract val internalIds: IntArray

    /**
     * 发送时间时间戳, 单位为秒.
     *
     * 自 2.8.0 起, 时间戳为服务器时区 (UTC+8).
     * 在 2.8.0 以前, 时间戳可能来自服务器 (UTC+8), 也可能来自 mirai (本地), 且无法保证两者时间同步.
     */
    public abstract val time: Int

    /**
     * 发送人用户 ID.
     *
     * - 当 [OnlineMessageSource.Outgoing] 时为 [机器人][Bot.id]
     * - 当 [OnlineMessageSource.Incoming] 时为发信 [来源用户][User.id] 或 [群][Group.id]
     * - 当 [OfflineMessageSource] 时取决于 [OfflineMessageSource.kind]
     */
    public abstract val fromId: Long

    /**
     * 消息发送目标用户或群号码.
     *
     * - 当 [OnlineMessageSource.Outgoing] 时为发信 [目标用户][User.id] 或 [群][Group.id]
     * - 当 [OnlineMessageSource.Incoming] 时为 [机器人][Bot.id]
     * - 当 [OfflineMessageSource] 时取决于 [OfflineMessageSource.kind]
     */
    public abstract val targetId: Long // groupCode / friendUin / memberUin

    /**
     * 该消息源指向的原消息的内容.
     *
     * ## 内容不一定完整
     * 如果消息源是来自一条引用回复, 即 [QuoteReply.source], 那么原消息内容不一定完整.
     *
     * 此属性是惰性初始化的: 它只会在第一次调用时初始化, 因为需要反序列化服务器发来的整个包, 相当于接收了一条新消息.
     */
    public abstract val originalMessage: MessageChain // see OutgoingMessageSourceInternal.originalMessage

    /**
     * 当 [originalMessage] 已被初始化后返回 `true`.
     *
     * @since 2.12
     */
    public abstract val isOriginalMessageInitialized: Boolean

    /**
     * 消息种类
     *
     * @since 2.15
     */
    public abstract val kind: MessageSourceKind

    public abstract override fun toString(): String

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitMessageSource(this, data)
    }

    @OptIn(MiraiInternalApi::class)
    @Deprecated("Do not use this serializer. Retrieve from `MessageSerializers.serializersModule`.")
    @DeprecatedSinceMirai(warningSince = "2.13")
    public object Serializer : KSerializer<MessageSource> by MessageSourceSerializerImpl("MessageSource")

    public companion object Key : AbstractMessageKey<MessageSource>({ it.safeCast() }) {
        /**
         * 从 [MessageSerializers] 获取到的对应[序列化器][KSerializer]在参与多态序列化时的[类型标识符][JsonConfiguration.classDiscriminator]的值.
         *
         * [OnlineMessageSource] 的部分属性无法通过序列化保存. 所有 [MessageSource] 子类型在序列化时都会序列化为 [OfflineMessageSource]. 反序列化时会得到 [OfflineMessageSource] 而不是原类型.
         */
        public const val SERIAL_NAME: String = "MessageSource"

        /**
         * 以 [Bot] 身份撤回该[消息源][this]指向的存在于服务器上的消息. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
         *
         * *提示: 若要撤回一条机器人自己发出的消息, 使用 [Contact.sendMessage] 返回的 [MessageReceipt] 中的 [MessageReceipt.recall]*
         *
         * ## 需求的权限
         *
         * - [Bot] 撤回自己的消息不需要权限.
         * - [Bot] 撤回群员的消息需要管理员权限.
         *
         * @throws PermissionDeniedException 当 [Bot] 无权限操作时
         * @throws IllegalStateException 当这条消息已经被撤回时抛出
         *
         * @see IMirai.recallMessage
         */
        @JvmStatic
        @JvmBlockingBridge
        public suspend fun MessageSource.recall() {
            // don't inline, compilation error
            Mirai.recallMessage(bot, this)
        }

        /**
         * 以 [Bot] 身份撤回[该消息][this]. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
         *
         * **注意:** 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以撤回.
         *
         * *提示: 若要撤回一条机器人自己发出的消息, 使用 [Contact.sendMessage] 返回的 [MessageReceipt] 中的 [MessageReceipt.recall]*
         *
         * ## 需求的权限
         *
         * - [Bot] 撤回自己的消息不需要权限.
         * - [Bot] 撤回群员的消息需要管理员权限.
         *
         * @throws PermissionDeniedException 当 [Bot] 无权限操作时
         * @throws IllegalStateException 当这条消息已经被撤回时抛出
         *
         * @see IMirai.recallMessage
         */
        @JvmStatic
        @JvmBlockingBridge
        public suspend fun MessageChain.recall() {
            this[MessageSource]?.let {
                it.recall()
                return
            }
            throw NoSuchElementException(tipsForNoMessageSource)
        }

        private const val tipsForNoMessageSource = "No MessageSource found from input MessageChain. Tips: " +
                "You can't recall a MessageChain which is built by you, " +
                "as it lacks ids of the message on the server. " +
                "If you want to recall a message after sending it, " +
                "you can call `recallIn` method on the `MessageReceipt` returned by `sendMessage`."

        /**
         * 在一段时间后撤回这条消息.
         *
         * **注意:** 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以撤回.
         *
         * *提示: 若要撤回一条机器人自己发出的消息, 使用 [Contact.sendMessage] 返回的 [MessageReceipt] 中的 [MessageReceipt.recall]*
         *
         * ## 需求的权限
         *
         * - [Bot] 撤回自己的消息不需要权限.
         * - [Bot] 撤回群员的消息需要管理员权限.
         *
         * @return 返回撤回的异步结果. 参考 [AsyncRecallResult].
         * @see MessageChain.recall
         */
        @JvmStatic
        public fun MessageChain.recallIn(millis: Long): AsyncRecallResult {
            this[MessageSource]?.let {
                return it.recallIn(millis)
            }
            throw NoSuchElementException(tipsForNoMessageSource)
        }

        /**
         * 在一段时间后撤回这条消息.
         *
         * **注意:** 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以撤回.
         *
         * *提示: 若要撤回一条机器人自己发出的消息, 使用 [Contact.sendMessage] 返回的 [MessageReceipt] 中的 [MessageReceipt.recall]*
         *
         * ## 需求的权限
         *
         * - [Bot] 撤回自己的消息不需要权限.
         * - [Bot] 撤回群员的消息需要管理员权限.
         *
         * @return 返回撤回的异步结果. 参考 [AsyncRecallResult].
         *
         * @see MessageSource.recall
         */
        @JvmStatic
        public fun MessageSource.recallIn(millis: Long): AsyncRecallResult {
            return AsyncRecallResult(bot.async {
                try {
                    delay(millis)
                    Mirai.recallMessage(bot, this@recallIn)
                    null
                } catch (e: Throwable) {
                    e
                }
            })
        }

        /**
         * 引用这条消息.
         * @see QuoteReply
         */
        @JvmStatic
        public fun MessageSource.quote(): QuoteReply = QuoteReply(this)

        /**
         * 引用这条消息. 仅从服务器接收的消息 (即来自 [MessageEvent]) 才可以通过这个方式被引用.
         * @see QuoteReply
         */
        @JvmStatic
        public fun MessageChain.quote(): QuoteReply = QuoteReply(this.source)
    }
}


/**
 * 消息来源类型
 */
@Serializable
public enum class MessageSourceKind {
    /**
     * 群消息
     */
    GROUP,

    /**
     * 好友消息
     */
    FRIEND,

    /**
     * 来自群成员的临时会话消息
     */
    TEMP,

    /**
     * 来自陌生人的消息
     */
    STRANGER
}

/*
  public static final net.mamoe.mirai.message.data.MessageSourceKind getKind(net.mamoe.mirai.message.data.MessageSource);
  public static final net.mamoe.mirai.message.data.MessageSourceKind getKind(net.mamoe.mirai.message.data.OnlineMessageSource);
 */
@JvmName("getKind")
@Deprecated("For ABI compatibility", level = DeprecationLevel.HIDDEN)
public fun getKindLegacy(source: MessageSource): MessageSourceKind = source.kind

@JvmName("getKind")
@Deprecated("For ABI compatibility", level = DeprecationLevel.HIDDEN)
public fun getKindLegacy(source: OnlineMessageSource): MessageSourceKind = source.kind


// For MessageChain, no need to expose to Java.

/**
 * 消息 ids.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源.
 *
 * @see MessageSource.ids
 */
@get:JvmSynthetic
public inline val MessageChain.ids: IntArray
    get() = this.source.ids

/**
 * 消息内部 ids.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源.
 *
 * @see MessageSource.ids
 */
@get:JvmSynthetic
public inline val MessageChain.internalId: IntArray
    get() = this.source.internalIds

/**
 * 消息时间.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源.
 *
 * @see MessageSource.ids
 */
@get:JvmSynthetic
public inline val MessageChain.time: Int
    get() = this.source.time

/**
 * 消息内部 ids.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取. 否则将抛出异常 [NoSuchElementException]
 *
 * @see MessageSource.ids
 */
@get:JvmSynthetic
public inline val MessageChain.bot: Bot
    get() = this.source.bot

/**
 * 获取这条消息的 [消息源][MessageSource].
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源, 否则将抛出异常 [NoSuchElementException]
 *
 * @see sourceOrNull
 */
@get:JvmSynthetic
public inline val MessageChain.source: MessageSource
    get() = this.getOrFail(MessageSource)

/**
 * 获取这条消息的 [消息源][MessageSource].
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源, 否则返回 `null`
 *
 * @see source
 */
@get:JvmSynthetic
public inline val MessageChain.sourceOrNull: MessageSource?
    get() = this[MessageSource]

/**
 * 获取此消息源的相关 [Bot].
 *
 * 对于 [OnlineMessageSource], 此操作总是会成功.
 * 但对于 [OfflineMessageSource], 若此时该 [ID][Bot.id] 的 [Bot] 不存在, 则会抛出 [NoSuchElementException].
 *
 * @throws NoSuchElementException 当目标 [Bot] 不存在时抛出
 */
public inline val MessageSource.bot: Bot
    get() = when (this) {
        is OnlineMessageSource -> bot
        is OfflineMessageSource -> Bot.getInstance(botId)
    }

/**
 * 获取此消息源的相关 [Bot].
 *
 * 对于 [OnlineMessageSource], 此操作总是返回非 `null`.
 * 但对于 [OfflineMessageSource], 若此时该 [ID][Bot.id] 的 [Bot] 不存在, 则会返回 `null`.
 */
public inline val MessageSource.botOrNull: Bot?
    get() = when (this) {
        is OnlineMessageSource -> bot
        is OfflineMessageSource -> Bot.getInstanceOrNull(botId)
    }



