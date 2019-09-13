package net.mamoe.mirai.network.packet.login

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.utils.ClientLoginStatus

/**
 * 改变在线状态: "我在线上", "隐身" 等
 *
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
@PacketId("00 EC")
class ClientChangeOnlineStatusPacket(
        private val qq: Long,
        private val sessionKey: ByteArray,
        private val loginStatus: ClientLoginStatus

) : ClientPacket() {

    override fun encode() {
        this.writeRandom(2)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("01 00")
            it.writeByte(loginStatus.id)
            it.writeHex("00 01 00 01 00 04 00 00 00 00")
        }
    }
}


