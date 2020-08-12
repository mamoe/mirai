/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.login

import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.event.events.BotOfflineEvent
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.guid
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestMSFForceOffline
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.RspMSFForceOffline
import net.mamoe.mirai.qqandroid.network.protocol.data.jce.SvcReqRegister
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Oidb0x769
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.StatSvcGetOnline
import net.mamoe.mirai.qqandroid.network.protocol.packet.*
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.qqandroid.utils.NetworkType
import net.mamoe.mirai.qqandroid.utils.encodeToString
import net.mamoe.mirai.qqandroid.utils.io.serialization.*
import net.mamoe.mirai.qqandroid.utils.toReadPacket

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
                        sServantName = "PushService",
                        sFuncName = "SvcReqRegister",
                        iRequestId = 0,
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
                                uNewSSOIp = MiraiPlatformUtils.localIpAddress().runCatching { ipToLong() }
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
                                bytes_0x769_reqbody = ProtoBuf.dump(
                                    Oidb0x769.RequestBody.serializer(), Oidb0x769.RequestBody(
                                        rpt_config_list = listOf(
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

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            return Response
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

        override suspend fun QQAndroidBot.handle(packet: BotOfflineEvent.MsfOffline, sequenceId: Int): OutgoingPacket? {
            val cause = packet.cause
            check(cause is MsfOfflineToken) { "internal error: handling $packet in StatSvc.ReqMSFOffline" }
            return buildResponseUniPacket(client) {
                writeJceStruct(
                    RequestPacket.serializer(),
                    RequestPacket(
                        sServantName = "StatSvc",
                        sFuncName = "RspMSFForceOffline",
                        iRequestId = 0,
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
}
