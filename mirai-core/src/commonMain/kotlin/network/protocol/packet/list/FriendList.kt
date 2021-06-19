/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.list

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Vec0xd50
import net.mamoe.mirai.internal.network.protocol.data.proto.Vec0xd6b
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeJceStruct
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY


internal class FriendList {

    internal object GetTroopMemberList :
        OutgoingPacketFactory<GetTroopMemberList.Response>("friendlist.GetTroopMemberListReq") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readUniPacket(GetTroopMemberListResp.serializer())
            return Response(
                res.vecTroopMember,
                res.nextUin
            )
        }

        operator fun invoke(
            client: QQAndroidClient,
            targetGroupUin: Long,
            targetGroupCode: Long,
            nextUin: Long = 0
        ) = buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    funcName = "GetTroopMemberListReq",
                    servantName = "mqq.IMService.FriendListServiceServantObj",
                    version = 3,
                    requestId = client.nextRequestPacketRequestId(),
                    sBuffer = jceRequestSBuffer(
                        "GTML",
                        GetTroopMemberListReq.serializer(),
                        GetTroopMemberListReq(
                            uin = client.uin,
                            groupCode = targetGroupCode,
                            groupUin = targetGroupUin,
                            nextUin = nextUin,
                            reqType = 0,
                            version = 2
                        )
                    )
                )
            )
        }

        class Response(
            val members: List<StTroopMemberInfo>,
            val nextUin: Long
        ) : Packet {
            override fun toString(): String = "FriendList.GetTroopMemberList.Response"
        }

    }

    internal object GetTroopListSimplify :
        OutgoingPacketFactory<GetTroopListSimplify.Response>("friendlist.GetTroopListReqV2") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readUniPacket(GetTroopListRespV2.serializer())
            return Response(res.vecTroopList.orEmpty())
        }

        class Response(
            val groups: List<StTroopNum>
        ) : Packet {
            override fun toString(): String = "FriendList.GetFriendGroupList.Response"
        }

        operator fun invoke(
            client: QQAndroidClient
        ) = buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    funcName = "GetTroopListReqV2Simplify",
                    servantName = "mqq.IMService.FriendListServiceServantObj",
                    version = 3,
                    cPacketType = 0x00,
                    requestId = client.nextRequestPacketRequestId(),
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

    internal object DelFriend :
        OutgoingPacketFactory<DelFriend.Response>("friendlist.delFriend") {

        class Response(val isSuccess: Boolean, val resultCode: Int) : Packet {
            override fun toString(): String = "FriendList.DelFriend.Response(isSuccess=$isSuccess)"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readUniPacket(DelFriendResp.serializer())
            return Response(res.result == 0, res.result)
        }

        operator fun invoke(
            client: QQAndroidClient,
            friend: Friend
        ) = buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    funcName = "DelFriendReq",
                    servantName = "mqq.IMService.FriendListServiceServantObj",
                    version = 3,
                    sBuffer = jceRequestSBuffer(
                        "DF",
                        DelFriendReq.serializer(),
                        DelFriendReq(
                            uin = client.uin,
                            delType = 2,
                            delUin = friend.uin,
                            version = 1
                        )
                    )
                )
            )
        }
    }

    internal object GetFriendGroupList :
        OutgoingPacketFactory<GetFriendGroupList.Response>("friendlist.getFriendGroupList") {

        class Response(
            val selfInfo: FriendInfo?,
            val totalFriendCount: Short,
            val friendList: List<FriendInfo>
        ) : Packet {
            override fun toString(): String = "FriendList.GetFriendGroupList.Response"
        }

        override suspend fun QQAndroidBot.handle(packet: Response) {
            packet.selfInfo?.let { this.nick = it.nick }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val res = this.readUniPacket(GetFriendListResp.serializer())
            return Response(
                res.stSelfInfo,
                res.totoalFriendCount,
                res.vecFriendInfo.orEmpty()
            )
        }

        fun forSingleFriend(
            client: QQAndroidClient,
            uin: Long
        ) = buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    funcName = "GetFriendListReq",
                    servantName = "mqq.IMService.FriendListServiceServantObj",
                    version = 3,
                    cPacketType = 0x003,
                    requestId = client.nextRequestPacketRequestId(),
                    sBuffer = jceRequestSBuffer(
                        "FL",
                        GetFriendListReq.serializer(),
                        GetFriendListReq(
                            reqtype = 3,
                            ifGetGroupInfo = 0,
                            ifReflush = 1,
                            uin = client.uin,
                            startIndex = 0,
                            groupstartIndex = 0,
                            getgroupCount = 0,
                            ifShowTermType = 1,
                            version = 27L,
                            getfriendCount = 0,
                            ifGetMSFGroup = 0,
                            eAppType = 0,
                            groupid = 0,
                            ifGetBothFlag = 0,
                            ifGetDOVId = 0,
                            uinList = listOf(uin),
                            vec0xd6bReq = Vec0xd6b.ReqBody().toByteArray(Vec0xd6b.ReqBody.serializer()),
                            vec0xd50Req = Vec0xd50.ReqBody(
                                appid = 10002L,
                                reqKsingSwitch = 1,
                                reqMusicSwitch = 1,
                                reqMutualmarkLbsshare = 1,
                                reqMutualmarkAlienation = 1
                            ).toByteArray(Vec0xd50.ReqBody.serializer())
                        )
                    )
                )
            )
        }

        operator fun invoke(
            client: QQAndroidClient,
            friendListStartIndex: Int,
            friendListCount: Int,
            groupListStartIndex: Int,
            groupListCount: Int
        ) = buildOutgoingUniPacket(client, bodyType = 1, key = client.wLoginSigInfo.d2Key) {
            writeJceStruct(
                RequestPacket.serializer(),
                RequestPacket(
                    funcName = "GetFriendListReq",
                    servantName = "mqq.IMService.FriendListServiceServantObj",
                    version = 3,
                    cPacketType = 0x003,
                    requestId = client.nextRequestPacketRequestId(),
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
