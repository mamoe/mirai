package net.mamoe.mirai.network.packet.client.session

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.*
import net.mamoe.mirai.util.ClientLoginStatus

/**
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
@PacketId("00 EC")
class ClientLoginStatusPacket(
        private val qq: Int,
        private val sessionKey: ByteArray,
        private val loginStatus: ClientLoginStatus

) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(qq)
        this.writeHex(Protocol._fixVer)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("01 00")
            it.writeByte(loginStatus.id)
            it.writeHex("00 01 00 01 00 04 00 00 00 00")
        }
    }
}