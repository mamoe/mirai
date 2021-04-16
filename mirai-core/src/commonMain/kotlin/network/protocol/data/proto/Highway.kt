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
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY


/**
 * v8.5.5
 */

@Serializable
internal class BdhExtinfo : ProtoBuf {
    @Serializable
    internal class CommFileExtReq(
        @JvmField @ProtoNumber(1) val actionType: Int = 0,
        @JvmField @ProtoNumber(2) val uuid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommFileExtRsp(
        @JvmField @ProtoNumber(1) val int32Retcode: Int = 0,
        @JvmField @ProtoNumber(2) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PicInfo(
        @JvmField @ProtoNumber(1) val idx: Int = 0,
        @JvmField @ProtoNumber(2) val size: Int = 0,
        @JvmField @ProtoNumber(3) val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(4) val type: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceExtReq(
        @JvmField @ProtoNumber(1) val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val fmt: Int = 0,
        @JvmField @ProtoNumber(3) val rate: Int = 0,
        @JvmField @ProtoNumber(4) val bits: Int = 0,
        @JvmField @ProtoNumber(5) val channel: Int = 0,
        @JvmField @ProtoNumber(6) val pinyin: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceExtRsp(
        @JvmField @ProtoNumber(1) val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val int32Retcode: Int = 0,
        @JvmField @ProtoNumber(3) val msgResult: List<QQVoiceResult> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceResult(
        @JvmField @ProtoNumber(1) val text: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val pinyin: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val source: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoReqExtInfo(
        @JvmField @ProtoNumber(1) val cmd: Int = 0,
        @JvmField @ProtoNumber(2) val sessionId: Long = 0L,
        @JvmField @ProtoNumber(3) val msgThumbinfo: PicInfo? = null,
        @JvmField @ProtoNumber(4) val msgVideoinfo: VideoInfo? = null,
        @JvmField @ProtoNumber(5) val msgShortvideoSureReq: ShortVideoSureReqInfo? = null,
        @JvmField @ProtoNumber(6) val boolIsMergeCmdBeforeData: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoRspExtInfo(
        @JvmField @ProtoNumber(1) val cmd: Int = 0,
        @JvmField @ProtoNumber(2) val sessionId: Long = 0L,
        @JvmField @ProtoNumber(3) val int32Retcode: Int = 0,
        @JvmField @ProtoNumber(4) val errinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(5) val msgThumbinfo: PicInfo? = null,
        @JvmField @ProtoNumber(6) val msgVideoinfo: VideoInfo? = null,
        @JvmField @ProtoNumber(7) val msgShortvideoSureRsp: ShortVideoSureRspInfo? = null,
        @JvmField @ProtoNumber(8) val retryFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoSureReqInfo(
        @JvmField @ProtoNumber(1) val fromuin: Long = 0L,
        @JvmField @ProtoNumber(2) val chatType: Int = 0,
        @JvmField @ProtoNumber(3) val touin: Long = 0L,
        @JvmField @ProtoNumber(4) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(5) val clientType: Int = 0,
        @JvmField @ProtoNumber(6) val msgThumbinfo: PicInfo? = null,
        @JvmField @ProtoNumber(7) val msgMergeVideoinfo: List<VideoInfo> = emptyList(),
        @JvmField @ProtoNumber(8) val msgDropVideoinfo: List<VideoInfo> = emptyList(),
        @JvmField @ProtoNumber(9) val businessType: Int = 0,
        @JvmField @ProtoNumber(10) val subBusinessType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoSureRspInfo(
        @JvmField @ProtoNumber(1) val fileid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val ukey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val msgVideoinfo: VideoInfo? = null,
        @JvmField @ProtoNumber(4) val mergeCost: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class StoryVideoExtReq : ProtoBuf

    @Serializable
    internal class StoryVideoExtRsp(
        @JvmField @ProtoNumber(1) val int32Retcode: Int = 0,
        @JvmField @ProtoNumber(2) val msg: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val cdnUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(4) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(5) val fileId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class UploadPicExtInfo(
        @JvmField @ProtoNumber(1) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val thumbDownloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class VideoInfo(
        @JvmField @ProtoNumber(1) val idx: Int = 0,
        @JvmField @ProtoNumber(2) val size: Int = 0,
        @JvmField @ProtoNumber(3) val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(4) val format: Int = 0,
        @JvmField @ProtoNumber(5) val resLen: Int = 0,
        @JvmField @ProtoNumber(6) val resWidth: Int = 0,
        @JvmField @ProtoNumber(7) val time: Int = 0,
        @JvmField @ProtoNumber(8) val starttime: Long = 0L,
        @JvmField @ProtoNumber(9) val isAudio: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class CSDataHighwayHead : ProtoBuf {
    @Serializable
    internal class C2CCommonExtendinfo(
        @JvmField @ProtoNumber(1) val infoId: Int = 0,
        @JvmField @ProtoNumber(2) val msgFilterExtendinfo: FilterExtendinfo? = null
    ) : ProtoBuf

    @Serializable
    internal class DataHighwayHead(
        @JvmField @ProtoNumber(1) val version: Int = 0,
        @JvmField @ProtoNumber(2) val uin: String = "",
        @JvmField @ProtoNumber(3) val command: String = "",
        @JvmField @ProtoNumber(4) val seq: Int = 0,
        @JvmField @ProtoNumber(5) val retryTimes: Int? = null,// = 0,
        @JvmField @ProtoNumber(6) val appid: Int? = null,// = 0,
        @JvmField @ProtoNumber(7) val dataflag: Int? = null,// = 0,
        @JvmField @ProtoNumber(8) val commandId: Int? = null,// = 0,
        @JvmField @ProtoNumber(9) val buildVer: String = "",
        @JvmField @ProtoNumber(10) val localeId: Int = 0,
        @JvmField @ProtoNumber(11) val envId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DataHole(
        @JvmField @ProtoNumber(1) val begin: Long = 0L,
        @JvmField @ProtoNumber(2) val end: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class FilterExtendinfo(
        @JvmField @ProtoNumber(1) val filterFlag: Int = 0,
        @JvmField @ProtoNumber(2) val msgImageFilterRequest: ImageFilterRequest? = null
    ) : ProtoBuf

    @Serializable
    internal class FilterStyle(
        @JvmField @ProtoNumber(1) val styleId: Int = 0,
        @JvmField @ProtoNumber(2) val styleName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ImageFilterRequest(
        @JvmField @ProtoNumber(1) val sessionId: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val clientIp: Int = 0,
        @JvmField @ProtoNumber(3) val uin: Long = 0L,
        @JvmField @ProtoNumber(4) val style: FilterStyle? = null,
        @JvmField @ProtoNumber(5) val width: Int = 0,
        @JvmField @ProtoNumber(6) val height: Int = 0,
        @JvmField @ProtoNumber(7) val imageData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ImageFilterResponse(
        @JvmField @ProtoNumber(1) val retCode: Int = 0,
        @JvmField @ProtoNumber(2) val imageData: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val costTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class LoginSigHead(
        @JvmField @ProtoNumber(1) val loginsigType: Int = 0,
        @JvmField @ProtoNumber(2) val loginsig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NewServiceTicket(
        @JvmField @ProtoNumber(1) val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val ukey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PicInfoExt(
        @JvmField @ProtoNumber(1) val picWidth: Int = 0,
        @JvmField @ProtoNumber(2) val picHeight: Int = 0,
        @JvmField @ProtoNumber(3) val picFlag: Int = 0,
        @JvmField @ProtoNumber(4) val busiType: Int = 0,
        @JvmField @ProtoNumber(5) val srcTerm: Int = 0,
        @JvmField @ProtoNumber(6) val platType: Int = 0,
        @JvmField @ProtoNumber(7) val netType: Int = 0,
        @JvmField @ProtoNumber(8) val imgType: Int = 0,
        @JvmField @ProtoNumber(9) val appPicType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PicRspExtInfo(
        @JvmField @ProtoNumber(1) val skey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val clientIp: Int = 0,
        @JvmField @ProtoNumber(3) val upOffset: Long = 0L,
        @JvmField @ProtoNumber(4) val blockSize: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class QueryHoleRsp(
        @JvmField @ProtoNumber(1) val result: Int = 0,
        @JvmField @ProtoNumber(2) val dataHole: List<DataHole> = emptyList(),
        @JvmField @ProtoNumber(3) val boolCompFlag: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class ReqDataHighwayHead(
        @JvmField @ProtoNumber(1) val msgBasehead: DataHighwayHead? = null,
        @JvmField @ProtoNumber(2) val msgSeghead: SegHead? = null,
        @JvmField @ProtoNumber(3) val reqExtendinfo: ByteArray, // = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(4) val timestamp: Long = 0L,
        @JvmField @ProtoNumber(5) val msgLoginSigHead: LoginSigHead? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val msgQueryHoleRsp: QueryHoleRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class RspDataHighwayHead(
        @JvmField @ProtoNumber(1) val msgBasehead: DataHighwayHead? = null,
        @JvmField @ProtoNumber(2) val msgSeghead: SegHead? = null,
        @JvmField @ProtoNumber(3) val errorCode: Int = 0,
        @JvmField @ProtoNumber(4) val allowRetry: Int = 0,
        @JvmField @ProtoNumber(5) val cachecost: Int = 0,
        @JvmField @ProtoNumber(6) val htcost: Int = 0,
        @JvmField @ProtoNumber(7) val rspExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(8) val timestamp: Long = 0L,
        @JvmField @ProtoNumber(9) val range: Long = 0L,
        @JvmField @ProtoNumber(10) val isReset: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SegHead(
        @JvmField @ProtoNumber(1) val serviceid: Int = 0,
        @JvmField @ProtoNumber(2) val filesize: Long = 0L,
        @JvmField @ProtoNumber(3) val dataoffset: Long = 0L,
        @JvmField @ProtoNumber(4) val datalength: Int = 0,
        @JvmField @ProtoNumber(5) val rtcode: Int? = null, // = 0,
        @JvmField @ProtoNumber(6) val serviceticket: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val flag: Int? = null, // = 0,
        @JvmField @ProtoNumber(8) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(9) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(10) val cacheAddr: Int = 0,
        @JvmField @ProtoNumber(11) val queryTimes: Int = 0,
        @JvmField @ProtoNumber(12) val updateCacheip: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class HwConfigPersistentPB : ProtoBuf {
    @Serializable
    internal class HwConfigItemPB(
        @JvmField @ProtoNumber(1) val key: String = "",
        @JvmField @ProtoNumber(2) val endPointList: List<HwEndPointPB> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class HwConfigPB(
        @JvmField @ProtoNumber(1) val configItemList: List<HwConfigItemPB> = emptyList(),
        @JvmField @ProtoNumber(2) val netSegConfList: List<HwNetSegConfPB> = emptyList(),
        @JvmField @ProtoNumber(3) val shortVideoNetConf: List<HwNetSegConfPB> = emptyList(),
        @JvmField @ProtoNumber(4) val configItemListIp6: List<HwConfigItemPB> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class HwEndPointPB(
        @JvmField @ProtoNumber(1) val host: String = "",
        @JvmField @ProtoNumber(2) val int32Port: Int = 0,
        @JvmField @ProtoNumber(3) val int64Timestampe: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class HwNetSegConfPB(
        @JvmField @ProtoNumber(1) val int64NetType: Long = 0L,
        @JvmField @ProtoNumber(2) val int64SegSize: Long = 0L,
        @JvmField @ProtoNumber(3) val int64SegNum: Long = 0L,
        @JvmField @ProtoNumber(4) val int64CurConnNum: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class HwSessionInfoPersistentPB : ProtoBuf {
    @Serializable
    internal class HwSessionInfoPB(
        @JvmField @ProtoNumber(1) val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Subcmd0x501 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1281) val msgSubcmd0x501ReqBody: SubCmd0x501ReqBody? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1281) val msgSubcmd0x501RspBody: SubCmd0x501Rspbody? = null
    ) : ProtoBuf

    @Serializable
    internal class SubCmd0x501ReqBody(
        @JvmField @ProtoNumber(1) val uin: Long = 0L,
        @JvmField @ProtoNumber(2) val idcId: Int = 0,
        @JvmField @ProtoNumber(3) val appid: Int = 0,
        @JvmField @ProtoNumber(4) val loginSigType: Int = 0,
        @JvmField @ProtoNumber(5) val loginSigTicket: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(6) val requestFlag: Int = 0,
        @JvmField @ProtoNumber(7) val uint32ServiceTypes: List<Int> = emptyList(),
        @JvmField @ProtoNumber(8) val bid: Int = 0,
        @JvmField @ProtoNumber(9) val term: Int = 0,
        @JvmField @ProtoNumber(10) val plat: Int = 0,
        @JvmField @ProtoNumber(11) val net: Int = 0,
        @JvmField @ProtoNumber(12) val caller: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SubCmd0x501Rspbody(
        @JvmField @ProtoNumber(1) val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(2) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val msgHttpconnAddrs: List<SrvAddrs> = emptyList(),
        @JvmField @ProtoNumber(4) val preConnection: Int = 0,
        @JvmField @ProtoNumber(5) val csConn: Int = 0,
        @JvmField @ProtoNumber(6) val msgIpLearnConf: IpLearnConf? = null,
        @JvmField @ProtoNumber(7) val msgDynTimeoutConf: DynTimeOutConf? = null,
        @JvmField @ProtoNumber(8) val msgOpenUpConf: OpenUpConf? = null,
        @JvmField @ProtoNumber(9) val msgDownloadEncryptConf: DownloadEncryptConf? = null,
        @JvmField @ProtoNumber(10) val msgShortVideoConf: ShortVideoConf? = null,
        @JvmField @ProtoNumber(11) val msgPtvConf: PTVConf? = null,
        @JvmField @ProtoNumber(12) val shareType: Int = 0,
        @JvmField @ProtoNumber(13) val shareChannel: Int = 0,
        @JvmField @ProtoNumber(14) val fmtPolicy: Int = 0,
        @JvmField @ProtoNumber(15) val bigdataPolicy: Int = 0,
        @JvmField @ProtoNumber(16) val connAttemptDelay: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class DownloadEncryptConf(
            @JvmField @ProtoNumber(1) val boolEnableEncryptRequest: Boolean = false,
            @JvmField @ProtoNumber(2) val boolEnableEncryptedPic: Boolean = false,
            @JvmField @ProtoNumber(3) val ctrlFlag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DynTimeOutConf(
            @JvmField @ProtoNumber(1) val tbase2g: Int = 0,
            @JvmField @ProtoNumber(2) val tbase3g: Int = 0,
            @JvmField @ProtoNumber(3) val tbase4g: Int = 0,
            @JvmField @ProtoNumber(4) val tbaseWifi: Int = 0,
            @JvmField @ProtoNumber(5) val torg2g: Int = 0,
            @JvmField @ProtoNumber(6) val torg3g: Int = 0,
            @JvmField @ProtoNumber(7) val torg4g: Int = 0,
            @JvmField @ProtoNumber(8) val torgWifi: Int = 0,
            @JvmField @ProtoNumber(9) val maxTimeout: Int = 0,
            @JvmField @ProtoNumber(10) val enableDynTimeout: Int = 0,
            @JvmField @ProtoNumber(11) val maxTimeout2g: Int = 0,
            @JvmField @ProtoNumber(12) val maxTimeout3g: Int = 0,
            @JvmField @ProtoNumber(13) val maxTimeout4g: Int = 0,
            @JvmField @ProtoNumber(14) val maxTimeoutWifi: Int = 0,
            @JvmField @ProtoNumber(15) val hbTimeout2g: Int = 0,
            @JvmField @ProtoNumber(16) val hbTimeout3g: Int = 0,
            @JvmField @ProtoNumber(17) val hbTimeout4g: Int = 0,
            @JvmField @ProtoNumber(18) val hbTimeoutWifi: Int = 0,
            @JvmField @ProtoNumber(19) val hbTimeoutDefault: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Ip6Addr(
            @JvmField @ProtoNumber(1) val type: Int = 0,
            @JvmField @ProtoNumber(2) val ip6: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(3) val port: Int = 0,
            @JvmField @ProtoNumber(4) val area: Int = 0,
            @JvmField @ProtoNumber(5) val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class IpAddr(
            @JvmField @ProtoNumber(1) val type: Int = 0,
            @ProtoType(ProtoIntegerType.FIXED) @JvmField @ProtoNumber(2) val ip: Int = 0,
            @JvmField @ProtoNumber(3) val port: Int = 0,
            @JvmField @ProtoNumber(4) val area: Int = 0,
            @JvmField @ProtoNumber(5) val sameIsp: Int = 0
        ) : ProtoBuf {
            fun decode(): Pair<Int, Int> = ip to port
        }

        @Serializable
        internal class IpLearnConf(
            @JvmField @ProtoNumber(1) val refreshCachedIp: Int = 0,
            @JvmField @ProtoNumber(2) val enableIpLearn: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class NetSegConf(
            @JvmField @ProtoNumber(1) val netType: Int = 0,
            @JvmField @ProtoNumber(2) val segsize: Int = 0,
            @JvmField @ProtoNumber(3) val segnum: Int = 0,
            @JvmField @ProtoNumber(4) val curconnnum: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OpenUpConf(
            @JvmField @ProtoNumber(1) val boolEnableOpenup: Boolean = false,
            @JvmField @ProtoNumber(2) val preSendSegnum: Int = 0,
            @JvmField @ProtoNumber(3) val preSendSegnum3g: Int = 0,
            @JvmField @ProtoNumber(4) val preSendSegnum4g: Int = 0,
            @JvmField @ProtoNumber(5) val preSendSegnumWifi: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class PTVConf(
            @JvmField @ProtoNumber(1) val channelType: Int = 0,
            @JvmField @ProtoNumber(2) val msgNetsegconf: List<NetSegConf> = emptyList(),
            @JvmField @ProtoNumber(3) val boolOpenHardwareCodec: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class ShortVideoConf(
            @JvmField @ProtoNumber(1) val channelType: Int = 0,
            @JvmField @ProtoNumber(2) val msgNetsegconf: List<NetSegConf> = emptyList(),
            @JvmField @ProtoNumber(3) val boolOpenHardwareCodec: Boolean = false,
            @JvmField @ProtoNumber(4) val boolSendAheadSignal: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal data class SrvAddrs(
            @JvmField @ProtoNumber(1) val serviceType: Int = 0,
            @JvmField @ProtoNumber(2) val msgAddrs: List<IpAddr> = emptyList(),
            @JvmField @ProtoNumber(3) val fragmentSize: Int = 0,
            @JvmField @ProtoNumber(4) val msgNetsegconf: List<NetSegConf> = emptyList(),
            @JvmField @ProtoNumber(5) val msgAddrsV6: List<Ip6Addr> = emptyList()
        ) : ProtoBuf
    }
}
        