@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*

/**
 * The packet received when logging in, used to redirect server address
 *
 * @see RedirectionPacket
 * @see SubmitPasswordPacket
 *
 * @author Him188moe
 */
@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")
@AnnotatedId(KnownPacketId.TOUCH)
class TouchResponsePacket(input: ByteReadPacket) : ServerPacket(input) {
    var serverIP: String? = null

    var loginTime: Int = 0
    lateinit var loginIP: String
    lateinit var token0825: ByteArray//56

    override fun decode() = with(input) {
        when (val id = readByte().toUByte().toInt()) {
            0xFE -> {
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

    @AnnotatedId(KnownPacketId.TOUCH)
    class Encrypted(input: ByteReadPacket) : ServerPacket(input) {
        fun decrypt(): TouchResponsePacket =
            TouchResponsePacket(decryptBy(TIMProtocol.touchKey.hexToBytes())).applySequence(sequenceId)
    }
}

/**
 * The packet to sendTouch server, that is, to start the connection to the server.
 *
 * @author Him188moe
 */
@AnnotatedId(KnownPacketId.TOUCH)
object TouchPacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        serverIp: String
    ) = buildOutgoingPacket {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer)
        writeHex(TIMProtocol.touchKey)

        encryptAndWrite(TIMProtocol.touchKey) {
            writeHex(TIMProtocol.constantData1)
            writeHex(TIMProtocol.constantData2)
            writeQQ(bot)
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
@AnnotatedId(KnownPacketId.TOUCH)
object RedirectionPacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        serverIP: String
    ) = buildOutgoingPacket {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer)
        writeHex(TIMProtocol.touchKey)//redirection key

        encryptAndWrite(TIMProtocol.touchKey) {
            writeHex(TIMProtocol.constantData1)
            writeHex(TIMProtocol.constantData2)
            writeQQ(bot)
            writeHex("00 01 00 00 03 09 00 0C 00 01")
            writeIP(serverIP)
            writeHex("01 6F A1 58 22 01 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 03 00 19")
            writeHex(TIMProtocol.publicKey)
        }
    }
}