package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class Cmd0x388 : ProtoBuf {
    @Serializable
internal class DelImgReq(
        @ProtoId(1) @JvmField val srcUin: Long = 0L,
        @ProtoId(2) @JvmField val dstUin: Long = 0L,
        @ProtoId(3) @JvmField val reqTerm: Int = 0,
        @ProtoId(4) @JvmField val reqPlatformType: Int = 0,
        @ProtoId(5) @JvmField val buType: Int = 0,
        @ProtoId(6) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val picWidth: Int = 0,
        @ProtoId(9) @JvmField val picHeight: Int = 0
    ) : ProtoBuf

    @Serializable
internal class DelImgRsp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ExpRoamExtendInfo(
        @ProtoId(1) @JvmField val resid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ExpRoamPicInfo(
        @ProtoId(1) @JvmField val shopFlag: Int = 0,
        @ProtoId(2) @JvmField val pkgId: Int = 0,
        @ProtoId(3) @JvmField val picId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ExtensionCommPicTryUp(
        @ProtoId(1) @JvmField val bytesExtinfo: List<ByteArray>? = null
    ) : ProtoBuf

    @Serializable
internal class ExtensionExpRoamTryUp(
        @ProtoId(1) @JvmField val msgExproamPicInfo: List<ExpRoamPicInfo>? = null
    ) : ProtoBuf

    @Serializable
internal class GetImgUrlReq(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val dstUin: Long = 0L,
        @ProtoId(3) @JvmField val fileid: Long = 0L,
        @ProtoId(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val urlFlag: Int = 0,
        @ProtoId(6) @JvmField val urlType: Int = 0,
        @ProtoId(7) @JvmField val reqTerm: Int = 0,
        @ProtoId(8) @JvmField val reqPlatformType: Int = 0,
        @ProtoId(9) @JvmField val innerIp: Int = 0,
        @ProtoId(10) @JvmField val buType: Int = 0,
        @ProtoId(11) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val fileId: Long = 0L,
        @ProtoId(13) @JvmField val fileSize: Long = 0L,
        @ProtoId(14) @JvmField val originalPic: Int = 0,
        @ProtoId(15) @JvmField val retryReq: Int = 0,
        @ProtoId(16) @JvmField val fileHeight: Int = 0,
        @ProtoId(17) @JvmField val fileWidth: Int = 0,
        @ProtoId(18) @JvmField val picType: Int = 0,
        @ProtoId(19) @JvmField val picUpTimestamp: Int = 0,
        @ProtoId(20) @JvmField val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class GetImgUrlRsp(
        @ProtoId(1) @JvmField val fileid: Long = 0L,
        @ProtoId(2) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val result: Int = 0,
        @ProtoId(4) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoId(6) @JvmField val bytesThumbDownUrl: List<ByteArray>? = null,
        @ProtoId(7) @JvmField val bytesOriginalDownUrl: List<ByteArray>? = null,
        @ProtoId(8) @JvmField val bytesBigDownUrl: List<ByteArray>? = null,
        @ProtoId(9) @JvmField val uint32DownIp: List<Int>? = null,
        @ProtoId(10) @JvmField val uint32DownPort: List<Int>? = null,
        @ProtoId(11) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) @JvmField val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) @JvmField val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) @JvmField val fileId: Long = 0L,
        @ProtoId(16) @JvmField val autoDownType: Int = 0,
        @ProtoId(17) @JvmField val uint32OrderDownType: List<Int>? = null,
        @ProtoId(19) @JvmField val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20) @JvmField val httpsUrlFlag: Int = 0,
        @ProtoId(26) @JvmField val msgDownIp6: List<IPv6Info>? = null,
        @ProtoId(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class GetPttUrlReq(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val dstUin: Long = 0L,
        @ProtoId(3) @JvmField val fileid: Long = 0L,
        @ProtoId(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val reqTerm: Int = 0,
        @ProtoId(6) @JvmField val reqPlatformType: Int = 0,
        @ProtoId(7) @JvmField val innerIp: Int = 0,
        @ProtoId(8) @JvmField val buType: Int = 0,
        @ProtoId(9) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val fileId: Long = 0L,
        @ProtoId(11) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val codec: Int = 0,
        @ProtoId(13) @JvmField val buId: Int = 0,
        @ProtoId(14) @JvmField val reqTransferType: Int = 0,
        @ProtoId(15) @JvmField val isAuto: Int = 0
    ) : ProtoBuf

    @Serializable
internal class GetPttUrlRsp(
        @ProtoId(1) @JvmField val fileid: Long = 0L,
        @ProtoId(2) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val result: Int = 0,
        @ProtoId(4) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val bytesDownUrl: List<ByteArray>? = null,
        @ProtoId(6) @JvmField val uint32DownIp: List<Int>? = null,
        @ProtoId(7) @JvmField val uint32DownPort: List<Int>? = null,
        @ProtoId(8) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val downPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val fileId: Long = 0L,
        @ProtoId(11) @JvmField val transferType: Int = 0,
        @ProtoId(12) @JvmField val allowRetry: Int = 0,
        @ProtoId(26) @JvmField val msgDownIp6: List<IPv6Info>? = null,
        @ProtoId(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(28) @JvmField val strDomain: String = ""
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
internal class ImgInfo(
        @ProtoId(1) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val fileType: Int = 0,
        @ProtoId(3) @JvmField val fileSize: Long = 0L,
        @ProtoId(4) @JvmField val fileWidth: Int = 0,
        @ProtoId(5) @JvmField val fileHeight: Int = 0
    ) : ProtoBuf {
        override fun toString(): String {
            return "ImgInfo(fileMd5=${fileMd5.contentToString()}, fileType=$fileType, fileSize=$fileSize, fileWidth=$fileWidth, fileHeight=$fileHeight)"
        }
    }

    @Serializable
internal class IPv6Info(
        @ProtoId(1) @JvmField val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val port: Int = 0
    ) : ProtoBuf

    @Serializable
internal class PicSize(
        @ProtoId(1) @JvmField val original: Int = 0,
        @ProtoId(2) @JvmField val thumb: Int = 0,
        @ProtoId(3) @JvmField val high: Int = 0
    ) : ProtoBuf

    @Serializable
internal class ReqBody(
        @ProtoId(1) @JvmField val netType: Int = 0,
        @ProtoId(2) @JvmField val subcmd: Int = 0,
        @ProtoId(3) @JvmField val msgTryupImgReq: List<TryUpImgReq>? = null,
        @ProtoId(4) @JvmField val msgGetimgUrlReq: List<GetImgUrlReq>? = null,
        @ProtoId(5) @JvmField val msgTryupPttReq: List<TryUpPttReq>? = null,
        @ProtoId(6) @JvmField val msgGetpttUrlReq: List<GetPttUrlReq>? = null,
        @ProtoId(7) @JvmField val commandId: Int = 0,
        @ProtoId(8) @JvmField val msgDelImgReq: List<DelImgReq>? = null,
        @ProtoId(1001) @JvmField val extension: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class RspBody(
        @ProtoId(1) @JvmField val clientIp: Int = 0,
        @ProtoId(2) @JvmField val subcmd: Int = 0,
        @ProtoId(3) @JvmField val msgTryupImgRsp: List<TryUpImgRsp>? = null,
        @ProtoId(4) @JvmField val msgGetimgUrlRsp: List<GetImgUrlRsp>? = null,
        @ProtoId(5) @JvmField val msgTryupPttRsp: List<TryUpPttRsp>? = null,
        @ProtoId(6) @JvmField val msgGetpttUrlRsp: List<GetPttUrlRsp>? = null,
        @ProtoId(7) @JvmField val msgDelImgRsp: List<DelImgRsp>? = null
    ) : ProtoBuf

    @Serializable
internal class TryUpImgReq(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val srcUin: Long = 0L,
        @ProtoId(3) @JvmField val fileId: Long = 0L,
        @ProtoId(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val fileSize: Long = 0L,
        @ProtoId(6) @JvmField val fileName: String = "",
        @ProtoId(7) @JvmField val srcTerm: Int = 0,
        @ProtoId(8) @JvmField val platformType: Int = 0,
        @ProtoId(9) @JvmField val buType: Int = 0,
        @ProtoId(10) @JvmField val picWidth: Int = 0,
        @ProtoId(11) @JvmField val picHeight: Int = 0,
        @ProtoId(12) @JvmField val picType: Int = 0,
        @ProtoId(13) @JvmField val buildVer: String = "",
        @ProtoId(14) @JvmField val innerIp: Int = 0,
        @ProtoId(15) @JvmField val appPicType: Int = 0,
        @ProtoId(16) @JvmField val originalPic: Int = 0,
        @ProtoId(17) @JvmField val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(18) @JvmField val dstUin: Long = 0L,
        @ProtoId(19) @JvmField val srvUpload: Int = 0,
        @ProtoId(20) @JvmField val transferUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
internal class TryUpImgRsp(
        @ProtoId(1) @JvmField val fileId: Long = 0L,
        @ProtoId(2) @JvmField val result: Int = 0,
        @ProtoId(3) @JvmField val failMsg: String = "",
        @ProtoId(4) @JvmField val boolFileExit: Boolean = false,
        @ProtoId(5) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoId(6) @JvmField val uint32UpIp: List<Int>? = null,
        @ProtoId(7) @JvmField val uint32UpPort: List<Int>? = null,
        @ProtoId(8) @JvmField val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val fileid: Long = 0L,
        @ProtoId(10) @JvmField val upOffset: Long = 0L,
        @ProtoId(11) @JvmField val blockSize: Long = 0L,
        @ProtoId(12) @JvmField val boolNewBigChan: Boolean = false,
        @ProtoId(26) @JvmField val msgUpIp6: List<IPv6Info>? = null,
        @ProtoId(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(1001) @JvmField val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
internal class TryUpInfo4Busi(
        @ProtoId(1) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class TryUpPttReq(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val srcUin: Long = 0L,
        @ProtoId(3) @JvmField val fileId: Long = 0L,
        @ProtoId(4) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val fileSize: Long = 0L,
        @ProtoId(6) @JvmField val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val srcTerm: Int = 0,
        @ProtoId(8) @JvmField val platformType: Int = 0,
        @ProtoId(9) @JvmField val buType: Int = 0,
        @ProtoId(10) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val innerIp: Int = 0,
        @ProtoId(12) @JvmField val voiceLength: Int = 0,
        @ProtoId(13) @JvmField val boolNewUpChan: Boolean = false,
        @ProtoId(14) @JvmField val codec: Int = 0,
        @ProtoId(15) @JvmField val voiceType: Int = 0,
        @ProtoId(16) @JvmField val buId: Int = 0
    ) : ProtoBuf

    @Serializable
internal class TryUpPttRsp(
        @ProtoId(1) @JvmField val fileId: Long = 0L,
        @ProtoId(2) @JvmField val result: Int = 0,
        @ProtoId(3) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val boolFileExit: Boolean = false,
        @ProtoId(5) @JvmField val uint32UpIp: List<Int>? = null,
        @ProtoId(6) @JvmField val uint32UpPort: List<Int>? = null,
        @ProtoId(7) @JvmField val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val fileid: Long = 0L,
        @ProtoId(9) @JvmField val upOffset: Long = 0L,
        @ProtoId(10) @JvmField val blockSize: Long = 0L,
        @ProtoId(11) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val channelType: Int = 0,
        @ProtoId(26) @JvmField val msgUpIp6: List<IPv6Info>? = null,
        @ProtoId(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}