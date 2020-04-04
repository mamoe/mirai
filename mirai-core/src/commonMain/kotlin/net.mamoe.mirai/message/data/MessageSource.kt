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
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.LazyProperty
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * MessageSource 正计划于 0.32 或 0.33 或之后进行 API 不兼容的重写.
 */
@RequiresOptIn(message = "MessageSource 正计划于 0.32 或 0.33 或之后进行 API 不兼容的重写", level = RequiresOptIn.Level.WARNING)
annotation class ExperimentalMessageSource

/**
 * 消息源, 它存在于 [MessageChain] 中, 用于表示这个消息的来源.
 *
 * 消息源只用于 [引用回复][QuoteReply] 或 [撤回][Bot.recall].
 *
 * `mirai-core-qqandroid`: `net.mamoe.mirai.qqandroid.message.MessageSourceFromMsg`
 *
 * @see Bot.recall 撤回一条消息
 * @see MessageSource.quote 引用这条消息, 创建 [MessageChain]
 *
 * @see OnlineMessageSource 在线消息的 [MessageSource]
 * @see OfflineMessageSource 离线消息的 [MessageSource]
 */
@SinceMirai("0.33.0")
sealed class MessageSource : Message, MessageMetadata {
    companion object Key : Message.Key<MessageSource>

    /**
     * 所属 [Bot]
     */
    abstract val bot: Bot

    /**
     * 消息 id.
     */
    abstract val id: Int // random

    /**
     * 发送时间, 单位为秒. 撤回好友消息时可能需要
     */
    abstract val time: Int

    /**
     * 发送人. 可能为机器人自己, 好友的 id, 或群 id
     */
    abstract val senderId: Long

    /**
     * 发送目标. 可能为机器人自己, 好友的 id, 或群 id
     */
    abstract val targetId: Long // groupCode / friendUin

    /**
     * 原消息内容.
     */
    @LazyProperty
    abstract val originalMessage: MessageChain

    final override fun toString(): String = ""
}

// ONLINE

/**
 * 在线消息的 [MessageSource].
 * 拥有对象化的 [sender], [target], 也可以直接 [recall] 和 [quote]
 *
 * ### 来源
 * **必定是一个发出去的消息或接收到的消息的 [MessageChain] 中的一个元数据 [MessageMetadata].**
 *
 * #### 机器人主动发送消息
 * 当机器人 [主动发出消息][Member.sendMessage], 将会得到一个 [消息回执][MessageReceipt].
 * 此回执的 [消息源][MessageReceipt.source] 即为一个 [外向消息源][OnlineMessageSource.Outgoing], 代表着刚刚发出的那条消息的来源.
 *
 * #### 机器人接受消息
 * 当机器人接收一条消息 [ContactMessage], 这条消息包含一个 [内向消息源][OnlineMessageSource.Incoming], 代表着接收到的这条消息的来源.
 */
@SinceMirai("0.33.0")
sealed class OnlineMessageSource : MessageSource() {
    /**
     * 消息发送人. 可能为 [机器人][Bot] 或 [好友][QQ] 或 [群员][Member].
     * 即类型必定为 [Bot], [QQ] 或 [Member]
     */
    abstract val sender: Any

    /**
     * 消息发送目标. 可能为 [机器人][Bot] 或 [好友][QQ] 或 [群][Group].
     * 即类型必定为 [Bot], [QQ] 或 [Group]
     */
    abstract val target: Any

    /**
     * 消息主体. 群消息时为 [Group]. 好友消息时为 [QQ].
     * 不论是机器人接收的消息还是发送的消息, 此属性都指向机器人能进行回复的目标.
     */
    abstract val subject: Contact // Group or QQ

    /**
     * 由 [机器人主动发送消息][Contact.sendMessage] 产生的 [MessageSource]
     */
    sealed class Outgoing : OnlineMessageSource() {
        abstract override val sender: Bot
        abstract override val target: Contact

        final override val senderId: Long get() = sender.id
        final override val targetId: Long get() = target.id

        abstract class ToFriend : Outgoing() {
            abstract override val target: QQ
            final override val subject: QQ get() = target
            //  final override fun toString(): String = "OnlineMessageSource.ToFriend(target=${target.id})"
        }

        abstract class ToGroup : Outgoing() {
            abstract override val target: Group
            final override val subject: Group get() = target
            //  final override fun toString(): String = "OnlineMessageSource.ToGroup(group=${target.id})"
        }
    }

    /**
     * 接收到的一条消息的 [MessageSource]
     */
    sealed class Incoming : OnlineMessageSource() {
        abstract override val sender: QQ // out QQ
        abstract override val target: Bot

        final override val senderId: Long get() = sender.id
        final override val targetId: Long get() = target.id

        abstract class FromFriend : Incoming() {
            abstract override val sender: QQ
            final override val subject: QQ get() = sender
            // final override fun toString(): String = "OnlineMessageSource.FromFriend(from=${sender.id})"
        }

        abstract class FromGroup : Incoming() {
            abstract override val sender: Member
            final override val subject: Group get() = sender.group
            val group: Group get() = sender.group
            // final override fun toString(): String = "OnlineMessageSource.FromGroup(group=${group.id}, sender=${sender.id})"
        }
    }
}

/**
 * 引用这条消息
 */
fun OnlineMessageSource.quote(): QuoteReply {
    @OptIn(MiraiInternalAPI::class)
    return QuoteReply(this)
}

/**
 * 引用这条消息
 */
fun MessageChain.quote(): QuoteReply {
    @OptIn(MiraiInternalAPI::class)
    return QuoteReply(this.source)
}

/**
 * 撤回这条消息
 */
suspend inline fun OnlineMessageSource.recall() = bot.recall(this)

/**
 * 撤回这条消息
 */
inline fun MessageSource.recallIn(
    timeMillis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = bot.recallIn(this, timeMillis, coroutineContext)


// OFFLINE

/**
 * 由一条消息中的 [QuoteReply] 得到的 [MessageSource].
 * 此消息源可能来自一条与机器人无关的消息. 因此无法提供对象化的 `sender` 或 `target` 获取.
 */
@SinceMirai("0.33.0")
abstract class OfflineMessageSource : MessageSource() {
    // final override fun toString(): String = "OfflineMessageSource(sender=$senderId, target=$targetId)"
} // TODO: 2020/4/4 可能要分群和好友

// For MessageChain

/**
 * 消息 id.
 * 仅从服务器接收的消息才可以获取 id
 *
 * @see MessageSource.id
 */
@get:JvmSynthetic
inline val MessageChain.id: Int
    get() = this.source.id

/**
 * 获取这条消息源
 * 仅从服务器接收的消息才可以获取消息源
 */
@get:JvmSynthetic
inline val MessageChain.source: MessageSource
    get() = this[MessageSource]