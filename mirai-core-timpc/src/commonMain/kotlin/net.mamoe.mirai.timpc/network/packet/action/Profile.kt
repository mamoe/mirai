@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.action

import io.ktor.util.date.GMTDate
import kotlinx.io.core.*
import net.mamoe.mirai.data.Gender
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.PacketVersion

import net.mamoe.mirai.utils.io.*

inline class AvatarLink(val value: String) : Packet

inline class NicknameMap(val delegate: Map<UInt, String>) : Packet

/**
 * 批量查询昵称.
 */
internal object QueryNicknamePacket : SessionPacketFactory<NicknameMap>() {
    /**
     * 单个查询.
     */
    @PacketVersion(date = "2019.12.7", timVersion = "2.3.2 (21173)")
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey,
        qq: Long
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey) {
        writeHex("03 00 00 00 00 00 00 00 00 00 00")
        writeByte(1)
        writeQQ(qq)
    }

    /**
     * 批量查询.
     * 注意!! 服务器不一定全都返回... 需要重复查没返回的
     */
    @PacketVersion(date = "2019.12.7", timVersion = "2.3.2 (21173)")
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey,
        qq: Array<UInt>
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey) {
        writeHex("03 00 00 00 00 00 00 00 00 00 00")
        writeUByte(qq.size.toUByte())
        qq.forEach {
            writeUInt(it)
        }
    }

/*

批量查询昵称

发出包ID = UnknownPacketId(01 26)
sequence = 44 57
fixVer2=02 00 00 00 01 2E 01 00 00 69 35
解密body=03 00 00 00 00 00 00 00 00 00 00 [24]
(02 45 16 DF) (6C 78 B1 E0) (11 73 69 76) (36 79 19 E1) (49 28 A4 F4) (81 66 8B BC) (2D 6B 19 EC) (28 3D 91 25) (00 54 E5 06) (37 E9 94 CF) (55 7A D6 86)
(01 60 31 EC) (2F B1 5E EF) (05 B0 F4 6F) (0F 0D 35 E1) (00 66 7C C5) A6 81 A4 9D 31 05 12 1C A6 A0 EE EF 10 18 86 83 37 99 77 D7 50 BA 4A 8F 10 CE 72 4C 32 71 EE 30 79 63 C6 98 (3E C2 FA 6E) 02 27 13 93 01 2E E5 D7 37 E9 68 46 00 64 B2 1D 03 37 67 20 0A 9C 58 FB 05 94 75 87
(0B 9F C6 B6)
(18 BE 4B 0E)

接收包id=UnknownPacketId(01 26),
sequence=44 57
  解析body=UnknownPacket(01 26)
body=    03 00 00 00 00 00 00 00 00 00 00 12 04 14 37
(02 45 16 DF) 00 36 FF 00
    [0F] E6 94 BE E7 9D 9B E3 81 AE E5 A4 A9 E7 A9 BA
    11 88 02 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 80 08 80 00 41
(6C 78 B1 E0) 00 2D 1C 01
    [19] 61 2E E8 B4 A2 E7 A5 9E 7C 20 E6 8E A5 E5 90 84 E7 A7 8D E4 B8 9A E5 8A A1
    11 C0 02 40 07 C7 08 1C 00 4D 59 53 00 00 4B 4C 00 4B 55 4C 00 00 00 00 00 00 04 00 00 E2 10 34
(11 73 69 76) 01 DD 19 00
    [0C] E5 BC 80 E5 BF 83 E5 B0 B1 E5 A5 BD
    11 08 82 46 07 CA 00 00 00 00 00 31 00 00 33 32 00 00 00 35 08 04 08 04 08 04 04 01 17 E3 10 32
(36 79 19 E1) 00 00 1F 00
    [0A] 45 70 69 6D 65 74 68 65 75 73
    00 08 02 00 07 C4 02 08 00 41 42 57 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 10 49
(49 28 A4 F4) 02 B5 17 01
    [21] E9 A1 B9 E7 9B AE 6B 61 6B 61 6F 74 61 6C 6B EF BC 88 E6 9C 89 E4 BA 8B E6 8A 96 E6 88 91 EF BC 89
    11 80 02 00 07 CC 06 0B 00 00 00 31 00 00 34 34 00 00 00 31 00 00 00 00 00 00 04 00 00 80 10 2E
(81 66 8B BC) 02 64 77 00
    [06] E4 B8 87 E7 A0 81
    00 80 02 00 07 6C 0A 03 00 00 00 31 00 00 33 31 00 00 00 35 00 00 00 00 00 00 04 00 00 00 00 37
(2D 6B 19 EC) 02 58 16 01
    [0F] E5 93 A5 E5 8F AA E6 98 AF E4 BC A0 E9 80 81
    11 00 02 00 07 CC 0C 0F 00 00 00 31 00 00 35 33 00 00 00 33 00 00 00 00 00 00 04 00 00 00 00 31
28 3D 91 25 02 D9 FF FF 09 E5 B0 8F E8 A1 A8 E5 BC 9F 11 C8 02 46 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 08 04 08 04 08 04 04 20 06 E2 00 3B 00 54 E5 06 02 5B 78 00 13 61 E5 85 A8 E6 96 B0 E5 9F 9F E5 90 8D E6 89 B9 E5 8F 91 00 08 82 46 07 6B 08 16 00 00 00 31 00 00 34 33 00 00 00 31 00 00 00 00 00 00 04 00 08 02 00 4C 37 E9 94 CF 00 00 00 FF 24 E6 B3 89 E5 B7 9E E5 B8 82 E6 86 A8 E9 BC A0 E7 BD 91 E7 BB 9C E7 A7 91 E6 8A 80 E6 9C 89 E9 99 90 E5 85 AC 01 00 00 00 00 00 00 00 00 00 00 31 00 00 33 35 00 00 00 35 00 00 00 00 00 00 04 00 00 40 02 30 55 7A D6 86 02 49 1B 01 08 E5 AE A2 E6 9C 8D 56 36 11 00 02 00 07 C8 01 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 12 00 4A 01 60 31 EC 02 58 FF 00 22 E5 BC 80 E5 BD A9 E7 BD 91 2D E5 B0 8F E8 B1 86 28 31 32 E5 88 B0 32 32 E7 82 B9 E5 9C A8 E7 BA BF 29 00 40 00 00 00 00 05 1E 00 00 00 31 00 00 35 31 00 00 00 31 08 04 04 09 0C 04 04 00 00 02 00 38 2F B1 5E EF 00 00 00 FF 10 E4 B8 8A E6 96 B9 65 E6 8E A8 E8 BD AF E4 BB B6 01 80 00 00 00 00 00 00 00 00 00 31 00 00 34 34 00 00 00 33 00 00 00 00 00 00 04 00 00 00 00 39 05 B0 F4 6F 00 78 23 01 11 E2 99 A1 20 E2 9C BF E2 80 BF E2 9C BF 20 52 4F 4E 11 88 82 42 07 C0 09 0C 00 00 00 31 00 00 33 35 00 00 00 35 00 00 00 00 00 00 04 00 00 80 00 2E 0F 0D 35 E1 00 00 23 00 06 E5 9C B0 E8 A1 A3 00 40 02 46 07 C0 02 08 00 00 00 31 00 00 34 35 00 00 00 33 00 00 00 00 00 00 04 10 00 02 00 31 00 66 7C C5 00 00 0A 00 09 E8 B5 9A E5 B0 8F E5 AE A2 00 C8 02 42 07 D8 0C 0C 00 00 00 31 00 00 34 31 00 00 00 31 00 00 00 00 00 00 04 24 02 00 00 3A
(A6 81 A4 9D) 02 25 29 00 12 E6 96 B0 E6 98 93 E9 80 9A E5 AE A2 E6 9C 8D E4 B8 89 11 80 02 01 07 BA 0A 10 00 00 00 31 00 00 33 35 00 00 00 35 00 00 00 00 00 00 04 00 00 00 00 3A 31 05 12 1C 01 86 1C 00 12 E5 BF A7 E4 BC A4 E8 BF 98 E6 98 AF E5 BF AB E4 B9 90 00 88 02 02 07 C7 04 13 00 00 00 31 00 00 33 35 00 00 00 35 00 00 00 00 00 00 04 00 08 20 00 00 04 5D EC AF 48

---------------------------

发出包ID = UnknownPacketId(01 26)
sequence = 44 58
  fixVer2=02 00 00 00 01 2E 01 00 00 69 35
  解密body=03 00 00 00 00 00 00 00 00 00 00 12 A6 A0 EE EF 10 18 86 83 37 99 77 D7 50 BA 4A 8F 10 CE 72 4C 32 71 EE 30 79 63 C6 98
  (3E C2 FA 6E) 02 27 13 93 01 2E E5 D7 37 E9 68 46 (00 64 B2 1D) (03 37 67 20) 0A 9C 58 FB 05 94 75 87 (11 48 2B 1A) 0B 9F C6 B6 18 BE 4B 0E

接收包id=UnknownPacketId(01 26),
sequence=44 58
  解析body=UnknownPacket(01 26)
body=03 00 00 00 00 00 00 00 00 00 00 00 03 8E 3C A6 A0 EE EF 02 07 6C 01 14 E8 8B B9 E6 9E 9C 49 44 E4 B8 93 E4 B8 9A E8 A7 A3 E9 94 81 11 00 02 01 07 77 01 01 00 00 00 31 00 00 31 31 00 00 00 31 00 00 00 00 00 00 04 00 00 02 00 31 10 18 86 83 00 C3 22 00 09 35 35 38 E7 94 B5 E8 AE AF 00 08 02 40 07 C1 0C 01 00 00 00 31 00 00 33 35 00 00 00 35 08 04 08 04 08 04 04 00 04 00 10 35 37 99 77 D7 02 5B 2A 00 0D E8 BF 9C E6 8B 93 E7 94 B5 E8 AE AF 33 00 C0 00 02 07 B9 09 0E 00 00 00 31 00 00 33 35 00 00 00 35 00 00 00 00 00 00 04 00 00 40 00 2E 50 BA 4A 8F 02 1C 09 00 06 E6 96 B9 E5 80 8D 11 40 02 00 07 DA 01 01 00 00 00 31 00 00 34 34 00 00 00 33 00 00 00 00 00 00 04 00 00 82 00 2C 10 CE 72 4C 00 00 23 00 04 4C 6F 73 74 11 08 02 00 07 C0 02 12 00 00 00 31 00 00 31 31 00 00 00 38 08 04 00 00 00 00 04 00 08 72 00 30 32 71 EE 30 00 93 21 00 08 69 57 65 62 53 68 6F 70 00 00 02 00 07 C2 01 01 00 00 00 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 12 00 2B 79 63 C6 98 02 A6 12 00 03 5E 4F 5E 11 80 02 00 07 D1 08 12 00 00 00 31 00 00 33 37 00 00 00 32 00 00 00 00 00 00 04 00 00 60 00 3D 3E C2 FA 6E 02 2B 1D 01 15 E9 A3 8E E9 93 83 E8 8D 89 E6 95 99 E8 82 B2 E6 9C 8D E5 8A A1 00 80 00 00 07 C6 06 10 00 00 00 31 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 00 00 00 2E 02 27 13 93 02 0D FF FF 06 E7 83 82 E8 8F 9C 00 08 02 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 00 04 A2 00 33 01 2E E5 D7 00 D8 22 00 0B 4F 28 E2 88 A9 5F E2 88 A9 29 4F 11 48 82 46 07 C1 08 1A 00 00 00 31 00 00 33 35 00 00 00 35 08 04 08 04 08 04 04 00 0C 80 08 30 37 E9 68 46 00 00 00 FF 08 33 35 E4 BA 92 E8 81 94 01 00 00 00 00 00 00 00 00 00 00 31 00 00 33 35 00 00 00 32 00 00 00 00 00 00 04 00 00 40 02 37 00 64 B2 1D 00 00 FF FF 0F E5 98 89 E4 BC A6 E8 99 8E E8 99 8E E8 99 8E 01 48 06 42 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 04 01 40 8C 00 34 03 37 67 20 00 93 24 00 0C E8 93 9D E6 98 9F E7 A7 91 E6 8A 80 00 08 02 46 07 BF 0A 1A 00 00 00 31 00 00 33 35 00 00 00 35 08 04 08 04 08 04 04 00 00 00 10 2C 0A 9C 58 FB 01 53 00 00 04 4B E3 80 81 00 48 02 02 07 E3 00 00 00 00 00 31 00 00 00 00 00 00 00 00 0C 04 08 04 04 09 04 FD 03 02 00 2E 05 94 75 87 02 25 1F 00 06 54 4E 54 50 52 4F 00 40 02 00 07 C4 01 01 00 00 00 31 00 00 34 33 00 00 00 31 00 00 00 00 00 00 04 00 00 02 00 2E 11 48 2B 1A 02 0A 0B 00 06 E6 9D 8E E9 98 B3 11 08 02 02 07 D8 03 1C 00 00 00 31 00 00 34 34 00 00 00 33 00 00 00 00 00 00 04 00 00 6C 00 30 0B 9F C6 B6 00 AE 14 00 08 F0 9F 91 BC F0 9F 91 BF 11 00 02 42 07 CF 01 18 00 00 00 00 00 00 00 00 00 00 00 00 08 04 08 04 04 07 04 00 04 A0 00 34
(18 BE 4B 0E) 00 00 36 00
    [0C] E5 B3 B0 E5 9B 9E E8 B7 AF E8 BD AC
    00 08 02 00 07 AD 02 03 00 00 00 31 00 00 31 35 00 00 32 32 00 00 00 00 00 00 04 00 00 00 00 00

04 5D EC AF 48
*/

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): NicknameMap {
        //03 00 00 00 00 00 00 00 00 00 00 12 04 14 37
        val type = readUByte().toInt()
        if (type == 15) {
            discardExact(14)

            val map = linkedMapOf<UInt, String>()
            while (remaining != 5L) { // 最后总是会剩余 04 5D EC AF 48
                val qq = readUInt()
                discardExact(4) // 4 个状态信息, 未知
                val nickname = readString(readUByte().toInt())
                discardExact(32) // 未知
                map[qq] = nickname
            }
            return NicknameMap(map)
        } else {
            error("Unsupported type $type")
        }
    }
}

// 用户资料的头像
/**
 * 请求获取头像
 */ // ? 这个包的数据跟下面那个包一样
internal object RequestProfileAvatarPacket : SessionPacketFactory<AvatarLink>() {
    //00 01 00 17 D4 54 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5
    operator fun invoke(
        bot: Long,
        qq: Long,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey) {
        writeUShort(0x01u)
        writeQQ(qq)
        writeHex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5")
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): AvatarLink {
        println("  RequestProfileAvatarPacket body=${this.readBytes().toUHexString()}")
        TODO()
    }
}

/**
 * 请求账号详细信息.
 *
 * @see Profile
 */
internal object RequestProfileDetailsPacket : SessionPacketFactory<RequestProfileDetailsResponse>() {
    //00 01 3E F8 FB E3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5
    //00 01 B1 89 BE 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5
    //00 01 87 73 86 9D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5
    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
    operator fun invoke(
        bot: Long,
        qq: Long,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey) {
        writeUShort(0x01u)
        writeQQ(qq)
        writeHex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5")
    }

    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): RequestProfileDetailsResponse {
        discardExact(3)
        val qq = readUInt().toLong()
        discardExact(6)
        val map = readTLVMap(tagSize = 2, expectingEOF = true)
        //map.printTLVMap("Profile(qq=$qq) raw=")
        //map.mapValues { it.value.encodeToString() }.printTLVMap("Profile(qq=$qq) str=")
        val profile = Profile(
            qq = qq,
            nickname = map[0x4E22u]?.encodeToString() ?: "",//error("Cannot determine nickname")
            englishName = map[0x4E54u]?.encodeToString(),
            chineseName = map[0x4E2Au]?.encodeToString(),
            qAge = map[0x6597u]?.get(0)?.toInt(),
            zipCode = map[0x4E25u]?.encodeToString(),
            phone = map[0x4E27u]?.encodeToString(),
            gender = when (map[0x4E29u]?.let { it[0] }?.toUInt()) {
                null -> Gender.SECRET //error("Cannot determine gender, entry 0x4E29u not found")
                0x02u -> Gender.FEMALE
                0x01u -> Gender.MALE
                else -> Gender.SECRET // 猜的
                //else -> error("Cannot determine gender, bad value of 0x4E29u: ${map[0x4729u]!![0].toHexString()}")
            },
            birthday = map[0x4E3Fu]?.let { GMTDate(it.toUInt().toLong()) },
            personalStatement = map[0x4E33u]?.encodeToString(),
            homepage = map[0x4E2Du]?.encodeToString(),
            company = map[0x5DC8u]?.encodeToString(),
            school = map[0x4E35u]?.encodeToString(),
            email = map[0x4E2Bu]?.encodeToString()
        )
        map.clear()

        return RequestProfileDetailsResponse(qq, profile)
    }
}

internal data class RequestProfileDetailsResponse(
    val qq: Long,
    val profile: Profile
) : Packet {
    //00 01 00 99 6B F8 D2 00 00 00 00 00 29
    // 4E 22 00 0F E4 B8 8B E9 9B A8 E6 97 B6 E6 B5 81 E6 B3 AA 4E 25 00 00 4E 26 00 0C E4 B8 AD E5 9B BD E6 B2 B3 E5 8C 97 4E 27 00 0B 30 33 31 39 39 39 39 39 39 39 39
    // 4E 29 [00 01] 01 4E 2A 00 00 4E 2B 00 17 6D 61 69 6C 2E 71 71 32 35 37 33 39 39 30 30 39 38 2E 40 2E 63 6F 6D 4E 2D 00 00 4E 2E 00 02 31 00 4E 2F 00 04 36 37 38 00 4E 30 00 00 4E 31 00 01 00 4E 33 00 00 4E 35 00 00 4E 36 00 01 00 4E 37 00 01 00 4E 38 00 01 00 4E 3F 00 04 07 C1 01 01 4E 40 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 4E 41 00 02 00 00 4E 42 00 02 00 00 4E 43 00 02 00 00 4E 45 00 01 22 4E 49 00 04 00 00 00 00 4E 4B 00 04 00 00 00 00 4E 4F 00 01 00 4E 54 00 00 4E 5B 00 00 52 0B 00 04 00 C0 00 01 52 0F 00 14 00 00 00 00 00 00 00 00 12 00 00 48 09 10 00 00 00 00 00 00 5D C2 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 5D C8 00 00 65 97 00 01 08 69 9D 00 04 00 00 00 00 69 A9 00 00 9D A5 00 02 00 01 A4 91 00 02 00 00 A4 93 00 02 00 00 A4 94 00 02 00 00 A4 9C 00 02 00 00 A4 B5 00 02 00 00

    //00 01 00 87 73 86 9D 00 00 00 00 00 29 4E 22 00 15 E6 98 AF E6 9C 9D E8 8F 8C E4 B8 8D E7 9F A5 E6 99 A6 E6 9C 94 4E 25 00 00 4E 26 00 00 4E 27 00 00
    // 4E 29 [00 01] 01 4E 2A 00 00 4E 2B 00 00 4E 2D 00 00 4E 2E 00 02 31 00 4E 2F 00 04 37 32 30 00 4E 30 00 00 4E 31 00 01 01 4E 33 00 00 4E 35 00 00 4E 36 00 01 00 4E 37 00 01 04 4E 38 00 01 00 4E 3F 00 04 07 CF 00 00 4E 40 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 4E 41 00 02 00 00 4E 42 00 02 00 00 4E 43 00 02 00 00 4E 45 00 01 13 4E 49 00 04 00 00 00 00 4E 4B 00 04 00 00 00 00 4E 4F 00 01 00 4E 54 00 00 4E 5B 00 04 00 00 00 00 52 0B 00 04 13 80 02 00 52 0F 00 14 00 04 02 00 00 00 00 00 12 04 10 58 89 50 C0 00 22 00 00 00 5D C2 00 0C 00 00 00 00 00 00 00 00 00 00 00 00 5D C8 00 00 65 97 00 01 08 69 9D 00 04 00 00 00 00 69 A9 00 00 9D A5 00 02 00 01 A4 91 00 02 00 00 A4 93 00 02 00 01 A4 94 00 02 00 00 A4 9C 00 02 00 00 A4 B5 00 02 00 00

    //00 01 00 76 E4 B8 DD
    // 00 00 00 00 00 29

    // 4E 22 [00 0E] 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E //昵称
    // 4E 25 [00 06] 34 33 33 31 30 30 //邮编
    // 4E 26 [00 09] E4 B8 8D E7 9F A5 E9 81 93 //?
    // 4E 27 [00 0A] 31 33 38 2A 2A 2A 2A 2A 2A 2A // 手机号
    // 4E 29 [00 01] 02  性别, 女02, 男01
    // 4E 2A [00 00]
    // 4E 2B [00 00]
    // 4E 2D [00 23] 68 74 74 70 3A 2F 2F 77 77 77 2E 34 33 39 39 2E 63 6F 6D 2F 66 6C 61 73 68 2F 33 32 39 37 39 2E 68 74 6D //http://www.4399.com/flash/32979.htm //???
    // 4E 2E [00 02] 31 00
    // 4E 2F [00 04] 36 30 33 00
    // 4E 30 [00 00]
    // 4E 31 [00 01] 00
    // 4E 33 [00 00]
    // 4E 35 [00 00]
    // 4E 36 [00 01] 0A
    // 4E 37 [00 01] 06
    // 4E 38 [00 01] 00
    // 4E 3F [00 04] 07 DD 0B 13  生日 short byte byte
    // 4E 40 [00 0C] 00 41 42 57 0// 0 00 00 00 00 00 00 00
    // 4E 41 [00 02] 08 04
    // 4E 42 [00 02] 00 00
    // 4E 43 [00 02] 0C 04
    // 4E 45 [00 01] 05
    // 4E 49 [00 04] 00 00 00 00
    // 4E 4B [00 04] 00 00 00 00
    // 4E 4F [00 01] 06
    // 4E 54 [00 00]
    // 4E 5B [00 04] 00 00 00 00
    // 52 0B [00 04] 13 80 02 00
    // 52 0F [00 14] 01 00 00 00 00 00 00 00 52 00 40 48 89 50 80 02 00 00 03 00
    // 5D C2 [00 0C] 00 41 42 57 00 00 00 00 00 00 00 00
    // 5D C8 [00 00]
    // 65 97 [00 01] 07
    // 69 9D [00 04] 00 00 00 00
    // 69 A9 [00 00]
    // 9D A5 [00 02] 00 01
    // A4 91 [00 02] 00 00
    // A4 93 [00 02] 00 01
    // A4 94 [00 02] 00 00
    // A4 9C [00 02] 00 00
    // A4 B5 [00 02] 00 00

    /*
    00 01 00 76 E4 B8 DD 00 00 00 00 00 29
    4E 22 00 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 4E 25 00 06 34 33 33 31 30 30 4E 26 00 09 E4 B8 8D E7 9F A5 E9 81 93 4E 27 00 0A 31 33 38 2A 2A 2A 2A 2A 2A 2A 4E 29 00 01 01 4E 2A 00 00 4E 2B 00 00 4E 2D 00 23 68 74 74 70 3A 2F 2F 77 77 77 2E 34 33 39 39 2E 63 6F 6D 2F 66 6C 61 73 68 2F 33 32 39 37 39 2E 68 74 6D 4E 2E 00 02 31 00 4E 2F 00 04 36 30 33 00 4E 30 00 00 4E 31 00 01 00 4E 33 00 00 4E 35 00 00 4E 36 00 01 0A 4E 37 00 01 06 4E 38 00 01 00 4E 3F 00 04 07 DD 0B 13 4E 40 00 0C 00 41 42 57 00 00 00 00 00 00 00 00 4E 41 00 02 08 04 4E 42 00 02 00 00 4E 43 00 02 0C 04 4E 45 00 01 05 4E 49 00 04 00 00 00 00 4E 4B 00 04 00 00 00 00 4E 4F 00 01 06 4E 54 00 00 4E 5B 00 04 00 00 00 00 52 0B 00 04 13 80 02 00 52 0F 00 14 01 00 00 00 00 00 00 00 52 00 40 48 89 50 80 02 00 00 03 00 5D C2 00 0C 00 41 42 57 00 00 00 00 00 00 00 00 5D C8 00 00 65 97 00 01 07 69 9D 00 04 00 00 00 00 69 A9 00 00 9D A5 00 02 00 01 A4 91 00 02 00 00 A4 93 00 02 00 01 A4 94 00 02 00 00 A4 9C 00 02 00 00 A4 B5 00 02 00 00
     */
}