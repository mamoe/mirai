package net.mamoe.mirai.qqandroid.network.protocol.packet.list

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.io.serialization.writeJceStruct
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.GetFriendListReq
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Vec0xd50
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.utils.io.debugPrint


internal class FriendList {

    internal object GetFriendGroupList : PacketFactory<GetFriendGroupList.Response>("friendlist.getFriendGroupList") {

        class Response : Packet {
            override fun toString(): String = "FriendList.GetFriendGroupList.Response"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            // 00 00 0A D6 10 03 2C 3C 42 72 85 3C F2 56 29 6D 71 71 2E 49 4D 53 65 72 76 69 63 65 2E 46 72 69 65 6E 64 4C 69 73 74 53 65 72 76 69 63 65 53 65 72 76 61 6E 74 4F 62 6A 66 10 47 65 74 46 72 69 65 6E 64 4C 69 73 74 52 65 71 7D 00 01 0A 82 08 00 01 06 06 46 4C 52 45 53 50 1D 00 01 0A 72 0A 00 03 1C 22 76 E4 B8 DD 3C 41 86 9F 50 0A 60 0A 79 00 0A 0A 02 1A F7 2F 11 1C 21 01 2C 36 09 65 27 74 65 72 6E 69 74 79 4C 50 14 6C 7C 8C 9C A0 14 BC C6 00 DC E6 09 65 27 74 65 72 6E 69 74 79 FC 0F FD 10 00 0C FD 11 00 0C FC 12 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B FC 14 FD 15 00 00 0A 08 91 DE DC D7 01 88 19 B8 17 FC 16 FC 17 FC 18 F1 19 27 1E FC 1A F6 1B 00 FC 1C F0 1D 01 F2 1E 5C 4F 2E 39 F0 1F 02 F2 20 00 01 1E 27 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 FC 26 FC 27 FC 28 FD 29 00 0C FC 2A FC 2B FC 2C F6 2D 00 F2 2E 5E 0E BE 48 FC 2F FC 30 F6 31 00 FC 32 F0 33 02 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 02 2D 5C 53 A6 1C 21 01 F2 36 06 E6 A2 A8 E5 A4 B4 4C 50 14 6C 7C 8C 9C A0 0A BC C6 00 DC E6 06 E6 A2 A8 E5 A4 B4 FC 0F FD 10 00 0C FD 11 00 0C F2 12 00 01 2F 02 FA 13 08 00 04 00 01 1A 0C 1C 20 03 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 20 03 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B FC 14 FD 15 00 00 0A 08 A6 A7 F1 EA 02 88 19 B8 17 FC 16 FC 17 FC 18 FC 19 F0 1A 0A F6 1B 0F 54 49 4D E7 A7 BB E5 8A A8 E5 9C A8 E7 BA BF FC 1C FC 1D FC 1E F0 1F FF F2 20 00 02 00 00 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 F2 26 5B 74 16 A2 FC 27 FC 28 FD 29 00 0C F0 2A 02 FC 2B FC 2C F6 2D 00 F2 2E 58 89 FB 37 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 02 3E 03 3F A2 1C 21 01 1D 36 09 48 69 6D 31 38 38 6D 6F 65 4C 50 14 6C 7C 8C 9C A0 0A BC C6 00 DC E6 09 48 69 6D 31 38 38 6D 6F 65 FC 0F FD 10 00 0C FD 11 00 0C F2 12 00 01 2E 01 FA 13 08 00 04 00 01 1A 0C 1C 20 07 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 20 07 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B FC 14 FD 15 00 00 0A 08 A2 FF 8C F0 03 88 19 B8 17 FC 16 FC 17 FC 18 FC 19 F0 1A 0A F6 1B 0F 54 49 4D E7 94 B5 E8 84 91 E5 9C A8 E7 BA BF FC 1C F0 1D 02 F2 1E 58 8C 5F D3 F0 1F 02 FC 20 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 F2 26 59 A6 FC BD FC 27 FC 28 FD 29 00 0C F0 2A 02 FC 2B FC 2C F6 2D 00 F2 2E 5A 9A A6 F9 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 02 59 17 3E 05 1C 21 02 1C 36 09 E3 82 A2 E3 82 A4 E3 83 A9 4C 50 0B 6C 7C 8C 9C A0 0A BC C6 00 DC E6 09 E3 82 A2 E3 82 A4 E3 83 A9 FC 0F FD 10 00 0C FD 11 00 0C F2 12 00 01 01 07 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B F0 14 01 FD 15 00 00 0A 08 85 FC DC C8 05 88 19 B8 17 F0 16 01 F1 17 08 F9 F0 18 01 F1 19 27 20 F0 1A 01 F6 1B 0C E6 89 8B E6 9C BA E5 9C A8 E7 BA BF FC 1C F0 1D 02 F2 1E 59 4A 8C EA F0 1F 02 F2 20 00 01 20 27 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 F2 26 5D 4F 9D 77 F2 27 59 84 39 2C FC 28 FD 29 00 0C F0 2A 02 FC 2B F0 2C 06 F6 2D 00 F2 2E 5C 4A B7 A2 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 02 76 E4 B8 DD 1C 21 02 5B 36 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 4C 50 0B 6C 7C 8C 9C A0 0A BC C6 00 DC E6 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E FC 0F FD 10 00 0C FD 11 00 0C F2 12 00 01 01 07 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B F0 14 01 FD 15 00 00 0A 08 DD F1 92 B7 07 88 19 B8 17 F0 16 01 FC 17 F0 18 01 FC 19 F0 1A 01 F6 1B 0C E6 89 8B E6 9C BA E5 9C A8 E7 BA BF FC 1C FC 1D FC 1E F0 1F 01 FC 20 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 FC 26 FC 27 FC 28 FD 29 00 0C F0 2A 02 FC 2B FC 2C F6 2D 00 F2 2E 5D B4 12 03 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 02 7C BC D3 C1 1C 21 00 ED 36 09 F0 9F 90 B8 6C 69 74 6F 75 4C 50 14 6C 7C 8C 9C A0 0A BC C6 00 DC E6 09 F0 9F 90 B8 6C 69 74 6F 75 FC 0F FD 10 00 0C FD 11 00 0C FC 12 FA 13 08 00 04 00 01 1A 0C 1C 20 07 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 20 07 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B F0 14 01 FD 15 00 00 0A 08 C1 A7 F3 E5 07 88 19 B8 17 F0 16 01 FC 17 FC 18 FC 19 F0 1A 0A F6 1B 0F 54 49 4D E7 A7 BB E5 8A A8 E5 9C A8 E7 BA BF FC 1C F0 1D 01 F2 1E 5D AA 93 F1 F0 1F 01 F2 20 00 02 00 00 F6 21 00 F6 22 00 FC 23 F2 24 5B C3 68 00 FC 25 F2 26 5C 66 22 C5 F2 27 59 93 B9 9D FC 28 FD 29 00 0C F0 2A 02 FC 2B F0 2C 14 F6 2D 00 F2 2E 5C A7 87 30 FC 2F FC 30 F6 31 00 FC 32 F0 33 02 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 03 00 00 00 00 88 C8 FE 49 1C 21 02 3D 36 0A 32 32 39 34 38 37 33 36 37 33 4C 50 14 6C 7C 8C 9C A0 14 BC C6 00 DC E6 0A 32 32 39 34 38 37 33 36 37 33 FC 0F FD 10 00 0C FD 11 00 0C FC 12 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B FC 14 FD 15 00 00 0A 08 C9 FC A3 C6 08 88 19 B8 17 FC 16 FC 17 FC 18 FC 19 FC 1A F6 1B 00 FC 1C FC 1D FC 1E F0 1F 01 FC 20 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 FC 26 FC 27 FC 28 FD 29 00 0C FC 2A FC 2B FC 2C F6 2D 00 FC 2E FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 03 00 00 00 00 A8 32 51 A1 1C 2C 36 01 4E 4C 50 0B 6C 7C 8C 9C A0 0A BC C6 00 D0 01 E6 0F E3 82 AE E3 83 A9 E3 83 86 E3 82 A3 E3 83 8A FC 0F FD 10 00 0C FD 11 00 0C F2 12 00 01 01 07 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B FC 14 FD 15 00 00 0A 08 A1 A3 C9 C1 0A 88 19 B8 17 FC 16 FC 17 FC 18 F1 19 27 1E F0 1A 65 F6 1B 0C E6 89 8B E6 9C BA E5 9C A8 E7 BA BF F1 1C 27 78 FC 1D FC 1E F0 1F 01 F2 20 00 01 1E 27 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 FC 26 FC 27 FC 28 FD 29 00 0C FC 2A FC 2B FC 2C F6 2D 00 F2 2E 59 62 C5 18 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 03 00 00 00 00 B1 89 BE 09 1C 21 02 58 36 01 4E 4C 50 14 6C 7C 8C 9C A0 0A BC C6 17 6C 69 75 6A 69 61 68 75 61 31 32 33 31 32 33 40 31 32 36 2E 63 6F 6D D0 01 E6 09 4E 61 74 75 72 61 6C 48 47 FC 0F FD 10 00 0C FD 11 00 0C F2 12 00 01 35 02 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B F0 14 71 FD 15 00 00 0A 08 89 FC A6 8C 0B 88 19 B8 17 FC 16 FC 17 F0 18 04 FC 19 F0 1A 04 F6 1B 0E 69 50 68 6F 6E 65 20 58 E5 9C A8 E7 BA BF FC 1C FC 1D FC 1E F0 1F 01 FC 20 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 F2 26 5D BB 7C 19 FC 27 FC 28 FD 29 00 0C FC 2A FC 2B FC 2C F6 2D 00 F2 2E 5D B5 3E F2 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 0A 03 00 00 00 00 BC 41 BA A7 1C 21 02 3A 36 06 32 33 33 33 33 33 4C 50 14 6C 7C 8C 9C A0 14 BC C6 00 DC E6 06 32 33 33 33 33 33 FC 0F FD 10 00 0C FD 11 00 0C FC 12 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B FC 14 FD 15 00 00 0A 08 A7 F5 86 E2 0B 88 19 B8 17 FC 16 FC 17 FC 18 FC 19 FC 1A F6 1B 00 FC 1C FC 1D FC 1E F0 1F 01 FC 20 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 FC 26 FC 27 FC 28 FD 29 00 0C FC 2A FC 2B FC 2C F6 2D 00 F2 2E 5A 76 BE 66 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B 8C 9C AC B0 9F C0 04 DC E9 0C FC 0F FC 10 F0 11 07 F2 12 5E 32 AF F9 FC 13 F9 14 0C FC 15 FC 16 FA 17 02 76 E4 B8 DD 1C 21 02 5B 36 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 4C 50 0B 6C 7C 8C 9C A0 0A BC C6 00 DC E6 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E FC 0F FD 10 00 0C FD 11 00 0C F2 12 00 01 01 07 FA 13 08 00 04 00 01 1A 0C 1C 2C 3C 4C 0B 00 02 1A 0C 1C 2C 3C 4C 0B 00 03 1A 0C 1C 2C 3C 4C 0B 00 07 1A 0C 1C 2C 3C 4C 0B 1C 2C 0B F0 14 01 FD 15 00 00 0A 08 DD F1 92 B7 07 88 19 B8 17 F0 16 01 FC 17 F0 18 01 FC 19 F0 1A 01 F6 1B 0C E6 89 8B E6 9C BA E5 9C A8 E7 BA BF FC 1C FC 1D FC 1E F0 1F 01 FC 20 F6 21 00 F6 22 00 FC 23 FC 24 FC 25 FC 26 FC 27 FC 28 FD 29 00 0C F0 2A 02 FC 2B FC 2C F6 2D 00 F2 2E 5D B4 12 03 FC 2F FC 30 F6 31 00 FC 32 FC 33 FD 34 00 0C FC 35 FC 36 FD 37 00 0C FD 38 00 0C FC 39 FC 3A 0B F0 18 01 FC 19 FA 1A 0C 1C 0B 0B 8C 98 0C A8 0C

            println("aaaa")
            this.debugPrint()
            return Response()
        }

        operator fun invoke(
            client: QQAndroidClient,
            friendListStartIndex: Int,
            friendListCount: Int,
            groupListStartIndex: Int,
            groupListCount: Int
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        sFuncName = "GetFriendListReq",
                        sServantName = "mqq.IMService.FriendListServiceServantObj",
                        iVersion = 3,
                        cPacketType = 0x003,
                        iMessageType = 0x00000,
                        iRequestId = 1921334514,
                        sBuffer = jceRequestSBuffer(
                            "FL",
                            GetFriendListReq.serializer(),
                            GetFriendListReq(
                                reqtype = 3,
                                ifReflush = if (friendListStartIndex <= 0) {
                                    0
                                } else {
                                    1
                                },
                                uin = client.uin,
                                startIndex = friendListStartIndex.toShort(),
                                getfriendCount = friendListCount.toShort(),
                                groupid = 0,
                                ifGetGroupInfo = if (friendListStartIndex <= 0) {
                                    0
                                } else {
                                    1
                                },
                                groupstartIndex = groupListStartIndex.toByte(),
                                getgroupCount = groupListCount.toByte(),
                                ifGetMSFGroup = 0,
                                ifShowTermType = 1,
                                version = 27L,
                                uinList = null,
                                eAppType = 0,
                                ifGetBothFlag = 0,
                                ifGetDOVId = 0,
                                vec0xd6bReq = EMPTY_BYTE_ARRAY,
                                vec0xd50Req = Vec0xd50.ReqBody(
                                    appid = 10002L,
                                    reqKsingSwitch = 1,
                                    reqMusicSwitch = 1,
                                    reqMutualmarkLbsshare = 1,
                                    reqMutualmarkAlienation = 1
                                ).toByteArray(Vec0xd50.ReqBody.serializer()),
                                vecSnsTypelist = listOf(13580L, 13581L, 13582L)
                            )
                        )
                    )
                )
            }
        }
    }
}