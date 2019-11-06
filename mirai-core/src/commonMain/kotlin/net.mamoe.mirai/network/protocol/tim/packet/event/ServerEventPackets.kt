@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.*
import net.mamoe.mirai.event.events.MemberPermissionChangedEvent
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.sessionKey
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.*

/**
 * 事件的识别 ID. 在 [事件确认包][ServerEventPacket.EventResponse] 中被使用.
 */
class EventPacketIdentity(
    val from: UInt,//对于好友消息, 这个是发送人
    val to: UInt,//对于好友消息, 这个是bot
    internal val uniqueId: IoBuffer//8
) {
    override fun toString(): String = "($from->$to)"
}

enum class SenderPermission {
    OWNER,
    OPERATOR,
    MEMBER;
}

object IgnoredEventPacket : Packet

class UnknownEventPacket(
    val id: UnknownEventId
// TODO: 2019/11/5  补充包数据 , 用于输出
) : Packet

/**
 * 事件包, 它将会分析 [事件ID][KnownEventId] 并解析事件为 [Packet]
 */
@NoLog
@Suppress("FunctionName")
object EventPacketFactory : PacketFactory<Packet, SessionKey>(SessionKey) {
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): Packet {
        val eventIdentity = EventPacketIdentity(
            from = readUInt(),
            to = readUInt(),
            uniqueId = readIoBuffer(8)
        )
        handler.sendPacket(EventPacketFactory(id, sequenceId, handler.bot.qqAccount, handler.sessionKey, eventIdentity))
        discardExact(2)
        return when (val type = EventId(readUShort())) {
            is KnownEventId -> type.parser(this, eventIdentity)
            is UnknownEventId -> UnknownEventPacket(type)
            is IgnoredEventId -> IgnoredEventPacket
            else -> throw AssertionError("Unknown EventId type")
        }
    }

    operator fun invoke(
        id: PacketId,
        sequenceId: UShort,
        bot: UInt,
        sessionKey: SessionKey,
        identity: EventPacketIdentity
    ): OutgoingPacket = buildOutgoingPacket(name = "EventPacket", id = id, sequenceId = sequenceId) {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeEventPacketIdentity(identity)
        }
    }
}

fun BytePacketBuilder.writeEventPacketIdentity(identity: EventPacketIdentity) = with(identity) {
    writeUInt(from)
    writeUInt(to)
    writeFully(uniqueId)
}

typealias EventPacketParser = ByteReadPacket.(EventPacketIdentity) -> Packet

interface EventId {
    val value: UShort
    val parser: EventPacketParser?
}


// TODO: 2019/11/5 整理文件

@Suppress("FunctionName")
fun EventId(value: UShort): EventId =
    KnownEventId.ofValueOrNull(value) ?: IgnoredEventIds.firstOrNull { it.value == value } ?: UnknownEventId(value)


object IgnoredEventIds : List<IgnoredEventId> by {
    listOf(
        0x0021u
    ).map { IgnoredEventId(it.toUShort()) }
}()

inline class IgnoredEventId(override val value: UShort) : EventId {
    override val parser: EventPacketParser get() = { IgnoredPacket }
}

inline class UnknownEventId(override val value: UShort) : EventId {
    override val parser: EventPacketParser
        get() = {
            MiraiLogger.debug("UnknownEventPacket type = ${value.toUHexString()}")
            MiraiLogger.debug("UnknownEventPacket data = ${readBytes().toUHexString()}")
            UnknownEventPacket(UnknownEventId(value))
        }
}

/**
 *
 * @param parser 解析器. 解析 [数据包][ByteReadPacket] 为 [Packet]
 */
@Suppress("unused")
enum class KnownEventId(override inline val value: UShort, override val parser: EventPacketParser) : EventId {
    /**
     * Android 客户端在线状态改变
     */
    ANDROID_DEVICE_ONLINE_STATUS_CHANGE(0x00C4u, {
        discardExact(13)
        EventPacket.AndroidDeviceStatusChange(
            if (readBoolean()) EventPacket.AndroidDeviceStatusChange.Kind.OFFLINE else EventPacket.AndroidDeviceStatusChange.Kind.ONLINE
        )
    }),


    GROUP_FILE_UPLOAD(0x002Du, {
        discardExact(60)
        val size = readShort().toInt()
        discardExact(3)
        EventPacket.GroupFileUpload(xmlMessage = readString(size))
    }),

    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
    GROUP_MEMBER_PERMISSION_CHANGE(0x002Cu, {
        // 群里一个人变成管理员:
        // 00 00 00 08 00 0A 00 04 01 00 00 00 22 96 29 7B 01 01 76 E4 B8 DD 01
        // 取消管理员
        // 00 00 00 08 00 0A 00 04 01 00 00 00 22 96 29 7B 01 00 76 E4 B8 DD 00
        discardExact(remaining - 5)
        EventPacket.MemberPermissionChange().apply {
            groupId = it.from
            qq = readUInt()
            kind = when (readByte().toInt()) {
                0x00 -> MemberPermissionChangedEvent.Kind.NO_LONGER_OPERATOR
                0x01 -> MemberPermissionChangedEvent.Kind.BECOME_OPERATOR
                else -> error("Could not determine permission change kind")
            }
        }
    }),


    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
    GROUP_MESSAGE(0x0052u, {
        EventPacket.GroupMessage().apply {
            discardExact(31)
            groupNumber = readUInt()
            discardExact(1)
            qq = readUInt()

            discardExact(48)
            readUShortLVByteArray()
            discardExact(2)//2个0x00
            message = readMessageChain()

            val map = readTLVMap(true)
            if (map.containsKey(18u)) {
                map.getValue(18u).read {
                    val tlv = readTLVMap(true)
                    senderPermission = when (tlv.takeIf { it.containsKey(0x04u) }?.get(0x04u)?.getOrNull(3)?.toUInt()) {
                        null -> SenderPermission.MEMBER
                        0x08u -> SenderPermission.OWNER
                        0x10u -> SenderPermission.OPERATOR
                        else -> {
                            tlv.printTLVMap("TLV(tag=18) Map")
                            MiraiLogger.warning("Could not determine member permission, default permission MEMBER is being used")
                            SenderPermission.MEMBER
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

        }
    }),


    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
    FRIEND_MESSAGE(0x00A6u, {
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
        EventPacket.FriendMessage(
            isPrevious = previous,
            qq = it.from,
            message = readMessageChain()
        )
    }),


    FRIEND_CONVERSATION_INITIALIZE(0x0079u, {
        discardExact(4)// 00 00 00 00
        EventPacket.FriendConversationInitialize().apply {
            qq = readUInt()
        }
    }),


    ;

    companion object {
        fun ofValueOrNull(value: UShort): KnownEventId? = values().firstOrNull { it.value == value }
    }
}