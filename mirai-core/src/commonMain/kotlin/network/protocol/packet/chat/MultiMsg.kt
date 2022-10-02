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
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.components.PacketCodec
import net.mamoe.mirai.internal.network.protocol.data.proto.MultiMsg
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.structureToString

internal class MessageValidationData(
    val data: ByteArray,
    val md5: ByteArray = data.md5()
) {
    override fun toString(): String {
        return "MessageValidationData(data=<size=${data.size}>, md5=${md5.contentToString()})"
    }
}

internal class MultiMsg {

    object ApplyUp : OutgoingPacketFactory<ApplyUp.Response>("MultiMsg.ApplyUp") {
        sealed class Response : Packet {
            data class RequireUpload(
                val proto: MultiMsg.MultiMsgApplyUpRsp
            ) : Response() {
                override fun toString(): String {
                    if (PacketCodec.PacketLogger.isEnabled) {
                        return structureToString()
                    }
                    return "MultiMsg.ApplyUp.Response.RequireUpload"
                }
            }

            object MessageTooLarge : Response()
        }

        // captured from group
        fun createForGroup(
            buType: Int,
            client: QQAndroidClient,
            messageData: MessageValidationData,
            dstUin: Long // group uin
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                MultiMsg.ReqBody.serializer(),
                MultiMsg.ReqBody(
                    buType = buType, // 1: long, 2: 合并转发
                    buildVer = "8.2.0.1296",
                    multimsgApplyupReq = listOf(
                        MultiMsg.MultiMsgApplyUpReq(
                            applyId = 0,
                            dstUin = dstUin,
                            msgMd5 = messageData.md5,
                            msgSize = messageData.data.size.toLong(),
                            msgType = 3 // TODO 3 for group?
                        )
                    ),
                    netType = 3, // wifi=3, wap=5
                    platformType = 9,
                    subcmd = 1,
                    termType = 5,
                    reqChannelType = 0
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val body = readProtoBuf(MultiMsg.RspBody.serializer())
            val response = body.multimsgApplyupRsp.first()
            return when (response.result) {
                0 -> Response.RequireUpload(response)
                193 -> Response.MessageTooLarge
                //1 -> Response.OK(resId = response.msgResid)
                else -> {
                    error(kotlin.run {
                        println(response.structureToString())
                    }.let { "Protocol error: MultiMsg.ApplyUp failed with result ${response.result}" })
                }
            }
        }
    }

    object ApplyDown : OutgoingPacketFactory<ApplyDown.Response>("MultiMsg.ApplyDown") {
        sealed class Response : Packet {
            class RequireDownload(
                val origin: MultiMsg.MultiMsgApplyDownRsp
            ) : Response() {
                override fun toString(): String = "MultiMsg.ApplyDown.Response"
            }

            object MessageTooLarge : Response()
        }

        operator fun invoke(
            client: QQAndroidClient,
            buType: Int,
            resId: String,
            msgType: Int,
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                MultiMsg.ReqBody.serializer(),
                MultiMsg.ReqBody(
                    buType = buType, // 1: long, 2: 合并转发
                    buildVer = "8.2.0.1296",
                    multimsgApplydownReq = listOf(
                        MultiMsg.MultiMsgApplyDownReq(
                            msgResid = resId,
                            msgType = msgType,
                        )
                    ),
                    netType = 3, // wifi=3, wap=5
                    platformType = 9,
                    subcmd = 2,
                    termType = 5,
                    reqChannelType = 2
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val body = readProtoBuf(MultiMsg.RspBody.serializer())
            val response = body.multimsgApplydownRsp.first()
            return when (response.result) {
                0 -> Response.RequireDownload(response)
                193 -> Response.MessageTooLarge
                //1 -> Response.OK(resId = response.msgResid)
                else -> throw contextualBugReportException(
                    "MultiMsg.ApplyDown",
                    response.structureToString(),
                    additional = "Decode failure result=${response.result}"
                )
            }
        }
    }
}