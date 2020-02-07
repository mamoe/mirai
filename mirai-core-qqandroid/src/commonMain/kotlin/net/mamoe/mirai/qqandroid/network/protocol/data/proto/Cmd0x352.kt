package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
internal class Cmd0x352 : ProtoBuf {
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
    class GetImgUrlReq(
        @SerialId(1) val srcUin: Long = 0L,
        @SerialId(2) val dstUin: Long = 0L,
        @SerialId(3) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val urlFlag: Int = 0,
        @SerialId(6) val urlType: Int = 0,
        @SerialId(7) val reqTerm: Int = 0,
        @SerialId(8) val reqPlatformType: Int = 0,
        @SerialId(9) val srcFileType: Int = 0,
        @SerialId(10) val innerIp: Int = 0,
        @SerialId(11) val boolAddressBook: Boolean = false,
        @SerialId(12) val buType: Int = 0,
        @SerialId(13) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(14) val picUpTimestamp: Int = 0,
        @SerialId(15) val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
    class GetImgUrlRsp(
        @SerialId(1) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val clientIp: Int = 0,
        @SerialId(3) val result: Int = 0,
        @SerialId(4) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val bytesThumbDownUrl: List<ByteArray>? = null,
        @SerialId(6) val bytesOriginalDownUrl: List<ByteArray>? = null,
        @SerialId(7) val msgImgInfo: ImgInfo? = null,
        @SerialId(8) val uint32DownIp: List<Int>? = null,
        @SerialId(9) val uint32DownPort: List<Int>? = null,
        @SerialId(10) val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(13) val bytesBigDownUrl: List<ByteArray>? = null,
        @SerialId(14) val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(15) val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(16) val httpsUrlFlag: Int = 0,
        @SerialId(26) val msgDownIp6: List<IPv6Info>? = null,
        @SerialId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
    data class ImgInfo(
        @SerialId(1) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fileType: Int = 0,
        @SerialId(3) val fileSize: Long = 0L,
        @SerialId(4) val fileWidth: Int = 0,
        @SerialId(5) val fileHeight: Int = 0,
        @SerialId(6) val fileFlag: Long = 0L,
        @SerialId(7) val fileCutPos: Int = 0
    ) : ProtoBuf

    @Serializable
    class IPv6Info(
        @SerialId(1) val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val port: Int = 0
    ) : ProtoBuf

    @Serializable
    class ReqBody(
        @SerialId(1) val subcmd: Int = 0, //2是GetImgUrlReq 1是UploadImgReq
        @SerialId(2) val msgTryupImgReq: List<TryUpImgReq>? = null,// optional
        @SerialId(3) val msgGetimgUrlReq: List<GetImgUrlReq>? = null,// optional
        @SerialId(4) val msgDelImgReq: List<DelImgReq>? = null,
        @SerialId(10) val netType: Int = 3// 数据网络=5
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @SerialId(1) val subcmd: Int = 0,
        @SerialId(2) val msgTryupImgRsp: List<TryUpImgRsp>? = null,
        @SerialId(3) val msgGetimgUrlRsp: List<GetImgUrlRsp>? = null,
        @SerialId(4) val boolNewBigchan: Boolean = false,
        @SerialId(5) val msgDelImgRsp: List<DelImgRsp>? = null,
        @SerialId(10) val failMsg: String? = ""
    ) : ProtoBuf

    @Serializable
    internal class TryUpImgReq(
        @SerialId(1) val srcUin: Int,
        @SerialId(2) val dstUin: Int,
        @SerialId(3) val fileId: Int = 0,//从0开始的自增数？貌似有一个连接就要自增1, 但是又会重置回0
        @SerialId(4) val fileMd5: ByteArray,
        @SerialId(5) val fileSize: Int,
        @SerialId(6) val fileName: String,//默认为md5+".jpg"
        @SerialId(7) val srcTerm: Int = 5,
        @SerialId(8) val platformType: Int = 9,
        @SerialId(9) val innerIP: Int = 0,
        @SerialId(10) val addressBook: Int = 0,//chatType == 1006为1 我觉得发0没问题
        @SerialId(11) val retry: Int = 0,//default
        @SerialId(12) val buType: Int = 1,//1或96 不确定
        @SerialId(13) val imgOriginal: Int,//是否为原图
        @SerialId(14) val imgWidth: Int,
        @SerialId(15) val imgHeight: Int,
        /**
         * ImgType:
         *  JPG:    1000
         *  PNG:    1001
         *  WEBP:   1002
         *  BMP:    1005
         *  GIG:    2000
         *  APNG:   2001
         *  SHARPP: 1004
         */
        @SerialId(16) val imgType: Int = 1000,
        @SerialId(17) val buildVer: String = "8.2.0.1296",//版本号
        @SerialId(18) val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,//default
        @SerialId(19) val fileStoreDays: Int = 0,//default
        @SerialId(20) val stepFlag: Int = 0,//default
        @SerialId(21) val rejectTryFast: Int = 0,//bool
        @SerialId(22) val srvUpload: Int = 1,//typeHotPic[1/2/3]
        @SerialId(23) val transferUrl: ByteArray = EMPTY_BYTE_ARRAY//rawDownloadUrl, 如果没有就是EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
    class TryUpImgRsp(
        @SerialId(1) val fileId: Long = 0L,
        @SerialId(2) val clientIp: Int = 0,
        @SerialId(3) val result: Int = 0,
        @SerialId(4) val failMsg: String? = "",
        @SerialId(5) val boolFileExit: Boolean = false,
        @SerialId(6) val msgImgInfo: ImgInfo? = null,
        @SerialId(7) val uint32UpIp: List<Int>? = null,
        @SerialId(8) val uint32UpPort: List<Int>? = null,
        @SerialId(9) val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val upResid: String = "",
        @SerialId(11) val upUuid: String = "",
        @SerialId(12) val upOffset: Long = 0L,
        @SerialId(13) val blockSize: Long = 0L,
        @SerialId(14) val encryptDstip: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(15) val roamdays: Int = 0,
        @SerialId(26) val msgUpIp6: List<IPv6Info>? = null,
        @SerialId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(60) val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(61) val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(62) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(64) val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(65) val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(66) val httpsUrlFlag: Int = 0,
        @SerialId(1001) val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
    class TryUpInfo4Busi(
        @SerialId(1) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}