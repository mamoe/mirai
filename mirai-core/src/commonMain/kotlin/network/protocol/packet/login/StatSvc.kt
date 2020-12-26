/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.withLock
import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.contact.appId
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.event.events.OtherClientOfflineEvent
import net.mamoe.mirai.event.events.OtherClientOnlineEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.createOtherClient
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.getRandomByteArray
import net.mamoe.mirai.internal.network.guid
import net.mamoe.mirai.internal.network.protocol.data.jce.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x769
import net.mamoe.mirai.internal.network.protocol.data.proto.StatSvcGetOnline
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.utils.NetworkType
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.internal.utils.io.serialization.*
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.mirai.utils.encodeToString
import net.mamoe.mirai.utils.localIpAddress
import net.mamoe.mirai.utils.toReadPacket

@Suppress("EnumEntryName", "unused")
internal enum class RegPushReason {
    appRegister,
    createDefaultRegInfo,
    fillRegProxy,
    msfBoot,
    msfByNetChange,
    msfHeartTimeTooLong,
    serverPush,
    setOnlineStatus,
    unknown
}

internal class StatSvc {
    internal object GetOnlineStatus : OutgoingPacketFactory<GetOnlineStatus.Response>("StatSvc.GetOnlineStatus") {

        internal sealed class Response : Packet {
            override fun toString(): String = "StatSvc.GetOnlineStatus.Response"

            object Success : Response() {
                override fun toString(): String {
                    return "StatSvc.GetOnlineStatus.Response.Success"
                }
            }

            class Failed(val errno: Int, val message: String) : Response() {
                override fun toString(): String {
                    return "StatSvc.GetOnlineStatus.Response.Failed(errno=$errno, message=$message)"
                }
            }
        }

        operator fun invoke(
            client: QQAndroidClient
        ): OutgoingPacket = buildLoginOutgoingPacket(client, 1) {
            writeProtoBuf(
                StatSvcGetOnline.ReqBody.serializer(), StatSvcGetOnline.ReqBody(
                    uin = client.uin,
                    appid = 0
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(StatSvcGetOnline.RspBody.serializer())
            return if (resp.errorCode != 0) {
                Response.Failed(resp.errorCode, resp.errorMsg)
            } else {
                Response.Success
            }
        }
    }

    internal object Register : OutgoingPacketFactory<Register.Response>("StatSvc.register") {

        internal object Response : Packet {
            override fun toString(): String = "Response(StatSvc.register)"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val packet = readUniPacket(SvcRespRegister.serializer())
            if (packet.updateFlag.toInt() == 1) {
                //TODO 加载好友列表
            }
            if (packet.largeSeqUpdate.toInt() == 1) {
                //TODO 刷新好友列表
            }
            packet.iHelloInterval.let {
                bot.configuration.heartbeatPeriodMillis = it.times(1000).toLong()
            }

            return Response
        }

        operator fun invoke(
            client: QQAndroidClient,
            regPushReason: RegPushReason = RegPushReason.appRegister
        ): OutgoingPacket = buildLoginOutgoingPacket(
            client,
            bodyType = 1,
            extraData = client.wLoginSigInfo.d2.data,
            key = client.wLoginSigInfo.d2Key
        ) { sequenceId ->
            writeSsoPacket(
                client, subAppId = client.subAppId, commandName = commandName,
                extraData = client.wLoginSigInfo.tgt.toReadPacket(), sequenceId = sequenceId
            ) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        servantName = "PushService",
                        funcName = "SvcReqRegister",
                        requestId = 0,
                        sBuffer = jceRequestSBuffer(
                            "SvcReqRegister",
                            SvcReqRegister.serializer(),
                            SvcReqRegister(
                                cConnType = 0,
                                lBid = 1 or 2 or 4,
                                lUin = client.uin,
                                iStatus = client.onlineStatus.id,
                                bKikPC = 0, // 是否把 PC 踢下线
                                bKikWeak = 0,
                                timeStamp = 0,
                                // timeStamp = currentTimeSeconds // millis or seconds??
                                iLargeSeq = 1551, // ?
                                bOpenPush = 1,
                                iLocaleID = 2052,
                                bRegType =
                                (if (regPushReason == RegPushReason.appRegister ||
                                    regPushReason == RegPushReason.fillRegProxy ||
                                    regPushReason == RegPushReason.createDefaultRegInfo ||
                                    regPushReason == RegPushReason.setOnlineStatus
                                ) 0 else 1).toByte(),
                                bIsSetStatus = if (regPushReason == RegPushReason.setOnlineStatus) 1 else 0,
                                iOSVersion = client.device.version.sdk.toLong(),
                                cNetType = if (client.networkType == NetworkType.WIFI) 1 else 0,
                                vecGuid = client.device.guid,
                                strDevName = client.device.model.encodeToString(),
                                strDevType = client.device.model.encodeToString(),
                                strOSVer = client.device.version.release.encodeToString(),
                                uOldSSOIp = 0,
                                uNewSSOIp = localIpAddress().runCatching { ipToLong() }
                                    .getOrElse { "192.168.1.123".ipToLong() },
                                strVendorName = "MIUI",
                                strVendorOSName = "?ONEPLUS A5000_23_17",
                                // register 时还需要
                                /*
                                var44.uNewSSOIp = field_127445;
                                var44.uOldSSOIp = field_127444;
                                var44.strVendorName = ROMUtil.getRomName();
                                var44.strVendorOSName = ROMUtil.getRomVersion(20);
                                */
                                bytes_0x769_reqbody = ProtoBuf.encodeToByteArray(
                                    Oidb0x769.ReqBody.serializer(), Oidb0x769.ReqBody(
                                        configList = listOf(
                                            Oidb0x769.ConfigSeq(
                                                type = 46,
                                                version = 0
                                            ),
                                            Oidb0x769.ConfigSeq(
                                                type = 283,
                                                version = 0
                                            )
                                        )
                                    )
                                ),
                                bSetMute = 0
                            )
                        )
                    )
                )
            }
        }

        private fun String.ipToLong(): Long {
            return split('.').foldIndexed(0L) { index: Int, acc: Long, s: String ->
                acc or (((s.toLongOrNull() ?: 0) shl (index * 16)))
            }
        }
    }

    internal object ReqMSFOffline :
        IncomingPacketFactory<BotOfflineEvent.MsfOffline>("StatSvc.ReqMSFOffline", "StatSvc.RspMSFForceOffline") {

        internal data class MsfOfflineToken(
            val uin: Long,
            val seq: Long,
            val const: Byte
        ) : Packet, RuntimeException("dropped by StatSvc.ReqMSFOffline")

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): BotOfflineEvent.MsfOffline {
            val decodeUniPacket = readUniPacket(RequestMSFForceOffline.serializer())
            @Suppress("INVISIBLE_MEMBER")
            return BotOfflineEvent.MsfOffline(bot, MsfOfflineToken(decodeUniPacket.uin, decodeUniPacket.iSeqno, 0))
        }

        override suspend fun QQAndroidBot.handle(packet: BotOfflineEvent.MsfOffline, sequenceId: Int): OutgoingPacket {
            val cause = packet.cause
            check(cause is MsfOfflineToken) { "internal error: handling $packet in StatSvc.ReqMSFOffline" }
            return buildResponseUniPacket(client) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        servantName = "StatSvc",
                        funcName = "RspMSFForceOffline",
                        requestId = 0,
                        sBuffer = jceRequestSBuffer(
                            "RspMSFForceOffline",
                            RspMSFForceOffline.serializer(),
                            RspMSFForceOffline(
                                cause.uin,
                                cause.seq,
                                cause.const
                            )
                        )
                    )
                )
            }
        }
    }

    internal object SvcReqMSFLoginNotify :
        IncomingPacketFactory<Packet?>("StatSvc.SvcReqMSFLoginNotify", "StatSvc.SvcReqMSFLoginNotify") {

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? =
            bot.otherClientsLock.withLock {
                val notify = readUniPacket(SvcReqMSFLoginNotifyData.serializer())

                val appId = notify.iAppId.toInt()

                when (notify.status.toInt()) {
                    1 -> { // online
                        if (bot.otherClients.any { it.appId == appId }) return null

                        val info = Mirai.getOnlineOtherClientsList(bot).find { it.appId == appId }
                            ?: throw  contextualBugReportException(
                                "SvcReqMSFLoginNotify (OtherClient online)",
                                notify._miraiContentToString(),
                                additional = "Failed to find corresponding instanceInfo."
                            )

                        val client = bot.createOtherClient(info)
                        bot.otherClients.delegate.add(client)
                        OtherClientOnlineEvent(
                            client,
                            ClientKind[notify.iClientType?.toInt() ?: 0]
                        )
                    }

                    2 -> { // off
                        val client = bot.otherClients.find { it.appId == appId } ?: return null
                        client.cancel(CancellationException("Offline"))
                        bot.otherClients.delegate.remove(client)
                        OtherClientOfflineEvent(client)
                    }

                    else -> throw contextualBugReportException(
                        "decode SvcReqMSFLoginNotify (OtherClient status change)",
                        notify._miraiContentToString(),
                        additional = "unknown notify.status=${notify.status}"
                    )
                }
            }
    }

    internal object GetDevLoginInfo : OutgoingPacketFactory<GetDevLoginInfo.Response>("StatSvc.GetDevLoginInfo") {

        @Suppress("unused") // false positive
        data class Response(
            val deviceList: List<SvcDevLoginInfo>,
        ) : Packet {
            override fun toString(): String {
                return "StatSvc.GetDevLoginInfo.Response(deviceList.size=${deviceList.size})"
            }
        }

        operator fun invoke(
            client: QQAndroidClient,
        ) = buildOutgoingUniPacket(client) {
            writeJceRequestPacket(
                servantName = "StatSvc",
                funcName = "SvcReqGetDevLoginInfo",
                serializer = SvcReqGetDevLoginInfo.serializer(),
                body = SvcReqGetDevLoginInfo(
                    iLoginType = 2,
                    iRequireMax = 20,
                    iTimeStamp = currentTimeMillis(),
                    iGetDevListType = 1,
                    vecGuid = getRandomByteArray(16), // 服务器防止频繁查询
                    iNextItemIndex = 0,
                    appName = client.protocol.apkId //"com.tencent.mobileqq"
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readUniPacket(SvcRspGetDevLoginInfo.serializer())

            // result 62 maybe too frequent
            return Response(
                resp.vecCurrentLoginDevInfo?.takeIf { it.isNotEmpty() }
                    ?: resp.vecAuthLoginDevInfo?.takeIf { it.isNotEmpty() }
                    ?: resp.vecHistoryLoginDevInfo.orEmpty()
            )
        }
    }
}
