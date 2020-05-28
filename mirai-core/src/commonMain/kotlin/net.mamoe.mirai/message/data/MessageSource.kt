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
@file:Suppress("NOTHING_TO_INLINE", "unused", "INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR", "UnUsedImport")

package net.mamoe.mirai.message.data

import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.quote
import net.mamoe.mirai.message.recall
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.LazyProperty
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 消息源. 消息源存在于 [MessageChain] 中, 用于表示这个消息的来源, 也可以用来分辨 [MessageChain].
 *
 * 对于来自 [MessageEvent.message] 的 [MessageChain]
 *
 *
 * ### 组成
 * [MessageSource] 由 metadata (元数据), form & target, content 组成
 *
 * #### metadata
 * - [id] 消息 id (序列号)
 * - [internalId] 消息内部 id
 * - [time] 时间
 *
 * 官方客户端通过 metadata 这三个数据定位消息, 撤回和引用回复都是如此.
 *
 * #### form & target
 * - [fromId] 消息发送人
 * - [targetId] 消息发送目标
 *
 * #### content
 * - [originalMessage] 消息内容
 *
 * ### 使用
 *
 * 消息源可用于 [引用回复][QuoteReply] 或 [撤回][Bot.recall].
 *
 * @see Bot.recall 撤回一条消息
 * @see MessageSource.quote 引用这条消息, 创建 [MessageChain]
 *
 * @see OnlineMessageSource 在线消息的 [MessageSource]
 * @see OfflineMessageSource 离线消息的 [MessageSource]
 *
 * @see buildMessageSource 构造一个 [OfflineMessageSource]
 */
sealed class MessageSource : Message, MessageMetadata, ConstrainSingle<MessageSource> {
    companion object Key : Message.Key<MessageSource> {
        override val typeName: String get() = "MessageSource"
    }

    final override val key: Message.Key<MessageSource> get() = Key

    /**
     * 所属 [Bot]
     */
    abstract val bot: Bot

    /**
     * 消息 id (序列号). 在获取失败时 (概率很低) 为 `-1`.
     **
     * #### 值域
     * 值的范围约为 [UShort] 的范围.
     *
     * #### 顺序
     * 群消息的 id 由服务器维护. 好友消息的 id 由 mirai 维护.
     * 此 id 不一定从 0 开始.
     *
     * - 在同一个群的消息中此值随每条消息递增 1, 但此行为由服务器决定, mirai 不保证自增顺序.
     * - 在好友消息中无法保证每次都递增 1. 也可能会产生大幅跳过的情况.
     */
    abstract val id: Int

    /**
     * 内部 id. **仅用于协议模块使用**
     *
     * 值没有顺序, 也可能为 0, 取决于服务器是否提供.
     *
     * 在事件中和在引用中无法保证同一条消息的 [internalId] 相同.
     */
    abstract val internalId: Int

    /**
     * 发送时间时间戳, 单位为秒.
     *
     * 时间戳可能来自服务器, 也可能来自 mirai, 且无法保证两者时间同步.
     */
    abstract val time: Int

    /**
     * 发送人.
     *
     * - 当 [OnlineMessageSource.Outgoing] 时为 [机器人][Bot.id]
     * - 当 [OnlineMessageSource.Incoming] 时为发信 [目标好友][Friend.id] 或 [群][Group.id]
     * - 当 [OfflineMessageSource] 时为 [机器人][Bot.id], 发信 [目标好友][Friend.id] 或 [群][Group.id] (取决于 [OfflineMessageSource.kind])
     */
    abstract val fromId: Long

    /**
     * 消息发送目标.
     *
     * - 当 [OnlineMessageSource.Outgoing] 时为发信 [目标好友][Friend.id] 或 [群][Group.id] 或 [临时消息][Member.id]
     * - 当 [OnlineMessageSource.Incoming] 时为 [机器人][Bot.id]
     * - 当 [OfflineMessageSource] 时为 [机器人][Bot.id], 发信 [目标好友][Friend.id] 或 [群][Group.id] 或 [临时消息][Member.id] (取决于 [OfflineMessageSource.kind])
     */
    abstract val targetId: Long // groupCode / friendUin / memberUin

    /**
     * 原消息内容.
     *
     * 此属性是 **lazy** 的: 它只会在第一次调用时初始化, 因为需要反序列化服务器发来的整个包, 相当于接收了一条新消息.
     */
    @LazyProperty
    abstract val originalMessage: MessageChain

    /**
     * 返回 `"[mirai:source:$id,$internalId]"`
     */
    final override fun toString(): String = "[mirai:source:$id,$internalId]"
}


/**
 * 在线消息的 [MessageSource].
 * 拥有对象化的 [sender], [target], 也可以直接 [recall] 和 [quote]
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
sealed class OnlineMessageSource : MessageSource() {
    companion object Key : Message.Key<OnlineMessageSource> {
        override val typeName: String get() = "OnlineMessageSource"
    }

    /**
     * 消息发送人. 可能为 [机器人][Bot] 或 [好友][Friend] 或 [群员][Member].
     * 即类型必定为 [Bot], [Friend] 或 [Member]
     */
    abstract val sender: ContactOrBot

    /**
     * 消息发送目标. 可能为 [机器人][Bot] 或 [好友][Friend] 或 [群][Group].
     * 即类型必定为 [Bot], [Friend] 或 [Group]
     */
    abstract val target: ContactOrBot

    /**
     * 消息主体. 群消息时为 [Group]. 好友消息时为 [Friend], 临时消息为 [Member]
     * 不论是机器人接收的消息还是发送的消息, 此属性都指向机器人能进行回复的目标.
     */
    abstract val subject: Contact

    /*
     * 以下子类型仅是覆盖了 [target], [subject], [sender] 等的类型
     */

    /**
     * 由 [机器人主动发送消息][Contact.sendMessage] 产生的 [MessageSource], 可通过 [MessageReceipt] 获得.
     */
    sealed class Outgoing : OnlineMessageSource() {
        companion object Key : Message.Key<Outgoing> {
            override val typeName: String get() = "OnlineMessageSource.Outgoing"
        }

        abstract override val sender: Bot
        abstract override val target: Contact

        final override val fromId: Long get() = sender.id
        final override val targetId: Long get() = target.id

        abstract class ToFriend : Outgoing() {
            companion object Key : Message.Key<ToFriend> {
                override val typeName: String get() = "OnlineMessageSource.Outgoing.ToFriend"
            }

            abstract override val target: Friend
            final override val subject: Friend get() = target
            //  final override fun toString(): String = "OnlineMessageSource.ToFriend(target=${target.id})"
        }

        abstract class ToTemp : Outgoing() {
            companion object Key : Message.Key<ToTemp> {
                override val typeName: String get() = "OnlineMessageSource.Outgoing.ToTemp"
            }

            abstract override val target: Member
            val group: Group get() = target.group
            final override val subject: Member get() = target
        }

        abstract class ToGroup : Outgoing() {
            companion object Key : Message.Key<ToGroup> {
                override val typeName: String get() = "OnlineMessageSource.Outgoing.ToGroup"
            }

            abstract override val target: Group
            final override val subject: Group get() = target
        }
    }

    /**
     * 接收到的一条消息的 [MessageSource]
     */
    sealed class Incoming : OnlineMessageSource() {
        companion object Key : Message.Key<Incoming> {
            override val typeName: String get() = "OnlineMessageSource.Incoming"
        }

        abstract override val sender: User

        final override val fromId: Long get() = sender.id
        final override val targetId: Long get() = target.id

        abstract class FromFriend : Incoming() {
            companion object Key : Message.Key<FromFriend> {
                override val typeName: String get() = "OnlineMessageSource.Incoming.FromFriend"
            }

            abstract override val sender: Friend
            final override val subject: Friend get() = sender
            final override val target: Bot get() = sender.bot
            // final override fun toString(): String = "OnlineMessageSource.FromFriend(from=${sender.id})"
        }

        abstract class FromTemp : Incoming() {
            companion object Key : Message.Key<FromTemp> {
                override val typeName: String get() = "OnlineMessageSource.Incoming.FromTemp"
            }

            abstract override val sender: Member
            inline val group: Group get() = sender.group
            final override val subject: Member get() = sender
            final override val target: Bot get() = sender.bot
        }

        abstract class FromGroup : Incoming() {
            companion object Key : Message.Key<FromGroup> {
                override val typeName: String get() = "OnlineMessageSource.Incoming.FromGroup"
            }

            abstract override val sender: Member
            final override val subject: Group get() = sender.group
            final override val target: Group get() = group
            inline val group: Group get() = sender.group
        }
    }
}

/**
 * 由一条消息中的 [QuoteReply] 得到的 [MessageSource].
 * 此消息源可能来自一条与机器人无关的消息. 因此无法提供对象化的 `sender` 或 `target` 获取.
 *
 * @see buildMessageSource 构建一个 [OfflineMessageSource]
 */
abstract class OfflineMessageSource : MessageSource() {
    companion object Key : Message.Key<OfflineMessageSource> {
        override val typeName: String get() = "OfflineMessageSource"
    }

    enum class Kind {
        GROUP,
        FRIEND,
        TEMP
    }

    /**
     * 消息种类
     */
    abstract val kind: Kind
}

/**
 * 判断是否是发送给群, 或从群接收的消息的消息源
 */
// inline for future removal
inline fun MessageSource.isAboutGroup(): Boolean {
    return when (this) {
        is OnlineMessageSource -> subject is Group
        is OfflineMessageSource -> kind == OfflineMessageSource.Kind.GROUP
    }
}

/**
 * 判断是否是发送给临时会话, 或从临时会话接收的消息的消息源
 */
inline fun MessageSource.isAboutTemp(): Boolean {
    return when (this) {
        is OnlineMessageSource -> subject is Member
        is OfflineMessageSource -> kind == OfflineMessageSource.Kind.TEMP
    }
}

/**
 * 判断是否是发送给好友, 或从好友接收的消息的消息源
 */
// inline for future removal
inline fun MessageSource.isAboutFriend(): Boolean {
    return when (this) {
        is OnlineMessageSource -> subject !is Group && subject !is Member
        is OfflineMessageSource -> kind == OfflineMessageSource.Kind.FRIEND
    }
}

/**
 * 引用这条消息
 * @see QuoteReply
 */
@JvmSynthetic
inline fun MessageSource.quote(): QuoteReply = QuoteReply(this)

/**
 * 引用这条消息. 仅从服务器接收的消息 (即来自 [MessageEvent]) 才可以通过这个方式被引用.
 * @see QuoteReply
 */
@JvmSynthetic
inline fun MessageChain.quote(): QuoteReply = QuoteReply(this.source)

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
 * @see Bot.recall
 */
@JvmSynthetic
suspend inline fun MessageSource.recall() = bot.recall(this)

/**
 * 在一段时间后撤回这条消息. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
 *
 * [Bot] 撤回自己的消息不需要权限.
 * [Bot] 撤回群员的消息需要管理员权限.
 *
 * @throws PermissionDeniedException 当 [Bot] 无权限操作时
 * @throws IllegalStateException 当这条消息已经被撤回时 (仅同步主动操作)
 *
 * @see Bot.recall
 */
@JvmSynthetic
inline fun MessageSource.recallIn(
    timeMillis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = bot.recallIn(this, timeMillis, coroutineContext)

// For MessageChain

/**
 * 消息 id.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源.
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
val MessageChain.id: Int
    get() = this.source.id

/**
 * 消息内部 id.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源.
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
val MessageChain.internalId: Int
    get() = this.source.internalId

/**
 * 消息时间.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源.
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
val MessageChain.time: Int
    get() = this.source.time

/**
 * 消息内部 id.
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取. 否则将抛出异常 [NoSuchElementException]
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
val MessageChain.bot: Bot
    get() = this.source.bot

/**
 * 获取这条消息的 [消息源][MessageSource].
 *
 * 仅从服务器接收的消息 (即来自 [MessageEvent.message]), 或手动添加了 [MessageSource] 元素的 [MessageChain] 才可以获取消息源, 否则将抛出异常 [NoSuchElementException]
 */
@get:JvmSynthetic
val MessageChain.source: MessageSource
    get() = this.getOrFail(MessageSource)

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
 * @see Bot.recall
 */
@JvmSynthetic
suspend inline fun MessageChain.recall() = this.source.recall()

/**
 * 在一段时间后撤回这条消息. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
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
 * @see Bot.recall
 */
@JvmSynthetic
inline fun MessageChain.recallIn(
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = source.recallIn(millis, coroutineContext)
