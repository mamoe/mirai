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
internal class Cmd0x352 : ProtoBuf {
    @Serializable
internal class DelImgReq(
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
internal class DelImgRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val fileResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class GetImgUrlReq(
        @ProtoId(1) val srcUin: Long = 0L,
        @ProtoId(2) val dstUin: Long = 0L,
        @ProtoId(3) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val urlFlag: Int = 0,
        @ProtoId(6) val urlType: Int = 0,
        @ProtoId(7) val reqTerm: Int = 0,
        @ProtoId(8) val reqPlatformType: Int = 0,
        @ProtoId(9) val srcFileType: Int = 0,
        @ProtoId(10) val innerIp: Int = 0,
        @ProtoId(11) val boolAddressBook: Boolean = false,
        @ProtoId(12) val buType: Int = 0,
        @ProtoId(13) val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val picUpTimestamp: Int = 0,
        @ProtoId(15) val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class GetImgUrlRsp(
        @ProtoId(1) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val clientIp: Int = 0,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val bytesThumbDownUrl: List<ByteArray>? = null,
        @ProtoId(6) val bytesOriginalDownUrl: List<ByteArray>? = null,
        @ProtoId(7) val msgImgInfo: ImgInfo? = null,
        @ProtoId(8) val uint32DownIp: List<Int>? = null,
        @ProtoId(9) val uint32DownPort: List<Int>? = null,
        @ProtoId(10) val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val bytesBigDownUrl: List<ByteArray>? = null,
        @ProtoId(14) val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(16) val httpsUrlFlag: Int = 0,
        @ProtoId(26) val msgDownIp6: List<IPv6Info>? = null,
        @ProtoId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
internal class ImgInfo(
        @ProtoId(1) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fileType: Int = 0,
        @ProtoId(3) val fileSize: Long = 0L,
        @ProtoId(4) val fileWidth: Int = 0,
        @ProtoId(5) val fileHeight: Int = 0,
        @ProtoId(6) val fileFlag: Long = 0L,
        @ProtoId(7) val fileCutPos: Int = 0
    ) : ProtoBuf

    @Serializable
internal class IPv6Info(
        @ProtoId(1) val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val port: Int = 0
    ) : ProtoBuf

    @Serializable
internal class ReqBody(
        @ProtoId(1) val subcmd: Int = 0, //2是GetImgUrlReq 1是UploadImgReq
        @ProtoId(2) val msgTryupImgReq: List<TryUpImgReq>? = null,// optional
        @ProtoId(3) val msgGetimgUrlReq: List<GetImgUrlReq>? = null,// optional
        @ProtoId(4) val msgDelImgReq: List<DelImgReq>? = null,
        @ProtoId(10) val netType: Int = 3// 数据网络=5
    ) : ProtoBuf

    @Serializable
internal class RspBody(
        @ProtoId(1) val subcmd: Int = 0,
        @ProtoId(2) val msgTryupImgRsp: List<TryUpImgRsp>? = null,
        @ProtoId(3) val msgGetimgUrlRsp: List<GetImgUrlRsp>? = null,
        @ProtoId(4) val boolNewBigchan: Boolean = false,
        @ProtoId(5) val msgDelImgRsp: List<DelImgRsp>? = null,
        @ProtoId(10) val failMsg: String? = ""
    ) : ProtoBuf

    @Serializable
    internal class TryUpImgReq(
        @ProtoId(1) val srcUin: Int,
        @ProtoId(2) val dstUin: Int,
        @ProtoId(3) val fileId: Int = 0,//从0开始的自增数？貌似有一个连接就要自增1, 但是又会重置回0
        @ProtoId(4) val fileMd5: ByteArray,
        @ProtoId(5) val fileSize: Int,
        @ProtoId(6) val fileName: String,//默认为md5+".jpg"
        @ProtoId(7) val srcTerm: Int = 5,
        @ProtoId(8) val platformType: Int = 9,
        @ProtoId(9) val innerIP: Int = 0,
        @ProtoId(10) val addressBook: Int = 0,//chatType == 1006为1 我觉得发0没问题
        @ProtoId(11) val retry: Int = 0,//default
        @ProtoId(12) val buType: Int = 1,//1或96 不确定
        @ProtoId(13) val imgOriginal: Int,//是否为原图
        @ProtoId(14) val imgWidth: Int,
        @ProtoId(15) val imgHeight: Int,
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
        @ProtoId(16) val imgType: Int = 1000,
        @ProtoId(17) val buildVer: String = "8.2.7.4410",//版本号
        @ProtoId(18) val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,//default
        @ProtoId(19) val fileStoreDays: Int = 0,//default
        @ProtoId(20) val stepFlag: Int = 0,//default
        @ProtoId(21) val rejectTryFast: Int = 0,//bool
        @ProtoId(22) val srvUpload: Int = 1,//typeHotPic[1/2/3]
        @ProtoId(23) val transferUrl: ByteArray = EMPTY_BYTE_ARRAY//rawDownloadUrl, 如果没有就是EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
internal class TryUpImgRsp(
        @ProtoId(1) val fileId: Long = 0L,
        @ProtoId(2) val clientIp: Int = 0,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val failMsg: String? = "",
        @ProtoId(5) val boolFileExit: Boolean = false,
        @ProtoId(6) val msgImgInfo: ImgInfo? = null,
        @ProtoId(7) val uint32UpIp: List<Int>? = null,
        @ProtoId(8) val uint32UpPort: List<Int>? = null,
        @ProtoId(9) val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val upResid: String = "",
        @ProtoId(11) val upUuid: String = "",
        @ProtoId(12) val upOffset: Long = 0L,
        @ProtoId(13) val blockSize: Long = 0L,
        @ProtoId(14) val encryptDstip: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) val roamdays: Int = 0,
        @ProtoId(26) val msgUpIp6: List<IPv6Info>? = null,
        @ProtoId(27) val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(60) val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(61) val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(62) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(64) val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(65) val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(66) val httpsUrlFlag: Int = 0,
        @ProtoId(1001) val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
internal class TryUpInfo4Busi(
        @ProtoId(1) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}