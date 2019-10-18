@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.*
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.event.*
import net.mamoe.mirai.utils.*

/**
 * 事件的识别 ID. 在 [事件确认包][ServerEventPacket.ResponsePacket] 中被使用.
 */
data class EventPacketIdentity(
        val from: UInt,//对于好友消息, 这个是发送人
        val to: UInt,//对于好友消息, 这个是bot
        internal val uniqueId: IoBuffer//8
) {
    override fun toString(): String = "(from=$from, to=$to)"
}

fun BytePacketBuilder.writeEventPacketIdentity(identity: EventPacketIdentity) = with(identity) {
    writeUInt(from)
    writeUInt(to)
    writeFully(uniqueId)
}

fun <S : ServerEventPacket> S.applyId(id: UShort): S {
    this.id = id
    return this
}

/**
 * Packet id: `00 CE` or `00 17`
 *
 * @author Him188moe
 */
abstract class ServerEventPacket(input: ByteReadPacket, val eventIdentity: EventPacketIdentity) : ServerPacket(input) {
    override var id: UShort = 0u

    class Raw(input: ByteReadPacket, override val id: UShort) : ServerPacket(input) {
        fun distribute(): ServerEventPacket = with(input) {
            val eventIdentity = EventPacketIdentity(
                    from = readUInt(),
                    to = readUInt(),
                    uniqueId = readIoBuffer(8)
            )
            discardExact(2)
            val type = readUShort()
            //DebugLogger.logPurple("unknown2Byte+byte = ${unknown2Byte.toUHexString()} ${type.toUHexString()}")
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

                0x0052u -> ServerGroupMessageEventPacket(input, eventIdentity)

                0x00A6u -> ServerFriendMessageEventPacket(input.debugPrint("好友消息事件"), eventIdentity)


                //00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 16 00 00 00 37 08 02 1A 12 08 95 02 10 90 04 40 98 E1 8C ED 05 48 AF 96 C3 A4 03 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 1A 29 08 00 10 05 18 98 E1 8C ED 05 20 01 28 FF FF FF FF 0F 32 15 E5 AF B9 E6 96 B9 E6 AD A3 E5 9C A8 E8 BE 93 E5 85 A5 2E 2E 2E
                //00 00 00 08 00 0A 00 04 01 00 00 00 00 00 00 07 00 00 00
                0x0210u -> {
                    discardExact(19)
                    println("type事件" + readUByte().toUInt())

                    //todo 错了. 可能是 00 79 才是.
                    ServerFriendTypingCanceledPacket(input, eventIdentity)
                    /*
                    if (readUByte().toUInt() == 0x37u) ServerFriendTypingStartedPacket(input, eventIdentity)
                    else /*0x22*/ ServerFriendTypingCanceledPacket(input, eventIdentity)*/
                }
                0x0079u -> IgnoredServerEventPacket(type, input, eventIdentity)

                //"02 10", "00 12" -> ServerUnknownEventPacket(input, eventIdentity)

                else -> {
                    MiraiLogger.logDebug("UnknownEvent type = ${type.toInt().toByteArray().toUHexString()}")
                    UnknownServerEventPacket(input, eventIdentity)
                }
            }.applyId(id).applySequence(sequenceId)
        }

        class Encrypted(input: ByteReadPacket, override var id: UShort, override var sequenceId: UShort) : ServerPacket(input) {
            fun decrypt(sessionKey: ByteArray): Raw = Raw(this.decryptBy(sessionKey), id).applySequence(sequenceId)
        }
    }

    inner class ResponsePacket(
            val bot: Long,
            val sessionKey: ByteArray
    ) : ClientPacket() {
        override val id: UShort get() = this@ServerEventPacket.id
        override val sequenceId: UShort get() = this@ServerEventPacket.sequenceId

        override fun encode(builder: BytePacketBuilder) = with(builder) {
            this.writeQQ(bot)
            this.writeHex(TIMProtocol.fixVer2)
            this.encryptAndWrite(sessionKey) {
                writeEventPacketIdentity(eventIdentity)
            }
        }

    }
}

/**
 * 忽略的事件.
 * 如 00 79: 总是与 01 12 一起发生, 但 00 79 却没多大意义
 */
@Suppress("unused")
class IgnoredServerEventPacket(val eventId: UShort, input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity)

/**
 * Unknown event
 */
class UnknownServerEventPacket(input: ByteReadPacket, eventIdentity: EventPacketIdentity) : ServerEventPacket(input, eventIdentity) {
    override fun decode() {
        MiraiLogger.logDebug("UnknownServerEventPacket data: " + this.input.readBytes().toUHexString())
    }
}
