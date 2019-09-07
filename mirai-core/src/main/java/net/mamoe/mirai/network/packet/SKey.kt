package net.mamoe.mirai.network.packet

import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.utils.TEA
import java.io.DataInputStream


/**
 * @author Him188moe
 */
@ExperimentalUnsignedTypes
@PacketId("00 1D")
class ClientSKeyRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id

        this.writeQQ(qq)
        this.writeHex(Protocol.fixVer2)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }

    override fun getFixedId(): String {
        return this.idHex + " ?? ??"
    }
}

/**
 * @author Him188moe
 */
@PacketId("00 1D")
@ExperimentalUnsignedTypes
class ClientSKeyRefreshmentRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode() {
        this.writeRandom(2)//part of packet id
        this.writeQQ(qq)
        this.encryptAndWrite(sessionKey) {
            it.writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }
}

/**
 * @author Him188moe
 */
class ServerSKeyResponsePacket(input: DataInputStream) : ServerPacket(input) {
    lateinit var sKey: String

    override fun decode() {
        this.sKey = String(this.input.goto(4).readNBytes(10))
    }


    class Encrypted(inputStream: DataInputStream) : ServerPacket(inputStream) {
        fun decrypt(sessionKey: ByteArray): ServerSKeyResponsePacket {
            this.input goto 14
            val data = this.input.readAllBytes().let { it.copyOfRange(0, it.size - 1) }
            return ServerSKeyResponsePacket(TEA.decrypt(data, sessionKey).dataInputStream()).setId(this.idHex)
        }
    }
}