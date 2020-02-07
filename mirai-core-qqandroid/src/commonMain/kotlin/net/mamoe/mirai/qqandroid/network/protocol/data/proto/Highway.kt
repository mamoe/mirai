package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
class BdhExtinfo : ProtoBuf {
    @Serializable
    class CommFileExtReq(
        @SerialId(1) val actionType: Int = 0,
        @SerialId(2) val uuid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class CommFileExtRsp(
        @SerialId(1) val int32Retcode: Int = 0,
        @SerialId(2) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PicInfo(
        @SerialId(1) val idx: Int = 0,
        @SerialId(2) val size: Int = 0,
        @SerialId(3) val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val type: Int = 0
    ) : ProtoBuf

    @Serializable
    class QQVoiceExtReq(
        @SerialId(1) val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fmt: Int = 0,
        @SerialId(3) val rate: Int = 0,
        @SerialId(4) val bits: Int = 0,
        @SerialId(5) val channel: Int = 0,
        @SerialId(6) val pinyin: Int = 0
    ) : ProtoBuf

    @Serializable
    class QQVoiceExtRsp(
        @SerialId(1) val qid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val int32Retcode: Int = 0,
        @SerialId(3) val msgResult: List<QQVoiceResult>? = null
    ) : ProtoBuf

    @Serializable
    class QQVoiceResult(
        @SerialId(1) val text: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val pinyin: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val source: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShortVideoReqExtInfo(
        @SerialId(1) val cmd: Int = 0,
        @SerialId(2) val sessionId: Long = 0L,
        @SerialId(3) val msgThumbinfo: PicInfo? = null,
        @SerialId(4) val msgVideoinfo: VideoInfo? = null,
        @SerialId(5) val msgShortvideoSureReq: ShortVideoSureReqInfo? = null,
        @SerialId(6) val boolIsMergeCmdBeforeData: Boolean = false
    ) : ProtoBuf

    @Serializable
    class ShortVideoRspExtInfo(
        @SerialId(1) val cmd: Int = 0,
        @SerialId(2) val sessionId: Long = 0L,
        @SerialId(3) val int32Retcode: Int = 0,
        @SerialId(4) val errinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val msgThumbinfo: PicInfo? = null,
        @SerialId(6) val msgVideoinfo: VideoInfo? = null,
        @SerialId(7) val msgShortvideoSureRsp: ShortVideoSureRspInfo? = null,
        @SerialId(8) val retryFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShortVideoSureReqInfo(
        @SerialId(1) val fromuin: Long = 0L,
        @SerialId(2) val chatType: Int = 0,
        @SerialId(3) val touin: Long = 0L,
        @SerialId(4) val groupCode: Long = 0L,
        @SerialId(5) val clientType: Int = 0,
        @SerialId(6) val msgThumbinfo: PicInfo? = null,
        @SerialId(7) val msgMergeVideoinfo: List<VideoInfo>? = null,
        @SerialId(8) val msgDropVideoinfo: List<VideoInfo>? = null,
        @SerialId(9) val businessType: Int = 0,
        @SerialId(10) val subBusinessType: Int = 0
    ) : ProtoBuf

    @Serializable
    class ShortVideoSureRspInfo(
        @SerialId(1) val fileid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val ukey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val msgVideoinfo: VideoInfo? = null,
        @SerialId(4) val mergeCost: Int = 0
    ) : ProtoBuf

    @Serializable
    class StoryVideoExtReq : ProtoBuf

    @Serializable
    class StoryVideoExtRsp(
        @SerialId(1) val int32Retcode: Int = 0,
        @SerialId(2) val msg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val cdnUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fileId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class UploadPicExtInfo(
        @SerialId(1) val fileResid: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val downloadUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val thumbDownloadUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class VideoInfo(
        @SerialId(1) val idx: Int = 0,
        @SerialId(2) val size: Int = 0,
        @SerialId(3) val binMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val format: Int = 0,
        @SerialId(5) val resLen: Int = 0,
        @SerialId(6) val resWidth: Int = 0,
        @SerialId(7) val time: Int = 0,
        @SerialId(8) val starttime: Long = 0L,
        @SerialId(9) val isAudio: Int = 0
    ) : ProtoBuf
}

@Serializable
class CSDataHighwayHead : ProtoBuf {
    @Serializable
    class C2CCommonExtendinfo(
        @SerialId(1) val infoId: Int = 0,
        @SerialId(2) val msgFilterExtendinfo: FilterExtendinfo? = null
    ) : ProtoBuf

    @Serializable
    class DataHighwayHead(
        @SerialId(1) val version: Int = 0,
        @SerialId(2) val uin: String = "", // yes
        @SerialId(3) val command: String = "",
        @SerialId(4) val seq: Int = 0,
        @SerialId(5) val retryTimes: Int = 0,
        @SerialId(6) val appid: Int = 0,
        @SerialId(7) val dataflag: Int = 0,
        @SerialId(8) val commandId: Int = 0,
        @SerialId(9) val buildVer: String = "",
        @SerialId(10) val localeId: Int = 0
    ) : ProtoBuf

    @Serializable
    class DataHole(
        @SerialId(1) val begin: Long = 0L,
        @SerialId(2) val end: Long = 0L
    ) : ProtoBuf

    @Serializable
    class FilterExtendinfo(
        @SerialId(1) val filterFlag: Int = 0,
        @SerialId(2) val msgImageFilterRequest: ImageFilterRequest? = null
    ) : ProtoBuf

    @Serializable
    class FilterStyle(
        @SerialId(1) val styleId: Int = 0,
        @SerialId(2) val styleName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ImageFilterRequest(
        @SerialId(1) val sessionId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val clientIp: Int = 0,
        @SerialId(3) val uin: Long = 0L,
        @SerialId(4) val style: FilterStyle? = null,
        @SerialId(5) val width: Int = 0,
        @SerialId(6) val height: Int = 0,
        @SerialId(7) val imageData: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ImageFilterResponse(
        @SerialId(1) val retCode: Int = 0,
        @SerialId(2) val imageData: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val costTime: Int = 0
    ) : ProtoBuf

    @Serializable
    class LoginSigHead(
        @SerialId(1) val loginsigType: Int = 0,
        @SerialId(2) val loginsig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class NewServiceTicket(
        @SerialId(1) val signature: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val ukey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class PicInfoExt(
        @SerialId(1) val picWidth: Int = 0,
        @SerialId(2) val picHeight: Int = 0,
        @SerialId(3) val picFlag: Int = 0,
        @SerialId(4) val busiType: Int = 0,
        @SerialId(5) val srcTerm: Int = 0,
        @SerialId(6) val platType: Int = 0,
        @SerialId(7) val netType: Int = 0,
        @SerialId(8) val imgType: Int = 0,
        @SerialId(9) val appPicType: Int = 0
    ) : ProtoBuf

    @Serializable
    class PicRspExtInfo(
        @SerialId(1) val skey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val clientIp: Int = 0,
        @SerialId(3) val upOffset: Long = 0L,
        @SerialId(4) val blockSize: Long = 0L
    ) : ProtoBuf

    @Serializable
    class QueryHoleRsp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val dataHole: List<DataHole>? = null,
        @SerialId(3) val boolCompFlag: Boolean = false
    ) : ProtoBuf

    @Serializable
    class ReqDataHighwayHead(
        @SerialId(1) val msgBasehead: DataHighwayHead? = null,
        @SerialId(2) val msgSeghead: SegHead? = null,
        @SerialId(3) val reqExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val timestamp: Long = 0L,
        @SerialId(5) val msgLoginSigHead: LoginSigHead? = null
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @SerialId(1) val msgQueryHoleRsp: QueryHoleRsp? = null
    ) : ProtoBuf

    @Serializable
    class RspDataHighwayHead(
        @SerialId(1) val msgBasehead: DataHighwayHead? = null,
        @SerialId(2) val msgSeghead: SegHead? = null,
        @SerialId(3) val errorCode: Int = 0,
        @SerialId(4) val allowRetry: Int = 0,
        @SerialId(5) val cachecost: Int = 0,
        @SerialId(6) val htcost: Int = 0,
        @SerialId(7) val rspExtendinfo: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val timestamp: Long = 0L,
        @SerialId(9) val range: Long = 0L,
        @SerialId(10) val isReset: Int = 0
    ) : ProtoBuf

    @Serializable
    class SegHead(
        @SerialId(1) val serviceid: Int = 0,
        @SerialId(2) val filesize: Long = 0L,
        @SerialId(3) val dataoffset: Long = 0L,
        @SerialId(4) val datalength: Int = 0,
        @SerialId(5) val rtcode: Int = 0,
        @SerialId(6) val serviceticket: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val flag: Int = 0,
        @SerialId(8) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val cacheAddr: Int = 0,
        @SerialId(11) val queryTimes: Int = 0,
        @SerialId(12) val updateCacheip: Int = 0
    ) : ProtoBuf
}

@Serializable
class HwConfigPersistentPB : ProtoBuf {
    @Serializable
    class HwConfigItemPB(
        @SerialId(1) val ingKey: String = "",
        @SerialId(2) val endPointList: List<HwEndPointPB>? = null
    ) : ProtoBuf

    @Serializable
    class HwConfigPB(
        @SerialId(1) val configItemList: List<HwConfigItemPB>? = null,
        @SerialId(2) val netSegConfList: List<HwNetSegConfPB>? = null,
        @SerialId(3) val shortVideoNetConf: List<HwNetSegConfPB>? = null,
        @SerialId(4) val configItemListIp6: List<HwConfigItemPB>? = null
    ) : ProtoBuf

    @Serializable
    class HwEndPointPB(
        @SerialId(1) val ingHost: String = "",
        @SerialId(2) val int32Port: Int = 0,
        @SerialId(3) val int64Timestampe: Long = 0L
    ) : ProtoBuf

    @Serializable
    class HwNetSegConfPB(
        @SerialId(1) val int64NetType: Long = 0L,
        @SerialId(2) val int64SegSize: Long = 0L,
        @SerialId(3) val int64SegNum: Long = 0L,
        @SerialId(4) val int64CurConnNum: Long = 0L
    ) : ProtoBuf
}

@Serializable
class HwSessionInfoPersistentPB : ProtoBuf {
    @Serializable
    class HwSessionInfoPB(
        @SerialId(1) val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
class Subcmd0x501 : ProtoBuf {
    @Serializable
    class ReqBody(
        @SerialId(1281) val msgSubcmd0x501ReqBody: SubCmd0x501ReqBody? = null
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @SerialId(1281) val msgSubcmd0x501RspBody: SubCmd0x501Rspbody? = null
    ) : ProtoBuf

    @Serializable
    class SubCmd0x501ReqBody(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val idcId: Int = 0,
        @SerialId(3) val appid: Int = 0,
        @SerialId(4) val loginSigType: Int = 0,
        @SerialId(5) val loginSigTicket: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val requestFlag: Int = 0,
        @SerialId(7) val uint32ServiceTypes: List<Int>? = null,
        @SerialId(8) val bid: Int = 0,
        @SerialId(9) val term: Int = 0,
        @SerialId(10) val plat: Int = 0,
        @SerialId(11) val net: Int = 0,
        @SerialId(12) val caller: Int = 0
    ) : ProtoBuf

    @Serializable
    class SubCmd0x501Rspbody(
        @SerialId(1) val httpconnSigSession: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val sessionKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val msgHttpconnAddrs: List<SrvAddrs>? = null,
        @SerialId(4) val preConnection: Int = 0,
        @SerialId(5) val csConn: Int = 0,
        @SerialId(6) val msgIpLearnConf: IpLearnConf? = null,
        @SerialId(7) val msgDynTimeoutConf: DynTimeOutConf? = null,
        @SerialId(8) val msgOpenUpConf: OpenUpConf? = null,
        @SerialId(9) val msgDownloadEncryptConf: DownloadEncryptConf? = null,
        @SerialId(10) val msgShortVideoConf: ShortVideoConf? = null,
        @SerialId(11) val msgPtvConf: PTVConf? = null
    ) : ProtoBuf {
        @Serializable
        class DownloadEncryptConf(
            @SerialId(1) val boolEnableEncryptRequest: Boolean = false,
            @SerialId(2) val boolEnableEncryptedPic: Boolean = false,
            @SerialId(3) val ctrlFlag: Int = 0
        ) : ProtoBuf

        @Serializable
        class DynTimeOutConf(
            @SerialId(1) val tbase2g: Int = 0,
            @SerialId(2) val tbase3g: Int = 0,
            @SerialId(3) val tbase4g: Int = 0,
            @SerialId(4) val tbaseWifi: Int = 0,
            @SerialId(5) val torg2g: Int = 0,
            @SerialId(6) val torg3g: Int = 0,
            @SerialId(7) val torg4g: Int = 0,
            @SerialId(8) val torgWifi: Int = 0,
            @SerialId(9) val maxTimeout: Int = 0,
            @SerialId(10) val enableDynTimeout: Int = 0,
            @SerialId(11) val maxTimeout2g: Int = 0,
            @SerialId(12) val maxTimeout3g: Int = 0,
            @SerialId(13) val maxTimeout4g: Int = 0,
            @SerialId(14) val maxTimeoutWifi: Int = 0,
            @SerialId(15) val hbTimeout2g: Int = 0,
            @SerialId(16) val hbTimeout3g: Int = 0,
            @SerialId(17) val hbTimeout4g: Int = 0,
            @SerialId(18) val hbTimeoutWifi: Int = 0,
            @SerialId(19) val hbTimeoutDefault: Int = 0
        ) : ProtoBuf

        @Serializable
        class Ip6Addr(
            @SerialId(1) val type: Int = 0,
            @SerialId(2) val ip6: ByteArray = EMPTY_BYTE_ARRAY,
            @SerialId(3) val port: Int = 0,
            @SerialId(4) val area: Int = 0,
            @SerialId(5) val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        class IpAddr(
            @SerialId(1) val type: Int = 0,
            @ProtoType(ProtoNumberType.FIXED) @SerialId(2) val ip: Int = 0,
            @SerialId(3) val port: Int = 0,
            @SerialId(4) val area: Int = 0,
            @SerialId(5) val sameIsp: Int = 0
        ) : ProtoBuf

        @Serializable
        class IpLearnConf(
            @SerialId(1) val refreshCachedIp: Int = 0,
            @SerialId(2) val enableIpLearn: Int = 0
        ) : ProtoBuf

        @Serializable
        class NetSegConf(
            @SerialId(1) val netType: Int = 0,
            @SerialId(2) val segsize: Int = 0,
            @SerialId(3) val segnum: Int = 0,
            @SerialId(4) val curconnnum: Int = 0
        ) : ProtoBuf

        @Serializable
        class OpenUpConf(
            @SerialId(1) val boolEnableOpenup: Boolean = false,
            @SerialId(2) val preSendSegnum: Int = 0,
            @SerialId(3) val preSendSegnum3g: Int = 0,
            @SerialId(4) val preSendSegnum4g: Int = 0,
            @SerialId(5) val preSendSegnumWifi: Int = 0
        ) : ProtoBuf

        @Serializable
        class PTVConf(
            @SerialId(1) val channelType: Int = 0,
            @SerialId(2) val msgNetsegconf: List<NetSegConf>? = null,
            @SerialId(3) val boolOpenHardwareCodec: Boolean = false
        ) : ProtoBuf

        @Serializable
        class ShortVideoConf(
            @SerialId(1) val channelType: Int = 0,
            @SerialId(2) val msgNetsegconf: List<NetSegConf>? = null,
            @SerialId(3) val boolOpenHardwareCodec: Boolean = false,
            @SerialId(4) val boolSendAheadSignal: Boolean = false
        ) : ProtoBuf

        @Serializable
        class SrvAddrs(
            @SerialId(1) val serviceType: Int = 0,
            @SerialId(2) val msgAddrs: List<IpAddr>? = null,
            @SerialId(3) val fragmentSize: Int = 0,
            @SerialId(4) val msgNetsegconf: List<NetSegConf>? = null,
            @SerialId(5) val msgAddrsV6: List<Ip6Addr>? = null
        ) : ProtoBuf
    }
}
