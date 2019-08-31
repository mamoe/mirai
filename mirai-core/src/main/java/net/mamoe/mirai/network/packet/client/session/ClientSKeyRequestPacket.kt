package net.mamoe.mirai.network.packet.client.session

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.*

/**
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
@PacketId("00 1D")
class ClientSKeyRequestPacket(
        private val qq: Int,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol._fixVer)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }
}