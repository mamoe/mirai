/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER")

package net.mamoe.mirai.internal.network.protocol.packet.chat

import io.ktor.utils.io.core.*
import net.mamoe.mirai.data.RequestEventData
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.components.NoticeProcessorPipeline.Companion.processPacketThroughPipeline
import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.network.toPacket
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.TypeKey
import net.mamoe.mirai.utils.buildTypeSafeMap

internal class NewContact {

    internal object SystemMsgNewFriend :
        OutgoingPacketFactory<SystemMsgNewFriend.Response>("ProfileService.Pb.ReqSystemMsgNew.Friend") {

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
                        grpMsgMaskInviteAutoJoin = 1,
                    ),
                    friendMsgTypeFlag = 1,
                    isGetFrdRibbon = false,
                    isGetGrpRibbon = false,
                    msgNum = 20,
                    version = 1000,
                    reqMsgType = 2,
                ),
            )
        }

        internal class Response(val list: List<RequestEventData.NewFriendRequest>) : Packet

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(Structmsg.RspSystemMsgNew.serializer())

            return resp.friendmsgs.map { struct ->
                bot.processPacketThroughPipeline(struct, buildTypeSafeMap { set(SYSTEM_MSG_TYPE, 0) })

                val systemMsg = struct.msg ?: return@map null
                if (systemMsg.actions.size < 2) return@map null // 只返回可以操作的（同意或拒绝好友请求）

                RequestEventData.NewFriendRequest(
                    struct.msgSeq,
                    struct.reqUin,
                    systemMsg.reqUinNick,
                    systemMsg.groupCode,
                    systemMsg.msgAdditional,
                )
            }.filterNotNull().let { Response(it) }
        }

        internal object Action : OutgoingPacketFactory<Nothing?>("ProfileService.Pb.ReqSystemMsgAction.Friend") {

            operator fun invoke(
                client: QQAndroidClient,
                eventId: Long,
                fromId: Long,
                accept: Boolean,
                blackList: Boolean = false,
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
                                blacklist = !accept && blackList,
                            ),
                            msgSeq = eventId,
                            reqUin = fromId,
                            srcId = 6,
                            subSrcId = 7,
                            subType = 1,
                        ),
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
                        grpMsgWordingDown = 1,
                    ),
                    friendMsgTypeFlag = 1,
                    isGetFrdRibbon = false,
                    isGetGrpRibbon = false,
                    msgNum = 5,
                    version = 1000,
                ),
            )
        }


        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Packet {
            val resp = readBytes().loadAs(Structmsg.RspSystemMsgNew.serializer())

            return resp.groupmsgs.map { struct ->
                bot.processPacketThroughPipeline(struct, buildTypeSafeMap { set(SYSTEM_MSG_TYPE, 1) })
            }.toPacket()
        }

        internal object Action : OutgoingPacketFactory<Nothing?>("ProfileService.Pb.ReqSystemMsgAction.Group") {

            operator fun invoke(
                client: QQAndroidClient,
                eventId: Long,
                fromId: Long,
                groupId: Long,
                isInvited: Boolean,
                accept: Boolean?,
                blackList: Boolean = false,
                message: String = "",
            ) = buildOutgoingUniPacket(client) {
                    writeProtoBuf(
                        Structmsg.ReqSystemMsgAction.serializer(),
                        Structmsg.ReqSystemMsgAction(
                            actionInfo = Structmsg.SystemMsgActionInfo(
                                type = when (accept) {
                                    null -> 14 // ignore
                                    true -> 11 // accept
                                    false -> 12 // reject
                                },
                                groupCode = groupId,
                                msg = message,
                                remark = "",
                                blacklist = blackList,
                            ),
                            groupMsgType = if (isInvited) 2 else 1,
                            language = 1000,
                            msgSeq = eventId,
                            reqUin = fromId,
                            srcId = 3,
                            subSrcId = if (isInvited) 10016 else 31,
                            subType = 1,
                        ),
                    )
                }

            override suspend fun ByteReadPacket.decode(bot: QQAndroidBot) = null
        }
    }

    internal companion object {
        /**
         * friend = 0, group = 1
         */
        internal val SYSTEM_MSG_TYPE = TypeKey<Int>("SystemMsgType")
    }
}
