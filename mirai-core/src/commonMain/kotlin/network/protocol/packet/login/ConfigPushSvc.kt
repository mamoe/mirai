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
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.components.BdhSessionSyncer
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.internal.network.components.ServerAddress
import net.mamoe.mirai.internal.network.components.ServerList
import net.mamoe.mirai.internal.network.context.BdhSession
import net.mamoe.mirai.internal.network.networkType
import net.mamoe.mirai.internal.network.protocol.data.jce.FileStoragePushFSSvcList
import net.mamoe.mirai.internal.network.protocol.data.jce.PushResp
import net.mamoe.mirai.internal.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.internal.network.protocol.data.jce.ServerListPush
import net.mamoe.mirai.internal.network.protocol.data.proto.Subcmd0x501
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.buildResponseUniPacket
import net.mamoe.mirai.internal.utils.NetworkType
import net.mamoe.mirai.internal.utils.io.serialization.jceRequestSBuffer
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.readUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.writeJceStruct
import net.mamoe.mirai.utils.info
import net.mamoe.mirai.utils.toUHexString
import net.mamoe.mirai.internal.network.protocol.data.jce.PushReq as PushReqJceStruct


internal class ConfigPushSvc {
    object PushReq : IncomingPacketFactory<PushReq.PushReqResponse>(
        receivingCommandName = "ConfigPushSvc.PushReq",
        responseCommandName = "ConfigPushSvc.PushResp"
    ) {
        override val canBeCached: Boolean get() = false

        sealed class PushReqResponse(
            val struct: PushReqJceStruct,
            val bot: QQAndroidBot,
        ) : Packet, Event, AbstractEvent(), Packet.NoEventLog {
            class Unknown(struct: PushReqJceStruct, bot: QQAndroidBot) : PushReqResponse(struct, bot) {
                override fun toString(): String {
                    return "ConfigPushSvc.PushReq.PushReqResponse.Unknown"
                }
            }

            class LogAction(struct: PushReqJceStruct, bot: QQAndroidBot) : PushReqResponse(struct, bot) {
                override fun toString(): String {
                    return "ConfigPushSvc.PushReq.PushReqResponse.LogAction"
                }
            }

            class ServerListPush(struct: PushReqJceStruct, bot: QQAndroidBot) : PushReqResponse(struct, bot) {
                override fun toString(): String {
                    return "ConfigPushSvc.PushReq.PushReqResponse.ServerListPush"
                }
            }

            class ConfigPush(struct: PushReqJceStruct, bot: QQAndroidBot) : PushReqResponse(struct, bot) {
                override fun toString(): String {
                    return "ConfigPushSvc.PushReq.PushReqResponse.ConfigPush"
                }
            }


        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): PushReqResponse {
            val pushReq = readUniPacket(PushReqJceStruct.serializer(), "PushReq")
            return when (pushReq.type) {
                1 -> PushReqResponse.ServerListPush(pushReq, bot)
                2 -> PushReqResponse.ConfigPush(pushReq, bot)
                3 -> PushReqResponse.LogAction(pushReq, bot)
                else -> PushReqResponse.Unknown(pushReq, bot)
            }
        }

        override suspend fun QQAndroidBot.handle(packet: PushReqResponse, sequenceId: Int): OutgoingPacket? {
            val bdhSyncer = bot.components[BdhSessionSyncer]

            fun handleConfigPush(packet: PushReqResponse.ConfigPush) {
                val pushReq = packet.struct

                // FS server
                val fileStoragePushFSSvcList = pushReq.jcebuf.loadAs(FileStoragePushFSSvcList.serializer())
                bot.client.fileStoragePushFSSvcList = fileStoragePushFSSvcList


                val bigDataChannel = fileStoragePushFSSvcList.bigDataChannel
                if (bigDataChannel?.vBigdataPbBuf == null) {
                    bdhSyncer.bdhSession.completeExceptionally(IllegalStateException("BdhSession not received."))
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
                        bdhSyncer.overrideSession(it)
                    },
                    onFailure = { cause ->
                        val e = IllegalStateException("Failed to decode BdhSession", cause)
                        bdhSyncer.bdhSession.completeExceptionally(e)
                        logger.error(e)
                    }
                )
            }

            fun handleServerListPush(resp: PushReqResponse.ServerListPush) {
                val serverListPush = kotlin.runCatching {
                    resp.struct.jcebuf.loadAs(ServerListPush.serializer())
                }.getOrElse {
                    throw contextualBugReportException(
                        "ConfigPush.ReqPush type=1",
                        forDebug = resp.struct.jcebuf.toUHexString(),
                    )
                }
                val pushServerList = if (client.networkType == NetworkType.WIFI) {
                    serverListPush.wifiSSOServerList
                } else {
                    serverListPush.mobileSSOServerList
                }

                if (pushServerList.isNotEmpty()) {
                    bot.components[ServerList].setPreferred(
                        pushServerList.shuffled().map { ServerAddress(it.host, it.port) })
                }
                bdhSyncer.saveToCache()
                bdhSyncer.saveServerListToCache()
                if (serverListPush.reconnectNeeded == 1) {
                    bot.logger.info { "Server request to change server." }
                    bot.components[EventDispatcher].broadcastAsync(
                        BotOfflineEvent.RequireReconnect(bot, IllegalStateException("Server request to change server."))
                    )
                }
            }

            when (packet) {
                is PushReqResponse.ConfigPush -> {
                    handleConfigPush(packet)
                }
                is PushReqResponse.ServerListPush -> {
                    handleServerListPush(packet)
                }
                is PushReqResponse.LogAction, is PushReqResponse.Unknown -> {
                    //ignore
                }
            }
            //Always send resp
            if (!client.wLoginSigInfoInitialized) return null // concurrently doing reconnection
            return buildResponseUniPacket(
                client,
                sequenceId = sequenceId,
                key = client.wLoginSigInfo.d2Key
            ) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        requestId = client.nextRequestPacketRequestId(),
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
    }
}
