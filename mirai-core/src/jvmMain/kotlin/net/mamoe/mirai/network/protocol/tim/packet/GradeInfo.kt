package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import java.io.DataInputStream

/**
 * 获取升级天数等.
 *
 * @author Him188moe
 */

@PacketId("00 5C")
class ClientAccountInfoRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id

        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            writeByte(0x88)
            writeQQ(qq)
            writeByte(0x00)
        }
    }
}

@PacketId("00 5C")
class ServerAccountInfoResponsePacket(input: DataInputStream) : ServerPacket(input) {
    //等级
    //升级剩余活跃天数
    //ignored
    override fun decode() {

    }

    @PacketId("00 5C")
    class Encrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
        fun decrypt(sessionKey: ByteArray): ServerAccountInfoResponsePacket = ServerAccountInfoResponsePacket(this.decryptBy(sessionKey)).setId(this.idHex)
    }
}