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
@file:Suppress("NOTHING_TO_INLINE", "unused", "INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "UnUsedImport")

package net.mamoe.mirai.message.data

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.MessageSourceSerializerImpl
import net.mamoe.mirai.message.data.MessageSource.Key.isAboutFriend
import net.mamoe.mirai.message.data.MessageSource.Key.isAboutGroup
import net.mamoe.mirai.message.data.MessageSource.Key.isAboutStranger
import net.mamoe.mirai.message.data.MessageSource.Key.isAboutTemp
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.LazyProperty
import net.mamoe.mirai.utils.safeCast

/**
 * 消息源. 消息源存在于 [MessageChain] 中, 用于表示这个消息的来源, 也可以用来分辨 [MessageChain].
 *
 * 对于来自 [MessageEvent.message] 的 [MessageChain]
 *
 *
 * ## 组成
 * [MessageSource] 由 metadata (元数据), form & target, content 组成
 *
 * ### metadata
 * - [ids] 消息 ids (序列号)
 * - [internalIds] 消息内部 ids
 * - [time] 时间
 *
 * 官方客户端通过 metadata 这三个数据定位消息, 撤回和引用回复都是如此.
 *
 * ### form & target
 * - [fromId] 消息发送人
 * - [targetId] 消息发送目标
 *
 * ### content
 * - [originalMessage] 消息内容
 *
 * ## 使用
 *
 * 消息源可用于 [引用回复][QuoteReply] 或 [撤回][IMirai.recallMessage].
 *
 * @see IMirai.recallMessage 撤回一条消息
 * @see MessageSource.quote 引用这条消息, 创建 [MessageChain]
 *
 * @see OnlineMessageSource 在线消息的 [MessageSource]
 * @see OfflineMessageSource 离线消息的 [MessageSource]
 *
 * @see buildMessageSource 构造一个 [OfflineMessageSource]
 */
@Serializable(MessageSourceSerializerImpl.Companion::class)
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
     * ### 值域
     * 值的范围约为 [UShort] 的范围.
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
     * 时间戳可能来自服务器, 也可能来自 mirai, 且无法保证两者时间同步.
     */
    public abstract val time: Int

    /**
     * 发送人.
     *
     * - 当 [OnlineMessageSource.Outgoing] 时为 [机器人][Bot.id]
     * - 当 [OnlineMessageSource.Incoming] 时为发信 [目标好友][Friend.id] 或 [群][Group.id]
     * - 当 [OfflineMessageSource] 时为 [机器人][Bot.id], 发信 [目标好友][Friend.id] 或 [群][Group.id] (取决于 [OfflineMessageSource.kind])
     */
    public abstract val fromId: Long

    /**
     * 消息发送目标.
     *
     * - 当 [OnlineMessageSource.Outgoing] 时为发信 [目标好友][Friend.id] 或 [群][Group.id] 或 [临时消息][Member.id]
     * - 当 [OnlineMessageSource.Incoming] 时为 [机器人][Bot.id]
     * - 当 [OfflineMessageSource] 时为 [机器人][Bot.id], 发信 [目标好友][Friend.id] 或 [群][Group.id] 或 [临时消息][Member.id] (取决于 [OfflineMessageSource.kind])
     */
    public abstract val targetId: Long // groupCode / friendUin / memberUin

    /**
     * 原消息内容.
     *
     * 此属性是 **lazy** 的: 它只会在第一次调用时初始化, 因为需要反序列化服务器发来的整个包, 相当于接收了一条新消息.
     */
    @LazyProperty
    public abstract val originalMessage: MessageChain

    /**
     * 返回 `"[mirai:source:${ids.contentToString()},${internalIds.contentToString()}]"`
     */
    public final override fun toString(): String =
        "[mirai:source:${ids.contentToString()},${internalIds.contentToString()}]"

    public companion object Key : AbstractMessageKey<MessageSource>({ it.safeCast() }) {
        /**
         * 撤回这条消息. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
         *
         * **注意:** 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以撤回.
         *
         * *提示: 若要撤回一条机器人自己发出的消息, 使用 [Contact.sendMessage] 返回的 [MessageReceipt] 中的 [MessageReceipt.recall]*
         *
         * [Bot] 撤回自己的消息不需要权限.
         * [Bot] 撤回群员的消息需要管理员权限.
         *
         * @throws PermissionDeniedException 当 [Bot] 无权限操作时
         * @throws IllegalStateException 当这条消息已经被撤回时 (仅同步主动操作)
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
         * 撤回这条消息. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
         *
         * **注意:** 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以撤回.
         *
         * *提示: 若要撤回一条机器人自己发出的消息, 使用 [Contact.sendMessage] 返回的 [MessageReceipt] 中的 [MessageReceipt.recall]*
         *
         * [Bot] 撤回自己的消息不需要权限.
         * [Bot] 撤回群员的消息需要管理员权限.
         *
         * @throws PermissionDeniedException 当 [Bot] 无权限操作时
         * @throws IllegalStateException 当这条消息已经被撤回时 (仅同步主动操作)
         *
         * @see IMirai.recallMessage
         */
        @JvmStatic
        @JvmBlockingBridge
        public suspend inline fun MessageChain.recall(): Unit = this.source.recall()

        /**
         * 在一段时间后撤回这条消息.
         *
         * @see IMirai.recallMessage
         */
        @JvmStatic
        @Suppress("DeferredIsResult")
        public fun MessageChain.recallIn(millis: Long): Deferred<Unit> = this.source.recallIn(millis)

        /**
         * 在一段时间后撤回这条消息.
         *
         * @see IMirai.recallMessage
         */
        @JvmStatic
        @Suppress("DeferredIsResult")
        public fun MessageSource.recallIn(millis: Long): Deferred<Unit> {
            return bot.async {
                delay(millis)
                Mirai.recallMessage(bot, this@recallIn)
            }
        }

        /**
         * 判断是否是发送给群, 或从群接收的消息的消息源
         */
        @JvmStatic
        public fun MessageSource.isAboutGroup(): Boolean {
            return when (this) {
                is OnlineMessageSource -> subject is Group
                is OfflineMessageSource -> kind == MessageSourceKind.GROUP
            }
        }

        /**
         * 判断是否是发送给陌生人 或从陌生人接收的消息的消息源
         */
        @JvmStatic
        public fun MessageSource.isAboutStranger(): Boolean {
            return when (this) {
                is OnlineMessageSource -> subject is Stranger
                is OfflineMessageSource -> kind == MessageSourceKind.STRANGER
            }
        }

        /**
         * 判断是否是发送给临时会话, 或从临时会话接收的消息的消息源
         */
        @JvmStatic
        public fun MessageSource.isAboutTemp(): Boolean {
            return when (this) {
                is OnlineMessageSource -> subject is Member
                is OfflineMessageSource -> kind == MessageSourceKind.TEMP
            }
        }

        /**
         * 判断是否是发送给好友, 或从好友接收的消息的消息源
         */
        @JvmStatic
        public inline fun MessageSource.isAboutFriend(): Boolean {
            return when (this) {
                is OnlineMessageSource -> subject !is Group && subject !is Member
                is OfflineMessageSource -> kind == MessageSourceKind.FRIEND
            }
        }

        /**
         * 引用这条消息
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

public inline val MessageSource.bot: Bot
    get() = when (this) {
        is OnlineMessageSource -> bot
        is OfflineMessageSource -> Bot.getInstance(botId)
    }

public inline val MessageSource.botOrNull: Bot?
    get() = when (this) {
        is OnlineMessageSource -> bot
        is OfflineMessageSource -> Bot.getInstanceOrNull(botId)
    }

/**
 * 在线消息的 [MessageSource].
 * 拥有对象化的 [sender], [target], 也可以直接 [recallMessage] 和 [quote]
 *
 * ### 来源
 * - 当 bot 主动发送消息时, 产生 (由协议模块主动构造) [OnlineMessageSource.Outgoing]
 * - 当 bot 接收消息时, 产生 (由协议模块根据服务器的提供的信息构造) [OnlineMessageSource.Incoming]
 *
 * #### 机器人主动发送消息
 * 当机器人 [主动发出消息][Member.sendMessage], 将会得到一个 [消息回执][MessageReceipt].
 * 此回执的 [消息源][MessageReceipt.source] 即为一个 [外向消息源][OnlineMessageSource.Outgoing], 代表着刚刚发出的那条消息的来源.
 *
 * #### 机器人接受消息
 * 当机器人接收一条消息 [MessageEvent], 这条消息包含一个 [内向消息源][OnlineMessageSource.Incoming], 代表着接收到的这条消息的来源.
 *
 *
 * ### 实现
 * 此类的所有子类都有协议模块实现. 不要自行实现它们, 否则将无法发送
 *
 * @see OnlineMessageSource.toOffline 转为 [OfflineMessageSource]
 */
public sealed class OnlineMessageSource : MessageSource() {
    public companion object Key : AbstractMessageKey<OnlineMessageSource>({ it.safeCast() })

    /**
     * @see botId
     */
    public abstract val bot: Bot
    final override val botId: Long get() = bot.id

    /**
     * 消息发送人. 可能为 [机器人][Bot] 或 [好友][Friend] 或 [群员][Member].
     * 即类型必定为 [Bot], [Friend] 或 [Member]
     */
    public abstract val sender: ContactOrBot

    /**
     * 消息发送目标. 可能为 [机器人][Bot] 或 [好友][Friend] 或 [群][Group].
     * 即类型必定为 [Bot], [Friend] 或 [Group]
     */
    public abstract val target: ContactOrBot

    /**
     * 消息主体. 群消息时为 [Group]. 好友消息时为 [Friend], 临时消息为 [Member]
     * 不论是机器人接收的消息还是发送的消息, 此属性都指向机器人能进行回复的目标.
     */
    public abstract val subject: Contact

    /*
     * 以下子类型仅是覆盖了 [target], [subject], [sender] 等的类型
     */

    /**
     * 由 [机器人主动发送消息][Contact.sendMessage] 产生的 [MessageSource], 可通过 [MessageReceipt] 获得.
     */
    public sealed class Outgoing : OnlineMessageSource() {
        public companion object Key :
            AbstractPolymorphicMessageKey<OnlineMessageSource, Outgoing>(OnlineMessageSource, { it.safeCast() })

        public abstract override val sender: Bot
        public abstract override val target: Contact

        public final override val fromId: Long get() = sender.id
        public final override val targetId: Long get() = target.id

        public abstract class ToFriend : Outgoing() {
            public companion object Key : AbstractPolymorphicMessageKey<Outgoing, ToFriend>(Outgoing, { it.safeCast() })

            public abstract override val target: Friend
            public final override val subject: Friend get() = target
            //  final override fun toString(): String = "OnlineMessageSource.ToFriend(target=${target.ids})"
        }

        public abstract class ToStranger : Outgoing() {
            public companion object Key : AbstractPolymorphicMessageKey<Outgoing, ToStranger>(Outgoing, { it.safeCast() })

            public abstract override val target: Stranger
            public final override val subject: Stranger get() = target
            //  final override fun toString(): String = "OnlineMessageSource.ToFriend(target=${target.ids})"
        }

        public abstract class ToTemp : Outgoing() {
            public companion object Key : AbstractPolymorphicMessageKey<Outgoing, ToTemp>(Outgoing, { it.safeCast() })

            public abstract override val target: Member
            public val group: Group get() = target.group
            public final override val subject: Member get() = target
        }

        public abstract class ToGroup : Outgoing() {
            public companion object Key : AbstractPolymorphicMessageKey<Outgoing, ToGroup>(Outgoing, { it.safeCast() })

            public abstract override val target: Group
            public final override val subject: Group get() = target
        }
    }

    /**
     * 接收到的一条消息的 [MessageSource]
     */
    public sealed class Incoming : OnlineMessageSource() {
        public abstract override val sender: User

        public final override val fromId: Long get() = sender.id
        public final override val targetId: Long get() = target.id

        public abstract class FromFriend : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromFriend>(Incoming, { it.safeCast() })

            public abstract override val sender: Friend
            public final override val subject: Friend get() = sender
            public final override val target: Bot get() = sender.bot
            // final override fun toString(): String = "OnlineMessageSource.FromFriend(from=${sender.ids})"
        }

        public abstract class FromTemp : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromTemp>(Incoming, { it.safeCast() })

            public abstract override val sender: Member
            public inline val group: Group get() = sender.group
            public final override val subject: Member get() = sender
            public final override val target: Bot get() = sender.bot
        }

        public abstract class FromStranger : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromStranger>(Incoming, { it.safeCast() })

            public abstract override val sender: Stranger
            public final override val subject: Stranger get() = sender
            public final override val target: Bot get() = sender.bot
        }

        public abstract class FromGroup : Incoming() {
            public companion object Key :
                AbstractPolymorphicMessageKey<Incoming, FromGroup>(Incoming, { it.safeCast() })

            public abstract override val sender: Member
            public final override val subject: Group get() = sender.group
            public final override val target: Group get() = group
            public inline val group: Group get() = sender.group
        }

        public companion object Key :
            AbstractPolymorphicMessageKey<OnlineMessageSource, FromTemp>(OnlineMessageSource, { it.safeCast() })
    }
}

/**
 * 由一条消息中的 [QuoteReply] 得到的 [MessageSource].
 * 此消息源可能来自一条与机器人无关的消息. 因此无法提供对象化的 `sender` 或 `target` 获取.
 *
 * @see buildMessageSource 构建一个 [OfflineMessageSource]
 */
public abstract class OfflineMessageSource : MessageSource() {
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageSource, OfflineMessageSource>(MessageSource, { it.safeCast() })

    /**
     * 消息种类
     */
    public abstract val kind: MessageSourceKind
}

@Serializable
public enum class MessageSourceKind {
    GROUP,
    FRIEND,
    TEMP,
    STRANGER
}

public val MessageSource.kind: MessageSourceKind
    get() = when (this) {
        is OnlineMessageSource -> kind
        is OfflineMessageSource -> kind
    }

public val OnlineMessageSource.kind: MessageSourceKind
    get() = when {
        isAboutGroup() -> MessageSourceKind.GROUP
        isAboutFriend() -> MessageSourceKind.FRIEND
        isAboutTemp() -> MessageSourceKind.TEMP
        isAboutStranger() -> MessageSourceKind.STRANGER
        else -> error("Internal error: OnlineMessageSource.kind reached an unexpected clause")
    }

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
