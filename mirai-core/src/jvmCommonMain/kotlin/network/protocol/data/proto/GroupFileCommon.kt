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

internal class GroupFileCommon : ProtoBuf {
    @Serializable
    internal class FeedsInfo(
        @JvmField @ProtoNumber(1) val busId: Int = 0,
        @JvmField @ProtoNumber(2) val fileId: String = "",
        @JvmField @ProtoNumber(3) val msgRandom: Int = 0,
        @JvmField @ProtoNumber(4) val ext: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(5) val feedFlag: Int = 0,
        @JvmField @ProtoNumber(6) val msgCtrl: MsgCtrl.MsgCtrl? = null
    ) : ProtoBuf

    @Serializable
    internal class FeedsResult(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val detail: String = "",
        @JvmField @ProtoNumber(3) val fileId: String = "",
        @JvmField @ProtoNumber(4) val busId: Int = 0,
        @JvmField @ProtoNumber(5) val deadTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FileInfo(
        @JvmField @ProtoNumber(1) val fileId: String = "",
        @JvmField @ProtoNumber(2) val fileName: String = "",
        @JvmField @ProtoNumber(3) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(4) val busId: Int = 0,
        @JvmField @ProtoNumber(5) val uploadedSize: Long = 0L,
        @JvmField @ProtoNumber(6) val uploadTime: Int = 0,
        @JvmField @ProtoNumber(7) val deadTime: Int = 0,
        @JvmField @ProtoNumber(8) val modifyTime: Int = 0,
        @JvmField @ProtoNumber(9) val downloadTimes: Int = 0,
        @JvmField @ProtoNumber(10) val sha: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(11) val sha3: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(12) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(13) val localPath: String = "",
        @JvmField @ProtoNumber(14) val uploaderName: String = "",
        @JvmField @ProtoNumber(15) val uploaderUin: Long = 0L,
        @JvmField @ProtoNumber(16) val parentFolderId: String = "",
        @JvmField @ProtoNumber(17) val safeType: Int = 0,
        @JvmField @ProtoNumber(20) val fileBlobExt: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(21) val ownerUin: Long = 0L,
        @JvmField @ProtoNumber(22) val feedId: String = "",
        @JvmField @ProtoNumber(23) val reservedField: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class FileInfoTmem(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val files: List<FileInfo> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class FileItem(
        @JvmField @ProtoNumber(1) val type: Int = 0,
        @JvmField @ProtoNumber(2) val folderInfo: FolderInfo? = null,
        @JvmField @ProtoNumber(3) val fileInfo: FileInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class FolderInfo(
        @JvmField @ProtoNumber(1) val folderId: String = "", // uuid
        @JvmField @ProtoNumber(2) val parentFolderId: String = "",
        @JvmField @ProtoNumber(3) val folderName: String = "",
        @JvmField @ProtoNumber(4) val createTime: Int = 0,
        @JvmField @ProtoNumber(5) val modifyTime: Int = 0,
        @JvmField @ProtoNumber(6) val createUin: Long = 0L,
        @JvmField @ProtoNumber(7) val creatorName: String = "",
        @JvmField @ProtoNumber(8) val totalFileCount: Int = 0,
        @JvmField @ProtoNumber(9) val modifyUin: Long = 0L,
        @JvmField @ProtoNumber(10) val modifyName: String = "",
        @JvmField @ProtoNumber(11) val usedSpace: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class FolderInfoTmem(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val folders: List<FolderInfo> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class OverwriteInfo(
        @JvmField @ProtoNumber(1) val fileId: String = "",
        @JvmField @ProtoNumber(2) val downloadTimes: Int = 0
    ) : ProtoBuf
}
        