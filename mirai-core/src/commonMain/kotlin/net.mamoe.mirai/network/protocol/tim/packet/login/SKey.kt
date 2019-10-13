package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.setId
import net.mamoe.mirai.utils.*

/**
 * SKey 用于 http api
 */
@PacketId("00 1D")
class ClientSKeyRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override val idHex: String by lazy {
        super.idHex + " " + getRandomByteArray(2).toUHexString()
    }

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(qq)
        writeHex(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }
}

@PacketId("00 1D")
class ClientSKeyRefreshmentRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override val idHex: String by lazy {
        super.idHex + " " + getRandomByteArray(2).toUHexString()
    }

    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.encryptAndWrite(sessionKey) {
            writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }
}

class ServerSKeyResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    lateinit var sKey: String

    override fun decode() = with(input) {
        discardExact(4)
        sKey = this.readString(10)//todo test
        MiraiLogger.logDebug("SKey=$sKey")
    }

    class Encrypted(inputStream: ByteReadPacket) : ServerPacket(inputStream) {
        fun decrypt(sessionKey: ByteArray): ServerSKeyResponsePacket = ServerSKeyResponsePacket(this.decryptBy(sessionKey)).setId(this.idHex)
    }
}