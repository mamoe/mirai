package net.mamoe.mirai.qqandroid.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.utils.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf

internal class NewContact {

    internal object SystemMsgNewFriend :
        OutgoingPacketFactory<NewFriendRequestEvent?>("ProfileService.Pb.ReqSystemMsgNew.Friend") {

        operator fun invoke(client: QQAndroidClient) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Structmsg.ReqSystemMsgNew.serializer(),
                Structmsg.ReqSystemMsgNew(
                    checktype = 2,
                    flag = Structmsg.FlagInfo(
                        frdMsgDiscuss2ManyChat = 1,
                        frdMsgGetBusiCard = 1,
                        frdMsgNeedWaitingMsg = 1,
                        frdMsgUint32NeedAllUnreadMsg = 1,
                        grpMsgMaskInviteAutoJoin = 1
                    ),
                    friendMsgTypeFlag = 1,
                    isGetFrdRibbon = false,
                    isGetGrpRibbon = false,
                    msgNum = 20,
                    version = 1000
                )
            )
        }


        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): NewFriendRequestEvent? {
            readBytes().loadAs(Structmsg.RspSystemMsgNew.serializer()).run {
                val struct = friendmsgs?.firstOrNull()
                return if (struct == null) null else {
                    struct.msg?.run {
                        NewFriendRequestEvent(
                            bot,
                            struct.msgSeq,
                            msgAdditional,
                            struct.reqUin,
                            groupCode,
                            reqUinNick
                        )
                    }
                }
            }
        }

        internal object Action : OutgoingPacketFactory<Nothing?>("ProfileService.Pb.ReqSystemMsgAction.Friend") {

            operator fun invoke(
                client: QQAndroidClient,
                event: NewFriendRequestEvent,
                accept: Boolean,
                blackList: Boolean = false
            ) =
                buildOutgoingUniPacket(client) {
                    writeProtoBuf(
                        Structmsg.ReqSystemMsgAction.serializer(),
                        Structmsg.ReqSystemMsgAction(
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = if (accept) 2 else 3,
                                addFrdSNInfo = Structmsg.AddFrdSNInfo(),
                                msg = "",
                                remark = "",
                                blacklist = !accept && blackList
                            ),
                            msgSeq = event.eventId,
                            reqUin = event.fromId,
                            srcId = 6,
                            subSrcId = 7,
                            subType = 1
                        )
                    )
                }

            override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
        }
    }


    internal object SystemMsgNewGroup :
        OutgoingPacketFactory<Packet?>("ProfileService.Pb.ReqSystemMsgNew.Group") {

        operator fun invoke(client: QQAndroidClient) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Structmsg.ReqSystemMsgNew.serializer(),
                Structmsg.ReqSystemMsgNew(
                    checktype = 3,
                    flag = Structmsg.FlagInfo(
                        frdMsgDiscuss2ManyChat = 1,
                        frdMsgGetBusiCard = 0,
                        frdMsgNeedWaitingMsg = 1,
                        frdMsgUint32NeedAllUnreadMsg = 1,
                        grpMsgGetC2cInviteJoinGroup = 1,
                        grpMsgMaskInviteAutoJoin = 1,
                        grpMsgGetDisbandedByAdmin = 1,
                        grpMsgGetOfficialAccount = 1,
                        grpMsgGetPayInGroup = 1,
                        grpMsgGetQuitPayGroupMsgFlag = 1,
                        grpMsgGetTransferGroupMsgFlag = 1,
                        grpMsgHiddenGrp = 1,
                        grpMsgKickAdmin = 1,
                        grpMsgNeedAutoAdminWording = 1,
                        grpMsgNotAllowJoinGrpInviteNotFrd = 1,
                        grpMsgSupportInviteAutoJoin = 1,
                        grpMsgWordingDown = 1
                    ),
                    friendMsgTypeFlag = 1,
                    isGetFrdRibbon = false,
                    isGetGrpRibbon = false,
                    msgNum = 5,
                    version = 1000
                )
            )
        }


        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Packet? {
            readBytes().loadAs(Structmsg.RspSystemMsgNew.serializer()).run {
                val struct = groupmsgs?.firstOrNull()

                return if (struct == null) null else {
                    struct.msg?.run {
                        if (c2cInviteJoinGroupFlag == 1) {
                            // 被邀请入群
                            BotInvitedJoinGroupRequestEvent(
                                bot,
                                struct.msgSeq,
                                actionUin,
                                groupCode,
                                groupName,
                                actionUinNick
                            )
                        } else {
                            // 成员申请入群
                            MemberJoinRequestEvent(
                                bot,
                                struct.msgSeq,
                                msgAdditional,
                                struct.reqUin,
                                groupCode,
                                groupName,
                                reqUinNick
                            )
                        }
                    } as Packet // 没有 as Packet 垃圾 kotlin 会把类型推断为Any
                }
            }
        }

        internal object Action : OutgoingPacketFactory<Nothing?>("ProfileService.Pb.ReqSystemMsgAction.Group") {

            operator fun invoke(
                client: QQAndroidClient,
                event: MemberJoinRequestEvent,
                accept: Boolean?,
                blackList: Boolean = false
            ) =
                buildOutgoingUniPacket(client) {
                    writeProtoBuf(
                        Structmsg.ReqSystemMsgAction.serializer(),
                        Structmsg.ReqSystemMsgAction(
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = when (accept) {
                                    null -> 14 // ignore
                                    true -> 11 // accept
                                    false -> 12 // reject
                                },
                                groupCode = event.groupId,
                                msg = "",
                                remark = "",
                                blacklist = blackList
                            ),
                            groupMsgType = 1,
                            language = 1000,
                            msgSeq = event.eventId,
                            reqUin = event.fromId,
                            srcId = 3,
                            subSrcId = 31,
                            subType = 1
                        )
                    )
                }

            operator fun invoke(
                client: QQAndroidClient,
                event: BotInvitedJoinGroupRequestEvent,
                accept: Boolean
            ) =
                buildOutgoingUniPacket(client) {
                    writeProtoBuf(
                        Structmsg.ReqSystemMsgAction.serializer(),
                        Structmsg.ReqSystemMsgAction(
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = if (accept) 11 else 12,
                                groupCode = event.groupId
                            ),
                            groupMsgType = 2,
                            language = 1000,
                            msgSeq = event.eventId,
                            reqUin = event.invitorId,
                            srcId = 3,
                            subSrcId = 10016,
                            subType = 1
                        )
                    )
                }

            override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
        }
    }
}
