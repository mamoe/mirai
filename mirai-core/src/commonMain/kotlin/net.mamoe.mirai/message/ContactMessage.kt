/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "EXPERIMENTAL_UNSIGNED_LITERALS",
    "EXPERIMENTAL_API_USAGE",
    "unused",
    "INVISIBLE_REFERENCE",
    "INVISIBLE_MEMBER"
)
@file:OptIn(MiraiInternalAPI::class)

package net.mamoe.mirai.message

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.io.ByteReadChannel
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.event.syncFromEvent
import net.mamoe.mirai.event.syncFromEventOrNull
import net.mamoe.mirai.event.whileSelectMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * 一条消息事件.
 * 它是一个 [BotEvent], 因此可以被 [监听][Bot.subscribe]
 *
 * 支持的消息类型:
 * [GroupMessage]
 * [FriendMessage]
 *
 * @see isContextIdenticalWith 判断语境是否相同
 */
@Suppress("DEPRECATION")
@SinceMirai("0.32.0")
abstract class ContactMessage : MessagePacket<QQ, Contact>(), BotEvent

/**
 * 一条从服务器接收到的消息事件.
 * 请查看各平台的 `actual` 实现的说明.
 */
@Suppress("DEPRECATION")
@Deprecated(
    message = "use ContactMessage",
    replaceWith = ReplaceWith("ContactMessage", "net.mamoe.mirai.message.ContactMessage")
)
expect abstract class MessagePacket<TSender : QQ, TSubject : Contact> constructor() :
    MessagePacketBase<TSender, TSubject>

/**
 * 仅内部使用, 请使用 [ContactMessage]
 */ // Tips: 在 IntelliJ 中 (左侧边栏) 打开 `Structure`, 可查看类结构
@Deprecated(
    message = "use ContactMessage",
    replaceWith = ReplaceWith("ContactMessage", "net.mamoe.mirai.message.ContactMessage")
)
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
abstract class MessagePacketBase<out TSender : QQ, out TSubject : Contact> : Packet, BotEvent {
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

    // endregion

    // region 撤回

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


    // region 引用回复
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

    open val source: OnlineMessageSource.Incoming get() = message.source as OnlineMessageSource.Incoming

    // endregion

    operator fun <M : Message> get(at: Message.Key<M>): M {
        return this.message[at]
    }

    /**
     * 创建 @ 这个账号的消息. 当且仅当消息为群消息时可用. 否则将会抛出 [IllegalArgumentException]
     */
    fun QQ.at(): At = At(this as? Member ?: error("`QQ.at` can only be used in GroupMessage"))

    fun At.member(): Member = (this@MessagePacketBase as? GroupMessage)?.group?.get(this.target)
        ?: error("`At.member` can only be used in GroupMessage")

    inline fun At.isBot(): Boolean = target == bot.id

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
    @Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION")
    @PlannedRemoval("1.0.0")
    @Deprecated("use your own Http clients, this is going to be removed in 1.0.0", level = DeprecationLevel.WARNING)
    suspend inline fun Image.channel(): ByteReadChannel = bot.openChannel(this)
    // endregion


    @Deprecated("use reply(String) for clear semantics", ReplaceWith("reply(this)"))
    @JvmName("reply1")
    suspend inline fun String.reply(): MessageReceipt<TSubject> = reply(this)

    @Deprecated("use reply(String) for clear semantics", ReplaceWith("reply(this)"))
    @JvmName("reply1")
    suspend inline fun Message.reply(): MessageReceipt<TSubject> = reply(this)
}

/**
 * 判断两个 [MessagePacket] 的 [MessagePacket.sender] 和 [MessagePacket.subject] 是否相同
 */
@SinceMirai("0.29.0")
fun ContactMessage.isContextIdenticalWith(another: ContactMessage): Boolean {
    return this.sender == another.sender && this.subject == another.subject && this.bot == another.bot
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 *
 * @see syncFromEvent
 */
@JvmSynthetic
suspend inline fun <reified P : ContactMessage> P.nextMessage(
    timeoutMillis: Long = -1,
    crossinline filter: suspend P.(P) -> Boolean
): MessageChain {
    return syncFromEvent<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }?.takeIf { filter(it, it) }
    }.message
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同且通过 [筛选][filter] 的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [syncFromEvent] 会返回这个值
 * @return 消息链. 超时时返回 `null`
 *
 * @see syncFromEventOrNull
 */
@JvmSynthetic
suspend inline fun <reified P : ContactMessage> P.nextMessageOrNull(
    timeoutMillis: Long = -1,
    crossinline filter: suspend P.(P) -> Boolean
): MessageChain? {
    return syncFromEventOrNull<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageOrNull) }?.takeIf { filter(it, it) }
    }?.message
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [this] 相同的 [MessagePacket]
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @throws TimeoutCancellationException
 *
 * @see syncFromEvent
 */
@JvmSynthetic
suspend inline fun <reified P : ContactMessage> P.nextMessage(
    timeoutMillis: Long = -1
): MessageChain {
    return syncFromEvent<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }
    }.message
}

/**
 * @see nextMessage
 * @throws TimeoutCancellationException
 */
@JvmSynthetic
inline fun <reified P : ContactMessage> P.nextMessageAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<MessageChain> {
    return this.bot.async(coroutineContext) {
        syncFromEvent<P, P>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageAsync) }
        }.message
    }
}

/**
 * @see nextMessage
 */
@JvmSynthetic
inline fun <reified P : ContactMessage> P.nextMessageAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    crossinline filter: suspend P.(P) -> Boolean
): Deferred<MessageChain> {
    return this.bot.async(coroutineContext) {
        syncFromEvent<P, P>(timeoutMillis) {
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
 * @see syncFromEventOrNull
 */
@JvmSynthetic
suspend inline fun <reified P : ContactMessage> P.nextMessageOrNull(
    timeoutMillis: Long = -1
): MessageChain? {
    return syncFromEventOrNull<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageOrNull) }
    }?.message
}

/**
 * @see nextMessageOrNull
 */
@JvmSynthetic
inline fun <reified P : ContactMessage> P.nextMessageOrNullAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<MessageChain?> {
    return this.bot.async(coroutineContext) {
        syncFromEventOrNull<P, P>(timeoutMillis) {
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
 * @see syncFromEvent
 * @see whileSelectMessages
 * @see selectMessages
 */
@JvmSynthetic
suspend inline fun <reified M : Message> ContactMessage.nextMessageContaining(
    timeoutMillis: Long = -1
): M {
    return syncFromEvent<ContactMessage, ContactMessage>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageContaining) }
            .takeIf { this.message.anyIsInstance<M>() }
    }.message.firstIsInstance()
}

@JvmSynthetic
inline fun <reified M : Message> ContactMessage.nextMessageContainingAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<M> {
    return this.bot.async(coroutineContext) {
        @Suppress("RemoveExplicitTypeArguments")
        syncFromEvent<ContactMessage, ContactMessage>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageContainingAsync) }
                .takeIf { this.message.anyIsInstance<M>() }
        }.message.firstIsInstance<M>()
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
 * @see syncFromEventOrNull
 */
@JvmSynthetic
suspend inline fun <reified M : Message> ContactMessage.nextMessageContainingOrNull(
    timeoutMillis: Long = -1
): M? {
    return syncFromEventOrNull<ContactMessage, ContactMessage>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessageContainingOrNull) }
            .takeIf { this.message.anyIsInstance<M>() }
    }?.message?.firstIsInstance()
}

@JvmSynthetic
inline fun <reified M : Message> ContactMessage.nextMessageContainingOrNullAsync(
    timeoutMillis: Long = -1,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Deferred<M?> {
    return this.bot.async(coroutineContext) {
        syncFromEventOrNull<ContactMessage, ContactMessage>(timeoutMillis) {
            takeIf { this.isContextIdenticalWith(this@nextMessageContainingOrNullAsync) }
                .takeIf { this.message.anyIsInstance<M>() }
        }?.message?.firstIsInstance<M>()
    }
}


@PlannedRemoval("1.0.0")
@Suppress("DEPRECATION")
@Deprecated(level = DeprecationLevel.HIDDEN, message = "for binary compatibility")
fun MessagePacket<*, *>.isContextIdenticalWith(another: MessagePacket<*, *>): Boolean {
    return (this as ContactMessage).isContextIdenticalWith(another as ContactMessage)
}
