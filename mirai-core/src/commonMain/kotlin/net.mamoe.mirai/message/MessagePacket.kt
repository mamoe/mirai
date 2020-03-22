/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.io.ByteReadChannel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.event.subscribingGet
import net.mamoe.mirai.event.subscribingGetOrNull
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.recall
import net.mamoe.mirai.recallIn
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName

/**
 * 一条从服务器接收到的消息事件.
 * 请查看各平台的 `actual` 实现的说明.
 */
@OptIn(MiraiInternalAPI::class)
expect abstract class MessagePacket<TSender : QQ, TSubject : Contact>() : MessagePacketBase<TSender, TSubject>

/**
 * 仅内部使用, 请使用 [MessagePacket]
 */ // Tips: 在 IntelliJ 中 (左侧边栏) 打开 `Structure`, 可查看类结构
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
@MiraiInternalAPI
abstract class MessagePacketBase<TSender : QQ, TSubject : Contact> : Packet, BotEvent {
    /**
     * 接受到这条消息的
     */
    @WeakRefProperty
    abstract override val bot: Bot

    /**
     * 消息事件主体.
     *
     * 对于好友消息, 这个属性为 [QQ] 的实例, 与 [sender] 引用相同;
     * 对于群消息, 这个属性为 [Group] 的实例, 与 [GroupMessage.group] 引用相同
     *
     * 在回复消息时, 可通过 [subject] 作为回复对象
     */
    @WeakRefProperty
    abstract val subject: TSubject

    /**
     * 发送人.
     *
     * 在好友消息时为 [QQ] 的实例, 在群消息时为 [Member] 的实例
     */
    @WeakRefProperty
    abstract val sender: TSender

    /**
     * 消息内容
     */
    abstract val message: MessageChain


    // region 发送 Message

    /**
     * 给这个消息事件的主体发送消息
     * 对于好友消息事件, 这个方法将会给好友 ([subject]) 发送消息
     * 对于群消息事件, 这个方法将会给群 ([subject]) 发送消息
     */
    suspend inline fun reply(message: Message): MessageReceipt<TSubject> =
        subject.sendMessage(message.asMessageChain()) as MessageReceipt<TSubject>

    suspend inline fun reply(plain: String): MessageReceipt<TSubject> =
        subject.sendMessage(plain.toMessage().asMessageChain()) as MessageReceipt<TSubject>

    @JvmName("reply1")
    suspend inline fun String.reply(): MessageReceipt<TSubject> = reply(this)

    @JvmName("reply1")
    suspend inline fun Message.reply(): MessageReceipt<TSubject> = reply(this)
    // endregion

    // region 撤回
    suspend inline fun MessageChain.recall() = bot.recall(this)
    suspend inline fun MessageSource.recall() = bot.recall(this)
    suspend inline fun QuoteReply.recall() = bot.recall(this.source)
    inline fun MessageChain.recallIn(
        millis: Long,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ) = bot.recallIn(this, millis, coroutineContext)

    inline fun MessageSource.recallIn(
        millis: Long,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ) = bot.recallIn(this, millis, coroutineContext)

    inline fun QuoteReply.recallIn(
        millis: Long,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ) = bot.recallIn(this.source, millis, coroutineContext)
    // endregion

    // region 上传图片
    suspend inline fun ExternalImage.upload(): Image = this.upload(subject)
    // endregion

    // region 发送图片
    suspend inline fun ExternalImage.send(): MessageReceipt<TSubject> = this.sendTo(subject)

    suspend inline fun Image.send(): MessageReceipt<TSubject> = this.sendTo(subject)
    suspend inline fun Message.send(): MessageReceipt<TSubject> = this.sendTo(subject)
    suspend inline fun String.send(): MessageReceipt<TSubject> = this.toMessage().sendTo(subject)
    // endregion


    /**
     * 给这个消息事件的主体发送引用回复消息
     * 对于好友消息事件, 这个方法将会给好友 ([subject]) 发送消息
     * 对于群消息事件, 这个方法将会给群 ([subject]) 发送消息
     */
    suspend inline fun quoteReply(message: MessageChain): MessageReceipt<TSubject> =
        reply(this.message.quote() + message)

    suspend inline fun quoteReply(message: Message): MessageReceipt<TSubject> = reply(this.message.quote() + message)
    suspend inline fun quoteReply(plain: String): MessageReceipt<TSubject> = reply(this.message.quote() + plain)

    @JvmName("reply2")
    suspend inline fun String.quoteReply(): MessageReceipt<TSubject> = quoteReply(this)

    @JvmName("reply2")
    suspend inline fun Message.quoteReply(): MessageReceipt<TSubject> = quoteReply(this)

    @JvmName("reply2")
    suspend inline fun MessageChain.quoteReply(): MessageReceipt<TSubject> = quoteReply(this)

    /**
     * 引用这个消息
     */
    inline fun MessageChain.quote(): QuoteReplyToSend = this.quote(sender)

    operator fun <M : Message> get(at: Message.Key<M>): M {
        return this.message[at]
    }

    /**
     * 创建 @ 这个账号的消息. 当且仅当消息为群消息时可用. 否则将会抛出 [IllegalArgumentException]
     */
    fun QQ.at(): At = At(this as? Member ?: error("`QQ.at` can only be used in GroupMessage"))

    fun At.member(): Member = (this@MessagePacketBase as? GroupMessage)?.group?.get(this.target)
        ?: error("`At.member` can only be used in GroupMessage")

    // endregion

    // region 下载图片


    /**
     * 获取图片下载链接
     *
     * @return "http://gchat.qpic.cn/gchatpic_new/..."
     */
    suspend inline fun Image.url(): String = bot.queryImageUrl(this@url)

    /**
     * 获取图片下载链接并开始下载.
     *
     * @see ByteReadChannel.copyAndClose
     * @see ByteReadChannel.copyTo
     */
    suspend inline fun Image.channel(): ByteReadChannel = bot.openChannel(this)
    // endregion
}

/**
 * 判断两个 [MessagePacket] 的 [MessagePacket.sender] 和 [MessagePacket.subject] 是否相同
 */
@SinceMirai("0.29.0")
fun MessagePacket<*, *>.isContextIdenticalWith(another: MessagePacket<*, *>): Boolean {
    return this.sender == another.sender && this.subject == another.subject && this.bot == another.bot
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 *
 * @see subscribingGet
 */
suspend inline fun <reified P : MessagePacket<*, *>> P.nextMessage(
    timeoutMillis: Long = -1,
    crossinline filter: suspend P.(P) -> Boolean
): MessageChain {
    return subscribingGet<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }?.takeIf { filter(it, it) }
    }.message
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 * @return 消息链. 超时时返回 `null`
 *
 * @see subscribingGetOrNull
 */
suspend inline fun <reified P : MessagePacket<*, *>> P.nextMessageOrNull(
    timeoutMillis: Long = -1,
    crossinline filter: suspend P.(P) -> Boolean
): MessageChain? {
    return subscribingGetOrNull<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageOrNull) }?.takeIf { filter(it, it) }
    }?.message
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @see subscribingGet
 */
suspend inline fun <reified P : MessagePacket<*, *>> P.nextMessage(
    timeoutMillis: Long = -1
): MessageChain {
    return subscribingGet<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }
    }.message
}

/**
 * @see nextMessage
 */
inline fun <reified P : MessagePacket<*, *>> P.nextMessageAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<MessageChain> {
    return this.bot.async(coroutineContext) {
        subscribingGet<P, P>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageAsync) }
        }.message
    }
}

/**
 * @see nextMessage
 */
inline fun <reified P : MessagePacket<*, *>> P.nextMessageAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline filter: suspend P.(P) -> Boolean
): Deferred<MessageChain> {
    return this.bot.async(coroutineContext) {
        subscribingGet<P, P>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageAsync) }
                .takeIf { filter(this, this) }
        }.message
    }
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @return 消息链. 超时时返回 `null`
 *
 * @see subscribingGetOrNull
 */
suspend inline fun <reified P : MessagePacket<*, *>> P.nextMessageOrNull(
    timeoutMillis: Long = -1
): MessageChain? {
    return subscribingGetOrNull<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageOrNull) }
    }?.message
}

/**
 * @see nextMessageOrNull
 */
inline fun <reified P : MessagePacket<*, *>> P.nextMessageOrNullAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<MessageChain?> {
    return this.bot.async(coroutineContext) {
        subscribingGetOrNull<P, P>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageOrNullAsync) }
        }?.message
    }
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @see subscribingGet
 * @see whileSelectMessages
 * @see selectMessages
 */
suspend inline fun <reified M : Message> MessagePacket<*, *>.nextMessageContaining(
    timeoutMillis: Long = -1
): M {
    return subscribingGet<MessagePacket<*, *>, MessagePacket<*, *>>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageContaining) }
    }.message.first()
}

inline fun <reified M : Message> MessagePacket<*, *>.nextMessageContainingAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<M> {
    return this.bot.async(coroutineContext) {
        @Suppress("RemoveExplicitTypeArguments")
        subscribingGet<MessagePacket<*, *>, MessagePacket<*, *>>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageContainingAsync) }
        }.message.first<M>()
    }
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同并含有 [M] 类型的消息的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @return 指定类型的消息. 超时时返回 `null`
 *
 * @see subscribingGetOrNull
 */
suspend inline fun <reified M : Message> MessagePacket<*, *>.nextMessageContainingOrNull(
    timeoutMillis: Long = -1
): M? {
    return subscribingGetOrNull<MessagePacket<*, *>, MessagePacket<*, *>>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageContainingOrNull) }
    }?.message?.first()
}

inline fun <reified M : Message> MessagePacket<*, *>.nextMessageContainingOrNullAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<M?> {
    return this.bot.async(coroutineContext) {
        subscribingGetOrNull<MessagePacket<*, *>, MessagePacket<*, *>>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageContainingOrNullAsync) }
        }?.message?.first<M>()
    }
}
