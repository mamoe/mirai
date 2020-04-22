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
@file:Suppress("NOTHING_TO_INLINE", "unused", "INAPPLICABLE_JVM_NAME", "DEPRECATION_ERROR")

package net.mamoe.mirai.message.data

import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.LazyProperty
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 消息源, 它存在于 [MessageChain] 中, 用于表示这个消息的来源.
 *
 * 消息源可用于 [引用回复][QuoteReply] 或 [撤回][Bot.recall].
 *
 * @see Bot.recall 撤回一条消息
 * @see MessageSource.quote 引用这条消息, 创建 [MessageChain]
 *
 * @see OnlineMessageSource 在线消息的 [MessageSource]
 * @see OfflineMessageSource 离线消息的 [MessageSource]
 */
@OptIn(MiraiExperimentalAPI::class)
@SinceMirai("0.33.0")
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
     * 消息 id.
     * 此值在同一会话中唯一且有顺序.
     */
    abstract val id: Int

    /**
     * 内部 id, 仅用于 [Bot.constructMessageSource]
     * 可能为 0, 取决于服务器是否提供.
     */
    abstract val internalId: Int

    /**
     * 发送时间时间戳, 单位为秒.
     * 撤回好友消息时需要
     */
    abstract val time: Int

    /**
     * 发送人.
     * 当 [OnlineMessageSource.Outgoing] 时为 [机器人][Bot.id]
     * 当 [OnlineMessageSource.Incoming] 时为发信 [目标好友][QQ.id] 或 [群][Group.id]
     * 当 [OfflineMessageSource] 时为 [机器人][Bot.id], 发信 [目标好友][QQ.id] 或 [群][Group.id] (取决于 [OfflineMessageSource.kind])
     */
    abstract val fromId: Long

    /**
     * 发送目标.
     * 当 [OnlineMessageSource.Outgoing] 时为发信 [目标好友][QQ.id] 或 [群][Group.id] 或 [临时消息][Member.id]
     * 当 [OnlineMessageSource.Incoming] 时为 [机器人][Bot.id]
     * 当 [OfflineMessageSource] 时为 [机器人][Bot.id], 发信 [目标好友][QQ.id] 或 [群][Group.id] 或 [临时消息][Member.id] (取决于 [OfflineMessageSource.kind])
     */
    abstract val targetId: Long // groupCode / friendUin / memberUin

    /**
     * 原消息内容.
     */
    @LazyProperty
    abstract val originalMessage: MessageChain

    final override fun toString(): String = "[mirai:source:$id]"
    final override fun contentToString(): String = ""
}


// inline for future removal
inline fun MessageSource.isAboutGroup(): Boolean {
    return when (this) {
        is OnlineMessageSource -> subject is Group
        is OfflineMessageSource -> kind == OfflineMessageSource.Kind.GROUP
    }
}

inline fun MessageSource.isAboutTemp(): Boolean {
    return when (this) {
        is OnlineMessageSource -> subject is Member
        is OfflineMessageSource -> kind == OfflineMessageSource.Kind.TEMP
    }
}

// inline for future removal
inline fun MessageSource.isAboutFriend(): Boolean {
    return when (this) {
        is OnlineMessageSource -> subject !is Group && subject !is Member
        is OfflineMessageSource -> kind == OfflineMessageSource.Kind.FRIEND
    }
}

/**
 * 引用这条消息
 */
fun MessageSource.quote(): QuoteReply {
    @OptIn(MiraiInternalAPI::class)
    return QuoteReply(this)
}

/**
 * 引用这条消息
 */
fun MessageChain.quote(): QuoteReply {
    @OptIn(MiraiInternalAPI::class)
    return QuoteReply(this.source as? OnlineMessageSource ?: error("only online messages can be quoted"))
}

@JvmSynthetic
suspend inline fun MessageSource.recall() = bot.recall(this)

/**
 * 撤回这条消息
 */
@JvmSynthetic
inline fun MessageSource.recallIn(
    timeMillis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = bot.recallIn(this, timeMillis, coroutineContext)

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

@JvmSynthetic
suspend inline fun MessageChain.recall() = this.source.recall()

@JvmSynthetic
inline fun MessageChain.recallIn(
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = source.recallIn(millis, coroutineContext)
