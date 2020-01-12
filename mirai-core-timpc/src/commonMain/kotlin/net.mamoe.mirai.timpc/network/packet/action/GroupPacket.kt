@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.action

import kotlinx.io.core.*
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.groupInternalId
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.timpc.internal.RawGroupInfo
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.timpc.utils.unsupportedFlag
import net.mamoe.mirai.timpc.utils.unsupportedType
import net.mamoe.mirai.utils.NoLog
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.io.*
import kotlin.collections.set


internal object GroupNotFound : GroupPacket.InfoResponse {
    override fun toString(): String = "GroupPacket.InfoResponse.GroupNotFound"
}

@Suppress("FunctionName")
internal object GroupPacket : SessionPacketFactory<GroupPacket.GroupPacketResponse>() {
    @PacketVersion(date = "2019.10.19", timVersion = "2.3.2 (21173)")
    fun Message(
        bot: Long,
        groupInternalId: GroupInternalId,
        sessionKey: SessionKey,
        message: MessageChain
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey, name = "GroupPacket.GroupMessage") {
        writeUByte(0x2Au)
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
     * 退出群
     */
    @PacketVersion(date = "2019.11.28", timVersion = "2.3.2 (21173)")
    fun QuitGroup(
        bot: Long,
        sessionKey: SessionKey,
        group: GroupInternalId
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey, name = "GroupPacket.QuitGroup") {
        writeUByte(0x09u)
        writeGroup(group)
    }

    /**
     * 查询群信息
     */
    @PacketVersion(date = "2019.11.27", timVersion = "2.3.2 (21173)")
    fun QueryGroupInfo(
        bot: Long,
        groupInternalId: GroupInternalId,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey, name = "GroupPacket.QueryGroupInfo", headerSizeHint = 9) {
        writeUByte(0x72u)
        writeGroup(groupInternalId)
        writeZero(4)
    }

    /**
     * 禁言群成员
     */
    @PacketVersion(date = "2019.12.2", timVersion = "2.3.2 (21173)")
    fun Mute(
        bot: Long,
        groupInternalId: GroupInternalId,
        sessionKey: SessionKey,
        target: Long,
        /**
         * 0 为取消
         */
        timeSeconds: UInt
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey, name = "GroupPacket.Mute") {
        writeUByte(0x7Eu)
        writeGroup(groupInternalId)
        writeByte(0x20)
        writeByte(0x00)
        writeByte(0x01)
        writeQQ(target)
        writeUInt(timeSeconds)
    }

    internal interface GroupPacketResponse : Packet

    //@NoLog
    internal object MessageResponse : Packet, GroupPacketResponse {
        override fun toString(): String = "GroupPacket.MessageResponse"
    }

    @NoLog
    internal object MuteResponse : Packet, GroupPacketResponse {
        override fun toString(): String = "GroupPacket.MuteResponse"
    }

    internal interface InfoResponse : Packet, GroupPacketResponse

    /**
     * 退出群的返回
     */
    class QuitGroupResponse(private val _group: GroupInternalId?) : Packet, GroupPacketResponse {
        val group: GroupInternalId get() = _group ?: error("request failed")
        val isSuccess: Boolean get() = _group != null

        override fun toString(): String = "GroupPacket.QuitResponse"
    }

    @PacketVersion(date = "2019.11.27", timVersion = "2.3.2 (21173)")
    @UseExperimental(ExperimentalStdlibApi::class)
    override suspend fun ByteReadPacket.decode(
        id: PacketId,
        sequenceId: UShort,
        handler: BotNetworkHandler
    ): GroupPacketResponse {
        return when (val packetType = readUByte().toUInt()) {
            0x2Au -> MessageResponse
            0x7Eu -> MuteResponse // 成功: 7E 00 22 96 29 7B;

            0x09u -> {
                if (readByte().toInt() == 0) {
                    QuitGroupResponse(readUInt().toLong().groupInternalId())
                } else {
                    QuitGroupResponse(null)
                }
            }

            0x72u -> {
                when (val flag = readByte().toInt()) {
                    0x02 -> GroupNotFound
                    0x00 -> {
                        /*
                        27 0B 60 E7
                        27 0B 60 E7
                        00 00 00 03 01 01 00 04 01
                        40 23 00 40
                        3E 03 3F A2 群主

                        01 00
                        00 00 00 00
                        00 00 00 27
                        19 01 F4 01
                        00 00 00 01 00 00
                        00 2B

                        05 4D 69 72 61 69 群名
                        00
                        00
                        00
                        00
                        00
                        38 96 A2 13 CE 50 65 AD E4 2C FB 26 6A 4C D5 0D F0 B4 79 0B A1 8A B8 48 17 B1 7D BD A6 27 AF BD E8 EF E2 C5 40 AA A7 9C C0 1E 65 9F 54 6D 0F ED 9B 30 B1 03 97 F0 46 2A 46
                        00
                        0F 00 00 00 00 06 00 03 00 02 00 00 00 04 00 04
                        00 00 00 01 00 05 00 04 5D F5 37 65 00 06 00 04 04 08 00 00 00 07 00 04 00 00 00 00 00 09 00 01 00
                        76 E4 B8 DD 00
                        38 B5 21 5D 00 00
                        3B E7 BB BC 00 00
                        3E 03 3F A2 00 00
                        76 E4 B8 DD 00 00
                         */
                        discardExact(4) // group internal id
                        val group = readUInt().toLong() // group id

                        discardExact(13) //00 00 00 03 01 01 00 04 01  00 80 01 40
                        val owner = readUInt().toLong()
                        discardExact(22)
                        val groupName = readUByteLVString()

                        /*
                        来自群2的完整数据, 需要回答问题然后经管理员审核
                        00
                        00
                        95 E7 AC AC 4D 2B 31 E6 AC A1 E5 85 A8 E7 BE A4 E4 B8 AD E5 B0 8F E5 92 B8 E9 B1 BC E8 81 9A E4 BC 97 E5 88 92 E6 B0 B4 EF BC 9B 0A E5 AE 9A E4 BA 8E E3 80 90 31 2E 31 31 EF BC 88 E5 91 A8 E5 85 AD EF BC 89 E4 B8 80 E6 A0 A1 E5 8C BA E3 80 91 E5 91 A8 E8 BE B9 E4 B8 BE E8 A1 8C EF BC 9B 0A E4 B8 AD E5 8D 88 31 EF BC 9A 30 30 E4 BA 8E E8 A5 BF E8 8B 91 E5 AE BE E9 A6 86 E9 9B 86 E5 90 88 EF BC 8C E5 90 9B E4 B8 B4 E5 9F 8E 2B E9 9F A9 E7 9B 9B 2F
                        00
                        00
                        38 86 35 BF ED DD 19 4A 1B FD C8 8C 18 89 6C 78 3D A7 F3 A3 47 0D 53 C0 81 B8 D5 D0 42 21 12 24 D1 43 88 79 BA 6A 69 A8 48 0D 2D DF C8 C5 B7 EC 30 D8 4D 65 DE FB 43 A0 77
                        00
                        0F
                        00 00 00 00

                        07
                        00
                        01
                        00
                        1C 42 58 32 31 E6 98 AF E5 93 AA E4 B8 AA E6 A0 A1 E5 8C BA E5 93 AA E6 A0 8B E6 A5 BC

                        00
                        03 00 02 00 03 00
                        04 00 04 00 00 00 06 00 05 00
                        04 58 B5 48 78
                        00 06 00 04 00 18 00 10 00 07 00 04 00 04 20 00 00 09 00 01 00

                        46 70 19 A0  01
                        06 20 98 58 00 00 08 1F 88 5C 00 00 10 0F 94 C5 00 00 11 3C B8 8C 00 00 11 4D 47 6B 00 00 11 AA 9B 45 00 00 13 8B 67 2F 00 00 14 24 5B 7D 00 00 14 9C 62 B9 00 00 14 F4 28 2A 00 00 15 0A 6F 5E 00 00 15 A5 8D 0C 00 00 17 B5 89 32 00 00 19 4E 07 87 00 00 1A 92 53 3C 00 00 1A CA 57 D1 00 00 1B 58 72 29 00 00 21 F8 67 A1 00 01 23 53 B0 8E 00 00 23 B5 55 61 00 00 23 B8 27 65 00 00 23 E8 5F 65 00 00 24 C6 B4 9B 00 00 25 2D A1 41 00 00 25 8E E2 CF 00 00 26 8B CB 82 00 00 2B 2A 0A B5 00 00 2B 2C 15 29 00 00 2C 0B 4B F3 00 00 2C DD 05 DC 00 00 2D BB 44 D6 00 01 2E EA 62 3E 00 00 2F 51 2C 3F 00 00 30 20 D3 5B 00 00 30 D6 0C 2E 00 00 31 B9 3E 72 00 00 31 FD E1 E8 00 00 32 70 75 0C 00 00 33 17 C2 62 00 00 33 73 74 62 00 00 35 58 8C 77 00 00 36 43 7E 2B 00 00 36 A5 8D AC 00 01 36 CF 1A 56 00 00 37 E6 20 8A 00 00 38 3B 42 07 00 01 38 42 CB 8F 00 00 39 0C 01 C9 00 00 39 53 A1 5A 00 00 39 79 9E CF 00 00 3A 17 AF 4A 00 00 3A 24 E6 6F 00 00 3A 83 8E A4 00 00 3A 91 3D D8 00 00 3A BF 77 3A 00 00 3A D4 C6 93 00 00 3B DA B0 79 00 00 3B DA BC 23 00 00 3C C0 C9 23 00 00 3D 3A 9C 64 00 00 3E 03 3F A2 00 00 3E 7A 07 E4 00 00 3F C5 CD 13 00 00 40 A8 6D F7 00 00 41 2A A7 B1 00 00 43 33 F3 F0 00 00 43 A1 51 93 00 00 43 C4 D3 8D 00 00 44 00 F8 A6 00 00 44 05 64 4F 00 00 44 7A A8 1D 00 00 45 0D B4 0D 00 01 46 07 29 CD 00 00 46 70 19 A0 00 00
                         */

                        /*
                        来自群1的从现在这个位置的数据, 直接加入群
                        00
                        0F
                        00 00 00 00

                        06

                        00
                        03 00 02 00 00 00
                        04 00 04 00 00 00 01 00 05 00
                        04 5D F5 37 65
                        00 06 00 04 04 08 00 00 00 07 00 04 00 00 00 00 00 09 00 01 00

                        76 E4 B8 DD 00
                        38 B5 21 5D 00 00
                        3B E7 BB BC 00 00
                        3E 03 3F A2 00 00
                        76 E4 B8 DD 00 00
                         */

                        discardExact(readUByte()) // 00
                        discardExact(readUByte()) // 00
                        val announcement = readUByteLVString()
                        discardExact(readUByte()) // 00
                        discardExact(readUByte()) // 00
                        discardExact(readUByte()) // 38 ... 未知

                        discardExact(2 + 4)


                        // 验证类型,
                        when (val verifyType = readByte().toInt()) {
                            6 -> { // 允许任何人
                            }

                            7 -> { // 需要回答问题?
                                discardExact(3) // 00 01 00, 需要提交给管理员审核
                                readUByteLVString() // 验证问题
                            }

                            else -> {
                                DebugLogger.error("Cannot parse GroupPacket.QueryGroupInfo. unknown verifyType=$verifyType. Still trying to parse...")
                                discardExact(3)
                                readUByteLVString() // 验证问题
                            }
                        }
                        discardExact(43)

                        val stop = readUInt().toLong() // 标记读取群成员的结束
                        discardExact(1) // 00
                        val members = mutableMapOf<Long, MemberPermission>()
                        do {
                            val qq = readUInt().toLong()
                            val status = readUShort().toInt() // 这个群成员的状态, 最后一 bit 为管理员权限. 这里面还包含其他状态
                            if (qq == owner) {
                                continue
                            }

                            val permission = when (status.takeLowestOneBit()) {
                                1 -> MemberPermission.ADMINISTRATOR
                                else -> MemberPermission.MEMBER
                            }
                            members[qq] = permission
                        } while (qq != stop && remaining != 0L)
                        members[owner] = MemberPermission.OWNER
                        return RawGroupInfo(group, owner, groupName, announcement, members)
                        /*
                         * 群 Mirai
                         *
                         * 00 00 00 03 01 41 00 04 01
                         * 40 23 04 40
                         * B1 89 BE 09 群主
                         *
                         * 02 00 00 00 00 00 00 00 00 00 21 00 C8 01
                         * 00 00 00 01 00 00
                         * 00 2D
                         *
                         * 06 4D 69 72 61 69 20 群名
                         * 00
                         * 00
                         * 00
                         * 00
                         * 00
                         * 38 87 5F D8 E8 D4 E9 79 73 8A A4 21 1C 3E 2C 43 D0 23 55 53 49 D3 1C DB F6 1F 84 59 77 66 DA 9C D7 26 0F E3 BD E1 F2 B9 29 D1 F6 97 1C 42 5E B0 AF 09 51 72 DA 03 37 AB 65
                         * 00
                         * 0A 00 00 00 00 06 00 03 00 02 00
                         * 01 00 04 00 04 00 00 00 01 00 05 00 04 5D 90 A7 25 00 06 00 04 04 08 00 00 00 07 00 04 00 00 05 80 00 09 00 01 01
                         * B1 89 BE 09 00
                         * 3E 03 3F A2 00 01
                         * 48 76 54 DC 00 00
                         * 76 E4 B8 DD 00 00
                         * 89 1A 5E AC 00 00
                         * B1 89 BE 09 00 00
                         */

                        /*
                         * 群 XkWhXi
                         *
                         * 00 00 00 03 01 41 00 04 01 40 21 04 40
                         * 3E 03 3F A2 群主
                         *
                         * 02 00 00 00 01 00 00 00 00 27 1A 00 C8 01
                         * 00 00 00 01 00 00
                         * F3 C8
                         *
                         * 06 58 6B 57 68 58 69
                         * 00
                         * 00
                         * 3B E6 AC A2 E8 BF 8E E5 BC 80 E8 BD A6 EF BC 8C E5 8E BB 74 6D E7 9A 84 E7 BD 91 E8 AD A6 0A E6 AC A2 E8 BF 8E E5 BC 80 E8 BD A6 EF BC 8C E5 8E BB 74 6D E7 9A 84 E7 BD 91 E8 AD A6
                         * 00
                         * 00
                         * 38 EB 3B A5 90 AC E3 70 1F 42 51 B4 72 81 C8 F5 5A D8 80 69 B6 76 AD A4 AA CC 6A 17 4C 79 81 FF 82 04 BA 13 CE 28 DA 6C 3F 41 77 C0 77 40 B5 87 8E EE 29 20 65 FC 2D FF 63
                         * 00
                         * 0A 00 00 00 00 06 00 03 00 02 00
                         * 01 00 04 00 04 00 00 00 05 00 05 00 04 57 94 6F 41 00 06 00 04 04 08 00 10 00 07 00 04 00 00 04 04 00 09 00 01 00
                         * B1 89 BE 09 00 2D 5C 53 A6 00 01 2F 9B 1C F2 00 00 35 49 95 D1 00 01 3B FA 06 9F 00 00 3E 03 3F A2 00 00 42 C4 32 63 00 01 59 17 3E 05 00 01 6A 89 3E 3E 00 00 6D D7 4E CA 00 00 76 E4 B8 DD 00 00 7C BB 60 3C 00 01 7C BC D3 C1 00 01 87 73 86 9D 00 00 90 19 72 65 00 00 97 30 9A 6B 00 00 9C B1 E5 55 00 01 B1 89 BE 09 00 01
                         */

                        /*
                         * 群 20秃顶28火葬30重生异世
                         *
                         *
                         */

                        /*
                         * 群 Big convene' (与上面两个来自不同 bot)
                         *
                         * 00 00 00 03 01 01 00 04 01 00 80 01 40
                         * 6C 18 F5 DA 群主
                         *
                         * 02 00 00 27 1B 00 00 00 00 27 1B 01 F4 01
                         * 00 00 00 01 00 00
                         * 0F 1F
                         *
                         * 0C 42 69 67 20 63 6F 6E 76 65 6E 65 27 00 群名
                         * 00 96 E6 AF 95 E4 B8 9A E4 BA 86 EF BC 8C E5 B8 8C E6 9C 9B E5 A4 A7 E5 AE B6 E8 83 BD E5 A4 9F E5 83 8F E4 BB A5 E5 89 8D E9 82 A3 E6 A0 B7 E5 BC 80 E5 BF 83 EF BC 8C E5 AD A6 E4 B9 A0 E8 BF 9B E6 AD A5 EF BC 8C E5 A4 A9 E5 A4 A9 E5 BF AB E4 B9 90 E3 80 82 E6 AD A4 E7 BE A4 E7 A6 81 E6 AD A2 E9 AA 82 E4 BA BA EF BC 8C E5 88 B7 E5 B1 8F E6 9A B4 E5 8A 9B EF BC 8C E8 BF 9D E8 A7 84 E8 80 85 E7 A6 81 E8 A8 80 EF BC 8C E4 B8 A5 E9 87 8D E8 80 85 E5 B0 B1
                         *    76 E8 BF 9B E7 BE A4 E6 97 B6 EF BC 8C E8 AF B7 E4 BF AE E6 94 B9 E6 AD A3 E7 A1 AE E5 A7 93 E5 90 8D E3 80 82 E4 B8 8D E8 83 BD 54 E5 90 8C E5 AD A6 EF BC 8C E5 A4 AA E8 BF 87 E5 88 86 E7 9A 84 54 21 28 E4 BA 92 E8 B5 9E E7 BE A4 EF BC 8C E6 89 8B E6 9C BA E5 9C A8 E7 BA BF E8 81 8A E5 A4 A9 E8 80 85 E5 8F AF E4 BB A5 E4 BA 92 E8 B5 9E E5 AF B9 E6 96 B9
                         * 00 38 D9 FD F5 21 A6 1F 8D 61 37 A1 7A 92 91 2A 2C 71 46 A9 B9 1C 45 EB 38 74 4A 74 EA 77 7D 14 DB 12 D0 B0 09 C2 AA 22 16 F1 D0 B9 97 21 F0 5A A0 06 59 A7 3B 2F 32 D2 B8 E3
                         * 00 0F 00 00 00 00 06 00 03 00 02 01 01 00 04 00 04 00 00 00 15 00 05 00 04 52 7C C5 7C 00 06 00 04 00 00 00 20 00 07 00 04 00 00 00 00 00 09 00 01 00
                         *
                         * C5 15 BE BE 00 ???为啥这个只有一个呢
                         * 1C ED 9F 9B 00 00
                         * 26 D0 E1 3A 00 00
                         * 2D 5C 53 A6 00 01  自己 管理员
                         * 2D BD 28 D2 00 00
                         * 2E 94 76 3E 00 00
                         * 35 F3 BC F2 00 00
                         * 37 D6 91 AB 00 00
                         * 3A 60 1C 3E 00 80  10000000 群员, 好友
                         * 3A 86 EA A3 00 48  01001000 群员 手机在线
                         * 3D 7F E7 70 00 00
                         * 3E 03 3F A2 00 09  00001001 好友, 特别关心, TIM PC 在线, 管理员
                         * 41 47 0C DD 00 40  01000000 群员, 离线
                         * 41 B6 32 A8 00 80
                         * 44 C8 DA 23 00 00
                         * 45 3E 1B 6A 00 80  10000000 群员 手机在线
                         * 45 C6 59 E9 00 C0  群员
                         * 4A BD C6 F9 00 00
                         * 4C 67 45 E8 00 00
                         * 4E AD C2 C2 00 80
                         * 4F A0 F7 EC 00 80
                         * 50 CB 11 E8 00 00
                         * 58 22 21 90 00 00
                         * 59 17 3E 05 00 01  管理员 好友
                         * 5E 74 48 D9 00 00
                         * 5E A2 B5 88 00 00
                         * 66 A1 32 9B 00 40
                         * 68 07 29 0A 00 00
                         * 68 0F EF 4F 00 00
                         * 69 8B 14 F3 00 80
                         * 6A A5 27 4E 00 00
                         * 6C 11 A0 89 00 81  10000001 管理员
                         * 6C 18 F5 DA 00 08  群主
                         * 6C 21 F8 E2 00 01  管理员
                         * 71 F8 F5 18 00 00
                         * 72 0B CC B6 00 00
                         * 75 53 38 DF 00 00
                         * 7A A1 8B 82 00 00
                         * 7C 8C 1D 1B 00 00
                         * 7C BC D3 C1 00 00
                         * 84 2D B8 5F 00 00
                         * 88 4C 33 76 00 00
                         * 8C C8 0D 43 00 00
                         * 90 B8 65 22 00 00
                         * 91 54 89 E9 00 00
                         * 9C E6 93 A5 00 01  管理员
                         * 9D 59 6A 36 00 00
                         * 9D 63 81 5C 00 00
                         * 9E 31 AF AC 00 00
                         * 9E 69 86 25 00 80
                         * A1 FD CA 2D 00 00
                         * A5 22 5C 48 00 00
                         * A5 F2 9A B7 00 00
                         * AF 25 74 9E 00 01
                         * B1 50 24 00 00 00
                         * B2 BD 81 A9 00 00
                         * B5 0E B3 DD 00 00
                         * B9 BF 0D BC 00 00
                         * C5 15 BE BE 00 00
                         */
                    }
                    else -> unsupportedFlag("GroupPacketResponse typed 0x72", flag.toUHexString())
                }
            }

            else -> unsupportedType("GroupPacketResponse", packetType.toUHexString())
        }
    }
}