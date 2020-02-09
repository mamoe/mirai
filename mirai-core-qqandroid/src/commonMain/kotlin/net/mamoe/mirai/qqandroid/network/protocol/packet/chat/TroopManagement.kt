/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.toByteArray
import kotlinx.serialization.toUtf8Bytes
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.*
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.ModifyGroupCardReq
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.stUinInfo
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.*
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement.GetGroupOperationInfo.decode
import net.mamoe.mirai.utils.daysToSeconds
import net.mamoe.mirai.utils.io.encodeToString

internal object TroopManagement {

    internal object Mute : OutgoingPacketFactory<Mute.Response>("OidbSvc.0x570_8") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            //屁用没有
            return Response
        }

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            memberUin: Long,
            timeInSecond: Int
        ): OutgoingPacket {
            require(timeInSecond in 0..30.daysToSeconds)
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 1392,
                        serviceType = 8,
                        result = 0,
                        bodybuffer = buildPacket {
                            writeInt(groupCode.toInt())//id or UIN?
                            writeByte(32)
                            writeShort(1)
                            writeInt(memberUin.toInt())
                            writeInt(timeInSecond)
                        }.readBytes()
                    )
                )
            }
        }

        object Response : Packet {
            override fun toString(): String {
                return "Response(Mute)"
            }
        }
    }


    internal object GetGroupOperationInfo : OutgoingPacketFactory<GetGroupOperationInfo.Response>("OidbSvc.0x88d_7") {
        class Response(
            val allowAnonymousChat: Boolean,
            val allowMemberInvite: Boolean,
            val autoApprove: Boolean,
            val confessTalk: Boolean
        ) : Packet {
            override fun toString(): String {
                return "Response(GroupInfo)"
            }
        }

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2189,
                        serviceType = 7,
                        result = 0,
                        bodybuffer = Oidb0x88d.ReqBody(
                            appid = 537062845,
                            stzreqgroupinfo = listOf(
                                Oidb0x88d.ReqGroupInfo(
                                    stgroupinfo = Oidb0x88d.GroupInfo(
                                        groupFlagExt = 0,
                                        groupFlagext4 = 0,
                                        groupFlag = 0,
                                        groupFlagext3 = 0,//获取confess
                                        noFingerOpenFlag = 0,
                                        cmduinFlagEx2 = 0,
                                        groupTypeFlag = 0,
                                        appPrivilegeFlag = 0,
                                        cmduinFlagEx = 0,
                                        cmduinNewMobileFlag = 0,
                                        cmduinUinFlag = 0,
                                        createSourceFlag = 0,
                                        noCodeFingerOpenFlag = 0,
                                        ingGroupQuestion = EMPTY_BYTE_ARRAY,
                                        ingGroupAnswer = EMPTY_BYTE_ARRAY
                                    ),
                                    groupCode = groupCode
                                )
                            )
                        ).toByteArray(Oidb0x88d.ReqBody.serializer())
                    )
                )
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            with(this.readBytes().loadAs(OidbSso.OIDBSSOPkg.serializer()).bodybuffer.loadAs(Oidb0x88d.RspBody.serializer()).stzrspgroupinfo!![0].stgroupinfo!!) {
                return Response(
                    allowMemberInvite = (this.groupFlagExt?.and(0x000000c0) != 0),
                    allowAnonymousChat = (this.groupFlagExt?.and(0x40000000) == 0),
                    autoApprove = (this.groupFlagext3?.and(0x00100000) == 0),
                    confessTalk = (this.groupFlagext3?.and(0x00002000) == 0)
                )
            }
        }
    }

    internal object Kick : OutgoingPacketFactory<Kick.Response>("OidbSvc.0x8a0_0") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response(this.readBytes().loadAs(OidbSso.OIDBSSOPkg.serializer()).bodybuffer.loadAs(Oidb0x8a0.RspBody.serializer()).msgKickResult!![0].optUint32Result == 1)
        }

        class Response(
            val success: Boolean
        ) : Packet {
            override fun toString(): String {
                return "Response(Kick Member)"
            }
        }

        operator fun invoke(
            client: QQAndroidClient,
            member: Member,
            message: String
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2208,
                        serviceType = 0,//或者1
                        result = 0,
                        bodybuffer = Oidb0x8a0.ReqBody(
                            optUint64GroupCode = member.group.id,
                            msgKickList = listOf(
                                Oidb0x8a0.KickMemberInfo(
                                    optUint32Operate = 5,
                                    optUint64MemberUin = member.id,
                                    optUint32Flag = 1//或者0
                                )
                            ),
                            kickMsg = message.toByteArray()
                        ).toByteArray(Oidb0x8a0.ReqBody.serializer())
                    )
                )
            }
        }


    }

    internal object GroupOperation : OutgoingPacketFactory<GroupOperation.Response>("OidbSvc.0x89a_0") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response
        }

        fun muteAll(
            client: QQAndroidClient,
            groupCode: Long,
            switch: Boolean
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2202,
                        bodybuffer = Oidb0x89a.ReqBody(
                            groupCode = groupCode,
                            stGroupInfo = Oidb0x89a.Groupinfo(
                                shutupTime = if (switch) {
                                    1
                                } else {
                                    0
                                }
                            )
                        ).toByteArray(Oidb0x89a.ReqBody.serializer())
                    )
                )
            }
        }

        fun confessTalk(
            client: QQAndroidClient,
            groupCode: Long,
            switch: Boolean
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2202,
                        bodybuffer = Oidb0x89a.ReqBody(
                            groupCode = groupCode,
                            stGroupInfo = Oidb0x89a.Groupinfo(
                                groupFlagext3Mask = 8192,
                                groupFlagext3 = if (switch) {
                                    0
                                } else {
                                    8192
                                }
                            )
                        ).toByteArray(Oidb0x89a.ReqBody.serializer())
                    )
                )
            }
        }

        fun autoApprove(
            client: QQAndroidClient,
            groupCode: Long,
            switch: Boolean
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2202,
                        bodybuffer = Oidb0x89a.ReqBody(
                            groupCode = groupCode,
                            stGroupInfo = Oidb0x89a.Groupinfo(
                                groupFlagext3 = if (switch) {
                                    0x00100000
                                } else {
                                    0x00000000
                                }//暂时无效
                            )
                        ).toByteArray(Oidb0x89a.ReqBody.serializer())
                    )
                )
            }
        }

        fun name(
            client: QQAndroidClient,
            groupCode: Long,
            newName: String
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2202,
                        bodybuffer = Oidb0x89a.ReqBody(
                            groupCode = groupCode,
                            stGroupInfo = Oidb0x89a.Groupinfo(
                                ingGroupName = newName.toByteArray()
                            )
                        ).toByteArray(Oidb0x89a.ReqBody.serializer())
                    )
                )
            }
        }

        fun memo(
            client: QQAndroidClient,
            groupCode: Long,
            newMemo: String
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2202,
                        bodybuffer = Oidb0x89a.ReqBody(
                            groupCode = groupCode,
                            stGroupInfo = Oidb0x89a.Groupinfo(
                                ingGroupMemo = newMemo.toByteArray()
                            )
                        ).toByteArray(Oidb0x89a.ReqBody.serializer())
                    )
                )
            }
        }

        fun allowMemberInvite(
            client: QQAndroidClient,
            groupCode: Long,
            switch: Boolean
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2202,
                        bodybuffer = Oidb0x89a.ReqBody(
                            groupCode = groupCode,
                            stGroupInfo = Oidb0x89a.Groupinfo(
                                allowMemberInvite = if (switch) {
                                    1
                                } else {
                                    0
                                }
                            )
                        ).toByteArray(Oidb0x89a.ReqBody.serializer())
                    )
                )
            }
        }


        object Response : Packet {
            override fun toString(): String {
                return "TroopManagement.GroupOperation.Response"
            }
        }
    }


    internal object EditSpecialTitle : OutgoingPacketFactory<EditSpecialTitle.Response>("OidbSvc.0x8fc_2") {
        object Response : Packet

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response
        }

        operator fun invoke(
            client: QQAndroidClient,
            member: Member,
            newName: String
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    OidbSso.OIDBSSOPkg.serializer(),
                    OidbSso.OIDBSSOPkg(
                        command = 2300,
                        serviceType = 2,
                        bodybuffer = Oidb0x8fc.ReqBody(
                            groupCode = member.group.id,
                            memLevelInfo = listOf(
                                Oidb0x8fc.MemberInfo(
                                    uin = member.id,
                                    uinName = newName.toByteArray(),
                                    specialTitle = newName.toByteArray(),
                                    specialTitleExpireTime = -1
                                )
                            )
                        ).toByteArray(Oidb0x8fc.ReqBody.serializer())
                    )
                )
            }
        }
    }

    internal object EditGroupNametag :
        OutgoingPacketFactory<EditGroupNametag.Response>("friendlist.ModifyGroupCardReq") {
        object Response : Packet

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): EditGroupNametag.Response {
            return Response
        }

        operator fun invoke(
            client: QQAndroidClient,
            member: Member,
            newName: String
        ): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        sFuncName = "ModifyGroupCardReq",
                        sServantName = "mqq.IMService.FriendListServiceServantObj",
                        iVersion = 3,
                        cPacketType = 0x00,
                        iMessageType = 0x00000,
                        iRequestId = client.nextRequestPacketRequestId(),
                        sBuffer = jceRequestSBuffer(
                            "MGCREQ",
                            ModifyGroupCardReq.serializer(),
                            ModifyGroupCardReq(
                                dwZero = 0L,
                                dwGroupCode = member.group.id,
                                dwNewSeq = 0L,
                                vecUinInfo = listOf(
                                    stUinInfo(
                                        gender = 0,
                                        dwuin = member.id,
                                        dwFlag = 31,
                                        sName = newName.toUtf8Bytes().encodeToString(charset = CharsetGBK),
                                        sPhone = "",
                                        sEmail = "",
                                        sRemark = ""
                                    )
                                )
                            ),
                            JceCharset.GBK
                        )
                    )
                )
            }
        }

    }

    /*
    internal object Recall: OutgoingPacketFactory<LoginPacket.LoginPacketResponse>("wtlogin.login"){
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacket.LoginPacketResponse {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
     */

}