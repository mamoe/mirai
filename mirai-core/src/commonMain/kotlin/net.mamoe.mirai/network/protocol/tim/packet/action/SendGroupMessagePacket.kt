@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*


@AnnotatedId(KnownPacketId.SEND_GROUP_MESSAGE)
object SendGroupMessagePacket : OutgoingPacketBuilder {
    operator fun invoke(
        botQQ: UInt,
        groupInternalId: GroupInternalId,
        sessionKey: ByteArray,
        message: MessageChain
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(botQQ)
        writeHex(TIMProtocol.fixVer2)

        encryptAndWrite(sessionKey) {
            writeByte(0x2A)
            writeGroup(groupInternalId)

            writeShortLVPacket {
                writeHex("00 01 01")
                writeHex("00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00")

                writeTime()
                writeRandom(4)
                writeHex("00 00 00 00 09 00 86")
                writeHex(TIMProtocol.messageConst1)
                writeZero(2)

                writePacket(message.toPacket(true))
            }
            /*it.writeByte(0x01)
            it.writeShort(bytes.size + 3)
            it.writeByte(0x01)
            it.writeShort(bytes.size)
            it.write(bytes)*/
        }
    }

    @AnnotatedId(KnownPacketId.SEND_GROUP_MESSAGE)
    class Response(input: ByteReadPacket) : ResponsePacket(input)
}