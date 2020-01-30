package net.mamoe.mirai.qqandroid.network.protocol.packet.list

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.decodeUniPacket
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
            //this.decodeUniPacket(GetfriendListRep)
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