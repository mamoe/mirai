@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.encryptAndWrite
import net.mamoe.mirai.utils.writeHex
import net.mamoe.mirai.utils.writeQQ
import net.mamoe.mirai.utils.writeRandom

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
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeRandom(2)

        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            writeUByte(0x88.toUByte())
            writeQQ(qq)
            writeByte(0x00)
        }
    }
}

@PacketId("00 5C")
class ServerAccountInfoResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    //等级
    //升级剩余活跃天数
    //ignored
    override fun decode() {

    }

    @PacketId("00 5C")
    class Encrypted(inputStream: ByteReadPacket) : ServerPacket(inputStream) {
        fun decrypt(sessionKey: ByteArray): ServerAccountInfoResponsePacket = ServerAccountInfoResponsePacket(this.decryptBy(sessionKey)).setId(this.idHex)
    }
}