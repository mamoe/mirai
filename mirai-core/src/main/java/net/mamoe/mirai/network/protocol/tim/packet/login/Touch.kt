package net.mamoe.mirai.network.protocol.tim.packet.login

import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.ByteArrayDataOutputStream
import net.mamoe.mirai.utils.TEA
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toUHexString
import java.io.DataInputStream
import java.io.IOException

/**
 * A packet received when logging in, used to redirect server address
 *
 * @see ClientServerRedirectionPacket
 * @see ClientPasswordSubmissionPacket
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")
@PacketId("08 25 31 01")
class ServerTouchResponsePacket(inputStream: DataInputStream) : ServerPacket(inputStream) {
    var serverIP: String? = null

    var loginTime: Int = 0
    lateinit var loginIP: String
    lateinit var token0825: ByteArray//56

    enum class Type {
        TYPE_08_25_31_01,
        TYPE_08_25_31_02,
    }


    override fun decode() {
        when (val id = input.readByte().toUByte().toInt()) {
            0xFE -> {
                input.skip(94)
                serverIP = input.readIP()
            }
            0x00 -> {
                input.skip(4)
                token0825 = input.readNBytes(56)
                input.skip(6)

                loginTime = input.readInt()
                loginIP = input.readIP()
            }

            else -> {
                throw IllegalStateException(arrayOf(id.toUByte()).toUByteArray().toUHexString())
            }
        }
    }

    class Encrypted(private val type: Type, inputStream: DataInputStream) : ServerPacket(inputStream) {

        fun decrypt(): ServerTouchResponsePacket = ServerTouchResponsePacket(decryptBy(when (type) {
            Type.TYPE_08_25_31_02 -> TIMProtocol.redirectionKey.hexToBytes()
            Type.TYPE_08_25_31_01 -> TIMProtocol.touchKey.hexToBytes()
        })).setId(this.idHex)
    }
}

/**
 * The packet to touch server, that is, to start the connection to the server.
 *
 * @author Him188moe
 */

@PacketId("08 25 31 01")
class ClientTouchPacket(private val qq: Long, private val serverIp: String) : ClientPacket() {

    @Throws(IOException::class)
    override fun encode() {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.writeHex(TIMProtocol.touchKey)

        this.encryptAndWrite(TIMProtocol.touchKey) {
            it.writeHex(TIMProtocol.constantData1)
            it.writeHex(TIMProtocol.constantData2)
            it.writeQQ(qq)
            it.writeHex("00 00 00 00 03 09 00 08 00 01")
            it.writeIP(serverIp)
            it.writeHex("00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19")
            it.writeHex(TIMProtocol.publicKey)
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

    override fun encode() {
        this.writeQQ(qq)
        this.writeHex(TIMProtocol.fixVer)
        this.writeHex(TIMProtocol.redirectionKey)


        this.write(TEA.encrypt(object : ByteArrayDataOutputStream() {
            @Throws(IOException::class)
            override fun toByteArray(): ByteArray {
                this.writeHex(TIMProtocol.constantData1)
                this.writeHex(TIMProtocol.constantData2)
                this.writeQQ(qq)
                this.writeHex("00 01 00 00 03 09 00 0C 00 01")
                this.writeIP(serverIP)
                this.writeHex("01 6F A1 58 22 01 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 03 00 19")
                this.writeHex(TIMProtocol.publicKey)
                return super.toByteArray()
            }
        }.toByteArray(), TIMProtocol.redirectionKey.hexToBytes()))
    }
}