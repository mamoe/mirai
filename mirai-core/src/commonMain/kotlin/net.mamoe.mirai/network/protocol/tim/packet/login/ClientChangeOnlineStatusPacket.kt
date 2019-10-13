@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.utils.*

/**
 * 改变在线状态: "我在线上", "隐身" 等
 */
@PacketId("00 EC")
class ClientChangeOnlineStatusPacket(
        private val qq: Long,
        private val sessionKey: ByteArray,
        private val loginStatus: OnlineStatus
) : ClientPacket() {
    override val idHex: String by lazy {
        super.idHex + " " + getRandomByteArray(2).toUHexString()
    }

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            writeHex("01 00")
            writeUByte(loginStatus.id)
            writeHex("00 01 00 01 00 04 00 00 00 00")
        }
    }
}


