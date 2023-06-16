/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.login

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.ClientKind
import net.mamoe.mirai.contact.OtherClientInfo
import net.mamoe.mirai.contact.Platform
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.event.events.OtherClientOfflineEvent
import net.mamoe.mirai.event.events.OtherClientOnlineEvent
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.appId
import net.mamoe.mirai.internal.contact.createOtherClient
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.components.ClockHolder
import net.mamoe.mirai.internal.network.components.ContactCacheService
import net.mamoe.mirai.internal.network.components.ContactUpdater
import net.mamoe.mirai.internal.network.components.ServerList
import net.mamoe.mirai.internal.network.getRandomByteArray
import net.mamoe.mirai.internal.network.handler.logger
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.impl.HeartbeatFailedException
import net.mamoe.mirai.internal.network.protocol.data.jce.*
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x769
import net.mamoe.mirai.internal.network.protocol.data.proto.StatSvcGetOnline
import net.mamoe.mirai.internal.network.protocol.data.proto.StatSvcSimpleGet
import net.mamoe.mirai.internal.network.protocol.packet.*
import net.mamoe.mirai.internal.utils.NetworkType
import net.mamoe.mirai.internal.utils.io.serialization.*
import net.mamoe.mirai.utils.*

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
        ) = buildLoginOutgoingPacket(client, PacketEncryptType.D2) {
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

    internal object SimpleGet : OutgoingPacketFactory<SimpleGet.Response>("StatSvc.SimpleGet") {
        internal sealed interface Response : Packet {
            object Success : Response {
                override fun toString(): String = "SimpleGet.Response.Success"
            }

            class Error(val code: Int, val msg: String) : Response {
                override fun toString(): String = "SimpleGet.Response.Error(code=$code,msg=$msg)"
            }
        }

        operator fun invoke(
            client: QQAndroidClient
        ) = buildLoginOutgoingPacket(
            client,
            encryptMethod = PacketEncryptType.D2
        ) {
            writeSsoPacket(client, client.subAppId, commandName, sequenceId = it) {

            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            readProtoBuf(StatSvcSimpleGet.RspBody.serializer()).let {
                return if (it.errorCode == 0) {
                    Response.Success
                } else {
                    Response.Error(it.errorCode, it.errmsg)
                }
            }
        }

        override suspend fun QQAndroidBot.handle(packet: Response) {
            if (packet is Response.Error) {
                network.close(HeartbeatFailedException("StatSvc.SimpleGet", IllegalStateException(packet.toString())))
            }
        }
    }

    internal object Register : OutgoingPacketFactory<Register.Response>("StatSvc.register") {

        internal class Response(
            val origin: SvcRespRegister
        ) : Packet {
            override fun toString(): String = "Response(StatSvc.register)"
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val packet = readUniPacket(SvcRespRegister.serializer())
            return Response(packet)
        }

        override suspend fun QQAndroidBot.handle(packet: Response) {
            packet.origin.iHelloInterval.let {
                bot.configuration.statHeartbeatPeriodMillis = it.times(1000).toLong()
            }

            val serverTime = packet.origin.serverTime
            val diffMillis = if (serverTime == 0L) 0 else serverTime - currentTimeSeconds()
            bot.components[ClockHolder].server = Clock.SystemDefault.adjusted(diffMillis)
            bot.network.logger.info { "Server time updated, serverTime: $serverTime, diff: ${diffMillis}ms=${diffMillis.millisToHumanReadableString()}" }
        }

        fun online(
            client: QQAndroidClient,
            regPushReason: RegPushReason = RegPushReason.appRegister
        ) = impl("online", client, 1L or 2 or 4, client.onlineStatus, regPushReason) {
            if (client.bot.configuration.protocol == BotConfiguration.MiraiProtocol.ANDROID_PHONE) {
                client.bot.components[ServerList].run {
                    kotlin.runCatching {
                        uOldSSOIp = lastDisconnectedIP.toIpV4Long()
                        uNewSSOIp = lastConnectedIP.toIpV4Long()
                    }.onFailure { err ->
                        client.bot.network.logger.warning({
                            "Exception when converting ipaddress to long: ld=${lastDisconnectedIP}, lc=${lastConnectedIP}"
                        }, err)
                        uNewSSOIp = 0
                        uOldSSOIp = 0
                    }
                }
            } else {
                uOldSSOIp = 0
                uNewSSOIp = 0
            }
            client.bot.components[ContactCacheService].friendListCache?.let { friendListCache ->
                iLargeSeq = friendListCache.friendListSeq
            }
            //  timeStamp = friendListCache.timeStamp
            strVendorName = "MIUI"
            strVendorOSName = "?ONEPLUS A5000_23_17"
        }

        fun offline(
            client: QQAndroidClient,
            regPushReason: RegPushReason = RegPushReason.appRegister
        ) = impl("offline", client, 1L or 2 or 4, OnlineStatus.OFFLINE, regPushReason)

        private fun impl(
            name: String,
            client: QQAndroidClient,
            bid: Long,
            status: OnlineStatus,
            regPushReason: RegPushReason = RegPushReason.appRegister,
            applyAction: SvcReqRegister.() -> Unit = {}
        ) = buildLoginOutgoingPacket(
            client,
            encryptMethod = PacketEncryptType.D2,
            extraData = client.wLoginSigInfo.d2.data,
            key = client.wLoginSigInfo.d2Key,
            remark = name,
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
                                lBid = bid,
                                lUin = client.uin,
                                iStatus = status.id,
                                bKikPC = 0, // 是否把 PC 踢下线
                                bKikWeak = 0,
                                timeStamp = 0,
                                // timeStamp = currentTimeSeconds // millis or seconds??
                                iLargeSeq = 0, // ?
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
                                strDevName = client.device.model.decodeToString(),
                                strDevType = client.device.model.decodeToString(),
                                strOSVer = client.device.version.release.decodeToString(),
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
                                                version = 1610538309
                                            ),
                                            Oidb0x769.ConfigSeq(
                                                type = 283,
                                                version = 0
                                            )
                                        )
                                    )
                                ),
                                bSetMute = 0
                            ).apply(applyAction)
                        )
                    )
                )
            }
        }


    }

    internal object ReqMSFOffline :
        IncomingPacketFactory<ReqMSFOffline.MsfOfflinePacket>("StatSvc.ReqMSFOffline", "StatSvc.RspMSFForceOffline") {

        internal class MsfOfflinePacket(
            val token: MsfOfflineToken,
        ) : Packet {
            override fun toString(): String = "StatSvc.ReqMSFOffline"
        }

        internal data class MsfOfflineToken(
            val uin: Long,
            val seq: Long,
            val const: Byte
        ) : Packet, NetworkException("dropped by StatSvc.ReqMSFOffline", true)

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): MsfOfflinePacket {
            val decodeUniPacket = readUniPacket(RequestMSFForceOffline.serializer())
            @Suppress("INVISIBLE_MEMBER")
            return MsfOfflinePacket(MsfOfflineToken(decodeUniPacket.uin, decodeUniPacket.iSeqno, 0))
        }

        override suspend fun QQAndroidBot.handle(packet: MsfOfflinePacket, sequenceId: Int): OutgoingPacket? {
            val cause = packet.token
            val resp = buildResponseUniPacket(client) {
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
            kotlin.runCatching {
                bot.network.sendWithoutExpect(resp)
            }
            bot.network.close(cause)
            return null
        }
    }

    internal object SvcReqMSFLoginNotify :
        IncomingPacketFactory<Packet?>("StatSvc.SvcReqMSFLoginNotify", "StatSvc.SvcReqMSFLoginNotify") {

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? =
            bot.components[ContactUpdater].otherClientsLock.withLock {
                val notify = readUniPacket(SvcReqMSFLoginNotifyData.serializer())

                val appId = notify.iAppId.toInt()

                when (notify.status.toInt()) {
                    1 -> { // online
                        if (bot.otherClients.any { it.appId == appId }) return null

                        val info = Mirai.getOnlineOtherClientsList(bot).find { it.appId == appId }
                            ?: kotlin.run {
                                delay(2000) // sometimes server sync slow
                                Mirai.getOnlineOtherClientsList(bot).find { it.appId == appId }
                            } ?: kotlin.run {
                                // 你的帐号在平板电脑上登录了
                                val kind = notify.info?.substringAfter("在")?.substringBefore("上").orEmpty()
                                OtherClientInfo(
                                    appId,
                                    Platform.MOBILE,
                                    deviceName = kind,
                                    deviceKind = kind
                                )
                            }

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
                        notify.structureToString(),
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

internal fun String.toIpV4Long(): Long {
    if (isEmpty()) return 0
    val split = split('.')
    if (split.size != 4) return 0
    return split.mapToByteArray { it.toUByte().toByte() }.toInt().toLongUnsigned()
}
