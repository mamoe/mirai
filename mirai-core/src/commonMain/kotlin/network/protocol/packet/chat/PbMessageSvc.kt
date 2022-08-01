/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.internal.network.protocol.packet.chat

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgRevokeUserDef
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgSvc
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.toLongUnsigned

internal class PbMessageSvc {
    object PbMsgWithDraw : OutgoingPacketFactory<PbMsgWithDraw.Response>(
        "PbMessageSvc.PbMsgWithDraw"
    ) {
        sealed class Response : Packet {
            object Success : Response() {
                override fun toString(): String {
                    return "PbMsgWithDraw.Success"
                }
            }

            data class Failed(
                val result: Int,
                val errorMessage: String
            ) : Response()
        }

        // 12 1A 08 01 10 00 18 E7 C1 AD B8 02 22 0A 08 BF BA 03 10 BF 81 CB B7 03 2A 02 08 00
        fun createForGroupMessage(
            client: QQAndroidClient,
            groupCode: Long,
            messageSequenceId: IntArray, // 56639
            messageRandom: IntArray, // 921878719
            messageType: Int = 0
        ): OutgoingPacketWithRespType<Response> {
            require(messageSequenceId.size == messageRandom.size)

            return buildOutgoingUniPacket(
                client,
                name = "PbMsgWithDraw(" +
                        "group=$groupCode, " +
                        "seq=${messageSequenceId.joinToString(separator = ",")}, " +
                        "rand=${messageRandom.joinToString(separator = ",")}" +
                        ")"
            ) {
                writeProtoBuf(
                    MsgSvc.PbMsgWithDrawReq.serializer(),
                    MsgSvc.PbMsgWithDrawReq(
                        groupWithDraw = listOf(
                            MsgSvc.PbGroupMsgWithDrawReq(
                                subCmd = 1,
                                groupType = 0, // 普通群
                                groupCode = groupCode,
                                msgList = messageSequenceId.zip(messageRandom).map { (seq, random) ->
                                    MsgSvc.PbGroupMsgWithDrawReq.MessageInfo(
                                        msgSeq = seq,
                                        msgRandom = random,
                                        msgType = messageType
                                    )
                                },
                                userdef = MsgRevokeUserDef.MsgInfoUserDef(
                                    longMessageFlag = 0
                                ).toByteArray(MsgRevokeUserDef.MsgInfoUserDef.serializer())
                            )
                        )
                    )
                )
            }
        }

        fun createForGroupTempMessage(
            client: QQAndroidClient,
            groupUin: Long,
            toUin: Long,
            messageSequenceId: IntArray, // 56639
            messageRandom: IntArray, // 921878719
            time: Int
        ): OutgoingPacketWithRespType<Response> {
            require(messageSequenceId.size == messageRandom.size)

            return buildOutgoingUniPacket(
                client,
                name = "PbMsgWithDraw(" +
                        "groupTemp=$toUin, " +
                        "seq=${messageSequenceId.joinToString(separator = ",")}, " +
                        "rand=${messageRandom.joinToString(separator = ",")}, " +
                        "time=${time}" +
                        ")"
            ) {
                writeProtoBuf(
                    MsgSvc.PbMsgWithDrawReq.serializer(),
                    MsgSvc.PbMsgWithDrawReq(
                        c2cWithDraw = listOf(
                            MsgSvc.PbC2CMsgWithDrawReq(
                                subCmd = 1,
                                longMessageFlag = 0,
                                msgInfo = messageSequenceId.zip(messageRandom).map { (seq, random) ->
                                    MsgSvc.PbC2CMsgWithDrawReq.MsgInfo(
                                        msgType = 0,
                                        fromUin = client.bot.id,
                                        toUin = toUin,
                                        msgSeq = seq,
                                        msgRandom = random,
                                        msgUid = 0x0100000000000000 or random.toLongUnsigned(),
                                        msgTime = time.toLongUnsigned(),
                                        routingHead = MsgSvc.RoutingHead(
                                            grpTmp = MsgSvc.GrpTmp(groupUin, toUin)
                                        ),
                                    )
                                },
                                reserved = RESERVED_TEMP
                            )
                        )
                    )
                )
            }
        }

        private val RESERVED_TEMP = "08 01 10 E3 E9 D6 80 02".hexToBytes()

        fun createForFriendMessage(
            client: QQAndroidClient,
            toUin: Long,
            messageSequenceId: IntArray, // 56639
            messageRandom: IntArray, // 921878719
            time: Int
        ): OutgoingPacketWithRespType<Response> {
            require(messageSequenceId.size == messageRandom.size)

            return buildOutgoingUniPacket(
                client,
                name = "PbMsgWithDraw(" +
                        "friend=$toUin, " +
                        "seq=${messageSequenceId.joinToString(separator = ",")}, " +
                        "rand=${messageRandom.joinToString(separator = ",")}, " +
                        "time=${time}" +
                        ")"
            ) {
                writeProtoBuf(
                    MsgSvc.PbMsgWithDrawReq.serializer(),
                    MsgSvc.PbMsgWithDrawReq(
                        c2cWithDraw = listOf(
                            MsgSvc.PbC2CMsgWithDrawReq(
                                subCmd = 1,
                                longMessageFlag = 0,
                                msgInfo = messageSequenceId.zip(messageRandom).map { (seq, random) ->
                                    MsgSvc.PbC2CMsgWithDrawReq.MsgInfo(
                                        msgType = 0,
                                        fromUin = client.bot.id,
                                        toUin = toUin,
                                        msgSeq = seq,
                                        msgRandom = random,
                                        msgUid = 0x0100000000000000 or random.toLongUnsigned(),
                                        msgTime = time.toLongUnsigned(),
                                        routingHead = MsgSvc.RoutingHead(
                                            c2c = MsgSvc.C2C(
                                                toUin = toUin
                                            )
                                        )
                                    )
                                },
                                reserved = MsgRevokeUserDef.UinTypeUserDef(
                                    0,
                                ).toByteArray(MsgRevokeUserDef.UinTypeUserDef.serializer())
                            )
                        )
                    )
                )
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(MsgSvc.PbMsgWithDrawResp.serializer())
            resp.groupWithDraw.firstOrNull()?.let {
                if (it.result != 0) {
                    return Response.Failed(it.result, it.errmsg)
                }
                return Response.Success
            }
            resp.c2cWithDraw.firstOrNull()?.let {
                if (it.result != 2 && it.result != 3) {
                    return Response.Failed(it.result, it.errmsg)
                }
                return Response.Success
            }
            return Response.Failed(-1, "No response")
        }
    }
}