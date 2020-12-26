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
import kotlinx.serialization.protobuf.ProtoIntegerType
import kotlinx.serialization.protobuf.ProtoNumber
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

@Serializable
internal class BdhExtinfo : ProtoBuf {
    @Serializable
    internal class CommFileExtReq(
        @ProtoNumber(1) @JvmField val actionType: Int = 0,
        @ProtoNumber(2) @JvmField val uuid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommFileExtRsp(
        @ProtoNumber(1) @JvmField val int32Retcode: Int = 0,
        @ProtoNumber(2) @JvmField val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PicInfo(
        @ProtoNumber(1) @JvmField val idx: Int = 0,
        @ProtoNumber(2) @JvmField val size: Int = 0,
        @ProtoNumber(3) @JvmField val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val type: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceExtReq(
        @ProtoNumber(1) @JvmField val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fmt: Int = 0,
        @ProtoNumber(3) @JvmField val rate: Int = 0,
        @ProtoNumber(4) @JvmField val bits: Int = 0,
        @ProtoNumber(5) @JvmField val channel: Int = 0,
        @ProtoNumber(6) @JvmField val pinyin: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceExtRsp(
        @ProtoNumber(1) @JvmField val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val int32Retcode: Int = 0,
        @ProtoNumber(3) @JvmField val msgResult: List<QQVoiceResult> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceResult(
        @ProtoNumber(1) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val pinyin: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val source: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoReqExtInfo(
        @ProtoNumber(1) @JvmField val cmd: Int = 0,
        @ProtoNumber(2) @JvmField val sessionId: Long = 0L,
        @ProtoNumber(3) @JvmField val msgThumbinfo: PicInfo? = null,
        @ProtoNumber(4) @JvmField val msgVideoinfo: VideoInfo? = null,
        @ProtoNumber(5) @JvmField val msgShortvideoSureReq: ShortVideoSureReqInfo? = null,
        @ProtoNumber(6) @JvmField val boolIsMergeCmdBeforeData: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoRspExtInfo(
        @ProtoNumber(1) @JvmField val cmd: Int = 0,
        @ProtoNumber(2) @JvmField val sessionId: Long = 0L,
        @ProtoNumber(3) @JvmField val int32Retcode: Int = 0,
        @ProtoNumber(4) @JvmField val errinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val msgThumbinfo: PicInfo? = null,
        @ProtoNumber(6) @JvmField val msgVideoinfo: VideoInfo? = null,
        @ProtoNumber(7) @JvmField val msgShortvideoSureRsp: ShortVideoSureRspInfo? = null,
        @ProtoNumber(8) @JvmField val retryFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoSureReqInfo(
        @ProtoNumber(1) @JvmField val fromuin: Long = 0L,
        @ProtoNumber(2) @JvmField val chatType: Int = 0,
        @ProtoNumber(3) @JvmField val touin: Long = 0L,
        @ProtoNumber(4) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(5) @JvmField val clientType: Int = 0,
        @ProtoNumber(6) @JvmField val msgThumbinfo: PicInfo? = null,
        @ProtoNumber(7) @JvmField val msgMergeVideoinfo: List<VideoInfo> = emptyList(),
        @ProtoNumber(8) @JvmField val msgDropVideoinfo: List<VideoInfo> = emptyList(),
        @ProtoNumber(9) @JvmField val businessType: Int = 0,
        @ProtoNumber(10) @JvmField val subBusinessType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoSureRspInfo(
        @ProtoNumber(1) @JvmField val fileid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val ukey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgVideoinfo: VideoInfo? = null,
        @ProtoNumber(4) @JvmField val mergeCost: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class StoryVideoExtReq : ProtoBuf

    @Serializable
    internal class StoryVideoExtRsp(
        @ProtoNumber(1) @JvmField val int32Retcode: Int = 0,
        @ProtoNumber(2) @JvmField val msg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val cdnUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fileId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class UploadPicExtInfo(
        @ProtoNumber(1) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val thumbDownloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class VideoInfo(
        @ProtoNumber(1) @JvmField val idx: Int = 0,
        @ProtoNumber(2) @JvmField val size: Int = 0,
        @ProtoNumber(3) @JvmField val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val format: Int = 0,
        @ProtoNumber(5) @JvmField val resLen: Int = 0,
        @ProtoNumber(6) @JvmField val resWidth: Int = 0,
        @ProtoNumber(7) @JvmField val time: Int = 0,
        @ProtoNumber(8) @JvmField val starttime: Long = 0L,
        @ProtoNumber(9) @JvmField val isAudio: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class CSDataHighwayHead : ProtoBuf {
    @Serializable
    internal class C2CCommonExtendinfo(
        @ProtoNumber(1) @JvmField val infoId: Int = 0,
        @ProtoNumber(2) @JvmField val msgFilterExtendinfo: FilterExtendinfo? = null
    ) : ProtoBuf

    @Serializable
    internal class DataHighwayHead(
        @ProtoNumber(1) @JvmField val version: Int = 0,
        @ProtoNumber(2) @JvmField val uin: String = "", // yes
        @ProtoNumber(3) @JvmField val command: String = "",
        @ProtoNumber(4) @JvmField val seq: Int = 0,
        @ProtoNumber(5) @JvmField val retryTimes: Int = 0,
        @ProtoNumber(6) @JvmField val appid: Int = 0,
        @ProtoNumber(7) @JvmField val dataflag: Int = 0,
        @ProtoNumber(8) @JvmField val commandId: Int = 0,
        @ProtoNumber(9) @JvmField val buildVer: String = "",
        @ProtoNumber(10) @JvmField val localeId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DataHole(
        @ProtoNumber(1) @JvmField val begin: Long = 0L,
        @ProtoNumber(2) @JvmField val end: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class FilterExtendinfo(
        @ProtoNumber(1) @JvmField val filterFlag: Int = 0,
        @ProtoNumber(2) @JvmField val msgImageFilterRequest: ImageFilterRequest? = null
    ) : ProtoBuf

    @Serializable
    internal class FilterStyle(
        @ProtoNumber(1) @JvmField val styleId: Int = 0,
        @ProtoNumber(2) @JvmField val styleName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ImageFilterRequest(
        @ProtoNumber(1) @JvmField val sessionId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val clientIp: Int = 0,
        @ProtoNumber(3) @JvmField val uin: Long = 0L,
        @ProtoNumber(4) @JvmField val style: FilterStyle? = null,
        @ProtoNumber(5) @JvmField val width: Int = 0,
        @ProtoNumber(6) @JvmField val height: Int = 0,
        @ProtoNumber(7) @JvmField val imageData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ImageFilterResponse(
        @ProtoNumber(1) @JvmField val retCode: Int = 0,
        @ProtoNumber(2) @JvmField val imageData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val costTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class LoginSigHead(
        @ProtoNumber(1) @JvmField val loginsigType: Int = 0,
        @ProtoNumber(2) @JvmField val loginsig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NewServiceTicket(
        @ProtoNumber(1) @JvmField val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val ukey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PicInfoExt(
        @ProtoNumber(1) @JvmField val picWidth: Int = 0,
        @ProtoNumber(2) @JvmField val picHeight: Int = 0,
        @ProtoNumber(3) @JvmField val picFlag: Int = 0,
        @ProtoNumber(4) @JvmField val busiType: Int = 0,
        @ProtoNumber(5) @JvmField val srcTerm: Int = 0,
        @ProtoNumber(6) @JvmField val platType: Int = 0,
        @ProtoNumber(7) @JvmField val netType: Int = 0,
        @ProtoNumber(8) @JvmField val imgType: Int = 0,
        @ProtoNumber(9) @JvmField val appPicType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PicRspExtInfo(
        @ProtoNumber(1) @JvmField val skey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val clientIp: Int = 0,
        @ProtoNumber(3) @JvmField val upOffset: Long = 0L,
        @ProtoNumber(4) @JvmField val blockSize: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class QueryHoleRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val dataHole: List<DataHole> = emptyList(),
        @ProtoNumber(3) @JvmField val boolCompFlag: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class ReqDataHighwayHead(
        @ProtoNumber(1) @JvmField val msgBasehead: DataHighwayHead? = null,
        @ProtoNumber(2) @JvmField val msgSeghead: SegHead? = null,
        @ProtoNumber(3) @JvmField val reqExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val timestamp: Long = 0L,
        @ProtoNumber(5) @JvmField val msgLoginSigHead: LoginSigHead? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgQueryHoleRsp: QueryHoleRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class RspDataHighwayHead(
        @ProtoNumber(1) @JvmField val msgBasehead: DataHighwayHead? = null,
        @ProtoNumber(2) @JvmField val msgSeghead: SegHead? = null,
        @ProtoNumber(3) @JvmField val errorCode: Int = 0,
        @ProtoNumber(4) @JvmField val allowRetry: Int = 0,
        @ProtoNumber(5) @JvmField val cachecost: Int = 0,
        @ProtoNumber(6) @JvmField val htcost: Int = 0,
        @ProtoNumber(7) @JvmField val rspExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val timestamp: Long = 0L,
        @ProtoNumber(9) @JvmField val range: Long = 0L,
        @ProtoNumber(10) @JvmField val isReset: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SegHead(
        @ProtoNumber(1) @JvmField val serviceid: Int = 0,
        @ProtoNumber(2) @JvmField val filesize: Long = 0L,
        @ProtoNumber(3) @JvmField val dataoffset: Long = 0L,
        @ProtoNumber(4) @JvmField val datalength: Int = 0,
        @ProtoNumber(5) @JvmField val rtcode: Int = 0,
        @ProtoNumber(6) @JvmField val serviceticket: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val flag: Int = 0,
        @ProtoNumber(8) @JvmField val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val cacheAddr: Int = 0,
        @ProtoNumber(11) @JvmField val queryTimes: Int = 0,
        @ProtoNumber(12) @JvmField val updateCacheip: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class HwConfigPersistentPB : ProtoBuf {
    @Serializable
    internal class HwConfigItemPB(
        @ProtoNumber(1) @JvmField val ingKey: String = "",
        @ProtoNumber(2) @JvmField val endPointList: List<HwEndPointPB> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class HwConfigPB(
        @ProtoNumber(1) @JvmField val configItemList: List<HwConfigItemPB> = emptyList(),
        @ProtoNumber(2) @JvmField val netSegConfList: List<HwNetSegConfPB> = emptyList(),
        @ProtoNumber(3) @JvmField val shortVideoNetConf: List<HwNetSegConfPB> = emptyList(),
        @ProtoNumber(4) @JvmField val configItemListIp6: List<HwConfigItemPB> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class HwEndPointPB(
        @ProtoNumber(1) @JvmField val ingHost: String = "",
        @ProtoNumber(2) @JvmField val int32Port: Int = 0,
        @ProtoNumber(3) @JvmField val int64Timestampe: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class HwNetSegConfPB(
        @ProtoNumber(1) @JvmField val int64NetType: Long = 0L,
        @ProtoNumber(2) @JvmField val int64SegSize: Long = 0L,
        @ProtoNumber(3) @JvmField val int64SegNum: Long = 0L,
        @ProtoNumber(4) @JvmField val int64CurConnNum: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class HwSessionInfoPersistentPB : ProtoBuf {
    @Serializable
    internal class HwSessionInfoPB(
        @ProtoNumber(1) @JvmField val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Subcmd0x501 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1281) @JvmField val msgSubcmd0x501ReqBody: SubCmd0x501ReqBody? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1281) @JvmField val msgSubcmd0x501RspBody: SubCmd0x501Rspbody? = null
    ) : ProtoBuf

    @Serializable
    internal class SubCmd0x501ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val idcId: Int = 0,
        @ProtoNumber(3) @JvmField val appid: Int = 0,
        @ProtoNumber(4) @JvmField val loginSigType: Int = 0,
        @ProtoNumber(5) @JvmField val loginSigTicket: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val requestFlag: Int = 0,
        @ProtoNumber(7) @JvmField val uint32ServiceTypes: List<Int> = emptyList(),
        @ProtoNumber(8) @JvmField val bid: Int = 0,
        @ProtoNumber(9) @JvmField val term: Int = 0,
        @ProtoNumber(10) @JvmField val plat: Int = 0,
        @ProtoNumber(11) @JvmField val net: Int = 0,
        @ProtoNumber(12) @JvmField val caller: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SubCmd0x501Rspbody(
        @ProtoNumber(1) @JvmField val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val sessionKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgHttpconnAddrs: List<SrvAddrs> = emptyList(),
        @ProtoNumber(4) @JvmField val preConnection: Int = 0,
        @ProtoNumber(5) @JvmField val csConn: Int = 0,
        @ProtoNumber(6) @JvmField val msgIpLearnConf: IpLearnConf? = null,
        @ProtoNumber(7) @JvmField val msgDynTimeoutConf: DynTimeOutConf? = null,
        @ProtoNumber(8) @JvmField val msgOpenUpConf: OpenUpConf? = null,
        @ProtoNumber(9) @JvmField val msgDownloadEncryptConf: DownloadEncryptConf? = null,
        @ProtoNumber(10) @JvmField val msgShortVideoConf: ShortVideoConf? = null,
        @ProtoNumber(11) @JvmField val msgPtvConf: PTVConf? = null
    ) : ProtoBuf {
        @Serializable
        internal class DownloadEncryptConf(
            @ProtoNumber(1) @JvmField val boolEnableEncryptRequest: Boolean = false,
            @ProtoNumber(2) @JvmField val boolEnableEncryptedPic: Boolean = false,
            @ProtoNumber(3) @JvmField val ctrlFlag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DynTimeOutConf(
            @ProtoNumber(1) @JvmField val tbase2g: Int = 0,
            @ProtoNumber(2) @JvmField val tbase3g: Int = 0,
            @ProtoNumber(3) @JvmField val tbase4g: Int = 0,
            @ProtoNumber(4) @JvmField val tbaseWifi: Int = 0,
            @ProtoNumber(5) @JvmField val torg2g: Int = 0,
            @ProtoNumber(6) @JvmField val torg3g: Int = 0,
            @ProtoNumber(7) @JvmField val torg4g: Int = 0,
            @ProtoNumber(8) @JvmField val torgWifi: Int = 0,
            @ProtoNumber(9) @JvmField val maxTimeout: Int = 0,
            @ProtoNumber(10) @JvmField val enableDynTimeout: Int = 0,
            @ProtoNumber(11) @JvmField val maxTimeout2g: Int = 0,
            @ProtoNumber(12) @JvmField val maxTimeout3g: Int = 0,
            @ProtoNumber(13) @JvmField val maxTimeout4g: Int = 0,
            @ProtoNumber(14) @JvmField val maxTimeoutWifi: Int = 0,
            @ProtoNumber(15) @JvmField val hbTimeout2g: Int = 0,
            @ProtoNumber(16) @JvmField val hbTimeout3g: Int = 0,
            @ProtoNumber(17) @JvmField val hbTimeout4g: Int = 0,
            @ProtoNumber(18) @JvmField val hbTimeoutWifi: Int = 0,
            @ProtoNumber(19) @JvmField val hbTimeoutDefault: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Ip6Addr(
            @ProtoNumber(1) @JvmField val type: Int = 0,
            @ProtoNumber(2) @JvmField val ip6: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(3) @JvmField val port: Int = 0,
            @ProtoNumber(4) @JvmField val area: Int = 0,
            @ProtoNumber(5) @JvmField val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class IpAddr(
            @ProtoNumber(1) @JvmField val type: Int = 0,
            @ProtoType(ProtoIntegerType.FIXED) @ProtoNumber(2) @JvmField val ip: Int = 0,
            @ProtoNumber(3) @JvmField val port: Int = 0,
            @ProtoNumber(4) @JvmField val area: Int = 0,
            @ProtoNumber(5) @JvmField val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class IpLearnConf(
            @ProtoNumber(1) @JvmField val refreshCachedIp: Int = 0,
            @ProtoNumber(2) @JvmField val enableIpLearn: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class NetSegConf(
            @ProtoNumber(1) @JvmField val netType: Int = 0,
            @ProtoNumber(2) @JvmField val segsize: Int = 0,
            @ProtoNumber(3) @JvmField val segnum: Int = 0,
            @ProtoNumber(4) @JvmField val curconnnum: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OpenUpConf(
            @ProtoNumber(1) @JvmField val boolEnableOpenup: Boolean = false,
            @ProtoNumber(2) @JvmField val preSendSegnum: Int = 0,
            @ProtoNumber(3) @JvmField val preSendSegnum3g: Int = 0,
            @ProtoNumber(4) @JvmField val preSendSegnum4g: Int = 0,
            @ProtoNumber(5) @JvmField val preSendSegnumWifi: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class PTVConf(
            @ProtoNumber(1) @JvmField val channelType: Int = 0,
            @ProtoNumber(2) @JvmField val msgNetsegconf: List<NetSegConf> = emptyList(),
            @ProtoNumber(3) @JvmField val boolOpenHardwareCodec: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class ShortVideoConf(
            @ProtoNumber(1) @JvmField val channelType: Int = 0,
            @ProtoNumber(2) @JvmField val msgNetsegconf: List<NetSegConf> = emptyList(),
            @ProtoNumber(3) @JvmField val boolOpenHardwareCodec: Boolean = false,
            @ProtoNumber(4) @JvmField val boolSendAheadSignal: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class SrvAddrs(
            @ProtoNumber(1) @JvmField val serviceType: Int = 0,
            @ProtoNumber(2) @JvmField val msgAddrs: List<IpAddr> = emptyList(),
            @ProtoNumber(3) @JvmField val fragmentSize: Int = 0,
            @ProtoNumber(4) @JvmField val msgNetsegconf: List<NetSegConf> = emptyList(),
            @ProtoNumber(5) @JvmField val msgAddrsV6: List<Ip6Addr> = emptyList()
        ) : ProtoBuf
    }
}
