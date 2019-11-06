@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*

object SendGroupMessagePacket : SessionPacketFactory<SendGroupMessagePacket.Response>() {
    operator fun invoke(
        botQQ: UInt,
        groupInternalId: GroupInternalId,
        sessionKey: SessionKey,
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

    object Response : Packet

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): Response = Response
}