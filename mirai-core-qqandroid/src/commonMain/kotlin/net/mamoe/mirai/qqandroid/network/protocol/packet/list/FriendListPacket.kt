package net.mamoe.mirai.qqandroid.network.protocol.packet.list

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.io.serialization.writeJceStruct
import net.mamoe.mirai.qqandroid.io.toByteArray
import net.mamoe.mirai.qqandroid.io.writeJcePacket
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.GetFriendListReq
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestDataStructSvcReqRegister
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestDataVersion3
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.GetImgUrlReq
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Vec0xd50
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Vec0xd6b
import net.mamoe.mirai.qqandroid.network.protocol.packet.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImageDownPacket


internal object FriendListPacket :
    PacketFactory<FriendListPacket.GetFriendListResponse>("friendlist.getFriendGroupList") {

    class GetFriendListResponse() : Packet


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GetFriendListResponse {
        println("aaaa")
        return GetFriendListResponse()
    }

    operator fun invoke(
        client: QQAndroidClient,
        friendListStartIndex: Int,
        friendListCount: Int,
        groupListStartIndex: Int,
        groupListCount: Int
    ): OutgoingPacket {
        return buildOutgoingUniPacket(client, key = client.wLoginSigInfo.d2Key) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    sFuncName = "GetFriendListReq",
                    sServantName = "mqq.IMService.FriendListServiceServantObj",
                    iVersion = 3,
                    cPacketType = 0x003,
                    iMessageType = 0x00000,
                    sBuffer = RequestDataVersion3(
                        mapOf(
                            "FL" to GetFriendListReq(
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
                                ifShowTermType = 0,
                                version = 27L,
                                uinList = null,
                                eAppType = 0,
                                ifGetBothFlag = 0,
                                ifGetDOVId = 0,
                                vec0xd6bReq = Vec0xd6b.ReqBody().toByteArray(Vec0xd6b.ReqBody.serializer()),
                                vec0xd50Req = Vec0xd50.ReqBody(
                                    appid = 10002L,
                                    reqKsingSwitch = 1,
                                    reqMusicSwitch = 1,
                                    reqMutualmarkLbsshare = 1,
                                    reqMutualmarkAlienation = 1
                                ).toByteArray(Vec0xd50.ReqBody.serializer()),
                                vecSnsTypelist = listOf(13580L, 13581L, 13582L)
                            ).toByteArray(GetFriendListReq.serializer())
                        )
                    ).toByteArray(RequestDataVersion3.serializer())
                )
            )
        }
    }

}

