@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.OutgoingPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.utils.OnlineStatus
import net.mamoe.mirai.utils.encryptAndWrite
import net.mamoe.mirai.utils.writeHex
import net.mamoe.mirai.utils.writeQQ

/**
 * 改变在线状态: "我在线上", "隐身" 等
 */
@PacketId(0x00_ECu)
class ChangeOnlineStatusPacket(
        private val bot: UInt,
        private val sessionKey: ByteArray,
        private val loginStatus: OnlineStatus
) : OutgoingPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(bot)
        this.writeHex(TIMProtocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            writeHex("01 00")
            writeUByte(loginStatus.id)
            writeHex("00 01 00 01 00 04 00 00 00 00")
        }
    }
}


