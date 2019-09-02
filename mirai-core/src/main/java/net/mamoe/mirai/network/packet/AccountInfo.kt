package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.TEACryptor
import java.io.DataInputStream

/**
 * 获取升级天数等.
 *
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
@PacketId("00 5C")
class ClientAccountInfoRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
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

class ServerAccountInfoResponsePacket(input: DataInputStream) : ServerPacket(input) {
    //等级
    //升级剩余活跃天数
    //ignored
    override fun decode() {

    }
}

class ServerAccountInfoResponsePacketEncrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
    override fun decode() {

    }

    fun decrypt(sessionKey: ByteArray): ServerAccountInfoResponsePacket {
        this.input goto 14
        val data = this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }
        return ServerAccountInfoResponsePacket(TEACryptor.decrypt(data, sessionKey).dataInputStream());
    }
}