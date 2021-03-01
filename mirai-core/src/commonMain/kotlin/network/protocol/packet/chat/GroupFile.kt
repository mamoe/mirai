/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.internal.network.protocol.packet.chat

import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.DeserializationStrategy
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x6d6
import net.mamoe.mirai.internal.network.protocol.data.proto.Oidb0x6d8
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.readOidbSsoPkg
import net.mamoe.mirai.internal.utils.io.serialization.writeOidb
import net.mamoe.mirai.utils.ExternalResource
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal sealed class CommonOidbResponse<T> : Packet {
    data class Failure<T>(
        val result: Int,
        val msg: String,
        val e: Throwable?,
    ) : CommonOidbResponse<T>() {
        inline fun createException(actionName: String): IllegalStateException {
            return IllegalStateException("Failed $actionName, result=$result, msg=$msg", e)
        }
    }

    class Success<T>(
        val resp: T
    ) : CommonOidbResponse<T>()
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
internal inline fun <T> CommonOidbResponse<T>.toResult(actionName: String): Result<T> {
    return if (this is CommonOidbResponse.Failure) {
        Result.failure(this.createException(actionName))
    } else {
        this as CommonOidbResponse.Success<T>
        Result.success(this.resp)
    }
}

/**
 * @param respMapper may throw any exception, which will be wrapped to CommonOidbResponse.Failure
 */
internal inline fun <T : ProtoBuf, R> ByteReadPacket.readOidbRespCommon(
    bodyBufferDeserializer: DeserializationStrategy<T>,
    respMapper: (T) -> R
): CommonOidbResponse<R> {
    contract { callsInPlace(respMapper, InvocationKind.AT_MOST_ONCE) }
    val oidb = readOidbSsoPkg(bodyBufferDeserializer)
    return oidb.fold(
        onSuccess = {
            CommonOidbResponse.Success(kotlin.runCatching {
                respMapper(this)
            }.getOrElse {
                return CommonOidbResponse.Failure(0, it.message ?: "", it)
            })
        },
        onFailure = {
            CommonOidbResponse.Failure(result, errorMsg, null)
        }
    )
}

internal object FileManagement {
    object GetFileList : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d8.GetFileListRspBody>>("OidbSvc.0x6d8_1") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            folderId: String,
            startIndex: Int,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                1752,
                1,
                Oidb0x6d8.ReqBody.serializer(),
                Oidb0x6d8.ReqBody(
                    fileListInfoReq = Oidb0x6d8.GetFileListReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        folderId = folderId,
                        fileCount = 20,
                        reqFrom = 3,
                        sortBy = 1,
                        startIndex = startIndex
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d8.GetFileListRspBody> {
            return readOidbRespCommon(Oidb0x6d8.RspBody.serializer()) { it.fileListInfoRsp!! }
        }
    }

    object RequestUpload : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d6.UploadFileRspBody>>("OidbSvc.0x6d6_0") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            folderId: String,
            resource: ExternalResource,
            filename: String,
        ) = buildOutgoingUniPacket(client) {
            resource.sha1 // check supported

            writeOidb(
                command = 1750,
                serviceType = 0,
                Oidb0x6d6.ReqBody.serializer(),
                Oidb0x6d6.ReqBody(
                    uploadFileReq = Oidb0x6d6.UploadFileReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        busId = 102,
                        entrance = 5,
                        parentFolderId = folderId,
                        fileName = filename,
                        localPath = "/storage/emulated/0/Pictures/files/s/$filename",
                        fileSize = resource.size,
                        sha = resource.sha1,
                        md5 = resource.md5,
                        boolSupportMultiUpload = true,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d6.UploadFileRspBody> {
            return readOidbRespCommon(Oidb0x6d6.RspBody.serializer()) { it.uploadFileRsp!! }
        }
    }
}