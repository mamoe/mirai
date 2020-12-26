/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

@Serializable
internal class Cmd0x388 : ProtoBuf {
    @Serializable
internal class DelImgReq(
        @ProtoNumber(1) @JvmField val srcUin: Long = 0L,
        @ProtoNumber(2) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(3) @JvmField val reqTerm: Int = 0,
        @ProtoNumber(4) @JvmField val reqPlatformType: Int = 0,
        @ProtoNumber(5) @JvmField val buType: Int = 0,
        @ProtoNumber(6) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val picWidth: Int = 0,
        @ProtoNumber(9) @JvmField val picHeight: Int = 0
    ) : ProtoBuf

    @Serializable
internal class DelImgRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ExpRoamExtendInfo(
        @ProtoNumber(1) @JvmField val resid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ExpRoamPicInfo(
        @ProtoNumber(1) @JvmField val shopFlag: Int = 0,
        @ProtoNumber(2) @JvmField val pkgId: Int = 0,
        @ProtoNumber(3) @JvmField val picId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ExtensionCommPicTryUp(
        @ProtoNumber(1) @JvmField val bytesExtinfo: List<ByteArray> = emptyList()
    ) : ProtoBuf

    @Serializable
internal class ExtensionExpRoamTryUp(
        @ProtoNumber(1) @JvmField val msgExproamPicInfo: List<ExpRoamPicInfo> = emptyList()
    ) : ProtoBuf

    @Serializable
internal class GetImgUrlReq(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(3) @JvmField val fileid: Long = 0L,
        @ProtoNumber(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val urlFlag: Int = 0,
        @ProtoNumber(6) @JvmField val urlType: Int = 0,
        @ProtoNumber(7) @JvmField val reqTerm: Int = 0,
        @ProtoNumber(8) @JvmField val reqPlatformType: Int = 0,
        @ProtoNumber(9) @JvmField val innerIp: Int = 0,
        @ProtoNumber(10) @JvmField val buType: Int = 0,
        @ProtoNumber(11) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val fileId: Long = 0L,
        @ProtoNumber(13) @JvmField val fileSize: Long = 0L,
        @ProtoNumber(14) @JvmField val originalPic: Int = 0,
        @ProtoNumber(15) @JvmField val retryReq: Int = 0,
        @ProtoNumber(16) @JvmField val fileHeight: Int = 0,
        @ProtoNumber(17) @JvmField val fileWidth: Int = 0,
        @ProtoNumber(18) @JvmField val picType: Int = 0,
        @ProtoNumber(19) @JvmField val picUpTimestamp: Int = 0,
        @ProtoNumber(20) @JvmField val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class GetImgUrlRsp(
        @ProtoNumber(1) @JvmField val fileid: Long = 0L,
        @ProtoNumber(2) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoNumber(6) @JvmField val bytesThumbDownUrl: List<ByteArray> = emptyList(),
        @ProtoNumber(7) @JvmField val bytesOriginalDownUrl: List<ByteArray> = emptyList(),
        @ProtoNumber(8) @JvmField val bytesBigDownUrl: List<ByteArray> = emptyList(),
        @ProtoNumber(9) @JvmField val uint32DownIp: List<Int> = emptyList(),
        @ProtoNumber(10) @JvmField val uint32DownPort: List<Int> = emptyList(),
        @ProtoNumber(11) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(15) @JvmField val fileId: Long = 0L,
        @ProtoNumber(16) @JvmField val autoDownType: Int = 0,
        @ProtoNumber(17) @JvmField val uint32OrderDownType: List<Int> = emptyList(),
        @ProtoNumber(19) @JvmField val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20) @JvmField val httpsUrlFlag: Int = 0,
        @ProtoNumber(26) @JvmField val msgDownIp6: List<IPv6Info> = emptyList(),
        @ProtoNumber(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class GetPttUrlReq(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(3) @JvmField val fileid: Long = 0L,
        @ProtoNumber(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val reqTerm: Int = 0,
        @ProtoNumber(6) @JvmField val reqPlatformType: Int = 0,
        @ProtoNumber(7) @JvmField val innerIp: Int = 0,
        @ProtoNumber(8) @JvmField val buType: Int = 0,
        @ProtoNumber(9) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val fileId: Long = 0L,
        @ProtoNumber(11) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val codec: Int = 0,
        @ProtoNumber(13) @JvmField val buId: Int = 0,
        @ProtoNumber(14) @JvmField val reqTransferType: Int = 0,
        @ProtoNumber(15) @JvmField val isAuto: Int = 0
    ) : ProtoBuf

    @Serializable
internal class GetPttUrlRsp(
        @ProtoNumber(1) @JvmField val fileid: Long = 0L,
        @ProtoNumber(2) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val bytesDownUrl: List<ByteArray> = emptyList(),
        @ProtoNumber(6) @JvmField val uint32DownIp: List<Int> = emptyList(),
        @ProtoNumber(7) @JvmField val uint32DownPort: List<Int> = emptyList(),
        @ProtoNumber(8) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val downPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val fileId: Long = 0L,
        @ProtoNumber(11) @JvmField val transferType: Int = 0,
        @ProtoNumber(12) @JvmField val allowRetry: Int = 0,
        @ProtoNumber(26) @JvmField val msgDownIp6: List<IPv6Info> = emptyList(),
        @ProtoNumber(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(28) @JvmField val strDomain: String = ""
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
internal class ImgInfo(
        @ProtoNumber(1) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fileType: Int = 0,
        @ProtoNumber(3) @JvmField val fileSize: Long = 0L,
        @ProtoNumber(4) @JvmField val fileWidth: Int = 0,
        @ProtoNumber(5) @JvmField val fileHeight: Int = 0
    ) : ProtoBuf {
        override fun toString(): String {
            return "ImgInfo(fileMd5=${fileMd5.contentToString()}, fileType=$fileType, fileSize=$fileSize, fileWidth=$fileWidth, fileHeight=$fileHeight)"
        }
    }

    @Serializable
internal class IPv6Info(
        @ProtoNumber(1) @JvmField val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val port: Int = 0
    ) : ProtoBuf

    @Serializable
internal class PicSize(
        @ProtoNumber(1) @JvmField val original: Int = 0,
        @ProtoNumber(2) @JvmField val thumb: Int = 0,
        @ProtoNumber(3) @JvmField val high: Int = 0
    ) : ProtoBuf

    @Serializable
internal class ReqBody(
        @ProtoNumber(1) @JvmField val netType: Int = 0,
        @ProtoNumber(2) @JvmField val subcmd: Int = 0,
        @ProtoNumber(3) @JvmField val msgTryupImgReq: List<TryUpImgReq> = emptyList(),
        @ProtoNumber(4) @JvmField val msgGetimgUrlReq: List<GetImgUrlReq> = emptyList(),
        @ProtoNumber(5) @JvmField val msgTryupPttReq: List<TryUpPttReq> = emptyList(),
        @ProtoNumber(6) @JvmField val msgGetpttUrlReq: List<GetPttUrlReq> = emptyList(),
        @ProtoNumber(7) @JvmField val commandId: Int = 0,
        @ProtoNumber(8) @JvmField val msgDelImgReq: List<DelImgReq> = emptyList(),
        @ProtoNumber(1001) @JvmField val extension: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class RspBody(
        @ProtoNumber(1) @JvmField val clientIp: Int = 0,
        @ProtoNumber(2) @JvmField val subcmd: Int = 0,
        @ProtoNumber(3) @JvmField val msgTryupImgRsp: List<TryUpImgRsp> = emptyList(),
        @ProtoNumber(4) @JvmField val msgGetimgUrlRsp: List<GetImgUrlRsp> = emptyList(),
        @ProtoNumber(5) @JvmField val msgTryupPttRsp: List<TryUpPttRsp> = emptyList(),
        @ProtoNumber(6) @JvmField val msgGetpttUrlRsp: List<GetPttUrlRsp> = emptyList(),
        @ProtoNumber(7) @JvmField val msgDelImgRsp: List<DelImgRsp> = emptyList()
    ) : ProtoBuf

    @Serializable
internal class TryUpImgReq(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val srcUin: Long = 0L,
        @ProtoNumber(3) @JvmField val fileId: Long = 0L,
        @ProtoNumber(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileSize: Long = 0L,
        @ProtoNumber(6) @JvmField val fileName: String = "",
        @ProtoNumber(7) @JvmField val srcTerm: Int = 0,
        @ProtoNumber(8) @JvmField val platformType: Int = 0,
        @ProtoNumber(9) @JvmField val buType: Int = 0,
        @ProtoNumber(10) @JvmField val picWidth: Int = 0,
        @ProtoNumber(11) @JvmField val picHeight: Int = 0,
        @ProtoNumber(12) @JvmField val picType: Int = 0,
        @ProtoNumber(13) @JvmField val buildVer: String = "",
        @ProtoNumber(14) @JvmField val innerIp: Int = 0,
        @ProtoNumber(15) @JvmField val appPicType: Int = 0,
        @ProtoNumber(16) @JvmField val originalPic: Int = 0,
        @ProtoNumber(17) @JvmField val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(18) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(19) @JvmField val srvUpload: Int = 0,
        @ProtoNumber(20) @JvmField val transferUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
internal class TryUpImgRsp(
        @ProtoNumber(1) @JvmField val fileId: Long = 0L,
        @ProtoNumber(2) @JvmField val result: Int = 0,
        @ProtoNumber(3) @JvmField val failMsg: String = "",
        @ProtoNumber(4) @JvmField val boolFileExit: Boolean = false,
        @ProtoNumber(5) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoNumber(6) @JvmField val uint32UpIp: List<Int> = emptyList(),
        @ProtoNumber(7) @JvmField val uint32UpPort: List<Int> = emptyList(),
        @ProtoNumber(8) @JvmField val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val fileid: Long = 0L,
        @ProtoNumber(10) @JvmField val upOffset: Long = 0L,
        @ProtoNumber(11) @JvmField val blockSize: Long = 0L,
        @ProtoNumber(12) @JvmField val boolNewBigChan: Boolean = false,
        @ProtoNumber(26) @JvmField val msgUpIp6: List<IPv6Info> = emptyList(),
        @ProtoNumber(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(1001) @JvmField val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
internal class TryUpInfo4Busi(
        @ProtoNumber(1) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class TryUpPttReq(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val srcUin: Long = 0L,
        @ProtoNumber(3) @JvmField val fileId: Long = 0L,
        @ProtoNumber(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileSize: Long = 0L,
        @ProtoNumber(6) @JvmField val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val srcTerm: Int = 0,
        @ProtoNumber(8) @JvmField val platformType: Int = 0,
        @ProtoNumber(9) @JvmField val buType: Int = 0,
        @ProtoNumber(10) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val innerIp: Int = 0,
        @ProtoNumber(12) @JvmField val voiceLength: Int = 0,
        @ProtoNumber(13) @JvmField val boolNewUpChan: Boolean = false,
        @ProtoNumber(14) @JvmField val codec: Int = 0,
        @ProtoNumber(15) @JvmField val voiceType: Int = 0,
        @ProtoNumber(16) @JvmField val buId: Int = 0
    ) : ProtoBuf

    @Serializable
internal class TryUpPttRsp(
        @ProtoNumber(1) @JvmField val fileId: Long = 0L,
        @ProtoNumber(2) @JvmField val result: Int = 0,
        @ProtoNumber(3) @JvmField val failMsg: ByteArray? = null,
        @ProtoNumber(4) @JvmField val boolFileExit: Boolean = false,
        @ProtoNumber(5) @JvmField val uint32UpIp: List<Int> = emptyList(),
        @ProtoNumber(6) @JvmField val uint32UpPort: List<Int> = emptyList(),
        @ProtoNumber(7) @JvmField val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val fileid: Long = 0L,
        @ProtoNumber(9) @JvmField val upOffset: Long = 0L,
        @ProtoNumber(10) @JvmField val blockSize: Long = 0L,
        @ProtoNumber(11) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val channelType: Int = 0,
        @ProtoNumber(26) @JvmField val msgUpIp6: List<IPv6Info> = emptyList(),
        @ProtoNumber(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}