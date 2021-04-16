/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.chat.CheckableStruct
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY

internal class Oidb0x6d6 : ProtoBuf {
    @Serializable
    internal class DeleteFileReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val parentFolderId: String = "",
        @JvmField @ProtoNumber(5) val fileId: String = "",
        @JvmField @ProtoNumber(6) val msgdbSeq: Int = 0,
        @JvmField @ProtoNumber(7) val msgRand: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DeleteFileRspBody(
        /**
         * -103: file not exist
         */
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = ""
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class DownloadFileReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val fileId: String = "",
        @JvmField @ProtoNumber(5) val boolThumbnailReq: Boolean = false,
        @JvmField @ProtoNumber(6) val urlType: Int = 0,
        @JvmField @ProtoNumber(7) val boolPreviewReq: Boolean = false,
        @JvmField @ProtoNumber(8) val src: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DownloadFileRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val downloadIp: String = "",
        @JvmField @ProtoNumber(5) val downloadDns: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(6) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(8) val sha3: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(9) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(10) val cookieVal: String = "",
        @JvmField @ProtoNumber(11) val saveFileName: String = "",
        @JvmField @ProtoNumber(12) val previewPort: Int = 0,
        @JvmField @ProtoNumber(13) val downloadDnsHttps: String = "",
        @JvmField @ProtoNumber(14) val previewPortHttps: Int = 0
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class MoveFileReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val fileId: String = "",
        @JvmField @ProtoNumber(5) val parentFolderId: String = "",
        @JvmField @ProtoNumber(6) val destFolderId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MoveFileRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val parentFolderId: String = ""
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class RenameFileReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val fileId: String = "",
        @JvmField @ProtoNumber(5) val parentFolderId: String = "",
        @JvmField @ProtoNumber(6) val newFileName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RenameFileRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = ""
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val uploadFileReq: UploadFileReqBody? = null,
        @JvmField @ProtoNumber(2) val resendFileReq: ResendReqBody? = null,
        @JvmField @ProtoNumber(3) val downloadFileReq: DownloadFileReqBody? = null,
        @JvmField @ProtoNumber(4) val deleteFileReq: DeleteFileReqBody? = null,
        @JvmField @ProtoNumber(5) val renameFileReq: RenameFileReqBody? = null,
        @JvmField @ProtoNumber(6) val moveFileReq: MoveFileReqBody? = null
    ) : ProtoBuf

    @Serializable
    internal class ResendReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val fileId: String = "",
        @JvmField @ProtoNumber(5) val sha: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ResendRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val uploadIp: String = "",
        @JvmField @ProtoNumber(5) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(6) val checkKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val uploadFileRsp: UploadFileRspBody? = null,
        @JvmField @ProtoNumber(2) val resendFileRsp: ResendRspBody? = null,
        @JvmField @ProtoNumber(3) val downloadFileRsp: DownloadFileRspBody? = null,
        @JvmField @ProtoNumber(4) val deleteFileRsp: DeleteFileRspBody? = null,
        @JvmField @ProtoNumber(5) val renameFileRsp: RenameFileRspBody? = null,
        @JvmField @ProtoNumber(6) val moveFileRsp: MoveFileRspBody? = null
    ) : ProtoBuf

    @Serializable
    internal class UploadFileReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val entrance: Int = 0,
        @JvmField @ProtoNumber(5) val parentFolderId: String = "",
        @JvmField @ProtoNumber(6) val fileName: String = "",
        @JvmField @ProtoNumber(7) val localPath: String = "",
        @JvmField @ProtoNumber(8) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(9) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(10) val sha3: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(11) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(15) val boolSupportMultiUpload: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class UploadFileRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val uploadIp: String = "",
        @JvmField @ProtoNumber(5) val serverDns: String = "",
        @JvmField @ProtoNumber(6) val busId: Int = 0,
        @JvmField @ProtoNumber(7) val fileId: String = "",
        @JvmField @ProtoNumber(8) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(9) val checkKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(10) val boolFileExist: Boolean = false,
        @JvmField @ProtoNumber(12) val uploadIpLanV4: List<String> = emptyList(),
        @JvmField @ProtoNumber(13) val uploadIpLanV6: List<String> = emptyList(),
        @JvmField @ProtoNumber(14) val uploadPort: Int = 0
    ) : ProtoBuf, CheckableStruct
}
        