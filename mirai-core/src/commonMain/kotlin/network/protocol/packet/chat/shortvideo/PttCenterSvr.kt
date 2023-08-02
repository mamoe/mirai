/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.video

import io.ktor.utils.io.core.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.PttShortVideo
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

internal class PttCenterSvr {
    object GroupShortVideoUpReq :
        OutgoingPacketFactory<GroupShortVideoUpReq.Response>("PttCenterSvr.GroupShortVideoUpReq") {
        sealed class Response : Packet {
            class FileExists(val fileId: String) : Response() {
                override fun toString(): String {
                    return "PttCenterSvr.GroupShortVideoUpReq.Response.FileExists(fileId=${fileId})"
                }
            }

            object RequireUpload : Response() {
                override fun toString(): String {
                    return "PttCenterSvr.GroupShortVideoUpReq.Response.RequireUpload"
                }
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(PttShortVideo.RspBody.serializer())
            val upResp = resp.msgPttShortVideoUploadResp ?: return Response.RequireUpload

            return if (upResp.fileExist == 1) {
                Response.FileExists(upResp.fileid)
            } else {
                Response.RequireUpload
            }
        }

        operator fun invoke(
            client: QQAndroidClient,
            contact: Contact,
            thumbnailFileMd5: ByteArray,
            thumbnailFileSize: Long,
            videoFileName: String,
            videoFileMd5: ByteArray,
            videoFileSize: Long,
            videoFileFormat: String
        ) = buildOutgoingUniPacket(client) { sequenceId ->
            writeProtoBuf(
                PttShortVideo.ReqBody.serializer(),
                PttShortVideo.ReqBody(
                    cmd = 300,
                    seq = sequenceId,
                    msgPttShortVideoUploadReq = buildShortVideoFileInfo(
                        client,
                        contact,
                        thumbnailFileMd5,
                        thumbnailFileSize,
                        videoFileName,
                        videoFileMd5,
                        videoFileSize,
                        videoFileFormat
                    ),
                    msgExtensionReq = listOf(
                        PttShortVideo.ExtensionReq(
                            subBusiType = 0,
                            userCnt = 1
                        )
                    )
                )
            )
        }

        internal fun buildShortVideoFileInfo(
            client: QQAndroidClient,
            contact: Contact,
            thumbnailFileMd5: ByteArray,
            thumbnailFileSize: Long,
            videoFileName: String,
            videoFileMd5: ByteArray,
            videoFileSize: Long,
            videoFileFormat: String
        ) = PttShortVideo.PttShortVideoUploadReq(
            fromuin = client.uin,
            touin = contact.uin,
            chatType = 1, // guild channel = 4, others = 1
            clientType = 2,
            msgPttShortVideoFileInfo = PttShortVideo.PttShortVideoFileInfo(
                fileName = videoFileName + videoFileFormat,
                fileMd5 = videoFileMd5,
                fileSize = videoFileSize,
                fileResLength = 1280,
                fileResWidth = 720,
                // Lcom/tencent/mobileqq/transfile/ShortVideoUploadProcessor;getFormat(Ljava/lang/String;)I
                fileFormat = 3,
                fileTime = 120,
                thumbFileMd5 = thumbnailFileMd5,
                thumbFileSize = thumbnailFileSize
            ),
            groupCode = if (contact is Group) contact.uin else 0,
            flagSupportLargeSize = 1
        )
    }

    object ShortVideoDownReq : OutgoingPacketFactory<ShortVideoDownReq.Response>("PttCenterSvr.ShortVideoDownReq") {
        sealed class Response : Packet {
            class Success(val fileMd5: ByteArray, val urlV4: String, val urlV6: String?) : Response() {
                override fun toString(): String {
                    return "PttCenterSvr.ShortVideoDownReq.Response.Success(" +
                            "urlV4=$urlV4, urlV6=$urlV6)"
                }
            }

            object Failed : Response() {
                override fun toString(): String {
                    return "PttCenterSvr.ShortVideoDownReq.Response.Failed"
                }
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(PttShortVideo.RspBody.serializer())

            val shortVideoDownloadResp = resp.msgPttShortVideoDownloadResp ?: return Response.Failed
            val attr = shortVideoDownloadResp.msgDownloadAddr ?: return Response.Failed

            val fileMd5 = shortVideoDownloadResp.fileMd5
            val urlV4 = attr.strHost.first() + attr.urlArgs
            val urlV6 = attr.strHostIpv6.firstOrNull()?.plus(attr.urlArgs)

            return Response.Success(fileMd5, urlV4, urlV6)
        }

        // Lcom/tencent/mobileqq/transfile/protohandler/ShortVideoDownHandler;constructReqBody(Ljava/util/List;)[B
        operator fun invoke(
            client: QQAndroidClient,
            contact: Contact,
            sender: User,
            videoFIleId: String,
            videoFileMd5: ByteArray,
        ) = buildOutgoingUniPacket(client) { sequenceId ->
            writeProtoBuf(
                PttShortVideo.ReqBody.serializer(),
                PttShortVideo.ReqBody(
                    cmd = 400,
                    seq = sequenceId,
                    msgPttShortVideoDownloadReq = PttShortVideo.PttShortVideoDownloadReq(
                        fromuin = sender.uin,
                        touin = client.uin,
                        chatType = if (sender is Friend) 0 else 1,
                        clientType = 7,
                        fileid = videoFIleId,
                        groupCode = if (contact is Group) contact.uin else 0L,
                        fileMd5 = videoFileMd5,
                        businessType = 1,
                        flagSupportLargeSize = 1,
                        flagClientQuicProtoEnable = 1,
                        fileType = 2, // maybe 1 = newly uploaded video, unverified
                        downType = 2,
                        sceneType = 2, // hooked 0 and 1, but unknown
                        reqTransferType = 1,
                        reqHostType = 11,
                    ),
                    msgExtensionReq = listOf(
                        PttShortVideo.ExtensionReq(subBusiType = 0)
                    )
                )
            )
        }
    }
}