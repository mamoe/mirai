@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ResponsePacket
import net.mamoe.mirai.utils.*


@PacketId(0x00_02u)
class SendGroupMessagePacket(
        private val botQQ: UInt,
        private val groupId: UInt,//不是 number
        private val sessionKey: ByteArray,
        private val message: MessageChain
) : OutgoingPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(botQQ)
        this.writeHex(TIMProtocol.fixVer2)

        this.encryptAndWrite(sessionKey) {
            writeByte(0x2A)
            writeGroup(groupId)

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

    @PacketId(0x00_02u)
    class Response(input: ByteReadPacket) : ResponsePacket(input)
}