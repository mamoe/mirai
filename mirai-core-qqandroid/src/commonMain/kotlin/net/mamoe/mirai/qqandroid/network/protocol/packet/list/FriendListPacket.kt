package net.mamoe.mirai.qqandroid.network.protocol.packet.list

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.decodeUniPacket
import net.mamoe.mirai.qqandroid.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.io.serialization.writeJceStruct
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.*
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.GetFriendListReq
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.GetFriendListResp
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.GetTroopListReqV2Simplify
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.GroupInfo
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Vec0xd50
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Vec0xd6b
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.list.FriendList.GetFriendGroupList.decode
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.debugPrint
import net.mamoe.mirai.utils.io.discardExact
import net.mamoe.mirai.utils.io.readRemainingBytes
import net.mamoe.mirai.utils.io.toUHexString


internal class FriendList {

    /**
     * Get Troop List不一定会得到服务器的回应 据推测与群数量有关
     * 因此 应对timeout方法做出处理
     * timeout时间应不小于 8s？
     *
     */
    internal object GetTroopList :
        PacketFactory<GetTroopList.Response>("friendlist.GetTroopListReqV2") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GetTroopList.Response {
            debugPrint()
            this.discardExact(4)
            val res = this.decodeUniPacket(GetTroopListRespV2.serializer())
            println(res.contentToString())
            return Response(

            )

        }

        class Response(

        ) : Packet {
            override fun toString(): String = "FriendList.GetFriendGroupList.Response"
        }

        operator fun invoke(
            client: QQAndroidClient
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        sFuncName = "GetTroopListReqV2Simplify",
                        sServantName = "mqq.IMService.FriendListServiceServantObj",
                        iVersion = 3,
                        cPacketType = 0x00,
                        iMessageType = 0x00000,
                        sBuffer = jceRequestSBuffer(
                            "GetTroopListReqV2Simplify",
                            GetTroopListReqV2Simplify.serializer(),
                            GetTroopListReqV2Simplify(
                                uin = client.uin,
                                getMSFMsgFlag = 0, // const
                                groupFlagExt = 1,// const
                                shVersion = 7, // const
                                dwCompanyId = 0,
                                versionNum = 1, // const
                                vecGroupInfo = listOf(),
                                getLongGroupName = 1// const
                            )
                        )
                    )
                )
            }
        }
    }
    internal object GetFriendGroupList : PacketFactory<GetFriendGroupList.Response>("friendlist.getFriendGroupList") {
        class Response(
            val totalFriendCount: Short,
            val friendList: List<FriendInfo>
        ) : Packet {
            override fun toString(): String = "FriendList.GetFriendGroupList.Response"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            this.discardExact(4)
            val res = this.decodeUniPacket(GetFriendListResp.serializer())
            return Response(
                res.totoalFriendCount,
                res.vecFriendInfo.orEmpty()
            )
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
                        sBuffer = jceRequestSBuffer(
                            "FL",
                            GetFriendListReq.serializer(),
                            GetFriendListReq(
                                reqtype = 3,
                                ifReflush = if (friendListStartIndex == 0) {
                                    1
                                } else {
                                    0
                                },
                                uin = client.uin,
                                startIndex = friendListStartIndex.toShort(),
                                getfriendCount = friendListCount.toShort(),
                                groupid = 0,
                                ifGetGroupInfo = if (friendListStartIndex == 0) {
                                    1
                                } else {
                                    0
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
                                vec0xd6bReq = Vec0xd6b.ReqBody().toByteArray(Vec0xd6b.ReqBody.serializer()),
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