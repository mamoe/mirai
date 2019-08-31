package net.mamoe.mirai.network.packet.client.session

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.*
import net.mamoe.mirai.network.packet.server.ServerPacket
import java.io.DataInputStream

/**
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
@PacketId("00 5C")
class ClientHandshake1Packet(
        val qq: Int,
        val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(qq)
        this.writeHex(Protocol._fixVer)
        this.encryptAndWrite(sessionKey) {
            it.writeByte(0x88)
            it.writeQQ(qq)
            it.writeByte(0x00)
        }
    }
}

class ServerHandshake1ResponsePacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }
}