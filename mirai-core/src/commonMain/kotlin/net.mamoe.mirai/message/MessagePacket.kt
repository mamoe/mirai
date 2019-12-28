@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.coerceAtLeastOrFail
import kotlin.jvm.JvmName

/**
 * 平台相关扩展
 */
@UseExperimental(MiraiInternalAPI::class)
expect abstract class MessagePacket<TSender : QQ, TSubject : Contact>(bot: Bot) : MessagePacketBase<TSender, TSubject>

@MiraiInternalAPI
abstract class MessagePacketBase<TSender : QQ, TSubject : Contact>(_bot: Bot) : EventPacket, BotEvent() {
    override val bot: Bot by _bot.unsafeWeakRef()

    /**
     * 消息事件主体.
     *
     * 对于好友消息, 这个属性为 [QQ] 的实例;
     * 对于群消息, 这个属性为 [Group] 的实例
     *
     * 在回复消息时, 可通过 [subject] 作为回复对象
     */
    abstract val subject: TSubject

    /**
     * 发送人
     */
    abstract val sender: TSender

    abstract val message: MessageChain


    // region Send to subject

    /**
     * 给这个消息事件的主体发送消息
     * 对于好友消息事件, 这个方法将会给好友 ([subject]) 发送消息
     * 对于群消息事件, 这个方法将会给群 ([subject]) 发送消息
     */
    suspend inline fun reply(message: MessageChain) = subject.sendMessage(message)

    suspend inline fun reply(message: Message) = subject.sendMessage(message.chain())
    suspend inline fun reply(plain: String) = subject.sendMessage(plain.toMessage())

    @JvmName("reply1")
    suspend inline fun String.reply() = reply(this)

    @JvmName("reply1")
    suspend inline fun Message.reply() = reply(this)

    @JvmName("reply1")
    suspend inline fun MessageChain.reply() = reply(this)

    suspend inline fun ExternalImage.send() = this.sendTo(subject)

    suspend inline fun ExternalImage.upload(): Image = this.upload(subject)
    suspend inline fun Image.send() = this.sendTo(subject)
    suspend inline fun ImageId.send() = this.sendTo(subject)
    suspend inline fun Message.send() = this.sendTo(subject)
    suspend inline fun String.send() = this.toMessage().sendTo(subject)

    // endregion

    // region Image download
    suspend inline fun Image.getLink(): ImageLink = with(bot) { getLink() }

    suspend inline fun Image.downloadAsByteArray(): ByteArray = getLink().downloadAsByteArray()
    suspend inline fun Image.download(): ByteReadPacket = getLink().download()
    // endregion

    fun At.qq(): QQ = bot.getQQ(this.target)

    fun Int.qq(): QQ = bot.getQQ(this.coerceAtLeastOrFail(0).toLong())
    fun Long.qq(): QQ = bot.getQQ(this.coerceAtLeastOrFail(0))

    suspend inline fun Int.group(): Group = bot.getGroup(this.coerceAtLeastOrFail(0).toLong())
    suspend inline fun Long.group(): Group = bot.getGroup(this.coerceAtLeastOrFail(0))
    suspend inline fun GroupId.group(): Group = bot.getGroup(this)
    suspend inline fun GroupInternalId.group(): Group = bot.getGroup(this)
}