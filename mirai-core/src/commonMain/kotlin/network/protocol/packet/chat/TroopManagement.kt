/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.ModifyGroupCardReq
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopNum
import net.mamoe.mirai.internal.network.protocol.data.jce.stUinInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.*
import net.mamoe.mirai.utils.daysToSeconds
import net.mamoe.mirai.data.GroupInfo as MiraiGroupInfo

@OptIn(LowLevelApi::class)
internal class GroupInfoImpl(
    private val stTroopNum: StTroopNum
) : MiraiGroupInfo, Packet, Packet.NoLog {
    override val uin: Long get() = stTroopNum.groupUin
    override val owner: Long get() = stTroopNum.dwGroupOwnerUin
    override val groupCode: Long get() = stTroopNum.groupCode
    override val memo: String get() = stTroopNum.groupMemo
    override val name: String get() = stTroopNum.groupName
    override val allowMemberInvite get() = stTroopNum.dwGroupFlagExt?.and(0x000000c0) != 0L
    override val allowAnonymousChat get() = stTroopNum.dwGroupFlagExt?.and(0x40000000) == 0L
    override val autoApprove get() = stTroopNum.dwGroupFlagExt3?.and(0x00100000) == 0L
    override val confessTalk get() = stTroopNum.dwGroupFlagExt3?.and(0x00002000) == 0L
    override val muteAll: Boolean get() = stTroopNum.dwShutUpTimestamp != 0L
    override val botMuteTimestamp: Int get() = stTroopNum.dwMyShutUpTimestamp?.toInt() ?: 0
}

internal class TroopManagement {

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
            override fun toString(): String = "Response(Mute)"
        }
    }


    internal object GetGroupInfo : OutgoingPacketFactory<GroupInfoImpl>("OidbSvc.0x88d_7") {
        @Deprecated("")
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
                            appid = client.subAppId.toInt(),
                            stzreqgroupinfo = listOf(
                                Oidb0x88d.ReqGroupInfo(
                                    stgroupinfo = Oidb0x88d.GroupInfo(
                                        groupFlagExt = 0,
                                        groupFlagext4 = 0,
                                        groupFlag = 0,
                                        groupFlagext3 = 1,//获取confess
                                        noFingerOpenFlag = 1,
                                        cmduinFlagEx2 = 0,
                                        groupTypeFlag = 0,
                                        appPrivilegeFlag = 0,
                                        cmduinFlagEx = 0,
                                        cmduinNewMobileFlag = 0,
                                        cmduinUinFlag = 0,
                                        createSourceFlag = 0,
                                        noCodeFingerOpenFlag = 0,
                                        ingGroupQuestion = "",
                                        ingGroupAnswer = "",
                                        groupName = "",
                                        longGroupName = "",
                                        groupMemo = "",
                                        groupUin = 0,
                                        groupOwner = 0
                                    ),
                                    groupCode = groupCode
                                )
                            )
                        ).toByteArray(Oidb0x88d.ReqBody.serializer())
                    )
                )
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GroupInfoImpl {
            error("deprecated")
            /*
            with(
                this.readBytes()
                    .loadAs(OidbSso.OIDBSSOPkg.serializer()).bodybuffer.loadAs(Oidb0x88d.RspBody.serializer()).stzrspgroupinfo!![0].stgroupinfo!!
            ) {
                return GroupInfoImpl()
            }*/
        }
    }

    internal object Kick : OutgoingPacketFactory<Kick.Response>("OidbSvc.0x8a0_0") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val ret = this.readBytes()
                .loadAs(OidbSso.OIDBSSOPkg.serializer()).bodybuffer.loadAs(Oidb0x8a0.RspBody.serializer()).msgKickResult.first().optUint32Result
            return Response(
                ret == 0,
                ret
            )
        }

        class Response(
            val success: Boolean,
            val ret: Int
        ) : Packet {
            override fun toString(): String = "TroopManagement.Kick.Response($success)"
        }

        operator fun invoke(
            client: QQAndroidClient,
            member: Member,
            message: String
        ): OutgoingPacket = buildOutgoingUniPacket(client) {
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
//
//        fun confessTalk(
//            client: QQAndroidClient,
//            groupCode: Long,
//            switch: Boolean
//        ): OutgoingPacket {
//            return buildOutgoingUniPacket(client) {
//                writeProtoBuf(
//                    OidbSso.OIDBSSOPkg.serializer(),
//                    OidbSso.OIDBSSOPkg(
//                        command = 2202,
//                        bodybuffer = Oidb0x89a.ReqBody(
//                            groupCode = groupCode,
//                            stGroupInfo = Oidb0x89a.Groupinfo(
//                                groupFlagext3Mask = 8192,
//                                groupFlagext3 = if (switch) {
//                                    0
//                                } else {
//                                    8192
//                                }
//                            )
//                        ).toByteArray(Oidb0x89a.ReqBody.serializer())
//                    )
//                )
//            }
//        }

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
        object Response : Packet {
            override fun toString(): String {
                return "TroopManagement.EditGroupNametag.Response"
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): EditGroupNametag.Response {
            this.close()
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
                        funcName = "ModifyGroupCardReq",
                        servantName = "mqq.IMService.FriendListServiceServantObj",
                        version = 3,
                        cPacketType = 0x00,
                        requestId = client.nextRequestPacketRequestId(),
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
                                        sName = newName,
                                        sPhone = "",
                                        sEmail = "",
                                        sRemark = ""
                                    )
                                )
                            )
                        )
                    )
                )
            }
        }

    }
}