/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat

import io.ktor.utils.io.core.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x346
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

internal class OfflineFilleHandleSvr {

    internal sealed class FileInfo : Packet {
        class Success(
            val fileUuid: ByteArray,
            val filename: String,
            val fileSha1: ByteArray,
            val fileMd5: ByteArray,
            val fileSize: Long,
            val expiryTime: Long,
            val ownerUin: Long,
        ) : FileInfo()
        class Failed(val message: String) : FileInfo()
    }

    internal object UploadSucc : OutgoingPacketFactory<FileInfo>(
        "OfflineFilleHandleSvr.pb_ftn_CMD_REQ_UPLOAD_SUCC-800"
    ) {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): FileInfo {
            val resp = readProtoBuf(Cmd0x346.RspBody.serializer())

            val upResp = resp.msgUploadSuccRsp
                ?: return FileInfo.Failed("msgUploadSuccRsp is null")

            if (upResp.int32RetCode != 0) {
                return FileInfo.Failed("return code is ${upResp.int32RetCode}: ${upResp.retMsg}")
            }

            val fileInfo = upResp.msgFileInfo
                ?: return FileInfo.Failed("msgUploadSuccRsp.msgFileInfo is null")

            return FileInfo.Success(
                fileInfo.uuid,
                fileInfo.fileName,
                fileInfo._3sha,
                fileInfo._10mMd5,
                fileInfo.fileSize,
                fileInfo.expireTime.toLong(),
                fileInfo.ownerUin,
            )
        }

        operator fun invoke(
            client: QQAndroidClient,
            contact: Contact,
            fileUuid: ByteArray,
        ) = buildOutgoingUniPacket(client, sequenceId = client.sendFriendMessageSeq.next()) { seq ->
            writeProtoBuf(
                Cmd0x346.ReqBody.serializer(),
                Cmd0x346.ReqBody(
                    cmd = 800,
                    seq = seq,
                    businessId = 3,
                    clientType = 104,
                    msgUploadSuccReq = Cmd0x346.UploadSuccReq(
                        senderUin = client.uin,
                        recverUin = contact.uin,
                        uuid = fileUuid,
                    )
                )
            )
        }

    }

    internal object ApplyDownload : OutgoingPacketFactory<ApplyDownload.Response>(
        "OfflineFilleHandleSvr.pb_ftn_CMD_REQ_APPLY_DOWNLOAD-1200"
    ) {
        internal sealed class Response : Packet {
            class Success(val url: String) : Response()
            class Failed(val message: String) : Response()
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(Cmd0x346.RspBody.serializer())

            val downResp = resp.msgApplyDownloadRsp
                ?: return Response.Failed("msgApplyDownloadRsp is null")

            if (downResp.int32RetCode != 0) {
                return Response.Failed("return code is ${downResp.int32RetCode}: ${downResp.retMsg}")
            }

            val downInfo = downResp.msgDownloadInfo
                ?: return Response.Failed("msgDownloadInfo is null")

            return Response.Success(buildString {
                append("http://")
                append(downInfo.downloadDomain)
                append(downInfo.downloadUrl)
            })

        }

        operator fun invoke(
            client: QQAndroidClient,
            fileUuid: ByteArray
        ) = buildOutgoingUniPacket(client) { seq ->
            writeProtoBuf(
                Cmd0x346.ReqBody.serializer(),
                Cmd0x346.ReqBody(
                    cmd = 1200,
                    seq = seq,
                    businessId = 3,
                    clientType = 104,
                    msgApplyDownloadReq = Cmd0x346.ApplyDownloadReq(
                        uin = client.uin,
                        uuid = fileUuid,
                        ownerType = 2
                    ),
                    msgExtensionReq = Cmd0x346.ExtensionReq(
                        downloadUrlType = 1
                    )
                )
            )
        }
    }

    internal object FileQuery : OutgoingPacketFactory<FileInfo>(
        "OfflineFilleHandleSvr.pb_ftn_CMD_REQ_FILE_QUERY-1400"
    ) {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): FileInfo {
            val resp = readProtoBuf(Cmd0x346.RspBody.serializer())

            val queryResp = resp.msgFileQueryRsp
                ?: return FileInfo.Failed("msgFileQueryRsp is null")

            if (queryResp.int32RetCode != 0) {
                return FileInfo.Failed("return code is ${queryResp.int32RetCode}: ${queryResp.retMsg}")
            }

            val fileInfo = queryResp.msgFileInfo
                ?: return FileInfo.Failed("msgFileQueryRsp.msgFileInfo is null")

            return FileInfo.Success(
                fileInfo.uuid,
                fileInfo.fileName,
                fileInfo._3sha,
                fileInfo.md5,
                fileInfo.fileSize,
                fileInfo.expireTime.toLong(),
                fileInfo.ownerUin,
            )
        }

        operator fun invoke(
            client: QQAndroidClient,
            fileUuid: ByteArray,
        ) = buildOutgoingUniPacket(client) { seq ->
            writeProtoBuf(
                Cmd0x346.ReqBody.serializer(),
                Cmd0x346.ReqBody(
                    cmd = 1400,
                    seq = seq,
                    businessId = 3,
                    clientType = 104,
                    msgFileQueryReq = Cmd0x346.FileQueryReq(
                        uin = client.uin,
                        uuid = fileUuid,
                    )
                )
            )
        }
    }

    internal object ApplyUploadV3 : OutgoingPacketFactory<ApplyUploadV3.Response>(
        "OfflineFilleHandleSvr.pb_ftn_CMD_REQ_APPLY_UPLOAD_V3-1700"
    ) {
        internal sealed class Response : Packet {
            class FileExists(val fileUuid: ByteArray,) : Response()
            class RequireUpload(val fileUuid: ByteArray, val uploadKey: ByteArray) : Response()
            class Failed(val message: String) : Response()
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(Cmd0x346.RspBody.serializer())

            val upResp = resp.msgApplyUploadRspV3
                ?: return Response.Failed("msgApplyUploadRspV3 is null")

            if (upResp.int32RetCode != 0) {
                return Response.Failed("return code is ${upResp.int32RetCode}: ${upResp.retMsg}")
            }

            return if (upResp.boolFileExist) {
                Response.FileExists(upResp.uuid)
            } else {
                Response.RequireUpload(upResp.uuid, upResp.mediaPlateformUploadKey)
            }
        }

        operator fun invoke(
            client: QQAndroidClient,
            contact: Contact,
            filename: String,
            fileSize: Long,
            fileMd5: ByteArray,
            fileSha1: ByteArray,
        ) = buildOutgoingUniPacket(client, sequenceId = client.sendFriendMessageSeq.next()) { seq ->
            writeProtoBuf(
                Cmd0x346.ReqBody.serializer(),
                Cmd0x346.ReqBody(
                    cmd = 1700,
                    seq = seq,
                    businessId = 3,
                    clientType = 104,
                    msgApplyUploadReqV3 = Cmd0x346.ApplyUploadReqV3(
                        senderUin = client.uin,
                        recverUin = contact.uin,
                        fileSize = fileSize,
                        fileName = filename,
                        _10mMd5 = fileMd5,
                        sha = fileSha1,
                        localFilepath = "/storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/QQfile_recv/$filename",
                        md5 = fileMd5
                    ),
                    flagSupportMediaplatform = 1,
                )
            )
        }
    }

}