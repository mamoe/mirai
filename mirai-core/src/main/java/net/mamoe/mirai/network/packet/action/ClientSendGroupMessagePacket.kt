package net.mamoe.mirai.network.packet.action

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream

/**
 * @author Him188moe
 */
@PacketId("00 02")

class ClientSendGroupMessagePacket(
        private val groupId: Long,//不是 number
        private val botQQ: Long,
        private val sessionKey: ByteArray,
        private val message: String
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(botQQ)
        this.writeHex(Protocol.fixVer2)

        this.encryptAndWrite(sessionKey) {
            val bytes = message.toByteArray()
            it.writeByte(0x2A)
            it.writeGroup(groupId)
            it.writeShort(56 + bytes.size)

            it.writeHex("00 01 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00")
            it.writeTime()
            it.writeRandom(4)
            it.writeHex("Protocol.messageConst1")
            it.writeZero(2)

            //messages
            it.writeByte(0x01)
            it.writeShort(bytes.size + 3)
            it.writeByte(0x01)
            it.writeShort(bytes.size)
            it.write(bytes)

            println(it.toByteArray().toUHexString())
        }
    }
}

@PacketId("00 02")
class ServerSendGroupMessageResponsePacket(input: DataInputStream) : ServerPacket(input)