@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.core.writeFully
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*

object TouchKey : DecrypterByteArray, DecrypterType<TouchKey> {
    override val value: ByteArray = TIMProtocol.touchKey
}

/**
 * The packet to sendTouch server, that is, to start the connection to the server.
 *
 * @author Him188moe
 */
@AnnotatedId(KnownPacketId.TOUCH)
object TouchPacket : PacketFactory<TouchPacket.TouchResponse, TouchKey>(TouchKey) {
    operator fun invoke(
        bot: UInt,
        serverIp: String,
        isRedirect: Boolean
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeFully(TIMProtocol.fixVer)
        writeFully(TIMProtocol.touchKey)

        encryptAndWrite(TIMProtocol.touchKey) {
            writeFully(TIMProtocol.constantData1)
            writeFully(TIMProtocol.constantData2)
            writeQQ(bot)
            writeHex(if (isRedirect) "00 01 00 00 03 09 00 0C 00 01" else "00 00 00 00 03 09 00 08 00 01")
            writeIP(serverIp)
            writeHex(
                if (isRedirect) "01 6F A1 58 22 01 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 03 00 19"
                else "00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19"
            )
            writeFully(TIMProtocol.publicKey)
        }
    }

    sealed class TouchResponse : Packet {
        data class OK(
            var loginTime: Int,
            val loginIP: String,
            val token0825: ByteArray
        ) : TouchResponse() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is OK) return false

                if (loginTime != other.loginTime) return false
                if (loginIP != other.loginIP) return false
                if (!token0825.contentEquals(other.token0825)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = loginTime
                result = 31 * result + loginIP.hashCode()
                result = 31 * result + token0825.contentHashCode()
                return result
            }
        }

        data class Redirection(
            val serverIP: String? = null
        ) : TouchResponse()
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): TouchResponse {
        when (val flag = readByte().toUByte().toInt()) {
            0xFE -> {
                discardExact(94)
                return TouchResponse.Redirection(readIP())
            }
            0x00 -> {
                discardExact(4)
                val token0825 = readBytes(56)
                discardExact(6)

                val loginTime = readInt()
                val loginIP = readIP()
                return TouchResponse.OK(loginTime, loginIP, token0825)
            }

            else -> throw IllegalStateException(flag.toByte().toUHexString())
        }
    }
}