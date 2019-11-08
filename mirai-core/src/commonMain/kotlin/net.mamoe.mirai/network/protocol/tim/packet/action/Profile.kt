@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import com.soywiz.klock.Date
import kotlinx.io.core.*
import net.mamoe.mirai.contact.Gender
import net.mamoe.mirai.contact.Profile
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.*
import kotlin.properties.Delegates

// 用户资料的头像
/**
 * 请求获取头像
 */
@AnnotatedId(KnownPacketId.REQUEST_PROFILE_AVATAR)
object RequestProfilePicturePacket : SessionPacketFactory<NoPacket>() {
    operator fun invoke(): OutgoingPacket = buildOutgoingPacket {
        TODO()
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>) = NoPacket
}

/**
 * 请求账号详细信息.
 *
 * @see Profile
 */
@AnnotatedId(KnownPacketId.REQUEST_PROFILE_DETAILS)
object RequestProfileDetailsPacket : SessionPacketFactory<RequestProfileDetailsResponse>() {
    //00 01 3E F8 FB E3 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5
    //00 01 B1 89 BE 09 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5
    //00 01 87 73 86 9D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5
    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
    operator fun invoke(
        bot: UInt,
        qq: UInt,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey) {
        writeUShort(0x01u)
        writeUInt(qq)
        writeHex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5")
    }

    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): RequestProfileDetailsResponse =
        RequestProfileDetailsResponse().apply {
            discardExact(3)
            qq = readUInt()
            discardExact(6)
            val map = readTLVMap(tagSize = 2, expectingEOF = true)
            profile = Profile(
                qq = qq,
                nickname = (map[0x4E22u] ?: error("Cannot determine nickname")).stringOfWitch(),
                zipCode = map[0x4E25u]?.stringOfWitch(),
                phone = map[0x4E27u]?.stringOfWitch(),
                gender = when (map[0x4E29u]?.let { it[0] }?.toUInt()) {
                    null -> error("Cannot determine gender, entry 0x4E29u not found")
                    0x02u -> Gender.FEMALE
                    0x01u -> Gender.MALE
                    else -> Gender.SECRET // 猜的
                    //else -> error("Cannot determine gender, bad value of 0x4E29u: ${map[0x4729u]!![0].toUHexString()}")
                },
                birthday = map[0x4E3Fu]?.let { Date(it.toUInt().toInt()) }
            )
            map.clear()
        }
}

@AnnotatedId(KnownPacketId.REQUEST_PROFILE_DETAILS)
class RequestProfileDetailsResponse : Packet {
    var qq: UInt by Delegates.notNull()
    lateinit var profile: Profile

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

fun main() {
    val mapFemale =
        "4E 22 00 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 4E 25 00 06 34 33 33 31 30 30 4E 26 00 09 E4 B8 8D E7 9F A5 E9 81 93 4E 27 00 0A 31 33 38 2A 2A 2A 2A 2A 2A 2A 4E 29 00 01 02 4E 2A 00 00 4E 2B 00 00 4E 2D 00 23 68 74 74 70 3A 2F 2F 77 77 77 2E 34 33 39 39 2E 63 6F 6D 2F 66 6C 61 73 68 2F 33 32 39 37 39 2E 68 74 6D 4E 2E 00 02 31 00 4E 2F 00 04 36 30 33 00 4E 30 00 00 4E 31 00 01 00 4E 33 00 00 4E 35 00 00 4E 36 00 01 0A 4E 37 00 01 06 4E 38 00 01 00 4E 3F 00 04 07 DD 0B 13 4E 40 00 0C 00 41 42 57 00 00 00 00 00 00 00 00 4E 41 00 02 08 04 4E 42 00 02 00 00 4E 43 00 02 0C 04 4E 45 00 01 05 4E 49 00 04 00 00 00 00 4E 4B 00 04 00 00 00 00 4E 4F 00 01 06 4E 54 00 00 4E 5B 00 04 00 00 00 00 52 0B 00 04 13 80 02 00 52 0F 00 14 01 00 00 00 00 00 00 00 52 00 40 48 89 50 80 02 00 00 03 00 5D C2 00 0C 00 41 42 57 00 00 00 00 00 00 00 00 5D C8 00 00 65 97 00 01 07 69 9D 00 04 00 00 00 00 69 A9 00 00 9D A5 00 02 00 01 A4 91 00 02 00 00 A4 93 00 02 00 01 A4 94 00 02 00 00 A4 9C 00 02 00 00 A4 B5 00 02 00 00".hexToBytes()
            .read {
                readTLVMap(tagSize = 2, expectingEOF = true)
            }
    val mapMale =
        "4E 22 00 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 4E 25 00 06 34 33 33 31 30 30 4E 26 00 09 E4 B8 8D E7 9F A5 E9 81 93 4E 27 00 0A 31 33 38 2A 2A 2A 2A 2A 2A 2A 4E 29 00 01 01 4E 2A 00 00 4E 2B 00 00 4E 2D 00 23 68 74 74 70 3A 2F 2F 77 77 77 2E 34 33 39 39 2E 63 6F 6D 2F 66 6C 61 73 68 2F 33 32 39 37 39 2E 68 74 6D 4E 2E 00 02 31 00 4E 2F 00 04 36 30 33 00 4E 30 00 00 4E 31 00 01 00 4E 33 00 00 4E 35 00 00 4E 36 00 01 0A 4E 37 00 01 06 4E 38 00 01 00 4E 3F 00 04 07 DD 0B 13 4E 40 00 0C 00 41 42 57 00 00 00 00 00 00 00 00 4E 41 00 02 08 04 4E 42 00 02 00 00 4E 43 00 02 0C 04 4E 45 00 01 05 4E 49 00 04 00 00 00 00 4E 4B 00 04 00 00 00 00 4E 4F 00 01 06 4E 54 00 00 4E 5B 00 04 00 00 00 00 52 0B 00 04 13 80 02 00 52 0F 00 14 01 00 00 00 00 00 00 00 52 00 40 48 89 50 80 02 00 00 03 00 5D C2 00 0C 00 41 42 57 00 00 00 00 00 00 00 00 5D C8 00 00 65 97 00 01 07 69 9D 00 04 00 00 00 00 69 A9 00 00 9D A5 00 02 00 01 A4 91 00 02 00 00 A4 93 00 02 00 01 A4 94 00 02 00 00 A4 9C 00 02 00 00 A4 B5 00 02 00 00".hexToBytes()
            .read {
                readTLVMap(tagSize = 2, expectingEOF = true)
            }

    mapFemale.filter { (key, value) -> !mapMale.containsKey(key) || !mapMale[key]!!.contentEquals(value) }.forEach {
        println("id=" + it.key.toUShort().toUHexString() + ", valueFemale=" + it.value.toUHexString() + ",valueMale=" + mapMale[it.key]!!.toUHexString())
    }
}