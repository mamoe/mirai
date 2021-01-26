/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.BdhSession
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.data.jce.FileStoragePushFSSvcList
import net.mamoe.mirai.internal.network.protocol.data.jce.PushResp
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.network.protocol.data.proto.Subcmd0x501
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.writeJceStruct
import net.mamoe.mirai.utils.toUHexString
import java.lang.IllegalStateException
import net.mamoe.mirai.internal.network.protocol.data.jce.PushReq as PushReqJceStruct


internal class ConfigPushSvc {
    object PushReq : IncomingPacketFactory<PushReq.PushReqResponse>(
        receivingCommandName = "ConfigPushSvc.PushReq",
        responseCommandName = "ConfigPushSvc.PushResp"
    ) {
        override val canBeCached: Boolean get() = false

        sealed class PushReqResponse : Packet, Event, AbstractEvent(), Packet.NoEventLog {
            class Success(
                val struct: PushReqJceStruct
            ) : PushReqResponse() {
                override fun toString(): String {
                    return "ConfigPushSvc.PushReq.PushReqResponse.Success"
                }
            }

            @Serializable
            data class ChangeServer(
                @ProtoNumber(1) val serverList: List<ServerInfo>
            ) : ProtoBuf, PushReqResponse() {

                @Serializable
                data class ServerInfo(
                    @ProtoNumber(1) val host: String,
                    @ProtoNumber(2) val port: Int,
                    @ProtoNumber(8) val unknown: String
                ) : ProtoBuf {
                    override fun toString(): String {
                        return "$host:$port"
                    }
                }
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): PushReqResponse {
            val pushReq = readUniPacket(PushReqJceStruct.serializer(), "PushReq")
            return PushReqResponse.Success(pushReq)
        }

        override suspend fun QQAndroidBot.handle(packet: PushReqResponse, sequenceId: Int): OutgoingPacket? {
            fun handleSuccess(packet: PushReqResponse.Success) {
                val pushReq = packet.struct
                when (pushReq.type) {
                    1 -> {
                        // change sso server
                        throw contextualBugReportException(
                            "ConfigPush.ReqPush type=1",
                            forDebug = pushReq.jcebuf.toUHexString(),
                        )
                    }

                    2 -> {
                        // FS server
                        val fileStoragePushFSSvcList = pushReq.jcebuf.loadAs(FileStoragePushFSSvcList.serializer())
                        bot.client.fileStoragePushFSSvcList = fileStoragePushFSSvcList

                        val bigDataChannel = fileStoragePushFSSvcList.bigDataChannel
                        if (bigDataChannel?.vBigdataPbBuf == null) {
                            client.bdhSession.completeExceptionally(IllegalStateException("BdhSession not received."))
                            return
                        }

                        kotlin.runCatching {
                            val resp =
                                bigDataChannel.vBigdataPbBuf.loadAs(Subcmd0x501.RspBody.serializer()).msgSubcmd0x501RspBody
                                    ?: error("msgSubcmd0x501RspBody not found")

                            val session = BdhSession(
                                sigSession = resp.httpconnSigSession,
                                sessionKey = resp.sessionKey
                            )

                            for ((type, addresses) in resp.msgHttpconnAddrs) {
                                when (type) {
                                    10 -> session.ssoAddresses.addAll(addresses.map { it.decode() })
                                    21 -> session.otherAddresses.addAll(addresses.map { it.decode() })
                                }
                            }

                            session
                        }.fold(
                            onSuccess = {
                                client.bdhSession.complete(it)
                            },
                            onFailure = { cause ->
                                val e = IllegalStateException("Failed to decode BdhSession", cause)
                                client.bdhSession.completeExceptionally(e)
                                logger.error(e)
                            }
                        )
                    }
                }
            }

            when (packet) {
                is PushReqResponse.Success -> {
                    handleSuccess(packet)
                    return buildResponseUniPacket(
                        client,
                        sequenceId = sequenceId
                    ) {
                        writeJceStruct(
                            RequestPacket.serializer(),
                            RequestPacket(
                                requestId = 0,
                                version = 3,
                                servantName = "QQService.ConfigPushSvc.MainServant",
                                funcName = "PushResp",
                                sBuffer = jceRequestSBuffer(
                                    "PushResp",
                                    PushResp.serializer(),
                                    PushResp(
                                        type = packet.struct.type,
                                        seq = packet.struct.seq,
                                        jcebuf = if (packet.struct.type == 3) packet.struct.jcebuf else null
                                    )
                                )
                            )
                        )
                        // writePacket(this.build().debugPrintThis())
                    }
                }
                else -> {
                    // handled in QQABot
                    return null
                }
            }
        }
    }
}