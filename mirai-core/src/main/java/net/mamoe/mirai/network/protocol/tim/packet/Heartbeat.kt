package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import java.io.DataInputStream
import java.io.IOException

/**
 * @author Him188moe
 */

@PacketId("00 58")
class ClientHeartbeatPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    @Throws(IOException::class)
    override fun encode() {
        this.writeRandom(2)
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("00 01 00 01")
        }
    }
}

class ServerHeartbeatResponsePacket(input: DataInputStream) : ServerPacket(input)