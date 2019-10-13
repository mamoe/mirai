package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.setId
import net.mamoe.mirai.utils.*

/**
 * The packet received when logging in, used to redirect server address
 *
 * @see ClientServerRedirectionPacket
 * @see ClientPasswordSubmissionPacket
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")
@PacketId("08 25 31 01")
class ServerTouchResponsePacket(inputStream: ByteReadPacket) : ServerPacket(inputStream) {
    var serverIP: String? = null

    var loginTime: Int = 0
    lateinit var loginIP: String
    lateinit var token0825: ByteArray//56

    enum class Type {
        TYPE_08_25_31_01,
        TYPE_08_25_31_02,
    }


    override fun decode() = with(input) {
        when (val id = readByte().toUByte().toInt()) {
            0xFE -> {//todo 在 packet 解析时分类而不是在这里分类
                discardExact(94)
                serverIP = readIP()
            }
            0x00 -> {
                discardExact(4)
                token0825 = readBytes(56)
                discardExact(6)

                loginTime = readInt()
                loginIP = readIP()
            }

            else -> {
                throw IllegalStateException(id.toByte().toUHexString())
            }
        }
    }

    class Encrypted(private val type: Type, inputStream: ByteReadPacket) : ServerPacket(inputStream) {

        fun decrypt(): ServerTouchResponsePacket = ServerTouchResponsePacket(decryptBy(when (type) {
            Type.TYPE_08_25_31_02 -> TIMProtocol.redirectionKey.hexToBytes()
            Type.TYPE_08_25_31_01 -> TIMProtocol.touchKey.hexToBytes()
        })).setId(this.idHex)
    }
}

/**
 * The packet to sendTouch server, that is, to start the connection to the server.
 *
 * @author Him188moe
 */
@PacketId("08 25 31 01")
class ClientTouchPacket(private val qq: Long, private val serverIp: String) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.writeHex(TIMProtocol.touchKey)

        this.encryptAndWrite(TIMProtocol.touchKey) {
            writeHex(TIMProtocol.constantData1)
            writeHex(TIMProtocol.constantData2)
            writeQQ(qq)
            writeHex("00 00 00 00 03 09 00 08 00 01")
            writeIP(serverIp)
            writeHex("00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19")
            writeHex(TIMProtocol.publicKey)
        }
    }
}

/**
 * Server redirection (0825 response)
 *
 * @author Him188moe
 */
@PacketId("08 25 31 02")
class ClientServerRedirectionPacket(private val serverIP: String, private val qq: Long) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.writeHex(TIMProtocol.redirectionKey)

        this.encryptAndWrite(TIMProtocol.redirectionKey) {
            this.writeHex(TIMProtocol.constantData1)
            this.writeHex(TIMProtocol.constantData2)
            this.writeQQ(qq)
            this.writeHex("00 01 00 00 03 09 00 0C 00 01")
            this.writeIP(serverIP)
            this.writeHex("01 6F A1 58 22 01 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 03 00 19")
            this.writeHex(TIMProtocol.publicKey)
        }
    }
}