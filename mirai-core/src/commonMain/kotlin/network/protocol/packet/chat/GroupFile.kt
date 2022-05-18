/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.internal.network.protocol.packet.chat

import io.ktor.utils.io.core.*
import kotlinx.serialization.DeserializationStrategy
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.*
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.readOidbSsoPkg
import net.mamoe.mirai.internal.utils.io.serialization.writeOidb
import net.mamoe.mirai.utils.ExternalResource
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.absoluteValue
import kotlin.random.Random

internal sealed class CommonOidbResponse<T> : Packet {
    data class Failure<T>(
        val result: Int,
        val msg: String,
        val e: Throwable?,
    ) : CommonOidbResponse<T>() {
        inline fun createException(actionName: String): IllegalStateException {
            return IllegalStateException("Failed $actionName, result=$result, msg=$msg", e)
        }

        override fun toString(): String {
            return "CommonOidbResponse.Failure(result=$result, msg=$msg, e=$e)"
        }
    }

    class Success<T>(
        val resp: T
    ) : CommonOidbResponse<T>() {
        override fun toString(): String {
            return "CommonOidbResponse.Success"
        }
    }
}

internal interface CheckableStruct {
    val int32RetCode: Int
    val retMsg: String
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
internal inline fun <T> CommonOidbResponse<T>.toResult(actionName: String, checkResp: Boolean = true): Result<T> {
    return if (this is CommonOidbResponse.Failure) {
        Result.failure(this.createException(actionName))
    } else {
        this as CommonOidbResponse.Success<T>
        if (!checkResp) return Result.success(this.resp)
        val result = this.resp
        if (result is CheckableStruct) {
            if (result.int32RetCode != 0) return Result.failure(IllegalStateException("Failed $actionName, result=${result.int32RetCode}, msg=${result.retMsg}"))
        }
        Result.success(this.resp)
    }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "RESULT_CLASS_IN_RETURN_TYPE")
@kotlin.internal.InlineOnly
internal inline fun <T> CommonOidbResponse<T>.toResult(
    actionName: String,
    checkResp: CheckableStruct.(Int) -> Boolean
): Result<T> {
    return if (this is CommonOidbResponse.Failure) {
        Result.failure(this.createException(actionName))
    } else {
        this as CommonOidbResponse.Success<T>
        val result = this.resp
        if (result is CheckableStruct) {
            if (!checkResp(
                    result,
                    result.int32RetCode
                )
            ) return Result.failure(IllegalStateException("Failed $actionName, result=${result.int32RetCode}, msg=${result.retMsg}"))
        }
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
    val factories = arrayOf(
        GetFileList,
        GetFileInfo,
        RequestDownload,
        RequestUpload,
        DeleteFile,
        MoveFile,
        RenameFile,
        TransferFile,
        Feed,
        RenameFolder,
//        MoveFolder,
        DeleteFolder,
        CreateFolder,
    )

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

    object GetFileInfo : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d8.GetFileInfoRspBody>>("OidbSvc.0x6d8_0") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            fileId: String,
            busId: Int,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                1752,
                0,
                Oidb0x6d8.ReqBody.serializer(),
                Oidb0x6d8.ReqBody(
                    fileInfoReq = Oidb0x6d8.GetFileInfoReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        fileId = fileId,
                        busId = busId
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d8.GetFileInfoRspBody> {
            return readOidbRespCommon(Oidb0x6d8.RspBody.serializer()) { it.fileInfoRsp!! }
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

    object RequestDownload :
        OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d6.DownloadFileRspBody>>("OidbSvc.0x6d6_2") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            busId: Int,
            fileId: String,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1750,
                serviceType = 2,
                Oidb0x6d6.ReqBody.serializer(),
                Oidb0x6d6.ReqBody(
                    downloadFileReq = Oidb0x6d6.DownloadFileReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        busId = busId,
                        fileId = fileId,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d6.DownloadFileRspBody> {
            return readOidbRespCommon(Oidb0x6d6.RspBody.serializer()) { it.downloadFileRsp!! }
        }
    }

    object MoveFile : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d6.MoveFileRspBody>>("OidbSvc.0x6d6_5") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            busId: Int,
            fileId: String,
            parentFolderId: String,
            destFolderId: String,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1750,
                serviceType = 5,
                Oidb0x6d6.ReqBody.serializer(),
                Oidb0x6d6.ReqBody(
                    moveFileReq = Oidb0x6d6.MoveFileReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        busId = busId,
                        fileId = fileId,
                        parentFolderId = parentFolderId,
                        destFolderId = destFolderId
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d6.MoveFileRspBody> {
            return readOidbRespCommon(Oidb0x6d6.RspBody.serializer()) { it.moveFileRsp!! }
        }
    }


    // 转发
    object TransferFile : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d9.TransFileRspBody>>("OidbSvc.0x6d9_0") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            busId: Int,
            fileId: String,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1753,
                serviceType = 0,
                Oidb0x6d9.ReqBody.serializer(),
                Oidb0x6d9.ReqBody(
                    transFileReq = Oidb0x6d9.TransFileReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        busId = busId,
                        fileId = fileId,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d9.TransFileRspBody> {
            return readOidbRespCommon(Oidb0x6d9.RspBody.serializer()) { it.transFileRsp!! }
        }
    }


    object RenameFile : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d6.RenameFileRspBody>>("OidbSvc.0x6d6_4") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            busId: Int,
            fileId: String,
            parentFolderId: String,
            newName: String,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1750,
                serviceType = 4,
                Oidb0x6d6.ReqBody.serializer(),
                Oidb0x6d6.ReqBody(
                    renameFileReq = Oidb0x6d6.RenameFileReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        busId = busId,
                        fileId = fileId,
                        parentFolderId = parentFolderId,
                        newFileName = newName,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d6.RenameFileRspBody> {
            return readOidbRespCommon(Oidb0x6d6.RspBody.serializer()) { it.renameFileRsp!! }
        }
    }

    object DeleteFile : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d6.DeleteFileRspBody>>("OidbSvc.0x6d6_3") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            busId: Int,
            fileId: String,
            parentFolderId: String,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1750,
                serviceType = 3,
                Oidb0x6d6.ReqBody.serializer(),
                Oidb0x6d6.ReqBody(
                    deleteFileReq = Oidb0x6d6.DeleteFileReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        busId = busId,
                        fileId = fileId,
                        parentFolderId = parentFolderId,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d6.DeleteFileRspBody> {
            return readOidbRespCommon(Oidb0x6d6.RspBody.serializer()) { it.deleteFileRsp!! }
        }
    }

    object Feed : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d9.FeedsRspBody>>("OidbSvc.0x6d9_4") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            busId: Int,
            fileId: String,
            random: Int = Random.nextInt().absoluteValue,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1753,
                serviceType = 4,
                Oidb0x6d9.ReqBody.serializer(),
                Oidb0x6d9.ReqBody(
                    feedsInfoReq = Oidb0x6d9.FeedsReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        feedsInfoList = listOf(
                            GroupFileCommon.FeedsInfo(
                                busId = busId,
                                fileId = fileId,
                                feedFlag = 1,
                                msgRandom = random,
                            )
                        )
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d9.FeedsRspBody> {
            return readOidbRespCommon(Oidb0x6d9.RspBody.serializer()) { it.feedsInfoRsp!! }
        }
    }


    object RenameFolder : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d7.RenameFolderRspBody>>("OidbSvc.0x6d7_2") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            folderId: String,
            newName: String
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1751,
                serviceType = 2,
                Oidb0x6d7.ReqBody.serializer(),
                Oidb0x6d7.ReqBody(
                    renameFolderReq = Oidb0x6d7.RenameFolderReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        folderId = folderId,
                        newFolderName = newName,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d7.RenameFolderRspBody> {
            return readOidbRespCommon(Oidb0x6d7.RspBody.serializer()) { it.renameFolderRsp!! }
        }
    }

    // qq doesn't support
//    object MoveFolder : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d7.MoveFolderRspBody>>("OidbSvc.0x6d7_3") {
//        operator fun invoke(
//            client: QQAndroidClient,
//            groupCode: Long,
//            folderId: String,
//            parentFolderId: String,
//            newParentFolderId: String,
//        ) = buildOutgoingUniPacket(client) {
//            writeOidb(
//                command = 1751,
//                serviceType = 3,
//                Oidb0x6d7.ReqBody.serializer(),
//                Oidb0x6d7.ReqBody(
//                    moveFolderReq = Oidb0x6d7.MoveFolderReqBody(
//                        groupCode = groupCode,
//                        appId = 3,
//                        folderId = folderId,
//                        parentFolderId = parentFolderId,
//                        destFolderId = newParentFolderId,
//                    )
//                )
//            )
//        }
//
//        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d7.MoveFolderRspBody> {
//            return readOidbRespCommon(Oidb0x6d7.RspBody.serializer()) { it.moveFolderRsp!! }
//        }
//    }

    object DeleteFolder : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d7.DeleteFolderRspBody>>("OidbSvc.0x6d7_1") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            folderId: String,
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1751,
                serviceType = 1,
                Oidb0x6d7.ReqBody.serializer(),
                Oidb0x6d7.ReqBody(
                    deleteFolderReq = Oidb0x6d7.DeleteFolderReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        folderId = folderId,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d7.DeleteFolderRspBody> {
            return readOidbRespCommon(Oidb0x6d7.RspBody.serializer()) { it.deleteFolderRsp!! }
        }
    }

    object CreateFolder : OutgoingPacketFactory<CommonOidbResponse<Oidb0x6d7.CreateFolderRspBody>>("OidbSvc.0x6d7_0") {
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            parentFolderId: String,
            name: String
        ) = buildOutgoingUniPacket(client) {
            writeOidb(
                command = 1751,
                serviceType = 0,
                Oidb0x6d7.ReqBody.serializer(),
                Oidb0x6d7.ReqBody(
                    createFolderReq = Oidb0x6d7.CreateFolderReqBody(
                        groupCode = groupCode,
                        appId = 3,
                        parentFolderId = parentFolderId,
                        folderName = name,
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): CommonOidbResponse<Oidb0x6d7.CreateFolderRspBody> {
            return readOidbRespCommon(Oidb0x6d7.RspBody.serializer()) { it.createFolderRsp!! }
        }
    }
}