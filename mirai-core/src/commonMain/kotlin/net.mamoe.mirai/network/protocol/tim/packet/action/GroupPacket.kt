@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.writeFully
import kotlinx.io.core.writeUByte
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*

/*
// TODO: 2019/11/27 群公告

群公告请求:
72 E2 E0 74 F7 00 00 00 00

群公告回复(同群消息的id):
72 00 E2 E0 74 F7 12 DB 48 77 00 00 00 03 01 01 00 04 01 00 80 01 40 6C 18 F5 DA 02 00 00 27 1B 00 00 00 00 27 1B 01 F4 01 00 00 00 01 00 00 0F 1F 0C 42 69 67 20 63 6F 6E 76 65 6E 65 27 00 00 96 E6 AF 95 E4 B8 9A E4 BA 86 EF BC 8C E5 B8 8C E6 9C 9B E5 A4 A7 E5 AE B6 E8 83 BD E5 A4 9F E5 83 8F E4 BB A5 E5 89 8D E9 82 A3 E6 A0 B7 E5 BC 80 E5 BF 83 EF BC 8C E5 AD A6 E4 B9 A0 E8 BF 9B E6 AD A5 EF BC 8C E5 A4 A9 E5 A4 A9 E5 BF AB E4 B9 90 E3 80 82 E6 AD A4 E7 BE A4 E7 A6 81 E6 AD A2 E9 AA 82 E4 BA BA EF BC 8C E5 88 B7 E5 B1 8F E6 9A B4 E5 8A 9B EF BC 8C E8 BF 9D E8 A7 84 E8 80 85 E7 A6 81 E8 A8 80 EF BC 8C E4 B8 A5 E9 87 8D E8 80 85 E5 B0 B1 76 E8 BF 9B E7 BE A4 E6 97 B6 EF BC 8C E8 AF B7 E4 BF AE E6 94 B9 E6 AD A3 E7 A1 AE E5 A7 93 E5 90 8D E3 80 82 E4 B8 8D E8 83 BD 54 E5 90 8C E5 AD A6 EF BC 8C E5 A4 AA E8 BF 87 E5 88 86 E7 9A 84 54 21 28 E4 BA 92 E8 B5 9E E7 BE A4 EF BC 8C E6 89 8B E6 9C BA E5 9C A8 E7 BA BF E8 81 8A E5 A4 A9 E8 80 85 E5 8F AF E4 BB A5 E4 BA 92 E8 B5 9E E5 AF B9 E6 96 B9 00 38 D9 FD F5 21 A6 1F 8D 61 37 A1 7A 92 91 2A 2C 71 46 A9 B9 1C 45 EB 38 74 4A 74 EA 77 7D 14 DB 12 D0 B0 09 C2 AA 22 16 F1 D0 B9 97 21 F0 5A A0 06 59 A7 3B 2F 32 D2 B8 E3 00 0F 00 00 00 00 06 00 03 00 02 01 01 00 04 00 04 00 00 00 15 00 05 00 04 52 7C C5 7C 00 06 00 04 00 00 00 20 00 07 00 04 00 00 00 00 00 09 00 01 00 C5 15 BE BE 00 1C ED 9F 9B 00 00 26 D0 E1 3A 00 00 2D 5C 53 A6 00 01 2D BD 28 D2 00 00 2E 94 76 3E 00 00 35 F3 BC F2 00 00 37 D6 91 AB 00 00 3A 60 1C 3E 00 80 3A 86 EA A3 00 48 3D 7F E7 70 00 00 3E 03 3F A2 00 09 41 47 0C DD 00 40 41 B6 32 A8 00 80 44 C8 DA 23 00 00 45 3E 1B 6A 00 80 45 C6 59 E9 00 C0 4A BD C6 F9 00 00 4C 67 45 E8 00 00 4E AD C2 C2 00 80 4F A0 F7 EC 00 80 50 CB 11 E8 00 00 58 22 21 90 00 00 59 17 3E 05 00 01 5E 74 48 D9 00 00 5E A2 B5 88 00 00 66 A1 32 9B 00 40 68 07 29 0A 00 00 68 0F EF 4F 00 00 69 8B 14 F3 00 80 6A A5 27 4E 00 00 6C 11 A0 89 00 81 6C 18 F5 DA 00 08 6C 21 F8 E2 00 01 71 F8 F5 18 00 00 72 0B CC B6 00 00 75 53 38 DF 00 00 7A A1 8B 82 00 00 7C 8C 1D 1B 00 00 7C BC D3 C1 00 00 84 2D B8 5F 00 00 88 4C 33 76 00 00 8C C8 0D 43 00 00 90 B8 65 22 00 00 91 54 89 E9 00 00 9C E6 93 A5 00 01 9D 59 6A 36 00 00 9D 63 81 5C 00 00 9E 31 AF AC 00 00 9E 69 86 25 00 80 A1 FD CA 2D 00 00 A5 22 5C 48 00 00 A5 F2 9A B7 00 00 AF 25 74 9E 00 01 B1 50 24 00 00 00 B2 BD 81 A9 00 00 B5 0E B3 DD 00 00 B9 BF 0D BC 00 00 C5 15 BE BE 00 00
 */

@AnnotatedId(KnownPacketId.SEND_GROUP_MESSAGE)
@PacketVersion(date = "2019.10.19", timVersion = "2.3.2 (21173)")
object GroupPacket : SessionPacketFactory<GroupPacket.Response>() {
    @Suppress("FunctionName")
    fun Message(
        botQQ: UInt,
        groupInternalId: GroupInternalId,
        sessionKey: SessionKey,
        message: MessageChain
    ): OutgoingPacket = buildSessionPacket(botQQ, sessionKey, name = "GroupMessage") {
        writeByte(0x2A)
        writeGroup(groupInternalId)

        writeShortLVPacket {
            writeHex("00 01 01")
            writeHex("00 00 00 00 00 00 00 4D 53 47 00 00 00 00 00")

            writeTime()
            writeRandom(4)
            writeHex("00 00 00 00 09 00 86")
            writeFully(TIMProtocol.messageConst1)
            writeZero(2)

            writePacket(message.toPacket())
        }
    }

    /**
     * 查询公告
     */
    @Suppress("FunctionName")
    fun QueryBulletin(
        botQQ: UInt,
        groupInternalId: GroupInternalId,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(
        botQQ, sessionKey, name = "QueryBulletin"
    ) {
        writeUByte(0x72u)
        writeGroup(groupInternalId)
        writeZero(4)
    }

    @NoLog
    object Response : Packet {
        override fun toString(): String = "SendGroupMessagePacket.Response"
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): Response = Response
}