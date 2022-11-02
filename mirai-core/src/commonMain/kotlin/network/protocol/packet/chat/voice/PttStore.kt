/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.voice

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x346
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketWithRespType
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.internal.utils.toIpV4AddressString
import net.mamoe.mirai.message.data.AudioCodec
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.toUHexString

internal inline val ExternalResource.voiceCodec: Int get() = audioCodec.id

internal val ExternalResource.audioCodec: AudioCodec
    get() {
        return when (formatName) {
            // 实际上 amr 是 0, 但用 1 也可以发. 为了避免 silk 错被以 amr 发送导致降音质就都用 1
            "amr" -> AudioCodec.SILK
            "silk" -> AudioCodec.SILK
            else -> AudioCodec.AMR     // use amr by default
        }
    }

internal class PttStore {
    object GroupPttUp : OutgoingPacketFactory<GroupPttUp.Response>("PttStore.GroupPttUp") {

        sealed class Response : Packet {

            class RequireUpload(
                val fileId: Long,
                val uKey: ByteArray,
                val uploadIpList: List<Int>,
                val uploadPortList: List<Int>,
                val fileKey: ByteArray
            ) : GroupPttUp.Response() {
                override fun toString(): String {
                    return "RequireUpload(" +
                            "fileId=$fileId, " +
                            "uploadServers=${
                                uploadIpList.zip(uploadPortList)
                                    .joinToString(
                                        prefix = "[",
                                        postfix = "]"
                                    ) { "${it.first.toIpV4AddressString()}:${it.second}1" }
                            }, " +
                            "uKey=${uKey.toUHexString("")}, " +
                            "fileKey=${fileKey.toUHexString("")}" +
                            ")"
                }
            }
        }

        fun createTryUpPttPack(
            uin: Long,
            groupCode: Long,
            resource: ExternalResource
        ): Cmd0x388.ReqBody {
            return Cmd0x388.ReqBody(
                netType = 3, // wifi
                subcmd = 3,
                msgTryupPttReq = listOf(
                    Cmd0x388.TryUpPttReq(
                        srcUin = uin,
                        groupCode = groupCode,
                        fileId = 0,
                        fileSize = resource.size,
                        fileMd5 = resource.md5,
                        fileName = resource.md5,
                        srcTerm = 5,
                        platformType = 9,
                        buType = 4,
                        innerIp = 0,
                        buildVer = "6.5.5.663".encodeToByteArray(),
                        voiceLength = 1,
                        codec = resource.voiceCodec, // HTTP 时只支持 0
                        // 2021/1/26 因为 #577 修改为 resource.voiceCodec
                        voiceType = 1,
                        boolNewUpChan = true
                    )
                )
            )
        }

        operator fun invoke(
            client: QQAndroidClient,
            uin: Long,
            groupCode: Long,
            resource: ExternalResource
        ): OutgoingPacketWithRespType<Response> {
            val pack = createTryUpPttPack(uin, groupCode, resource)
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(Cmd0x388.ReqBody.serializer(), pack)
            }
        }


        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp0 = readProtoBuf(Cmd0x388.RspBody.serializer())
            val resp =
                resp0.msgTryupPttRsp.firstOrNull() ?: error("cannot find `msgTryupPttRsp` from `Cmd0x388.RspBody`")
            if (resp.failMsg != null) {
                throw IllegalStateException(resp.failMsg.decodeToString())
            }
            return Response.RequireUpload(
                fileId = resp.fileid,
                uKey = resp.upUkey,
                uploadIpList = resp.uint32UpIp,
                uploadPortList = resp.uint32UpPort,
                fileKey = resp.fileKey
            )

        }

    }

    object GroupPttDown : OutgoingPacketFactory<GroupPttDown.Response.DownLoadInfo>("PttStore.GroupPttDown") {

        sealed class Response : Packet {
            class DownLoadInfo(
                val downDomain: ByteArray,
                val downPara: ByteArray,
                val strDomain: String,
                val uint32DownIp: List<Int>,
                val uint32DownPort: List<Int>
            ) : GroupPttDown.Response() {
                override fun toString(): String {
                    return "GroupPttDown(downPara=${downPara.decodeToString()},strDomain=$strDomain})"
                }
            }

        }

        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            ptt: ImMsgBody.Ptt,
            msg: MsgComm.Msg,
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Cmd0x388.ReqBody.serializer(), Cmd0x388.ReqBody(
                    netType = 3, // wifi
                    subcmd = 4,
                    msgGetpttUrlReq = listOf(
                        Cmd0x388.GetPttUrlReq(
                            groupCode = groupCode,
                            fileid = 0,
                            fileId = ptt.fileId.toLong(),
                            fileMd5 = ptt.fileMd5,
                            dstUin = msg.msgHead.toUin,
                            fileKey = ptt.groupFileKey,
                            buType = 3,
                            innerIp = 0,
                            buildVer = "8.5.5".encodeToByteArray(),
                            codec = ptt.format,
                            reqTerm = 5,
                            reqPlatformType = 9,
                        )
                    )
                )
            )
        }

        @OptIn(ExperimentalStdlibApi::class)
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            dstUin: Long,
            md5: ByteArray

        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Cmd0x388.ReqBody.serializer(), Cmd0x388.ReqBody(
                    netType = 3, // wifi
                    subcmd = 4,
                    msgGetpttUrlReq = listOf(
                        Cmd0x388.GetPttUrlReq(
                            groupCode = groupCode,
                            fileMd5 = md5,
                            dstUin = dstUin,
                            buType = 4,
                            innerIp = 0,
                            buildVer = "8.5.5".encodeToByteArray(),
                            codec = 0,
                            reqTerm = 5,
                            reqPlatformType = 9
                        )
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response.DownLoadInfo {
            val resp0 = readProtoBuf(Cmd0x388.RspBody.serializer())
            val resp =
                resp0.msgGetpttUrlRsp.firstOrNull() ?: error("cannot find `msgGetpttUrlRsp` from `Cmd0x388.RspBody`")
            if (!resp.failMsg.contentEquals(EMPTY_BYTE_ARRAY)) {
                throw IllegalStateException(resp.failMsg.decodeToString())
            }
            return Response.DownLoadInfo(
                downDomain = resp.downDomain,
                downPara = resp.downPara,
                uint32DownIp = resp.uint32DownIp,
                uint32DownPort = resp.uint32DownPort,
                strDomain = resp.strDomain
            )
        }
    }

    object C2CPttDown : OutgoingPacketFactory<C2CPttDown.Response>(
        "PttCenterSvr.pb_pttCenter_CMD_REQ_APPLY_DOWNLOAD-1200"
    ) {
        operator fun invoke(client: QQAndroidClient, uin: Long, uuid: ByteArray) =
            buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    Cmd0x346.ReqBody.serializer(), Cmd0x346.ReqBody(
                        msgApplyDownloadReq = Cmd0x346.ApplyDownloadReq(
                            uin = uin,
                            uuid = uuid,
                            needHttpsUrl = 1,
                        ),
                        clientType = 104,
                        cmd = 1200,
                        businessId = 17, // or 3?
                    )
                )
            }

        sealed class Response : Packet {
            class Failed(val retMsg: String) : Response() {
                override fun toString(): String {
                    return "PttCenterSvr.pb_pttCenter#download.Failed(retMsg=$retMsg)"
                }
            }

            class Success(val downloadUrl: String) : Response() {
                override fun toString(): String {
                    return "PttCenterSvr.pb_pttCenter#download.Success"
                }
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): C2CPttDown.Response {
            val data = readProtoBuf(Cmd0x346.RspBody.serializer())
            val rsp = data.msgApplyDownloadRsp ?: return Response.Failed("Response not found")
            if (rsp.retMsg != "success") {
                return Response.Failed(rsp.retMsg)
            }
            val downloadInfo = rsp.msgDownloadInfo ?: return Response.Failed("Download info not found")
            return Response.Success(downloadInfo.downloadUrl)
        }
    }

    object C2C {
        fun createC2CPttStoreBDHExt(
            bot: QQAndroidBot,
            uin: Long,
            resource: ExternalResource
        ): Cmd0x346.ReqBody {
            return Cmd0x346.ReqBody(
                cmd = 500,
                businessId = 17,
                clientType = 104,
                msgExtensionReq = Cmd0x346.ExtensionReq(
                    id = 3,
                    pttFormat = 1,
                    netType = 3,
                    voiceType = 1,
                    pttTime = 1,
                ),
                msgApplyUploadReq = Cmd0x346.ApplyUploadReq(
                    senderUin = bot.uin,
                    recverUin = uin,
                    fileType = 2,
                    fileSize = resource.size,
                    fileName = resource.md5,
                    _10mMd5 = resource.md5,
                ),
            )
        }
    }
}
