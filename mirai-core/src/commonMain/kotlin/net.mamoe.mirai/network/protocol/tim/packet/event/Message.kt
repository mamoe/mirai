@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.String
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.getGroup
import net.mamoe.mirai.getQQ
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion
import net.mamoe.mirai.network.protocol.tim.packet.action.ImageLink
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.printTLVMap
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.readTLVMap
import net.mamoe.mirai.utils.io.readUShortLVByteArray
import net.mamoe.mirai.withSession
import kotlin.jvm.JvmName

/**
 * 平台相关扩展
 */
@UseExperimental(MiraiInternalAPI::class)
expect abstract class MessagePacket<TSubject : Contact>() : MessagePacketBase<TSubject>

@MiraiInternalAPI
abstract class MessagePacketBase<TSubject : Contact> : EventPacket, BotEvent() {
    internal lateinit var botVar: Bot

    override val bot: Bot get() = botVar

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
    abstract val sender: QQ

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
    suspend inline fun Image.getLink(): ImageLink = bot.withSession { getLink() }

    suspend inline fun Image.downloadAsByteArray(): ByteArray = getLink().downloadAsByteArray()
    suspend inline fun Image.download(): ByteReadPacket = getLink().download()
    // endregion
}

// region group message

data class GroupMessage(
    val group: Group,
    val senderName: String,
    /**
     * 发送方权限.
     */
    val permission: MemberPermission,
    override val sender: QQ,
    override val message: MessageChain
) : MessagePacket<Group>() {

    override val subject: Group get() = group
}

@PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
object GroupMessageEventParserAndHandler : KnownEventParserAndHandler<GroupMessage>(0x0052u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): GroupMessage {
        discardExact(31)
        val groupNumber = readUInt()
        discardExact(1)
        val qq = readUInt()

        discardExact(48)
        readUShortLVByteArray()
        discardExact(2)//2个0x00
        val message = readMessageChain()

        var senderPermission: MemberPermission = MemberPermission.MEMBER
        var senderName = ""
        val map = readTLVMap(true)
        if (map.containsKey(18u)) {
            map.getValue(18u).read {
                val tlv = readTLVMap(true)
                senderPermission = when (tlv.takeIf { it.containsKey(0x04u) }?.get(0x04u)?.getOrNull(3)?.toUInt()) {
                    null -> MemberPermission.MEMBER
                    0x08u -> MemberPermission.OWNER
                    0x10u -> MemberPermission.OPERATOR
                    else -> {
                        tlv.printTLVMap("TLV(tag=18) Map")
                        MiraiLogger.warning("Could not determine member permission, default permission MEMBER is being used")
                        MemberPermission.MEMBER
                    }
                }

                senderName = when {
                    tlv.containsKey(0x01u) -> String(tlv.getValue(0x01u))//这个人的qq昵称
                    tlv.containsKey(0x02u) -> String(tlv.getValue(0x02u))//这个人的群名片
                    else -> {
                        tlv.printTLVMap("TLV(tag=18) Map")
                        MiraiLogger.warning("Could not determine senderName")
                        "null"
                    }
                }
            }
        }

        return GroupMessage(bot.getGroup(groupNumber), senderName, senderPermission, bot.getQQ(qq), message)
    }
}

// endregion

// region friend message

data class FriendMessage(
    /**
     * 是否是在这次登录之前的消息, 即消息记录
     */
    val previous: Boolean,
    override val sender: QQ,
    override val message: MessageChain
) : MessagePacket<QQ>(), BroadcastControllable {
    /**
     * 是否应被自动广播. 此为内部 API
     */
    @MiraiInternalAPI
    override val shouldBroadcast: Boolean
        get() = !previous

    override val subject: QQ get() = sender
}


@Suppress("unused")
@PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
object FriendMessageEventParserAndHandler : KnownEventParserAndHandler<FriendMessage>(0x00A6u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): FriendMessage {
        discardExact(2)
        val l1 = readShort()
        discardExact(1)//0x00
        val previous = readByte().toInt() == 0x08
        discardExact(l1.toInt() - 2)
        //java.io.EOFException: Only 49 bytes were discarded of 69 requested
        //抖动窗口消息
        discardExact(69)
        readUShortLVByteArray()//font
        discardExact(2)//2个0x00
        val message = readMessageChain()
        return FriendMessage(
            previous = previous,
            sender = bot.getQQ(identity.from),
            message = message
        )
    }
}
// endregion