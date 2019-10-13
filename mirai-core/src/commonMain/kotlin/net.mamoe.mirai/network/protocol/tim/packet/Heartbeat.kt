package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.utils.*


@PacketId("00 58")
class ClientHeartbeatPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override val idHex: String by lazy {
        super.idHex + " " + getRandomByteArray(2).toUHexString()
    }

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.encryptAndWrite(sessionKey) {
            writeHex("00 01 00 01")
        }
    }
}

class ServerHeartbeatResponsePacket(input: ByteReadPacket) : ServerPacket(input)