@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*

object TouchKey : DecrypterByteArray, DecrypterType<TouchKey> {
    override val value: ByteArray = TIMProtocol.touchKey.hexToBytes(withCache = false)
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
        writeHex(TIMProtocol.fixVer)
        writeHex(TIMProtocol.touchKey)

        encryptAndWrite(TIMProtocol.touchKey) {
            writeHex(TIMProtocol.constantData1)
            writeHex(TIMProtocol.constantData2)
            writeQQ(bot)
            writeHex(if (isRedirect) "00 01 00 00 03 09 00 0C 00 01" else "00 00 00 00 03 09 00 08 00 01")
            writeIP(serverIp)
            writeHex(
                if (isRedirect) "01 6F A1 58 22 01 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 03 00 19"
                else "00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19"
            )
            writeHex(TIMProtocol.publicKey)
        }
    }

    class TouchResponse : Packet {
        var serverIP: String? = null
            internal set
        var loginTime: Int = 0
            internal set

        lateinit var loginIP: String
            internal set
        lateinit var token0825: ByteArray
            internal set
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): TouchResponse = TouchResponse().apply {
        when (val flag = readByte().toUByte().toInt()) {
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

            else -> throw IllegalStateException(flag.toByte().toUHexString())
        }
    }
}