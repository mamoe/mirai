package net.mamoe.mirai.network.packet.client.touch

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.PacketId
import net.mamoe.mirai.network.packet.client.*
import net.mamoe.mirai.network.packet.server.ServerPacket
import java.io.DataInputStream
import java.io.IOException

/**
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
@PacketId("00 58")
class ClientHeartbeatPacket(
        private val qq: Int,
        private val sessionKey: ByteArray
) : ClientPacket() {
    @Throws(IOException::class)
    override fun encode() {
        this.writeRandom(2)
        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("00 01 00 01")
        }
    }
}

class ServerHeartbeatResponsePacket(input: DataInputStream) : ServerPacket(input) {
    override fun decode() {

    }
}