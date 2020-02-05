package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.io.serialization.writeProtoBuf
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Oidb0x88d
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Oidb0x89a
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.OidbSso
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.utils.daysToSeconds

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


        object Response : Packet
    }


    internal object EditNametag : OutgoingPacketFactory<LoginPacket.LoginPacketResponse>("OidbSvc.0x8fc_2") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): LoginPacket.LoginPacketResponse {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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