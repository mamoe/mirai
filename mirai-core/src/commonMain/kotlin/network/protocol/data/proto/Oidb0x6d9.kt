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

internal class Oidb0x6d9 : ProtoBuf {
    @Serializable
    internal class CopyFromReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val srcBusId: Int = 0,
        @JvmField @ProtoNumber(4) val srcParentFolder: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(5) val srcFilePath: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(6) val dstBusId: Int = 0,
        @JvmField @ProtoNumber(7) val dstFolderId: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(8) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(9) val localPath: String = "",
        @JvmField @ProtoNumber(10) val fileName: String = "",
        @JvmField @ProtoNumber(11) val srcUin: Long = 0L,
        @JvmField @ProtoNumber(12) val md5: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CopyFromRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val saveFilePath: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(5) val busId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CopyToReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val srcBusId: Int = 0,
        @JvmField @ProtoNumber(4) val srcFileId: String = "",
        @JvmField @ProtoNumber(5) val dstBusId: Int = 0,
        @JvmField @ProtoNumber(6) val dstUin: Long = 0L,
        @JvmField @ProtoNumber(40) val newFileName: String = "",
        @JvmField @ProtoNumber(100) val timCloudPdirKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(101) val timCloudPpdirKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(102) val timCloudExtensionInfo: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(103) val timFileExistOption: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CopyToRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val saveFilePath: String = "",
        @JvmField @ProtoNumber(5) val busId: Int = 0,
        @JvmField @ProtoNumber(40) val fileName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class FeedsReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val feedsInfoList: List<GroupFileCommon.FeedsInfo> = emptyList(),
        @JvmField @ProtoNumber(4) val multiSendSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FeedsRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val feedsResultList: List<GroupFileCommon.FeedsResult> = emptyList(),
        @JvmField @ProtoNumber(5) val svrbusyWaitTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val transFileReq: TransFileReqBody? = null,
        @JvmField @ProtoNumber(2) val copyFromReq: CopyFromReqBody? = null,
        @JvmField @ProtoNumber(3) val copyToReq: CopyToReqBody? = null,
        @JvmField @ProtoNumber(5) val feedsInfoReq: FeedsReqBody? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val transFileRsp: TransFileRspBody? = null,
        @JvmField @ProtoNumber(2) val copyFromRsp: CopyFromRspBody? = null,
        @JvmField @ProtoNumber(3) val copyToRsp: CopyToRspBody? = null,
        @JvmField @ProtoNumber(5) val feedsInfoRsp: FeedsRspBody? = null
    ) : ProtoBuf

    @Serializable
    internal class TransFileReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val busId: Int = 0,
        @JvmField @ProtoNumber(4) val fileId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TransFileRspBody(
        @JvmField @ProtoNumber(1) val int32RetCode: Int = 0,
        @JvmField @ProtoNumber(2) val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val saveBusId: Int = 0,
        @JvmField @ProtoNumber(5) val saveFilePath: String = ""
    ) : ProtoBuf
}
        