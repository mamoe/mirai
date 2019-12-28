package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.message.FriendMessage
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.*


@UseExperimental(ExperimentalUnsignedTypes::class)
internal object GroupMessageEventParserAndHandler : KnownEventParserAndHandler<GroupMessage>(0x0052u) {

    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): GroupMessage {
        discardExact(31)
        val groupNumber = readGroup()
        discardExact(1)
        val qq = readQQ()

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
                    tlv.containsKey(0x01u) -> kotlinx.io.core.String(tlv.getValue(0x01u))//这个人的qq昵称
                    tlv.containsKey(0x02u) -> kotlinx.io.core.String(tlv.getValue(0x02u))//这个人的群名片
                    else -> {
                        tlv.printTLVMap("TLV(tag=18) Map")
                        MiraiLogger.warning("Could not determine senderName")
                        "null"
                    }
                }
            }
        }

        val group = bot.getGroup(groupNumber)
        return GroupMessage(
            bot = bot,
            group = group,
            senderName = senderName,
            permission = senderPermission,
            sender = group.getMember(qq),
            message = message
        )
    }
}

// endregion

// region friend message


@Suppress("unused")
@UseExperimental(ExperimentalUnsignedTypes::class)
internal object FriendMessageEventParserAndHandler : KnownEventParserAndHandler<FriendMessage>(0x00A6u) {

    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
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
            bot = bot,
            previous = previous,
            sender = bot.getQQ(identity.from),
            message = message
        )
    }
}
// endregion