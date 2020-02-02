package net.mamoe.mirai.qqandroid.network.protocol.packet.list

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.contact.toInternalId
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.decodeUniPacket
import net.mamoe.mirai.qqandroid.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.io.serialization.writeJceStruct
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.*
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Vec0xd50
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.utils.io.discardExact


internal class FriendList {

    internal object GetTroopMemberList :
        OutgoingPacketFactory<GetTroopMemberList.Response>("friendlist.GetTroopMemberListReq") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GetTroopMemberList.Response {
            TODO()
        }

        operator fun invoke(
            client: QQAndroidClient,
            targetGroupId: Long,
            nextUin: Long = 0
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        sFuncName = "GetTroopMemberListReq",
                        sServantName = "mqq.IMService.FriendListServiceServantObj",
                        iVersion = 3,
                        iRequestId = client.nextRequestPacketRequestId(),
                        sBuffer = jceRequestSBuffer(
                            "GTML",
                            GetTroopMemberListReq.serializer(),
                            GetTroopMemberListReq(
                                uin = client.uin,
                                groupCode = GroupId(targetGroupId).toInternalId().value,
                                groupUin = targetGroupId,
                                nextUin = 0,
                                reqType = 0
                            )
                        )
                    )
                )
            }
        }

        class Response(

        ) : Packet {
            override fun toString(): String = "FriendList.GetFriendGroupList.Response"
        }

    }

    internal object GetTroopListSimplify :
        OutgoingPacketFactory<GetTroopListSimplify.Response>("friendlist.GetTroopListReqV2") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GetTroopListSimplify.Response {
            val res = this.decodeUniPacket(GetTroopListRespV2.serializer())
            return Response(res.vecTroopList.orEmpty())
        }

        class Response(
            val groups: List<stTroopNum>
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
                        iRequestId = client.nextRequestPacketRequestId(),
                        sBuffer = jceRequestSBuffer(
                            "GetTroopListReqV2Simplify",
                            GetTroopListReqV2Simplify.serializer(),
                            GetTroopListReqV2Simplify(
                                uin = client.uin,
                                getMSFMsgFlag = 0,
                                groupFlagExt = 1,
                                shVersion = 7,
                                dwCompanyId = 0,
                                versionNum = 1,
                                vecGroupInfo = listOf(),
                                getLongGroupName = 1
                            )
                        )
                    )
                )
            }
        }
    }

    internal object GetFriendGroupList : OutgoingPacketFactory<GetFriendGroupList.Response>("friendlist.getFriendGroupList") {

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
                                ifGetGroupInfo = if (groupListCount <= 0) {
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