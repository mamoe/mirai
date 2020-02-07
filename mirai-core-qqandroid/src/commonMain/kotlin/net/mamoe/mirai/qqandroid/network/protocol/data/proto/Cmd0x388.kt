package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
internal class Cmd0x388 : ProtoBuf {
    @Serializable
    class DelImgReq(
        @SerialId(1) val srcUin: Long = 0L,
        @SerialId(2) val dstUin: Long = 0L,
        @SerialId(3) val reqTerm: Int = 0,
        @SerialId(4) val reqPlatformType: Int = 0,
        @SerialId(5) val buType: Int = 0,
        @SerialId(6) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val picWidth: Int = 0,
        @SerialId(9) val picHeight: Int = 0
    ) : ProtoBuf

    @Serializable
    class DelImgRsp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ExpRoamExtendInfo(
        @SerialId(1) val resid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ExpRoamPicInfo(
        @SerialId(1) val shopFlag: Int = 0,
        @SerialId(2) val pkgId: Int = 0,
        @SerialId(3) val picId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ExtensionCommPicTryUp(
        @SerialId(1) val bytesExtinfo: List<ByteArray>? = null
    ) : ProtoBuf

    @Serializable
    class ExtensionExpRoamTryUp(
        @SerialId(1) val msgExproamPicInfo: List<ExpRoamPicInfo>? = null
    ) : ProtoBuf

    @Serializable
    class GetImgUrlReq(
        @SerialId(1) val groupCode: Long = 0L,
        @SerialId(2) val dstUin: Long = 0L,
        @SerialId(3) val fileid: Long = 0L,
        @SerialId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val urlFlag: Int = 0,
        @SerialId(6) val urlType: Int = 0,
        @SerialId(7) val reqTerm: Int = 0,
        @SerialId(8) val reqPlatformType: Int = 0,
        @SerialId(9) val innerIp: Int = 0,
        @SerialId(10) val buType: Int = 0,
        @SerialId(11) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val fileId: Long = 0L,
        @SerialId(13) val fileSize: Long = 0L,
        @SerialId(14) val originalPic: Int = 0,
        @SerialId(15) val retryReq: Int = 0,
        @SerialId(16) val fileHeight: Int = 0,
        @SerialId(17) val fileWidth: Int = 0,
        @SerialId(18) val picType: Int = 0,
        @SerialId(19) val picUpTimestamp: Int = 0,
        @SerialId(20) val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
    class GetImgUrlRsp(
        @SerialId(1) val fileid: Long = 0L,
        @SerialId(2) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val result: Int = 0,
        @SerialId(4) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val msgImgInfo: ImgInfo? = null,
        @SerialId(6) val bytesThumbDownUrl: List<ByteArray>? = null,
        @SerialId(7) val bytesOriginalDownUrl: List<ByteArray>? = null,
        @SerialId(8) val bytesBigDownUrl: List<ByteArray>? = null,
        @SerialId(9) val uint32DownIp: List<Int>? = null,
        @SerialId(10) val uint32DownPort: List<Int>? = null,
        @SerialId(11) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(13) val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(14) val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(15) val fileId: Long = 0L,
        @SerialId(16) val autoDownType: Int = 0,
        @SerialId(17) val uint32OrderDownType: List<Int>? = null,
        @SerialId(19) val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(20) val httpsUrlFlag: Int = 0,
        @SerialId(26) val msgDownIp6: List<IPv6Info>? = null,
        @SerialId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class GetPttUrlReq(
        @SerialId(1) val groupCode: Long = 0L,
        @SerialId(2) val dstUin: Long = 0L,
        @SerialId(3) val fileid: Long = 0L,
        @SerialId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val reqTerm: Int = 0,
        @SerialId(6) val reqPlatformType: Int = 0,
        @SerialId(7) val innerIp: Int = 0,
        @SerialId(8) val buType: Int = 0,
        @SerialId(9) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val fileId: Long = 0L,
        @SerialId(11) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val codec: Int = 0,
        @SerialId(13) val buId: Int = 0,
        @SerialId(14) val reqTransferType: Int = 0,
        @SerialId(15) val isAuto: Int = 0
    ) : ProtoBuf

    @Serializable
    class GetPttUrlRsp(
        @SerialId(1) val fileid: Long = 0L,
        @SerialId(2) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val result: Int = 0,
        @SerialId(4) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val bytesDownUrl: List<ByteArray>? = null,
        @SerialId(6) val uint32DownIp: List<Int>? = null,
        @SerialId(7) val uint32DownPort: List<Int>? = null,
        @SerialId(8) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val downPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val fileId: Long = 0L,
        @SerialId(11) val transferType: Int = 0,
        @SerialId(12) val allowRetry: Int = 0,
        @SerialId(26) val msgDownIp6: List<IPv6Info>? = null,
        @SerialId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(28) val strDomain: String = ""
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
    data class ImgInfo(
        @SerialId(1) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fileType: Int = 0,
        @SerialId(3) val fileSize: Long = 0L,
        @SerialId(4) val fileWidth: Int = 0,
        @SerialId(5) val fileHeight: Int = 0
    ) : ProtoBuf {
        override fun toString(): String {
            return "ImgInfo(fileMd5=${fileMd5.contentToString()}, fileType=$fileType, fileSize=$fileSize, fileWidth=$fileWidth, fileHeight=$fileHeight)"
        }
    }

    @Serializable
    class IPv6Info(
        @SerialId(1) val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val port: Int = 0
    ) : ProtoBuf

    @Serializable
    class PicSize(
        @SerialId(1) val original: Int = 0,
        @SerialId(2) val thumb: Int = 0,
        @SerialId(3) val high: Int = 0
    ) : ProtoBuf

    @Serializable
    class ReqBody(
        @SerialId(1) val netType: Int = 0,
        @SerialId(2) val subcmd: Int = 0,
        @SerialId(3) val msgTryupImgReq: List<TryUpImgReq>? = null,
        @SerialId(4) val msgGetimgUrlReq: List<GetImgUrlReq>? = null,
        @SerialId(5) val msgTryupPttReq: List<TryUpPttReq>? = null,
        @SerialId(6) val msgGetpttUrlReq: List<GetPttUrlReq>? = null,
        @SerialId(7) val commandId: Int = 0,
        @SerialId(8) val msgDelImgReq: List<DelImgReq>? = null,
        @SerialId(1001) val extension: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @SerialId(1) val clientIp: Int = 0,
        @SerialId(2) val subcmd: Int = 0,
        @SerialId(3) val msgTryupImgRsp: List<TryUpImgRsp>? = null,
        @SerialId(4) val msgGetimgUrlRsp: List<GetImgUrlRsp>? = null,
        @SerialId(5) val msgTryupPttRsp: List<TryUpPttRsp>? = null,
        @SerialId(6) val msgGetpttUrlRsp: List<GetPttUrlRsp>? = null,
        @SerialId(7) val msgDelImgRsp: List<DelImgRsp>? = null
    ) : ProtoBuf

    @Serializable
    class TryUpImgReq(
        @SerialId(1) val groupCode: Long = 0L,
        @SerialId(2) val srcUin: Long = 0L,
        @SerialId(3) val fileId: Long = 0L,
        @SerialId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileSize: Long = 0L,
        @SerialId(6) val fileName: String ="",
        @SerialId(7) val srcTerm: Int = 0,
        @SerialId(8) val platformType: Int = 0,
        @SerialId(9) val buType: Int = 0,
        @SerialId(10) val picWidth: Int = 0,
        @SerialId(11) val picHeight: Int = 0,
        @SerialId(12) val picType: Int = 0,
        @SerialId(13) val buildVer: String = "",
        @SerialId(14) val innerIp: Int = 0,
        @SerialId(15) val appPicType: Int = 0,
        @SerialId(16) val originalPic: Int = 0,
        @SerialId(17) val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(18) val dstUin: Long = 0L,
        @SerialId(19) val srvUpload: Int = 0,
        @SerialId(20) val transferUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
    class TryUpImgRsp(
        @SerialId(1) val fileId: Long = 0L,
        @SerialId(2) val result: Int = 0,
        @SerialId(3) val failMsg: String = "",
        @SerialId(4) val boolFileExit: Boolean = false,
        @SerialId(5) val msgImgInfo: ImgInfo? = null,
        @SerialId(6) val uint32UpIp: List<Int>? = null,
        @SerialId(7) val uint32UpPort: List<Int>? = null,
        @SerialId(8) val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val fileid: Long = 0L,
        @SerialId(10) val upOffset: Long = 0L,
        @SerialId(11) val blockSize: Long = 0L,
        @SerialId(12) val boolNewBigChan: Boolean = false,
        @SerialId(26) val msgUpIp6: List<IPv6Info>? = null,
        @SerialId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(1001) val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
    class TryUpInfo4Busi(
        @SerialId(1) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class TryUpPttReq(
        @SerialId(1) val groupCode: Long = 0L,
        @SerialId(2) val srcUin: Long = 0L,
        @SerialId(3) val fileId: Long = 0L,
        @SerialId(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileSize: Long = 0L,
        @SerialId(6) val fileName: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val srcTerm: Int = 0,
        @SerialId(8) val platformType: Int = 0,
        @SerialId(9) val buType: Int = 0,
        @SerialId(10) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val innerIp: Int = 0,
        @SerialId(12) val voiceLength: Int = 0,
        @SerialId(13) val boolNewUpChan: Boolean = false,
        @SerialId(14) val codec: Int = 0,
        @SerialId(15) val voiceType: Int = 0,
        @SerialId(16) val buId: Int = 0
    ) : ProtoBuf

    @Serializable
    class TryUpPttRsp(
        @SerialId(1) val fileId: Long = 0L,
        @SerialId(2) val result: Int = 0,
        @SerialId(3) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val boolFileExit: Boolean = false,
        @SerialId(5) val uint32UpIp: List<Int>? = null,
        @SerialId(6) val uint32UpPort: List<Int>? = null,
        @SerialId(7) val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val fileid: Long = 0L,
        @SerialId(9) val upOffset: Long = 0L,
        @SerialId(10) val blockSize: Long = 0L,
        @SerialId(11) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val channelType: Int = 0,
        @SerialId(26) val msgUpIp6: List<IPv6Info>? = null,
        @SerialId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}