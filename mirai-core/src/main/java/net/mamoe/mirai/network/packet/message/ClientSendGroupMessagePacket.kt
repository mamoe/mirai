package net.mamoe.mirai.network.packet.message

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import java.io.DataInputStream

/**
 * @author Him188moe
 */
@PacketId("00 02")
@ExperimentalUnsignedTypes
class ClientSendGroupMessagePacket(
        private val groupId: Int,//不是 number
        private val qq: Int,
        private val sessionKey: ByteArray,
        private val message: String
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(qq)
        this.writeHex(Protocol._fixVer)

        this.encryptAndWrite(sessionKey) {
            it.writeHex("00 01 01 00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00")
            it.writeTime()
            it.writeRandom(4)
            it.writeHex("00 00 00 00 09 00 86 00 00 0C E5 BE AE E8 BD AF E9 9B 85 E9 BB 91")
            it.writeZero(2)

            //messages
            val bytes = message.toByteArray()
            it.writeByte(0x2A)
            it.writeInt(groupId)
            it.writeShort(19 + bytes.size)

            it.writeByte(0x01)
            it.writeByte(0x01)
            it.writeShort(bytes.size + 3)
            it.writeByte(0x01)
            it.writeShort(bytes.size)
            it.write(bytes)
        }
    }
}

@PacketId("00 02")
class ServerSendGroupMessageResponsePacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {
    }
}