@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.*

/**
 * 事件的识别 ID. 在 [事件确认包][ServerEventPacket.EventResponse] 中被使用.
 */
data class EventPacketIdentity(
    val from: UInt,//对于好友消息, 这个是发送人
    val to: UInt,//对于好友消息, 这个是bot
    internal val uniqueId: IoBuffer//8
) {
    override fun toString(): String = "($from->$to)"
}

fun BytePacketBuilder.writeEventPacketIdentity(identity: EventPacketIdentity) = with(identity) {
    writeUInt(from)
    writeUInt(to)
    writeFully(uniqueId)
}

fun <S : ServerEventPacket> S.applyId(id: PacketId): S {
    this.packetId = id
    return this
}

/**
 * Packet id: `00 CE` or `00 17`
 *
 * @author Him188moe
 */
abstract class ServerEventPacket(input: ByteReadPacket, val eventIdentity: EventPacketIdentity) : ServerPacket(input) {
    override var packetId: PacketId = NullPacketId

    class Raw(input: ByteReadPacket, override val packetId: PacketId) : ServerPacket(input) {
        fun distribute(): ServerEventPacket = with(input) {
            val eventIdentity = EventPacketIdentity(
                from = readUInt(),
                to = readUInt(),
                uniqueId = readIoBuffer(8)
            )
            discardExact(2)
            val type = readUShort()
            //DebugLogger.warning("unknown2Byte+byte = ${unknown2Byte.toUHexString()} ${type.toUHexString()}")
            return when (type.toUInt()) {
                0x00C4u -> {
                    discardExact(13)
                    if (readBoolean()) {
                        ServerAndroidOfflineEventPacket(input, eventIdentity)
                    } else {
                        ServerAndroidOnlineEventPacket(input, eventIdentity)
                    }
                }
                0x002Du -> ServerGroupUploadFileEventPacket(input, eventIdentity)

                0x002Cu -> GroupMemberPermissionChangedPacket(input, eventIdentity)

                /*
                 *
    inline GROUP_MEMBER_NICK_CHANGED(0x002Fu, null),
    inline GROUP_MEMBER_PERMISSION_CHANGED(0x002Cu, null),

                 */

                0x0052u -> GroupMessageEventPacket(input, eventIdentity)

                0x00A6u -> FriendMessageEventPacket(input, eventIdentity)

                // "对方正在输入..."
                0x0210u -> IgnoredServerEventPacket(input, eventIdentity)

                else -> {
                    UnknownServerEventPacket(type.toByteArray(), true, input, eventIdentity)
                }
            }.applyId(packetId).applySequence(sequenceId)
        }

        class Encrypted(input: ByteReadPacket, override var packetId: PacketId, override var sequenceId: UShort) :
            ServerPacket(input) {
            fun decrypt(sessionKey: ByteArray): Raw =
                Raw(this.decryptBy(sessionKey), packetId).applySequence(sequenceId)
        }
    }

    @Suppress("FunctionName")
    fun ResponsePacket(
        bot: UInt,
        sessionKey: ByteArray
    ): OutgoingPacket = EventResponse(this.packetId, this.sequenceId, bot, sessionKey, this.eventIdentity)

    @NoLog
    @Suppress("FunctionName")
    object EventResponse : OutgoingPacketBuilder {
        operator fun invoke(
            id: PacketId,
            sequenceId: UShort,
            bot: UInt,
            sessionKey: ByteArray,
            identity: EventPacketIdentity
        ): OutgoingPacket = buildOutgoingPacket(name = "EventResponse", id = id, sequenceId = sequenceId) {
            writeQQ(bot)
            writeHex(TIMProtocol.fixVer2)
            encryptAndWrite(sessionKey) {
                writeEventPacketIdentity(identity)
            }
        }
    }
}

/**
 * 忽略的事件
 */
@Suppress("unused")
class IgnoredServerEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) :
    ServerEventPacket(input, eventIdentity)

/**
 * Unknown event
 */
class UnknownServerEventPacket(
    @Suppress("MemberVisibilityCanBePrivate")
    val eventId: ByteArray,
    private val showData: Boolean = false,
    input: ByteReadPacket,
    eventIdentity: EventPacketIdentity
) :
    ServerEventPacket(input, eventIdentity) {
    override fun decode() {
        MiraiLogger.debug("UnknownEvent type = ${eventId.toUHexString()}")
        if (showData) {
            MiraiLogger.debug("UnknownServerEventPacket data: " + this.input.readBytes().toUHexString())
        } else {
            this.input.discard()
        }
    }
}