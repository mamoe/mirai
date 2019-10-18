@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.*

/**
 * SKey 用于 http api
 */
@PacketId(0x00_1Du)
class ClientSKeyRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeQQ(qq)
        writeHex(TIMProtocol.fixVer2)
        encryptAndWrite(sessionKey) {
            writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }
}

@PacketId(0x00_1Du)
class ClientSKeyRefreshmentRequestPacket(
        private val qq: Long,
        private val sessionKey: ByteArray
) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.encryptAndWrite(sessionKey) {
            writeHex("33 00 05 00 08 74 2E 71 71 2E 63 6F 6D 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 0C 6A 75 62 61 6F 2E 71 71 2E 63 6F 6D 00 09 6B 65 2E 71 71 2E 63 6F 6D")
        }
    }
}

@PacketId(0x00_1Du)
class ServerSKeyResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    lateinit var sKey: String

    override fun decode() = with(input) {
        discardExact(4)
        //debugDiscardExact(2)
        sKey = this.readString(10)
        DebugLogger.logPurple("SKey=$sKey")
        DebugLogger.logPurple("Skey包后面${this.readRemainingBytes().toUHexString()}")
    }

    @PacketId(0x00_1Du)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(sessionKey: ByteArray): ServerSKeyResponsePacket = ServerSKeyResponsePacket(this.decryptBy(sessionKey)).applySequence(sequenceId)
    }
}