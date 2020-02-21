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

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.subscribingGet
import net.mamoe.mirai.event.subscribingGetAsync
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmName

/**
 * 一条从服务器接收到的消息事件.
 * 请查看各平台的 `actual` 实现的说明.
 */
@UseExperimental(MiraiInternalAPI::class)
expect abstract class MessagePacket<TSender : QQ, TSubject : Contact>(bot: Bot) : MessagePacketBase<TSender, TSubject>

/**
 * 仅内部使用, 请使用 [MessagePacket]
 */ // Tips: 在 IntelliJ 中 (左侧边栏) 打开 `Structure`, 可查看类结构
@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
@MiraiInternalAPI
abstract class MessagePacketBase<TSender : QQ, TSubject : Contact>(_bot: Bot) : Packet, BotEvent {
    /**
     * 接受到这条消息的
     */
    override val bot: Bot by _bot.unsafeWeakRef()

    /**
     * 消息事件主体.
     *
     * 对于好友消息, 这个属性为 [QQ] 的实例, 与 [sender] 引用相同;
     * 对于群消息, 这个属性为 [Group] 的实例, 与 [GroupMessage.group] 引用相同
     *
     * 在回复消息时, 可通过 [subject] 作为回复对象
     */
    abstract val subject: TSubject

    /**
     * 发送人.
     *
     * 在好友消息时为 [QQ] 的实例, 在群消息时为 [Member] 的实例
     */
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
    suspend inline fun reply(message: MessageChain): MessageReceipt<TSubject> = subject.sendMessage(message) as MessageReceipt<TSubject>

    suspend inline fun reply(message: Message): MessageReceipt<TSubject> = subject.sendMessage(message.toChain()) as MessageReceipt<TSubject>
    suspend inline fun reply(plain: String): MessageReceipt<TSubject> = subject.sendMessage(plain.toMessage().toChain()) as MessageReceipt<TSubject>

    @JvmName("reply1")
    suspend inline fun String.reply(): MessageReceipt<TSubject> = reply(this)

    @JvmName("reply1")
    suspend inline fun Message.reply(): MessageReceipt<TSubject> = reply(this)

    @JvmName("reply1")
    suspend inline fun MessageChain.reply(): MessageReceipt<TSubject> = reply(this)
    // endregion

    // region

    /**
     * 引用这个消息. 当且仅当消息为群消息时可用. 否则将会抛出 [IllegalArgumentException]
     */
    inline fun MessageChain.quote(): MessageChain = this.quote(sender as? Member ?: error("only group message can be quoted"))

    // endregion

    // region 上传图片
    suspend inline fun ExternalImage.upload(): Image = this.upload(subject)
    // endregion

    // region 发送图片
    suspend inline fun ExternalImage.send() = this.sendTo(subject)

    suspend inline fun Image.send() = this.sendTo(subject)
    suspend inline fun Message.send() = this.sendTo(subject)
    suspend inline fun String.send() = this.toMessage().sendTo(subject)
    // endregion

    operator fun <M : Message> get(at: Message.Key<M>): M {
        return this.message[at]
    }

    /**
     * 创建 @ 这个账号的消息. 当且仅当消息为群消息时可用. 否则将会抛出 [IllegalArgumentException]
     */
    fun QQ.at(): At = At(this as? Member ?: error("`QQ.at` can only be used in GroupMessage"))

    fun At.member(): Member = (this@MessagePacketBase as? GroupMessage)?.group?.get(this.target) ?: error("`At.member` can only be used in GroupMessage")

    // endregion

    // region 下载图片
    /**
     * 将图片下载到内存.
     *
     * 非常不推荐这样做.
     */
    @Deprecated("内存使用效率十分低下", ReplaceWith("this.download()"), DeprecationLevel.WARNING)
    suspend inline fun Image.downloadAsByteArray(): ByteArray = bot.run { download().readBytes() }

    // TODO: 2020/2/5 为下载图片添加文件系统的存储方式

    /**
     * 将图片下载到内存缓存中 (使用 [IoBuffer.Pool])
     */
    suspend inline fun Image.download(): ByteReadPacket = bot.run { download() }
    // endregion
}

/**
 * 判断两个 [MessagePacket] 的 [MessagePacket.sender] 和 [MessagePacket.subject] 是否相同
 */
fun MessagePacket<*, *>.isContextIdenticalWith(another: MessagePacket<*, *>): Boolean {
    return this.sender == another.sender && this.subject == another.subject
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [P] 相同且通过 [筛选][filter] 的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 * @param filter 过滤器. 返回非 null 则代表得到了需要的值. [subscribingGet] 会返回这个值
 *
 * @see subscribingGetAsync 本函数的异步版本
 */
suspend inline fun <reified P : MessagePacket<*, *>> P.nextMessage(
    timeoutMillis: Long = -1,
    crossinline filter: P.(P) -> Boolean
): P {
    return subscribingGet<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }?.takeIf { filter(it, it) }
    }
}

/**
 * 挂起当前协程, 等待下一条 [MessagePacket.sender] 和 [MessagePacket.subject] 与 [P] 相同的 [MessagePacket]
 *
 * 若 [filter] 抛出了一个异常, 本函数会立即抛出这个异常.
 *
 * @param timeoutMillis 超时. 单位为毫秒. `-1` 为不限制
 *
 * @see subscribingGetAsync 本函数的异步版本
 */
suspend inline fun <reified P : MessagePacket<*, *>> P.nextMessage(
    timeoutMillis: Long = -1
): P {
    return subscribingGet<P, P>(timeoutMillis) {
        takeIf { this.isContextIdenticalWith(this@nextMessage) }
    }
}