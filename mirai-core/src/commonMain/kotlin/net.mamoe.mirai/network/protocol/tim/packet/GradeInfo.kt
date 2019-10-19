@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.encryptAndWrite
import net.mamoe.mirai.utils.writeHex
import net.mamoe.mirai.utils.writeQQ

/**
 * 获取升级天数等.
 *
 * @author Him188moe
 */
@PacketId(0x00_5Cu)
class ClientAccountInfoRequestPacket(
        private val qq: UInt,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            writeUByte(0x88u)
            writeQQ(qq)
            writeByte(0x00)
        }
    }
}

@PacketId(0x00_5Cu)
class ServerAccountInfoResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    //等级
    //升级剩余活跃天数
    //ignored
    override fun decode() {

    }

    @PacketId(0x00_5Cu)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerAccountInfoResponsePacket = ServerAccountInfoResponsePacket(this.decryptBy(sessionKey)).applySequence(sequenceId)
    }
}