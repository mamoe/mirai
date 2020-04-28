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
import kotlin.jvm.JvmField

@Serializable
internal class Cmd0x352 : ProtoBuf {
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
    internal class GetImgUrlReq(
        @ProtoId(1) @JvmField val srcUin: Long = 0L,
        @ProtoId(2) @JvmField val dstUin: Long = 0L,
        @ProtoId(3) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val urlFlag: Int = 0,
        @ProtoId(6) @JvmField val urlType: Int = 0,
        @ProtoId(7) @JvmField val reqTerm: Int = 0,
        @ProtoId(8) @JvmField val reqPlatformType: Int = 0,
        @ProtoId(9) @JvmField val srcFileType: Int = 0,
        @ProtoId(10) @JvmField val innerIp: Int = 0,
        @ProtoId(11) @JvmField val boolAddressBook: Boolean = false,
        @ProtoId(12) @JvmField val buType: Int = 0,
        @ProtoId(13) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) @JvmField val picUpTimestamp: Int = 0,
        @ProtoId(15) @JvmField val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetImgUrlRsp(
        @ProtoId(1) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val clientIp: Int = 0,
        @ProtoId(3) @JvmField val result: Int = 0,
        @ProtoId(4) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val bytesThumbDownUrl: List<ByteArray>? = null,
        @ProtoId(6) @JvmField val bytesOriginalDownUrl: List<ByteArray>? = null,
        @ProtoId(7) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoId(8) @JvmField val uint32DownIp: List<Int>? = null,
        @ProtoId(9) @JvmField val uint32DownPort: List<Int>? = null,
        @ProtoId(10) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) @JvmField val bytesBigDownUrl: List<ByteArray>? = null,
        @ProtoId(14) @JvmField val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) @JvmField val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(16) @JvmField val httpsUrlFlag: Int = 0,
        @ProtoId(26) @JvmField val msgDownIp6: List<IPv6Info>? = null,
        @ProtoId(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
    internal class ImgInfo(
        @ProtoId(1) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val fileType: Int = 0,
        @ProtoId(3) @JvmField val fileSize: Long = 0L,
        @ProtoId(4) @JvmField val fileWidth: Int = 0,
        @ProtoId(5) @JvmField val fileHeight: Int = 0,
        @ProtoId(6) @JvmField val fileFlag: Long = 0L,
        @ProtoId(7) @JvmField val fileCutPos: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class IPv6Info(
        @ProtoId(1) @JvmField val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val port: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val subcmd: Int = 0, //2是GetImgUrlReq 1是UploadImgReq
        @ProtoId(2) @JvmField val msgTryupImgReq: List<TryUpImgReq>? = null,// optional
        @ProtoId(3) @JvmField val msgGetimgUrlReq: List<GetImgUrlReq>? = null,// optional
        @ProtoId(4) @JvmField val msgDelImgReq: List<DelImgReq>? = null,
        @ProtoId(10) @JvmField val netType: Int = 3// 数据网络=5
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val subcmd: Int = 0,
        @ProtoId(2) @JvmField val msgTryupImgRsp: List<TryUpImgRsp>? = null,
        @ProtoId(3) @JvmField val msgGetimgUrlRsp: List<GetImgUrlRsp>? = null,
        @ProtoId(4) @JvmField val boolNewBigchan: Boolean = false,
        @ProtoId(5) @JvmField val msgDelImgRsp: List<DelImgRsp>? = null,
        @ProtoId(10) @JvmField val failMsg: String? = ""
    ) : ProtoBuf

    @Serializable
    internal class TryUpImgReq(
        @ProtoId(1) @JvmField val srcUin: Int,
        @ProtoId(2) @JvmField val dstUin: Int,
        @ProtoId(3) @JvmField val fileId: Int = 0,//从0开始的自增数？貌似有一个连接就要自增1, 但是又会重置回0
        @ProtoId(4) @JvmField val fileMd5: ByteArray,
        @ProtoId(5) @JvmField val fileSize: Int,
        @ProtoId(6) @JvmField val fileName: String,//默认为md5+".jpg"
        @ProtoId(7) @JvmField val srcTerm: Int = 5,
        @ProtoId(8) @JvmField val platformType: Int = 9,
        @ProtoId(9) @JvmField val innerIP: Int = 0,
        @ProtoId(10) @JvmField val addressBook: Int = 0,//chatType == 1006为1 我觉得发0没问题
        @ProtoId(11) @JvmField val retry: Int = 0,//default
        @ProtoId(12) @JvmField val buType: Int = 1,//1或96 不确定
        @ProtoId(13) @JvmField val imgOriginal: Int,//是否为原图
        @ProtoId(14) @JvmField val imgWidth: Int = 0,
        @ProtoId(15) @JvmField val imgHeight: Int = 0,
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
        @ProtoId(16) @JvmField val imgType: Int = 1000,
        @ProtoId(17) @JvmField val buildVer: String = "8.2.7.4410",//版本号
        @ProtoId(18) @JvmField val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,//default
        @ProtoId(19) @JvmField val fileStoreDays: Int = 0,//default
        @ProtoId(20) @JvmField val stepFlag: Int = 0,//default
        @ProtoId(21) @JvmField val rejectTryFast: Int = 0,//bool
        @ProtoId(22) @JvmField val srvUpload: Int = 1,//typeHotPic[1/2/3]
        @ProtoId(23) @JvmField val transferUrl: ByteArray = EMPTY_BYTE_ARRAY//rawDownloadUrl, 如果没有就是EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
    internal class TryUpImgRsp(
        @ProtoId(1) @JvmField val fileId: Long = 0L,
        @ProtoId(2) @JvmField val clientIp: Int = 0,
        @ProtoId(3) @JvmField val result: Int = 0,
        @ProtoId(4) @JvmField val failMsg: String? = "",
        @ProtoId(5) @JvmField val boolFileExit: Boolean = false,
        @ProtoId(6) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoId(7) @JvmField val uint32UpIp: List<Int>? = null,
        @ProtoId(8) @JvmField val uint32UpPort: List<Int>? = null,
        @ProtoId(9) @JvmField val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val upResid: String = "",
        @ProtoId(11) @JvmField val upUuid: String = "",
        @ProtoId(12) @JvmField val upOffset: Long = 0L,
        @ProtoId(13) @JvmField val blockSize: Long = 0L,
        @ProtoId(14) @JvmField val encryptDstip: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) @JvmField val roamdays: Int = 0,
        @ProtoId(26) @JvmField val msgUpIp6: List<IPv6Info>? = null,
        @ProtoId(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(60) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(61) @JvmField val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(62) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(64) @JvmField val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(65) @JvmField val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(66) @JvmField val httpsUrlFlag: Int = 0,
        @ProtoId(1001) @JvmField val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
    internal class TryUpInfo4Busi(
        @ProtoId(1) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}