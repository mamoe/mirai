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
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY

internal class Oidb0x6d8 : ProtoBuf {
    @Serializable
    internal class FileTimeStamp(
        @JvmField @ProtoNumber(1) val uploadTime: Int = 0,
        @JvmField @ProtoNumber(2) val fileId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GetFileCountReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetFileCountRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val allFileCount: Int = 0,
        @JvmField @ProtoNumber(5) val boolFileTooMany: Boolean = false,
        @JvmField @ProtoNumber(6) val limitCount: Int = 0,
        @JvmField @ProtoNumber(7) val boolIsFull: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class GetFileInfoReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val fileId: String = "",
        @JvmField @ProtoNumber(5) val fieldFlag: Int = 16777215
    ) : ProtoBuf

    @Serializable
    internal class GetFileInfoRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val fileInfo: GroupFileCommon.FileInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class GetFileListReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val folderId: String = "",
        @JvmField @ProtoNumber(4) val startTimestamp: FileTimeStamp? = null,
        @JvmField @ProtoNumber(5) val fileCount: Int = 0,
        @JvmField @ProtoNumber(6) val maxTimestamp: FileTimeStamp? = null,
        @JvmField @ProtoNumber(7) val allFileCount: Int = 0,
        @JvmField @ProtoNumber(8) val reqFrom: Int = 0,
        @JvmField @ProtoNumber(9) val sortBy: Int = 0,
        @JvmField @ProtoNumber(10) val filterCode: Int = 0,
        @JvmField @ProtoNumber(11) val uin: Long = 0L,
        @JvmField @ProtoNumber(12) val fieldFlag: Int = 16777215,
        @JvmField @ProtoNumber(13) val startIndex: Int = 0,
        @JvmField @ProtoNumber(14) val context: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(15) val clientVersion: Int = 0,
        @JvmField @ProtoNumber(16) val whiteList: Int = 0,
        @JvmField @ProtoNumber(17) val sortOrder: Int = 0,
        @JvmField @ProtoNumber(18) val showOnlinedocFolder: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetFileListRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val boolIsEnd: Boolean = false,
        @JvmField @ProtoNumber(5) val itemList: List<Item> = emptyList(),
        @JvmField @ProtoNumber(6) val msgMaxTimestamp: FileTimeStamp? = null,
        @JvmField @ProtoNumber(7) val allFileCount: Int = 0,
        @JvmField @ProtoNumber(8) val filterCode: Int = 0,
        @JvmField @ProtoNumber(11) val boolSafeCheckFlag: Boolean = false,
        @JvmField @ProtoNumber(12) val safeCheckRes: Int = 0,
        @JvmField @ProtoNumber(13) val nextIndex: Int = 0,
        @JvmField @ProtoNumber(14) val context: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(15) val role: Int = 0,
        @JvmField @ProtoNumber(16) val openFlag: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class Item(
            @JvmField @ProtoNumber(1) val type: Int = 0, // folder=2,
            @JvmField @ProtoNumber(2) val folderInfo: GroupFileCommon.FolderInfo? = null,
            @JvmField @ProtoNumber(3) val fileInfo: GroupFileCommon.FileInfo? = null
        ) : ProtoBuf {
            val id get() = fileInfo?.fileId ?: folderInfo?.folderId
            val name get() = fileInfo?.fileName ?: folderInfo?.folderName
        }
    }

    @Serializable
    internal class GetFilePreviewReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val fileId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GetFilePreviewRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val int32ServerIp: Int = 0,
        @JvmField @ProtoNumber(5) val int32ServerPort: Int = 0,
        @JvmField @ProtoNumber(6) val downloadDns: String = "",
        @JvmField @ProtoNumber(7) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(8) val cookieVal: String = "",
        @JvmField @ProtoNumber(9) val reservedField: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(10) val downloadDnsHttps: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(11) val previewPortHttps: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetSpaceReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetSpaceRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val totalSpace: Long = 0L,
        @JvmField @ProtoNumber(5) val usedSpace: Long = 0L,
        @JvmField @ProtoNumber(6) val boolAllUpload: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val fileInfoReq: GetFileInfoReqBody? = null,
        @JvmField @ProtoNumber(2) val fileListInfoReq: GetFileListReqBody? = null,
        @JvmField @ProtoNumber(3) val groupFileCntReq: GetFileCountReqBody? = null,
        @JvmField @ProtoNumber(4) val groupSpaceReq: GetSpaceReqBody? = null,
        @JvmField @ProtoNumber(5) val filePreviewReq: GetFilePreviewReqBody? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val fileInfoRsp: GetFileInfoRspBody? = null,
        @JvmField @ProtoNumber(2) val fileListInfoRsp: GetFileListRspBody? = null,
        @JvmField @ProtoNumber(3) val groupFileCntRsp: GetFileCountRspBody? = null,
        @JvmField @ProtoNumber(4) val groupSpaceRsp: GetSpaceRspBody? = null,
        @JvmField @ProtoNumber(5) val filePreviewRsp: GetFilePreviewRspBody? = null
    ) : ProtoBuf
}
        