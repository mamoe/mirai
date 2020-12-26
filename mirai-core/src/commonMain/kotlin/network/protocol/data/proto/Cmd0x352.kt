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
internal class Cmd0x352 : ProtoBuf {
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
    internal class GetImgUrlReq(
        @ProtoNumber(1) @JvmField val srcUin: Long = 0L,
        @ProtoNumber(2) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(3) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val urlFlag: Int = 0,
        @ProtoNumber(6) @JvmField val urlType: Int = 0,
        @ProtoNumber(7) @JvmField val reqTerm: Int = 0,
        @ProtoNumber(8) @JvmField val reqPlatformType: Int = 0,
        @ProtoNumber(9) @JvmField val srcFileType: Int = 0,
        @ProtoNumber(10) @JvmField val innerIp: Int = 0,
        @ProtoNumber(11) @JvmField val boolAddressBook: Boolean = false,
        @ProtoNumber(12) @JvmField val buType: Int = 0,
        @ProtoNumber(13) @JvmField val buildVer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val picUpTimestamp: Int = 0,
        @ProtoNumber(15) @JvmField val reqTransferType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetImgUrlRsp(
        @ProtoNumber(1) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val clientIp: Int = 0,
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val failMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val bytesThumbDownUrl: List<ByteArray> = emptyList(),
        @ProtoNumber(6) @JvmField val bytesOriginalDownUrl: List<ByteArray> = emptyList(),
        @ProtoNumber(7) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoNumber(8) @JvmField val uint32DownIp: List<Int> = emptyList(),
        @ProtoNumber(9) @JvmField val uint32DownPort: List<Int> = emptyList(),
        @ProtoNumber(10) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val bytesBigDownUrl: List<ByteArray> = emptyList(),
        @ProtoNumber(14) @JvmField val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(15) @JvmField val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(16) @JvmField val httpsUrlFlag: Int = 0,
        @ProtoNumber(26) @JvmField val msgDownIp6: List<IPv6Info> = emptyList(),
        @ProtoNumber(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Suppress("ArrayInDataClass")
    @Serializable
    internal class ImgInfo(
        @ProtoNumber(1) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fileType: Int = 0,
        @ProtoNumber(3) @JvmField val fileSize: Long = 0L,
        @ProtoNumber(4) @JvmField val fileWidth: Int = 0,
        @ProtoNumber(5) @JvmField val fileHeight: Int = 0,
        @ProtoNumber(6) @JvmField val fileFlag: Long = 0L,
        @ProtoNumber(7) @JvmField val fileCutPos: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class IPv6Info(
        @ProtoNumber(1) @JvmField val ip6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val port: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0, //2是GetImgUrlReq 1是UploadImgReq
        @ProtoNumber(2) @JvmField val msgTryupImgReq: List<TryUpImgReq> = emptyList(),// optional
        @ProtoNumber(3) @JvmField val msgGetimgUrlReq: List<GetImgUrlReq> = emptyList(),// optional
        @ProtoNumber(4) @JvmField val msgDelImgReq: List<DelImgReq> = emptyList(),
        @ProtoNumber(10) @JvmField val netType: Int = 3// 数据网络=5
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val msgTryupImgRsp: List<TryUpImgRsp> = emptyList(),
        @ProtoNumber(3) @JvmField val msgGetimgUrlRsp: List<GetImgUrlRsp> = emptyList(),
        @ProtoNumber(4) @JvmField val boolNewBigchan: Boolean = false,
        @ProtoNumber(5) @JvmField val msgDelImgRsp: List<DelImgRsp> = emptyList(),
        @ProtoNumber(10) @JvmField val failMsg: String? = ""
    ) : ProtoBuf

    @Serializable
    internal class TryUpImgReq(
        @ProtoNumber(1) @JvmField val srcUin: Int,
        @ProtoNumber(2) @JvmField val dstUin: Int,
        @ProtoNumber(3) @JvmField val fileId: Int = 0,//从0开始的自增数？貌似有一个连接就要自增1, 但是又会重置回0
        @ProtoNumber(4) @JvmField val fileMd5: ByteArray,
        @ProtoNumber(5) @JvmField val fileSize: Int,
        @ProtoNumber(6) @JvmField val fileName: String,//默认为md5+".jpg"
        @ProtoNumber(7) @JvmField val srcTerm: Int = 5,
        @ProtoNumber(8) @JvmField val platformType: Int = 9,
        @ProtoNumber(9) @JvmField val innerIP: Int = 0,
        @ProtoNumber(10) @JvmField val addressBook: Int = 0,//chatType == 1006为1 我觉得发0没问题
        @ProtoNumber(11) @JvmField val retry: Int = 0,//default
        @ProtoNumber(12) @JvmField val buType: Int = 1,//1或96 不确定
        @ProtoNumber(13) @JvmField val imgOriginal: Int,//是否为原图
        @ProtoNumber(14) @JvmField val imgWidth: Int = 0,
        @ProtoNumber(15) @JvmField val imgHeight: Int = 0,
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
        @ProtoNumber(16) @JvmField val imgType: Int = 1000,
        @ProtoNumber(17) @JvmField val buildVer: String = "8.2.7.4410",//版本号
        @ProtoNumber(18) @JvmField val fileIndex: ByteArray = EMPTY_BYTE_ARRAY,//default
        @ProtoNumber(19) @JvmField val fileStoreDays: Int = 0,//default
        @ProtoNumber(20) @JvmField val stepFlag: Int = 0,//default
        @ProtoNumber(21) @JvmField val rejectTryFast: Int = 0,//bool
        @ProtoNumber(22) @JvmField val srvUpload: Int = 1,//typeHotPic[1/2/3]
        @ProtoNumber(23) @JvmField val transferUrl: ByteArray = EMPTY_BYTE_ARRAY//rawDownloadUrl, 如果没有就是EMPTY_BYTE_ARRAY
    ) : ImgReq

    @Serializable
    internal class TryUpImgRsp(
        @ProtoNumber(1) @JvmField val fileId: Long = 0L,
        @ProtoNumber(2) @JvmField val clientIp: Int = 0,
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val failMsg: String? = "",
        @ProtoNumber(5) @JvmField val boolFileExit: Boolean = false,
        @ProtoNumber(6) @JvmField val msgImgInfo: ImgInfo? = null,
        @ProtoNumber(7) @JvmField val uint32UpIp: List<Int> = emptyList(),
        @ProtoNumber(8) @JvmField val uint32UpPort: List<Int> = emptyList(),
        @ProtoNumber(9) @JvmField val upUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val upResid: String = "",
        @ProtoNumber(11) @JvmField val upUuid: String = "",
        @ProtoNumber(12) @JvmField val upOffset: Long = 0L,
        @ProtoNumber(13) @JvmField val blockSize: Long = 0L,
        @ProtoNumber(14) @JvmField val encryptDstip: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(15) @JvmField val roamdays: Int = 0,
        @ProtoNumber(26) @JvmField val msgUpIp6: List<IPv6Info> = emptyList(),
        @ProtoNumber(27) @JvmField val clientIp6: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(60) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(61) @JvmField val originalDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(62) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(64) @JvmField val bigDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(65) @JvmField val bigThumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(66) @JvmField val httpsUrlFlag: Int = 0,
        @ProtoNumber(1001) @JvmField val msgInfo4busi: TryUpInfo4Busi? = null
    ) : ProtoBuf

    @Serializable
    internal class TryUpInfo4Busi(
        @ProtoNumber(1) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val downDomain: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val thumbDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val originalDownUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val bigDownUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}