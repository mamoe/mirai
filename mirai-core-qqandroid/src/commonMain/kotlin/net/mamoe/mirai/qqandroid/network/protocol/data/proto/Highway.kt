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
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
internal class BdhExtinfo : ProtoBuf {
    @Serializable
    class CommFileExtReq(
        @ProtoId(1) val actionType: Int = 0,
        @ProtoId(2) val uuid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class CommFileExtRsp(
        @ProtoId(1) val int32Retcode: Int = 0,
        @ProtoId(2) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PicInfo(
        @ProtoId(1) val idx: Int = 0,
        @ProtoId(2) val size: Int = 0,
        @ProtoId(3) val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val type: Int = 0
    ) : ProtoBuf

    @Serializable
    class QQVoiceExtReq(
        @ProtoId(1) val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fmt: Int = 0,
        @ProtoId(3) val rate: Int = 0,
        @ProtoId(4) val bits: Int = 0,
        @ProtoId(5) val channel: Int = 0,
        @ProtoId(6) val pinyin: Int = 0
    ) : ProtoBuf

    @Serializable
    class QQVoiceExtRsp(
        @ProtoId(1) val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val int32Retcode: Int = 0,
        @ProtoId(3) val msgResult: List<QQVoiceResult>? = null
    ) : ProtoBuf

    @Serializable
    class QQVoiceResult(
        @ProtoId(1) val text: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val pinyin: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val source: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShortVideoReqExtInfo(
        @ProtoId(1) val cmd: Int = 0,
        @ProtoId(2) val sessionId: Long = 0L,
        @ProtoId(3) val msgThumbinfo: PicInfo? = null,
        @ProtoId(4) val msgVideoinfo: VideoInfo? = null,
        @ProtoId(5) val msgShortvideoSureReq: ShortVideoSureReqInfo? = null,
        @ProtoId(6) val boolIsMergeCmdBeforeData: Boolean = false
    ) : ProtoBuf

    @Serializable
    class ShortVideoRspExtInfo(
        @ProtoId(1) val cmd: Int = 0,
        @ProtoId(2) val sessionId: Long = 0L,
        @ProtoId(3) val int32Retcode: Int = 0,
        @ProtoId(4) val errinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val msgThumbinfo: PicInfo? = null,
        @ProtoId(6) val msgVideoinfo: VideoInfo? = null,
        @ProtoId(7) val msgShortvideoSureRsp: ShortVideoSureRspInfo? = null,
        @ProtoId(8) val retryFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShortVideoSureReqInfo(
        @ProtoId(1) val fromuin: Long = 0L,
        @ProtoId(2) val chatType: Int = 0,
        @ProtoId(3) val touin: Long = 0L,
        @ProtoId(4) val groupCode: Long = 0L,
        @ProtoId(5) val clientType: Int = 0,
        @ProtoId(6) val msgThumbinfo: PicInfo? = null,
        @ProtoId(7) val msgMergeVideoinfo: List<VideoInfo>? = null,
        @ProtoId(8) val msgDropVideoinfo: List<VideoInfo>? = null,
        @ProtoId(9) val businessType: Int = 0,
        @ProtoId(10) val subBusinessType: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShortVideoSureRspInfo(
        @ProtoId(1) val fileid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val ukey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgVideoinfo: VideoInfo? = null,
        @ProtoId(4) val mergeCost: Int = 0
    ) : ProtoBuf

    @Serializable
    class StoryVideoExtReq : ProtoBuf

    @Serializable
    class StoryVideoExtRsp(
        @ProtoId(1) val int32Retcode: Int = 0,
        @ProtoId(2) val msg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val cdnUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fileId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class UploadPicExtInfo(
        @ProtoId(1) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val thumbDownloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class VideoInfo(
        @ProtoId(1) val idx: Int = 0,
        @ProtoId(2) val size: Int = 0,
        @ProtoId(3) val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val format: Int = 0,
        @ProtoId(5) val resLen: Int = 0,
        @ProtoId(6) val resWidth: Int = 0,
        @ProtoId(7) val time: Int = 0,
        @ProtoId(8) val starttime: Long = 0L,
        @ProtoId(9) val isAudio: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class CSDataHighwayHead : ProtoBuf {
    @Serializable
    class C2CCommonExtendinfo(
        @ProtoId(1) val infoId: Int = 0,
        @ProtoId(2) val msgFilterExtendinfo: FilterExtendinfo? = null
    ) : ProtoBuf

    @Serializable
    class DataHighwayHead(
        @ProtoId(1) val version: Int = 0,
        @ProtoId(2) val uin: String = "", // yes
        @ProtoId(3) val command: String = "",
        @ProtoId(4) val seq: Int = 0,
        @ProtoId(5) val retryTimes: Int = 0,
        @ProtoId(6) val appid: Int = 0,
        @ProtoId(7) val dataflag: Int = 0,
        @ProtoId(8) val commandId: Int = 0,
        @ProtoId(9) val buildVer: String = "",
        @ProtoId(10) val localeId: Int = 0
    ) : ProtoBuf

    @Serializable
    class DataHole(
        @ProtoId(1) val begin: Long = 0L,
        @ProtoId(2) val end: Long = 0L
    ) : ProtoBuf

    @Serializable
    class FilterExtendinfo(
        @ProtoId(1) val filterFlag: Int = 0,
        @ProtoId(2) val msgImageFilterRequest: ImageFilterRequest? = null
    ) : ProtoBuf

    @Serializable
    class FilterStyle(
        @ProtoId(1) val styleId: Int = 0,
        @ProtoId(2) val styleName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ImageFilterRequest(
        @ProtoId(1) val sessionId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val clientIp: Int = 0,
        @ProtoId(3) val uin: Long = 0L,
        @ProtoId(4) val style: FilterStyle? = null,
        @ProtoId(5) val width: Int = 0,
        @ProtoId(6) val height: Int = 0,
        @ProtoId(7) val imageData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ImageFilterResponse(
        @ProtoId(1) val retCode: Int = 0,
        @ProtoId(2) val imageData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val costTime: Int = 0
    ) : ProtoBuf

    @Serializable
    class LoginSigHead(
        @ProtoId(1) val loginsigType: Int = 0,
        @ProtoId(2) val loginsig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class NewServiceTicket(
        @ProtoId(1) val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val ukey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PicInfoExt(
        @ProtoId(1) val picWidth: Int = 0,
        @ProtoId(2) val picHeight: Int = 0,
        @ProtoId(3) val picFlag: Int = 0,
        @ProtoId(4) val busiType: Int = 0,
        @ProtoId(5) val srcTerm: Int = 0,
        @ProtoId(6) val platType: Int = 0,
        @ProtoId(7) val netType: Int = 0,
        @ProtoId(8) val imgType: Int = 0,
        @ProtoId(9) val appPicType: Int = 0
    ) : ProtoBuf

    @Serializable
    class PicRspExtInfo(
        @ProtoId(1) val skey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val clientIp: Int = 0,
        @ProtoId(3) val upOffset: Long = 0L,
        @ProtoId(4) val blockSize: Long = 0L
    ) : ProtoBuf

    @Serializable
    class QueryHoleRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val dataHole: List<DataHole>? = null,
        @ProtoId(3) val boolCompFlag: Boolean = false
    ) : ProtoBuf

    @Serializable
    class ReqDataHighwayHead(
        @ProtoId(1) val msgBasehead: DataHighwayHead? = null,
        @ProtoId(2) val msgSeghead: SegHead? = null,
        @ProtoId(3) val reqExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val timestamp: Long = 0L,
        @ProtoId(5) val msgLoginSigHead: LoginSigHead? = null
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @ProtoId(1) val msgQueryHoleRsp: QueryHoleRsp? = null
    ) : ProtoBuf

    @Serializable
    class RspDataHighwayHead(
        @ProtoId(1) val msgBasehead: DataHighwayHead? = null,
        @ProtoId(2) val msgSeghead: SegHead? = null,
        @ProtoId(3) val errorCode: Int = 0,
        @ProtoId(4) val allowRetry: Int = 0,
        @ProtoId(5) val cachecost: Int = 0,
        @ProtoId(6) val htcost: Int = 0,
        @ProtoId(7) val rspExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val timestamp: Long = 0L,
        @ProtoId(9) val range: Long = 0L,
        @ProtoId(10) val isReset: Int = 0
    ) : ProtoBuf

    @Serializable
    class SegHead(
        @ProtoId(1) val serviceid: Int = 0,
        @ProtoId(2) val filesize: Long = 0L,
        @ProtoId(3) val dataoffset: Long = 0L,
        @ProtoId(4) val datalength: Int = 0,
        @ProtoId(5) val rtcode: Int = 0,
        @ProtoId(6) val serviceticket: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val flag: Int = 0,
        @ProtoId(8) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val cacheAddr: Int = 0,
        @ProtoId(11) val queryTimes: Int = 0,
        @ProtoId(12) val updateCacheip: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class HwConfigPersistentPB : ProtoBuf {
    @Serializable
    class HwConfigItemPB(
        @ProtoId(1) val ingKey: String = "",
        @ProtoId(2) val endPointList: List<HwEndPointPB>? = null
    ) : ProtoBuf

    @Serializable
    class HwConfigPB(
        @ProtoId(1) val configItemList: List<HwConfigItemPB>? = null,
        @ProtoId(2) val netSegConfList: List<HwNetSegConfPB>? = null,
        @ProtoId(3) val shortVideoNetConf: List<HwNetSegConfPB>? = null,
        @ProtoId(4) val configItemListIp6: List<HwConfigItemPB>? = null
    ) : ProtoBuf

    @Serializable
    class HwEndPointPB(
        @ProtoId(1) val ingHost: String = "",
        @ProtoId(2) val int32Port: Int = 0,
        @ProtoId(3) val int64Timestampe: Long = 0L
    ) : ProtoBuf

    @Serializable
    class HwNetSegConfPB(
        @ProtoId(1) val int64NetType: Long = 0L,
        @ProtoId(2) val int64SegSize: Long = 0L,
        @ProtoId(3) val int64SegNum: Long = 0L,
        @ProtoId(4) val int64CurConnNum: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class HwSessionInfoPersistentPB : ProtoBuf {
    @Serializable
    class HwSessionInfoPB(
        @ProtoId(1) val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Subcmd0x501 : ProtoBuf {
    @Serializable
    class ReqBody(
        @ProtoId(1281) val msgSubcmd0x501ReqBody: SubCmd0x501ReqBody? = null
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @ProtoId(1281) val msgSubcmd0x501RspBody: SubCmd0x501Rspbody? = null
    ) : ProtoBuf

    @Serializable
    class SubCmd0x501ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val idcId: Int = 0,
        @ProtoId(3) val appid: Int = 0,
        @ProtoId(4) val loginSigType: Int = 0,
        @ProtoId(5) val loginSigTicket: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val requestFlag: Int = 0,
        @ProtoId(7) val uint32ServiceTypes: List<Int>? = null,
        @ProtoId(8) val bid: Int = 0,
        @ProtoId(9) val term: Int = 0,
        @ProtoId(10) val plat: Int = 0,
        @ProtoId(11) val net: Int = 0,
        @ProtoId(12) val caller: Int = 0
    ) : ProtoBuf

    @Serializable
    class SubCmd0x501Rspbody(
        @ProtoId(1) val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgHttpconnAddrs: List<SrvAddrs>? = null,
        @ProtoId(4) val preConnection: Int = 0,
        @ProtoId(5) val csConn: Int = 0,
        @ProtoId(6) val msgIpLearnConf: IpLearnConf? = null,
        @ProtoId(7) val msgDynTimeoutConf: DynTimeOutConf? = null,
        @ProtoId(8) val msgOpenUpConf: OpenUpConf? = null,
        @ProtoId(9) val msgDownloadEncryptConf: DownloadEncryptConf? = null,
        @ProtoId(10) val msgShortVideoConf: ShortVideoConf? = null,
        @ProtoId(11) val msgPtvConf: PTVConf? = null
    ) : ProtoBuf {
        @Serializable
        class DownloadEncryptConf(
            @ProtoId(1) val boolEnableEncryptRequest: Boolean = false,
            @ProtoId(2) val boolEnableEncryptedPic: Boolean = false,
            @ProtoId(3) val ctrlFlag: Int = 0
        ) : ProtoBuf

        @Serializable
        class DynTimeOutConf(
            @ProtoId(1) val tbase2g: Int = 0,
            @ProtoId(2) val tbase3g: Int = 0,
            @ProtoId(3) val tbase4g: Int = 0,
            @ProtoId(4) val tbaseWifi: Int = 0,
            @ProtoId(5) val torg2g: Int = 0,
            @ProtoId(6) val torg3g: Int = 0,
            @ProtoId(7) val torg4g: Int = 0,
            @ProtoId(8) val torgWifi: Int = 0,
            @ProtoId(9) val maxTimeout: Int = 0,
            @ProtoId(10) val enableDynTimeout: Int = 0,
            @ProtoId(11) val maxTimeout2g: Int = 0,
            @ProtoId(12) val maxTimeout3g: Int = 0,
            @ProtoId(13) val maxTimeout4g: Int = 0,
            @ProtoId(14) val maxTimeoutWifi: Int = 0,
            @ProtoId(15) val hbTimeout2g: Int = 0,
            @ProtoId(16) val hbTimeout3g: Int = 0,
            @ProtoId(17) val hbTimeout4g: Int = 0,
            @ProtoId(18) val hbTimeoutWifi: Int = 0,
            @ProtoId(19) val hbTimeoutDefault: Int = 0
        ) : ProtoBuf

        @Serializable
        class Ip6Addr(
            @ProtoId(1) val type: Int = 0,
            @ProtoId(2) val ip6: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val port: Int = 0,
            @ProtoId(4) val area: Int = 0,
            @ProtoId(5) val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        class IpAddr(
            @ProtoId(1) val type: Int = 0,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(2) val ip: Int = 0,
            @ProtoId(3) val port: Int = 0,
            @ProtoId(4) val area: Int = 0,
            @ProtoId(5) val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        class IpLearnConf(
            @ProtoId(1) val refreshCachedIp: Int = 0,
            @ProtoId(2) val enableIpLearn: Int = 0
        ) : ProtoBuf

        @Serializable
        class NetSegConf(
            @ProtoId(1) val netType: Int = 0,
            @ProtoId(2) val segsize: Int = 0,
            @ProtoId(3) val segnum: Int = 0,
            @ProtoId(4) val curconnnum: Int = 0
        ) : ProtoBuf

        @Serializable
        class OpenUpConf(
            @ProtoId(1) val boolEnableOpenup: Boolean = false,
            @ProtoId(2) val preSendSegnum: Int = 0,
            @ProtoId(3) val preSendSegnum3g: Int = 0,
            @ProtoId(4) val preSendSegnum4g: Int = 0,
            @ProtoId(5) val preSendSegnumWifi: Int = 0
        ) : ProtoBuf

        @Serializable
        class PTVConf(
            @ProtoId(1) val channelType: Int = 0,
            @ProtoId(2) val msgNetsegconf: List<NetSegConf>? = null,
            @ProtoId(3) val boolOpenHardwareCodec: Boolean = false
        ) : ProtoBuf

        @Serializable
        class ShortVideoConf(
            @ProtoId(1) val channelType: Int = 0,
            @ProtoId(2) val msgNetsegconf: List<NetSegConf>? = null,
            @ProtoId(3) val boolOpenHardwareCodec: Boolean = false,
            @ProtoId(4) val boolSendAheadSignal: Boolean = false
        ) : ProtoBuf

        @Serializable
        class SrvAddrs(
            @ProtoId(1) val serviceType: Int = 0,
            @ProtoId(2) val msgAddrs: List<IpAddr>? = null,
            @ProtoId(3) val fragmentSize: Int = 0,
            @ProtoId(4) val msgNetsegconf: List<NetSegConf>? = null,
            @ProtoId(5) val msgAddrsV6: List<Ip6Addr>? = null
        ) : ProtoBuf
    }
}
