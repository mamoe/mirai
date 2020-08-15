package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class BdhExtinfo : ProtoBuf {
    @Serializable
    internal class CommFileExtReq(
        @ProtoId(1) @JvmField val actionType: Int = 0,
        @ProtoId(2) @JvmField val uuid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommFileExtRsp(
        @ProtoId(1) @JvmField val int32Retcode: Int = 0,
        @ProtoId(2) @JvmField val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PicInfo(
        @ProtoId(1) @JvmField val idx: Int = 0,
        @ProtoId(2) @JvmField val size: Int = 0,
        @ProtoId(3) @JvmField val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val type: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceExtReq(
        @ProtoId(1) @JvmField val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val fmt: Int = 0,
        @ProtoId(3) @JvmField val rate: Int = 0,
        @ProtoId(4) @JvmField val bits: Int = 0,
        @ProtoId(5) @JvmField val channel: Int = 0,
        @ProtoId(6) @JvmField val pinyin: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceExtRsp(
        @ProtoId(1) @JvmField val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val int32Retcode: Int = 0,
        @ProtoId(3) @JvmField val msgResult: List<QQVoiceResult>? = null
    ) : ProtoBuf

    @Serializable
    internal class QQVoiceResult(
        @ProtoId(1) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val pinyin: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val source: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoReqExtInfo(
        @ProtoId(1) @JvmField val cmd: Int = 0,
        @ProtoId(2) @JvmField val sessionId: Long = 0L,
        @ProtoId(3) @JvmField val msgThumbinfo: PicInfo? = null,
        @ProtoId(4) @JvmField val msgVideoinfo: VideoInfo? = null,
        @ProtoId(5) @JvmField val msgShortvideoSureReq: ShortVideoSureReqInfo? = null,
        @ProtoId(6) @JvmField val boolIsMergeCmdBeforeData: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoRspExtInfo(
        @ProtoId(1) @JvmField val cmd: Int = 0,
        @ProtoId(2) @JvmField val sessionId: Long = 0L,
        @ProtoId(3) @JvmField val int32Retcode: Int = 0,
        @ProtoId(4) @JvmField val errinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val msgThumbinfo: PicInfo? = null,
        @ProtoId(6) @JvmField val msgVideoinfo: VideoInfo? = null,
        @ProtoId(7) @JvmField val msgShortvideoSureRsp: ShortVideoSureRspInfo? = null,
        @ProtoId(8) @JvmField val retryFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoSureReqInfo(
        @ProtoId(1) @JvmField val fromuin: Long = 0L,
        @ProtoId(2) @JvmField val chatType: Int = 0,
        @ProtoId(3) @JvmField val touin: Long = 0L,
        @ProtoId(4) @JvmField val groupCode: Long = 0L,
        @ProtoId(5) @JvmField val clientType: Int = 0,
        @ProtoId(6) @JvmField val msgThumbinfo: PicInfo? = null,
        @ProtoId(7) @JvmField val msgMergeVideoinfo: List<VideoInfo>? = null,
        @ProtoId(8) @JvmField val msgDropVideoinfo: List<VideoInfo>? = null,
        @ProtoId(9) @JvmField val businessType: Int = 0,
        @ProtoId(10) @JvmField val subBusinessType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ShortVideoSureRspInfo(
        @ProtoId(1) @JvmField val fileid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val ukey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val msgVideoinfo: VideoInfo? = null,
        @ProtoId(4) @JvmField val mergeCost: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class StoryVideoExtReq : ProtoBuf

    @Serializable
    internal class StoryVideoExtRsp(
        @ProtoId(1) @JvmField val int32Retcode: Int = 0,
        @ProtoId(2) @JvmField val msg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val cdnUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val fileId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class UploadPicExtInfo(
        @ProtoId(1) @JvmField val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val thumbDownloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class VideoInfo(
        @ProtoId(1) @JvmField val idx: Int = 0,
        @ProtoId(2) @JvmField val size: Int = 0,
        @ProtoId(3) @JvmField val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val format: Int = 0,
        @ProtoId(5) @JvmField val resLen: Int = 0,
        @ProtoId(6) @JvmField val resWidth: Int = 0,
        @ProtoId(7) @JvmField val time: Int = 0,
        @ProtoId(8) @JvmField val starttime: Long = 0L,
        @ProtoId(9) @JvmField val isAudio: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class CSDataHighwayHead : ProtoBuf {
    @Serializable
    internal class C2CCommonExtendinfo(
        @ProtoId(1) @JvmField val infoId: Int = 0,
        @ProtoId(2) @JvmField val msgFilterExtendinfo: FilterExtendinfo? = null
    ) : ProtoBuf

    @Serializable
    internal class DataHighwayHead(
        @ProtoId(1) @JvmField val version: Int = 0,
        @ProtoId(2) @JvmField val uin: String = "", // yes
        @ProtoId(3) @JvmField val command: String = "",
        @ProtoId(4) @JvmField val seq: Int = 0,
        @ProtoId(5) @JvmField val retryTimes: Int = 0,
        @ProtoId(6) @JvmField val appid: Int = 0,
        @ProtoId(7) @JvmField val dataflag: Int = 0,
        @ProtoId(8) @JvmField val commandId: Int = 0,
        @ProtoId(9) @JvmField val buildVer: String = "",
        @ProtoId(10) @JvmField val localeId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DataHole(
        @ProtoId(1) @JvmField val begin: Long = 0L,
        @ProtoId(2) @JvmField val end: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class FilterExtendinfo(
        @ProtoId(1) @JvmField val filterFlag: Int = 0,
        @ProtoId(2) @JvmField val msgImageFilterRequest: ImageFilterRequest? = null
    ) : ProtoBuf

    @Serializable
    internal class FilterStyle(
        @ProtoId(1) @JvmField val styleId: Int = 0,
        @ProtoId(2) @JvmField val styleName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ImageFilterRequest(
        @ProtoId(1) @JvmField val sessionId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val clientIp: Int = 0,
        @ProtoId(3) @JvmField val uin: Long = 0L,
        @ProtoId(4) @JvmField val style: FilterStyle? = null,
        @ProtoId(5) @JvmField val width: Int = 0,
        @ProtoId(6) @JvmField val height: Int = 0,
        @ProtoId(7) @JvmField val imageData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ImageFilterResponse(
        @ProtoId(1) @JvmField val retCode: Int = 0,
        @ProtoId(2) @JvmField val imageData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val costTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class LoginSigHead(
        @ProtoId(1) @JvmField val loginsigType: Int = 0,
        @ProtoId(2) @JvmField val loginsig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NewServiceTicket(
        @ProtoId(1) @JvmField val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val ukey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PicInfoExt(
        @ProtoId(1) @JvmField val picWidth: Int = 0,
        @ProtoId(2) @JvmField val picHeight: Int = 0,
        @ProtoId(3) @JvmField val picFlag: Int = 0,
        @ProtoId(4) @JvmField val busiType: Int = 0,
        @ProtoId(5) @JvmField val srcTerm: Int = 0,
        @ProtoId(6) @JvmField val platType: Int = 0,
        @ProtoId(7) @JvmField val netType: Int = 0,
        @ProtoId(8) @JvmField val imgType: Int = 0,
        @ProtoId(9) @JvmField val appPicType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PicRspExtInfo(
        @ProtoId(1) @JvmField val skey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val clientIp: Int = 0,
        @ProtoId(3) @JvmField val upOffset: Long = 0L,
        @ProtoId(4) @JvmField val blockSize: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class QueryHoleRsp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val dataHole: List<DataHole>? = null,
        @ProtoId(3) @JvmField val boolCompFlag: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class ReqDataHighwayHead(
        @ProtoId(1) @JvmField val msgBasehead: DataHighwayHead? = null,
        @ProtoId(2) @JvmField val msgSeghead: SegHead? = null,
        @ProtoId(3) @JvmField val reqExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val timestamp: Long = 0L,
        @ProtoId(5) @JvmField val msgLoginSigHead: LoginSigHead? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgQueryHoleRsp: QueryHoleRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class RspDataHighwayHead(
        @ProtoId(1) @JvmField val msgBasehead: DataHighwayHead? = null,
        @ProtoId(2) @JvmField val msgSeghead: SegHead? = null,
        @ProtoId(3) @JvmField val errorCode: Int = 0,
        @ProtoId(4) @JvmField val allowRetry: Int = 0,
        @ProtoId(5) @JvmField val cachecost: Int = 0,
        @ProtoId(6) @JvmField val htcost: Int = 0,
        @ProtoId(7) @JvmField val rspExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val timestamp: Long = 0L,
        @ProtoId(9) @JvmField val range: Long = 0L,
        @ProtoId(10) @JvmField val isReset: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SegHead(
        @ProtoId(1) @JvmField val serviceid: Int = 0,
        @ProtoId(2) @JvmField val filesize: Long = 0L,
        @ProtoId(3) @JvmField val dataoffset: Long = 0L,
        @ProtoId(4) @JvmField val datalength: Int = 0,
        @ProtoId(5) @JvmField val rtcode: Int = 0,
        @ProtoId(6) @JvmField val serviceticket: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val flag: Int = 0,
        @ProtoId(8) @JvmField val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val cacheAddr: Int = 0,
        @ProtoId(11) @JvmField val queryTimes: Int = 0,
        @ProtoId(12) @JvmField val updateCacheip: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class HwConfigPersistentPB : ProtoBuf {
    @Serializable
    internal class HwConfigItemPB(
        @ProtoId(1) @JvmField val ingKey: String = "",
        @ProtoId(2) @JvmField val endPointList: List<HwEndPointPB>? = null
    ) : ProtoBuf

    @Serializable
    internal class HwConfigPB(
        @ProtoId(1) @JvmField val configItemList: List<HwConfigItemPB>? = null,
        @ProtoId(2) @JvmField val netSegConfList: List<HwNetSegConfPB>? = null,
        @ProtoId(3) @JvmField val shortVideoNetConf: List<HwNetSegConfPB>? = null,
        @ProtoId(4) @JvmField val configItemListIp6: List<HwConfigItemPB>? = null
    ) : ProtoBuf

    @Serializable
    internal class HwEndPointPB(
        @ProtoId(1) @JvmField val ingHost: String = "",
        @ProtoId(2) @JvmField val int32Port: Int = 0,
        @ProtoId(3) @JvmField val int64Timestampe: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class HwNetSegConfPB(
        @ProtoId(1) @JvmField val int64NetType: Long = 0L,
        @ProtoId(2) @JvmField val int64SegSize: Long = 0L,
        @ProtoId(3) @JvmField val int64SegNum: Long = 0L,
        @ProtoId(4) @JvmField val int64CurConnNum: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class HwSessionInfoPersistentPB : ProtoBuf {
    @Serializable
    internal class HwSessionInfoPB(
        @ProtoId(1) @JvmField val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Subcmd0x501 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1281) @JvmField val msgSubcmd0x501ReqBody: SubCmd0x501ReqBody? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1281) @JvmField val msgSubcmd0x501RspBody: SubCmd0x501Rspbody? = null
    ) : ProtoBuf

    @Serializable
    internal class SubCmd0x501ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val idcId: Int = 0,
        @ProtoId(3) @JvmField val appid: Int = 0,
        @ProtoId(4) @JvmField val loginSigType: Int = 0,
        @ProtoId(5) @JvmField val loginSigTicket: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val requestFlag: Int = 0,
        @ProtoId(7) @JvmField val uint32ServiceTypes: List<Int>? = null,
        @ProtoId(8) @JvmField val bid: Int = 0,
        @ProtoId(9) @JvmField val term: Int = 0,
        @ProtoId(10) @JvmField val plat: Int = 0,
        @ProtoId(11) @JvmField val net: Int = 0,
        @ProtoId(12) @JvmField val caller: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SubCmd0x501Rspbody(
        @ProtoId(1) @JvmField val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val sessionKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val msgHttpconnAddrs: List<SrvAddrs>? = null,
        @ProtoId(4) @JvmField val preConnection: Int = 0,
        @ProtoId(5) @JvmField val csConn: Int = 0,
        @ProtoId(6) @JvmField val msgIpLearnConf: IpLearnConf? = null,
        @ProtoId(7) @JvmField val msgDynTimeoutConf: DynTimeOutConf? = null,
        @ProtoId(8) @JvmField val msgOpenUpConf: OpenUpConf? = null,
        @ProtoId(9) @JvmField val msgDownloadEncryptConf: DownloadEncryptConf? = null,
        @ProtoId(10) @JvmField val msgShortVideoConf: ShortVideoConf? = null,
        @ProtoId(11) @JvmField val msgPtvConf: PTVConf? = null
    ) : ProtoBuf {
        @Serializable
        internal class DownloadEncryptConf(
            @ProtoId(1) @JvmField val boolEnableEncryptRequest: Boolean = false,
            @ProtoId(2) @JvmField val boolEnableEncryptedPic: Boolean = false,
            @ProtoId(3) @JvmField val ctrlFlag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DynTimeOutConf(
            @ProtoId(1) @JvmField val tbase2g: Int = 0,
            @ProtoId(2) @JvmField val tbase3g: Int = 0,
            @ProtoId(3) @JvmField val tbase4g: Int = 0,
            @ProtoId(4) @JvmField val tbaseWifi: Int = 0,
            @ProtoId(5) @JvmField val torg2g: Int = 0,
            @ProtoId(6) @JvmField val torg3g: Int = 0,
            @ProtoId(7) @JvmField val torg4g: Int = 0,
            @ProtoId(8) @JvmField val torgWifi: Int = 0,
            @ProtoId(9) @JvmField val maxTimeout: Int = 0,
            @ProtoId(10) @JvmField val enableDynTimeout: Int = 0,
            @ProtoId(11) @JvmField val maxTimeout2g: Int = 0,
            @ProtoId(12) @JvmField val maxTimeout3g: Int = 0,
            @ProtoId(13) @JvmField val maxTimeout4g: Int = 0,
            @ProtoId(14) @JvmField val maxTimeoutWifi: Int = 0,
            @ProtoId(15) @JvmField val hbTimeout2g: Int = 0,
            @ProtoId(16) @JvmField val hbTimeout3g: Int = 0,
            @ProtoId(17) @JvmField val hbTimeout4g: Int = 0,
            @ProtoId(18) @JvmField val hbTimeoutWifi: Int = 0,
            @ProtoId(19) @JvmField val hbTimeoutDefault: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class Ip6Addr(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoId(2) @JvmField val ip6: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val port: Int = 0,
            @ProtoId(4) @JvmField val area: Int = 0,
            @ProtoId(5) @JvmField val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class IpAddr(
            @ProtoId(1) @JvmField val type: Int = 0,
            @ProtoType(ProtoNumberType.FIXED) @ProtoId(2) @JvmField val ip: Int = 0,
            @ProtoId(3) @JvmField val port: Int = 0,
            @ProtoId(4) @JvmField val area: Int = 0,
            @ProtoId(5) @JvmField val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class IpLearnConf(
            @ProtoId(1) @JvmField val refreshCachedIp: Int = 0,
            @ProtoId(2) @JvmField val enableIpLearn: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class NetSegConf(
            @ProtoId(1) @JvmField val netType: Int = 0,
            @ProtoId(2) @JvmField val segsize: Int = 0,
            @ProtoId(3) @JvmField val segnum: Int = 0,
            @ProtoId(4) @JvmField val curconnnum: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class OpenUpConf(
            @ProtoId(1) @JvmField val boolEnableOpenup: Boolean = false,
            @ProtoId(2) @JvmField val preSendSegnum: Int = 0,
            @ProtoId(3) @JvmField val preSendSegnum3g: Int = 0,
            @ProtoId(4) @JvmField val preSendSegnum4g: Int = 0,
            @ProtoId(5) @JvmField val preSendSegnumWifi: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class PTVConf(
            @ProtoId(1) @JvmField val channelType: Int = 0,
            @ProtoId(2) @JvmField val msgNetsegconf: List<NetSegConf>? = null,
            @ProtoId(3) @JvmField val boolOpenHardwareCodec: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class ShortVideoConf(
            @ProtoId(1) @JvmField val channelType: Int = 0,
            @ProtoId(2) @JvmField val msgNetsegconf: List<NetSegConf>? = null,
            @ProtoId(3) @JvmField val boolOpenHardwareCodec: Boolean = false,
            @ProtoId(4) @JvmField val boolSendAheadSignal: Boolean = false
        ) : ProtoBuf

        @Serializable
        internal class SrvAddrs(
            @ProtoId(1) @JvmField val serviceType: Int = 0,
            @ProtoId(2) @JvmField val msgAddrs: List<IpAddr>? = null,
            @ProtoId(3) @JvmField val fragmentSize: Int = 0,
            @ProtoId(4) @JvmField val msgNetsegconf: List<NetSegConf>? = null,
            @ProtoId(5) @JvmField val msgAddrsV6: List<Ip6Addr>? = null
        ) : ProtoBuf
    }
}
