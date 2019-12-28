@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.login

import kotlinx.io.core.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.PacketFactory
import net.mamoe.mirai.timpc.network.packet.PacketId
import net.mamoe.mirai.timpc.network.packet.SessionKey
import net.mamoe.mirai.timpc.network.packet.buildOutgoingPacket
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.localIpAddress

internal object RequestSessionPacket : PacketFactory<RequestSessionPacket.SessionKeyResponse, SessionResponseDecryptionKey>(SessionResponseDecryptionKey) {
    operator fun invoke(
        bot: Long,
        serverIp: String,
        token38: IoBuffer,
        token88: IoBuffer,
        encryptionKey: IoBuffer
    ) = buildOutgoingPacket {
        writeQQ(bot)
        writeHex("02 00 00 00 01 2E 01 00 00 68 52 00 30 00 3A")
        writeHex("00 38")
        writeFully(token38)
        encryptAndWrite(encryptionKey) {
            writeHex("00 07 00 88")
            writeFully(token88)
            writeHex("00 0C 00 16 00 02 00 00 00 00 00 00 00 00 00 00")
            writeIP(serverIp)
            writeHex("1F 40 00 00 00 00 00 15 00 30 00 01")//fix1
            writeHex("01 92 A5 D2 59 00 10 54 2D CF 9B 60 BF BB EC 0D D4 81 CE 36 87 DE 35 02 AE 6D ED DC 00 10 ")
            writeHex("06 A9 12 97 B7 F8 76 25 AF AF D3 EA B4 C8 BC E7")//fix0836
            writeHex("00 36 00 12 00 02 00 01 00 00 00 05 00 00 00 00 00 00 00 00 00 00")
            writeFully(TIMProtocol.constantData1)
            writeFully(TIMProtocol.constantData2)
            writeQQ(bot)
            writeHex("00 00 00 00 00 1F 00 22 00 01")
            writeHex("1A 68 73 66 E4 BA 79 92 CC C2 D4 EC 14 7C 8B AF 43 B0 62 FB 65 58 A9 EB 37 55 1D 26 13 A8 E5 3D")//device ID

            //tlv0106
            writeHex("01 05 00 30")
            writeHex("00 01 01 02 00 14 01 01 00 10")
            writeRandom(16)
            writeHex("00 14 01 02 00 10")
            writeRandom(16)

            writeHex("01 0B 00 85 00 02")
            writeHex("B9 ED EF D7 CD E5 47 96 7A B5 28 34 CA 93 6B 5C")//fix2
            writeRandom(1)
            writeHex("10 00 00 00 00 00 00 00 02")

            //fix3
            writeHex("00 63 3E 00 63 02 04 03 06 02 00 04 00 52 D9 00 00 00 00 A9 58 3E 6D 6D 49 AA F6 A6 D9 33 0A E7 7E 36 84 03 01 00 00 68 20 15 8B 00 00 01 02 00 00 03 00 07 DF 00 0A 00 0C 00 01 00 04 00 03 00 04 20 5C 00")
            writeRandom(32)//md5 32
            writeHex("68")

            writeHex("00 00 00 00 00 2D 00 06 00 01")
            writeIP(localIpAddress())//todo  random to avoid being banned? or that may cause errors?
        }
    }

    internal   class SessionKeyResponse(
        val sessionKey: SessionKey,
        val tlv0105: ByteReadPacket? = null
    ) : Packet {
        override fun toString(): String = "SessionKeyResponse"
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): SessionKeyResponse {
        when (remaining) {
            407L -> {
                discardExact(25)//todo test
                return SessionKeyResponse(SessionKey(readBytes(16)))
            }

            439L -> {
                discardExact(63)
                return SessionKeyResponse(SessionKey(readBytes(16)))
            }

            502L,//?
            512L,
            527L -> {
                discardExact(63)//00 00 0D 00 06 00 01 00 00 00 00 00 1F 00 22 00 01 D7 EC FC 38 1B 74 6F 91 42 00 B9 DB 69 32 43 EC 8C 02 DC E0 07 35 58 8C 6C FE 43 5D AA 6A 88 E0 00 14 00 04 00 01 00 3C 01 0C 00 73 00 01
                val sessionKey = SessionKey(readBytes(16))
                val tlv0105 = buildPacket {
                    writeHex("01 05 00 88 00 01 01 02 00 40 02 01 03 3C 01 03 00 00")
                    discardExact(remaining - 122 - 1)
                    writeFully(readIoBuffer(56))
                    writeHex("00 40 02 02 03 3C 01 03 00 00")
                    discardExact(11)
                    writeFully(readIoBuffer(56))
                } //todo 这个 tlv0105似乎可以保存起来然后下次登录时使用.
                return SessionKeyResponse(sessionKey, tlv0105)
                /*
                    Discarded(63) =00 00 0D 00 06 00 01 00 00 00 00 00 1F 00 22 00 01 F7 AB 01 4B 23 B5 47 FC 79 02 09 E0 19 EF 61 91 14 AD 8F 38 2E 8B D7 47 39 DE FE 84 A7 E5 6E 3D 00 14 00 04 00 01 00 3C 01 0C 00 73 00 01
                    sessionKey=7E 8C 1D AC 52 64 B8 D0 9A 55 3A A6 DF 53 88 C8
                    Discarded(301) =76 E4 B8 DD AB 53 02 2B 53 F1 5D A2 DA CB 00 00 00 B4 03 3D 97 B4 D1 3D 97 B4 C7 00 00 00 07 00 30 D4 E2 53 73 2E 00 F6 3F 8E 45 9F 2E 74 63 39 99 B4 AC 3B 40 C8 9A EE B0 62 A8 E1 39 FE 8E 75 EC 28 6C 03 E6 3B 5F F5 6D 50 7D 1E 29 EC 3D 47 85 08 02 04 08 08 08 08 08 04 00 05 01 0E 12 AC F6 01 0E 00 56 00 01 00 52 13 80 42 00 00 02 02 00 00 18 AB 52 CF 5B E8 CD 95 CC 3F 5C A7 BA C9 C1 5D DD F8 E2 6E 0D A3 DF F8 76 00 20 D3 87 6B 1F F2 2B C7 53 38 60 F3 AD 07 82 8B F6 62 3C E0 DB 66 BC AD D0 68 D0 30 9D 8A 41 E7 75 00 0C 00 00 00 01 00 00 00 00 00 00 00 40 00 2F 00 2A 00 01 8F FE 4F BB B2 63 C7 69 C3 F1 3C DC A1 E8 77 A3 DD 97 FA 00 36 04 40 EF 11 7A 31 02 4E 10 13 94 02 28 00 00 00 00 00 00 01 0D 00 2C 00 01 00 28 EF CB 22 58 6F AE DC F5 CC CE 45 EE 6D CA E7 EF 06 3F 60 B5 8A 22 D5 9E 37 FA 92 9F A9 11 68 F0 2A 25 4A 45 C3 D4 56 CF 01 05 00 8A 00 01 02 02 00 41 01 00 01 03 3C 01 03 00 00 FB
                    56长度=39 89 04 81 64 6B C0 71 B5 6E B0 DF 7D D4 C0 7E 97 83 BC 9F 31 39 39 C3 95 93 D9 CD 48 00 1D 0D 18 52 87 21 B2 C1 B1 AD EF 96 82 D6 D4 57 EA 48 5A 27 8C 14 6F E2 83 00
                    Discarded(11) =41 01 00 02 03 3C 01 03 00 00 86
                 */
            }

            else -> throw IllegalArgumentException(remaining.toString())
        }
    }
}