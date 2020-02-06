@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.event.events.BotEvent
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
@Suppress("NOTHING_TO_INLINE")
@MiraiInternalAPI
abstract class MessagePacketBase<TSender : QQ, TSubject : Contact>(_bot: Bot) : EventPacket, BotEvent() {
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
    suspend inline fun reply(message: MessageChain) = subject.sendMessage(message)

    suspend inline fun reply(message: Message) = subject.sendMessage(message.toChain())
    suspend inline fun reply(plain: String) = subject.sendMessage(plain.singleChain())

    @JvmName("reply1")
    suspend inline fun String.reply() = reply(this)

    @JvmName("reply1")
    suspend inline fun Message.reply() = reply(this)

    @JvmName("reply1")
    suspend inline fun MessageChain.reply() = reply(this)

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

    /**
     * 创建 @ 这个账号的消息. 当且仅当消息为群消息时可用. 否则将会抛出 [IllegalArgumentException]
     */
    inline fun QQ.at(): At = At(this as? Member ?: error("`QQ.at` can only be used in GroupMessage"))

    // endregion

    // region 下载图片
    /**
     * 将图片下载到内存.
     *
     * 非常不推荐这样做.
     */
    @Deprecated("内存使用效率十分低下", ReplaceWith("this.download()"), DeprecationLevel.WARNING)
    suspend inline fun Image.downloadAsByteArray(): ByteArray = bot.run { downloadAsByteArray() }

    // TODO: 2020/2/5 为下载图片添加文件系统的存储方式

    /**
     * 将图片下载到内存缓存中 (使用 [IoBuffer.Pool])
     */
    suspend inline fun Image.download(): ByteReadPacket = bot.run { download() }
    // endregion

    @Deprecated(message = "这个函数有歧义, 将在不久后删除", replaceWith = ReplaceWith("bot.getFriend(this.target)"))
    fun At.qq(): QQ = bot.getFriend(this.target)

    @Deprecated(message = "这个函数有歧义, 将在不久后删除", replaceWith = ReplaceWith("bot.getFriend(this.toLong())"))
    fun Int.qq(): QQ = bot.getFriend(this.coerceAtLeastOrFail(0).toLong())

    @Deprecated(message = "这个函数有歧义, 将在不久后删除", replaceWith = ReplaceWith("bot.getFriend(this)"))
    fun Long.qq(): QQ = bot.getFriend(this.coerceAtLeastOrFail(0))

    @Deprecated(message = "这个函数有歧义, 将在不久后删除", replaceWith = ReplaceWith("bot.getGroup(this)"))
    fun Long.group(): Group = bot.getGroup(this)
}