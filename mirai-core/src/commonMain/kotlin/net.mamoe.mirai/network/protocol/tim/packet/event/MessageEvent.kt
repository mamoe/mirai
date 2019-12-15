@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.String
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.getGroup
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion
import net.mamoe.mirai.network.protocol.tim.packet.action.ImageLink
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.internal.coerceAtLeastOrFail
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
expect abstract class MessagePacket<TSender : QQ, TSubject : Contact>() : MessagePacketBase<TSender, TSubject>

@MiraiInternalAPI
abstract class MessagePacketBase<TSender : QQ, TSubject : Contact> : EventPacket, BotEvent() {
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
    suspend inline fun Image.getLink(): ImageLink = bot.withSession { getLink() }

    suspend inline fun Image.downloadAsByteArray(): ByteArray = getLink().downloadAsByteArray()
    suspend inline fun Image.download(): ByteReadPacket = getLink().download()
    // endregion

    inline fun At.qq(): QQ = bot.getQQ(this.target)

    inline fun Int.qq(): QQ = bot.getQQ(this.coerceAtLeastOrFail(0).toUInt())
    inline fun Long.qq(): QQ = bot.getQQ(this.coerceAtLeastOrFail(0))
    inline fun UInt.qq(): QQ = bot.getQQ(this)

    suspend inline fun Int.group(): Group = bot.getGroup(this.coerceAtLeastOrFail(0).toUInt())
    suspend inline fun Long.group(): Group = bot.getGroup(this.coerceAtLeastOrFail(0))
    suspend inline fun UInt.group(): Group = bot.getGroup(GroupId(this))
    suspend inline fun GroupId.group(): Group = bot.getGroup(this)
    suspend inline fun GroupInternalId.group(): Group = bot.getGroup(this)
}

// region group message

@Suppress("unused", "NOTHING_TO_INLINE")
data class GroupMessage(
    val group: Group,
    val senderName: String,
    /**
     * 发送方权限.
     */
    val permission: MemberPermission,
    override val sender: Member,
    override val message: MessageChain
) : MessagePacket<Member, Group>() {

    /*
    01 00 09 01 00 06 66 61 69 6C 65 64 19 00 45 01 00 42 AA 02 3F 08 06 50 02 60 00 68 00 88 01 00 9A 01 31 08 0A 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 02 B0 03 00 C0 03 00 D0 03 00 E8 03 02 8A 04 04 08 02 08 01 90 04 80 C8 10 0E 00 0E 01 00 04 00 00 08 E4 07 00 04 00 00 00 01 12 00 1E 02 00 09 E9 85 B1 E9 87 8E E6 98 9F 03 00 01 02 05 00 04 00 00 00 03 08 00 04 00 00 00 04
     */
    override val subject: Group get() = group

    inline fun At.member(): Member = group.getMember(this.target)
    inline fun UInt.member(): Member = group.getMember(this)
    inline fun Long.member(): Member = group.getMember(this.toUInt())
    override fun toString(): String =
        "GroupMessage(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"
}

@PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
internal object GroupMessageEventParserAndHandler : KnownEventParserAndHandler<GroupMessage>(0x0052u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): GroupMessage {
        discardExact(31)
        val groupNumber = readUInt()
        discardExact(1)
        val qq = readUInt()

        discardExact(48)
        readUShortLVByteArray()
        discardExact(2)//2个0x00

        //debugPrintIfFail {
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
                    0x10u -> MemberPermission.ADMINISTRATOR
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

        val group = bot.getGroup(groupNumber)
        return GroupMessage(group, senderName, senderPermission, group.getMember(qq), message).apply { this.botVar = bot }
        // }

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
) : MessagePacket<QQ, QQ>(), BroadcastControllable {
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
internal object FriendMessageEventParserAndHandler : KnownEventParserAndHandler<FriendMessage>(0x00A6u) {
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
        ).apply { this.botVar = bot }
    }
}
// endregion