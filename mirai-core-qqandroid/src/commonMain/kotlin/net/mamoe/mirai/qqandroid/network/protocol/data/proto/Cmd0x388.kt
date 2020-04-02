/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
internal class Cmd0x388 : ProtoBuf {
    @Serializable
    class DelImgReq(
        @ProtoId(1) val srcUin: Long = 0L,
        @ProtoId(2) val dstUin: Long = 0L,
        @ProtoId(3) val reqTerm: Int = 0,
        @ProtoId(4) val reqPlatformType: Int = 0,
        @ProtoId(5) val buType: Int = 0,
        @ProtoId(6) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val picWidth: Int = 0,
        @ProtoId(9) val picHeight: Int = 0
    ) : ProtoBuf

    @Serializable
    class DelImgRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ExpRoamExtendInfo(
        @ProtoId(1) val resid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ExpRoamPicInfo(
        @ProtoId(1) val shopFlag: Int = 0,
        @ProtoId(2) val pkgId: Int = 0,
        @ProtoId(3) val picId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ExtensionCommPicTryUp(
        @ProtoId(1) val bytesExtinfo: List<ByteArray>? = null
    ) : ProtoBuf

    @Serializable
    class ExtensionExpRoamTryUp(
        @ProtoId(1) val msgExproamPicInfo: List<ExpRoamPicInfo>? = null
    ) : ProtoBuf

    @Serializable
    class GetImgUrlReq(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val dstUin: Long = 0L,
        @ProtoId(3) val fileid: Long = 0L,
        @ProtoId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val urlFlag: Int = 0,
        @ProtoId(6) val urlType: Int = 0,
        @ProtoId(7) val reqTerm: Int = 0,
        @ProtoId(8) val reqPlatformType: Int = 0,
        @ProtoId(9) val innerIp: Int = 0,
        @ProtoId(10) val buType: Int = 0,
        @ProtoId(11) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val fileId: Long = 0L,
        @ProtoId(13) val fileSize: Long = 0L,
        @ProtoId(14) val originalPic: Int = 0,
        @ProtoId(15) val retryReq: Int = 0,
        @ProtoId(16) val fileHeight: Int = 0,
        @ProtoId(17) val fileWidth: Int = 0,
        @ProtoId(18) val picType: Int = 0,
        @ProtoId(19) val picUpTimestamp: Int = 0,
        @ProtoId(20) val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
    class GetImgUrlRsp(
        @ProtoId(1) val fileid: Long = 0L,
        @ProtoId(2) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val msgImgInfo: ImgInfo? = null,
        @ProtoId(6) val bytesThumbDownUrl: List<ByteArray>? = null,
        @ProtoId(7) val bytesOriginalDownUrl: List<ByteArray>? = null,
        @ProtoId(8) val bytesBigDownUrl: List<ByteArray>? = null,
        @ProtoId(9) val uint32DownIp: List<Int>? = null,
        @ProtoId(10) val uint32DownPort: List<Int>? = null,
        @ProtoId(11) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) val fileId: Long = 0L,
        @ProtoId(16) val autoDownType: Int = 0,
        @ProtoId(17) val uint32OrderDownType: List<Int>? = null,
        @ProtoId(19) val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20) val httpsUrlFlag: Int = 0,
        @ProtoId(26) val msgDownIp6: List<IPv6Info>? = null,
        @ProtoId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GetPttUrlReq(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val dstUin: Long = 0L,
        @ProtoId(3) val fileid: Long = 0L,
        @ProtoId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val reqTerm: Int = 0,
        @ProtoId(6) val reqPlatformType: Int = 0,
        @ProtoId(7) val innerIp: Int = 0,
        @ProtoId(8) val buType: Int = 0,
        @ProtoId(9) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val fileId: Long = 0L,
        @ProtoId(11) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val codec: Int = 0,
        @ProtoId(13) val buId: Int = 0,
        @ProtoId(14) val reqTransferType: Int = 0,
        @ProtoId(15) val isAuto: Int = 0
    ) : ProtoBuf

    @Serializable
    class GetPttUrlRsp(
        @ProtoId(1) val fileid: Long = 0L,
        @ProtoId(2) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val bytesDownUrl: List<ByteArray>? = null,
        @ProtoId(6) val uint32DownIp: List<Int>? = null,
        @ProtoId(7) val uint32DownPort: List<Int>? = null,
        @ProtoId(8) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val downPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val fileId: Long = 0L,
        @ProtoId(11) val transferType: Int = 0,
        @ProtoId(12) val allowRetry: Int = 0,
        @ProtoId(26) val msgDownIp6: List<IPv6Info>? = null,
        @ProtoId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(28) val strDomain: String = ""
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
    class ImgInfo(
        @ProtoId(1) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fileType: Int = 0,
        @ProtoId(3) val fileSize: Long = 0L,
        @ProtoId(4) val fileWidth: Int = 0,
        @ProtoId(5) val fileHeight: Int = 0
    ) : ProtoBuf {
        override fun toString(): String {
            return "ImgInfo(fileMd5=${fileMd5.contentToString()}, fileType=$fileType, fileSize=$fileSize, fileWidth=$fileWidth, fileHeight=$fileHeight)"
        }
    }

    @Serializable
    class IPv6Info(
        @ProtoId(1) val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val port: Int = 0
    ) : ProtoBuf

    @Serializable
    class PicSize(
        @ProtoId(1) val original: Int = 0,
        @ProtoId(2) val thumb: Int = 0,
        @ProtoId(3) val high: Int = 0
    ) : ProtoBuf

    @Serializable
    class ReqBody(
        @ProtoId(1) val netType: Int = 0,
        @ProtoId(2) val subcmd: Int = 0,
        @ProtoId(3) val msgTryupImgReq: List<TryUpImgReq>? = null,
        @ProtoId(4) val msgGetimgUrlReq: List<GetImgUrlReq>? = null,
        @ProtoId(5) val msgTryupPttReq: List<TryUpPttReq>? = null,
        @ProtoId(6) val msgGetpttUrlReq: List<GetPttUrlReq>? = null,
        @ProtoId(7) val commandId: Int = 0,
        @ProtoId(8) val msgDelImgReq: List<DelImgReq>? = null,
        @ProtoId(1001) val extension: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @ProtoId(1) val clientIp: Int = 0,
        @ProtoId(2) val subcmd: Int = 0,
        @ProtoId(3) val msgTryupImgRsp: List<TryUpImgRsp>? = null,
        @ProtoId(4) val msgGetimgUrlRsp: List<GetImgUrlRsp>? = null,
        @ProtoId(5) val msgTryupPttRsp: List<TryUpPttRsp>? = null,
        @ProtoId(6) val msgGetpttUrlRsp: List<GetPttUrlRsp>? = null,
        @ProtoId(7) val msgDelImgRsp: List<DelImgRsp>? = null
    ) : ProtoBuf

    @Serializable
    class TryUpImgReq(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val srcUin: Long = 0L,
        @ProtoId(3) val fileId: Long = 0L,
        @ProtoId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileSize: Long = 0L,
        @ProtoId(6) val fileName: String = "",
        @ProtoId(7) val srcTerm: Int = 0,
        @ProtoId(8) val platformType: Int = 0,
        @ProtoId(9) val buType: Int = 0,
        @ProtoId(10) val picWidth: Int = 0,
        @ProtoId(11) val picHeight: Int = 0,
        @ProtoId(12) val picType: Int = 0,
        @ProtoId(13) val buildVer: String = "",
        @ProtoId(14) val innerIp: Int = 0,
        @ProtoId(15) val appPicType: Int = 0,
        @ProtoId(16) val originalPic: Int = 0,
        @ProtoId(17) val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(18) val dstUin: Long = 0L,
        @ProtoId(19) val srvUpload: Int = 0,
        @ProtoId(20) val transferUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
    class TryUpImgRsp(
        @ProtoId(1) val fileId: Long = 0L,
        @ProtoId(2) val result: Int = 0,
        @ProtoId(3) val failMsg: String = "",
        @ProtoId(4) val boolFileExit: Boolean = false,
        @ProtoId(5) val msgImgInfo: ImgInfo? = null,
        @ProtoId(6) val uint32UpIp: List<Int>? = null,
        @ProtoId(7) val uint32UpPort: List<Int>? = null,
        @ProtoId(8) val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val fileid: Long = 0L,
        @ProtoId(10) val upOffset: Long = 0L,
        @ProtoId(11) val blockSize: Long = 0L,
        @ProtoId(12) val boolNewBigChan: Boolean = false,
        @ProtoId(26) val msgUpIp6: List<IPv6Info>? = null,
        @ProtoId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(1001) val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
    class TryUpInfo4Busi(
        @ProtoId(1) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class TryUpPttReq(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val srcUin: Long = 0L,
        @ProtoId(3) val fileId: Long = 0L,
        @ProtoId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileSize: Long = 0L,
        @ProtoId(6) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val srcTerm: Int = 0,
        @ProtoId(8) val platformType: Int = 0,
        @ProtoId(9) val buType: Int = 0,
        @ProtoId(10) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val innerIp: Int = 0,
        @ProtoId(12) val voiceLength: Int = 0,
        @ProtoId(13) val boolNewUpChan: Boolean = false,
        @ProtoId(14) val codec: Int = 0,
        @ProtoId(15) val voiceType: Int = 0,
        @ProtoId(16) val buId: Int = 0
    ) : ProtoBuf

    @Serializable
    class TryUpPttRsp(
        @ProtoId(1) val fileId: Long = 0L,
        @ProtoId(2) val result: Int = 0,
        @ProtoId(3) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val boolFileExit: Boolean = false,
        @ProtoId(5) val uint32UpIp: List<Int>? = null,
        @ProtoId(6) val uint32UpPort: List<Int>? = null,
        @ProtoId(7) val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val fileid: Long = 0L,
        @ProtoId(9) val upOffset: Long = 0L,
        @ProtoId(10) val blockSize: Long = 0L,
        @ProtoId(11) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val channelType: Int = 0,
        @ProtoId(26) val msgUpIp6: List<IPv6Info>? = null,
        @ProtoId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}