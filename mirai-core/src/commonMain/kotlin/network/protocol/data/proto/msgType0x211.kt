/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf


@Serializable
internal class SubMsgType0x3 : ProtoBuf {
    @Serializable
    internal class FailNotify(
        @JvmField @ProtoNumber(1) val sessionid: Int = 0,
        @JvmField @ProtoNumber(2) val retCode: Int = 0,
        @JvmField @ProtoNumber(3) val reason: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @JvmField @ProtoNumber(1) val msgProgressNotify: ProgressNotify? = null,
        @JvmField @ProtoNumber(2) val msgFailNotify: FailNotify? = null
    ) : ProtoBuf

    @Serializable
    internal class ProgressNotify(
        @JvmField @ProtoNumber(1) val sessionid: Int = 0,
        @JvmField @ProtoNumber(2) val uuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(3) val progress: Int = 0,
        @JvmField @ProtoNumber(4) val averageSpeed: Int = 0
    ) : ProtoBuf
}


@Serializable
internal class SubMsgType0x4 : ProtoBuf {
    @Serializable
    internal class MsgBody(
        @JvmField @ProtoNumber(1) val msgNotOnlineFile: ImMsgBody.NotOnlineFile? = null,
        @JvmField @ProtoNumber(2) val msgTime: Int = 0,
        @JvmField @ProtoNumber(3) val onlineFileForPolyToOffline: Int = 0,
        @JvmField @ProtoNumber(4) val fileImageInfo: HummerResv21.FileImgInfo? = null,
        @JvmField @ProtoNumber(5) val msgXtfSenderInfo: HummerResv21.XtfSenderInfo? = null,
        @JvmField @ProtoNumber(6) val resvAttr: HummerResv21.ResvAttr? = null
    ) : ProtoBuf
}


@Serializable
internal class SubMsgType0x5 : ProtoBuf {
    @Serializable
    internal class MsgBody(
        @JvmField @ProtoNumber(1) val sessionid: Int = 0
    ) : ProtoBuf
}


@Serializable
internal class SubMsgType0x7 : ProtoBuf {
    @Serializable
    internal class MsgBody(
        @JvmField @ProtoNumber(1) val subCmd: Int = 0,
        @JvmField @ProtoNumber(2) val msgHeader: MsgHeader? = null,
        @JvmField @ProtoNumber(3) val msgSubcmd0x1FtnNotify: List<FTNNotify> = emptyList(),
        @JvmField @ProtoNumber(4) val msgSubcmd0x2NfcNotify: List<NFCNotify> = emptyList(),
        @JvmField @ProtoNumber(5) val msgSubcmd0x3Filecontrol: List<FileControl> = emptyList(),
        @JvmField @ProtoNumber(6) val msgSubcmd0x4Generic: GenericSubCmd? = null,
        @JvmField @ProtoNumber(7) val msgSubcmd0x5MoloNotify: List<MoloNotify> = emptyList(),
        @JvmField @ProtoNumber(8) val msgSubcmd0x8RnfcNotify: List<RNFCNotify> = emptyList(),
        @JvmField @ProtoNumber(9) val msgSubcmd0x9FtnThumbNotify: List<FTNNotify> = emptyList(),
        @JvmField @ProtoNumber(10) val msgSubcmd0xaNfcThumbNotify: List<NFCNotify> = emptyList(),
        @JvmField @ProtoNumber(11) val msgSubcmd0xbMpfileNotify: List<MpFileNotify> = emptyList(),
        @JvmField @ProtoNumber(12) val msgSubcmd0xcProgressReq: ProgressReq? = null,
        @JvmField @ProtoNumber(13) val msgSubcmd0xdProgressRsp: ProgressRsp? = null
    ) : ProtoBuf {
        @Serializable
        internal class ActionInfo(
            @JvmField @ProtoNumber(1) val serviceName: String = "",
            @JvmField @ProtoNumber(2) val buf: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class FileControl(
            @JvmField @ProtoNumber(1) val sessionid: Long = 0L,
            @JvmField @ProtoNumber(2) val operate: Int = 0,
            @JvmField @ProtoNumber(3) val seq: Int = 0,
            @JvmField @ProtoNumber(4) val code: Int = 0,
            @JvmField @ProtoNumber(5) val msg: String = "",
            @JvmField @ProtoNumber(6) val groupId: Int = 0,
            @JvmField @ProtoNumber(7) val groupCurindex: Int = 0,
            @JvmField @ProtoNumber(8) val batchID: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class FTNNotify(
            @JvmField @ProtoNumber(1) val sessionid: Long = 0L,
            @JvmField @ProtoNumber(2) val fileName: String = "",
            @JvmField @ProtoNumber(3) val fileIndex: String = "",
            @JvmField @ProtoNumber(4) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(5) val fileKey: String = "",
            @JvmField @ProtoNumber(6) val fileLen: Long = 0L,
            @JvmField @ProtoNumber(7) val originfileMd5: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(8) val originfiletype: Int = 0,
            @JvmField @ProtoNumber(9) val groupId: Int = 0,
            @JvmField @ProtoNumber(10) val groupSize: Int = 0,
            @JvmField @ProtoNumber(11) val groupCurindex: Int = 0,
            @JvmField @ProtoNumber(20) val msgActionInfo: ActionInfo? = null,
            @JvmField @ProtoNumber(21) val batchID: Int = 0,
            @JvmField @ProtoNumber(22) val groupflag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MsgItem(
            @JvmField @ProtoNumber(1) val type: Int = 0,
            @JvmField @ProtoNumber(2) val text: String = ""
        ) : ProtoBuf

        @Serializable
        internal class QQDataTextMsg(
            @JvmField @ProtoNumber(1) val msgItems: List<MsgItem> = emptyList()
        ) : ProtoBuf

        @Serializable
        internal class WifiPhotoNoPush(
            @JvmField @ProtoNumber(1) val json: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class WifiPhotoWithPush(
            @JvmField @ProtoNumber(1) val json: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class GenericSubCmd(
            @JvmField @ProtoNumber(1) val sessionid: Long = 0L,
            @JvmField @ProtoNumber(2) val size: Int = 0,
            @JvmField @ProtoNumber(3) val index: Int = 0,
            @JvmField @ProtoNumber(4) val type: Int = 0,
            @JvmField @ProtoNumber(5) val buf: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(6) val supportAuth: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MoloNotify(
            @JvmField @ProtoNumber(1) val buf: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(2) val groupId: Int = 0,
            @JvmField @ProtoNumber(3) val groupSize: Int = 0,
            @JvmField @ProtoNumber(4) val groupCurindex: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class MpFileNotify(
            @JvmField @ProtoNumber(1) val sessionid: Long = 0L,
            @JvmField @ProtoNumber(2) val operate: Int = 0,
            @ProtoType(ProtoIntegerType.FIXED) @JvmField @ProtoNumber(3) val fixed32Ip: Int = 0,
            @JvmField @ProtoNumber(4) val port: Int = 0,
            @JvmField @ProtoNumber(5) val type: Int = 0,
            @JvmField @ProtoNumber(6) val power: Int = 0,
            @JvmField @ProtoNumber(7) val json: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf

        @Serializable
        internal class MsgHeader(
            @JvmField @ProtoNumber(1) val srcAppId: Int = 0,
            @JvmField @ProtoNumber(2) val srcInstId: Int = 0,
            @JvmField @ProtoNumber(3) val dstAppId: Int = 0,
            @JvmField @ProtoNumber(4) val dstInstId: Int = 0,
            @JvmField @ProtoNumber(5) val dstUin: Long = 0L,
            @JvmField @ProtoNumber(6) val srcUin: Long = 0L,
            @JvmField @ProtoNumber(7) val srcUinType: Int = 0,
            @JvmField @ProtoNumber(8) val dstUinType: Int = 0,
            @JvmField @ProtoNumber(9) val srcTerType: Int = 0,
            @JvmField @ProtoNumber(10) val dstTerType: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class NFCNotify(
            @JvmField @ProtoNumber(1) val sessionid: Long = 0L,
            @JvmField @ProtoNumber(2) val fileName: String = "",
            @JvmField @ProtoNumber(3) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoType(ProtoIntegerType.FIXED) @JvmField @ProtoNumber(4) val fixed32Ip: Int = 0,
            @JvmField @ProtoNumber(5) val port: Int = 0,
            @JvmField @ProtoNumber(6) val urlNotify: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(7) val tokenkey: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(8) val fileLen: Long = 0L,
            @JvmField @ProtoNumber(9) val originfileMd5: ByteArray = EMPTY_BYTE_ARRAY,
            @JvmField @ProtoNumber(10) val originfiletype: Int = 0,
            @JvmField @ProtoNumber(11) val groupId: Int = 0,
            @JvmField @ProtoNumber(12) val groupSize: Int = 0,
            @JvmField @ProtoNumber(13) val groupCurindex: Int = 0,
            @JvmField @ProtoNumber(20) val msgActionInfo: ActionInfo? = null,
            @JvmField @ProtoNumber(21) val batchID: Int = 0,
            @JvmField @ProtoNumber(22) val groupflag: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class ProgressReq(
            @JvmField @ProtoNumber(1) val cmd: Int = 0,
            @JvmField @ProtoNumber(2) val cookie: Long = 0L,
            @JvmField @ProtoNumber(3) val infoflag: Int = 0,
            @JvmField @ProtoNumber(4) val uint64Sessionid: List<Long> = emptyList()
        ) : ProtoBuf

        @Serializable
        internal class ProgressInfo(
            @JvmField @ProtoNumber(1) val sessionid: Long = 0L,
            @JvmField @ProtoNumber(2) val progress: Long = 0L,
            @JvmField @ProtoNumber(3) val status: Int = 0,
            @JvmField @ProtoNumber(4) val filesize: Long = 0L,
            @JvmField @ProtoNumber(5) val filename: String = "",
            @JvmField @ProtoNumber(6) val time: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class ProgressRsp(
            @JvmField @ProtoNumber(1) val cmd: Int = 0,
            @JvmField @ProtoNumber(2) val cookie: Long = 0L,
            @JvmField @ProtoNumber(3) val packageCount: Int = 0,
            @JvmField @ProtoNumber(4) val packageIndex: Int = 0,
            @JvmField @ProtoNumber(5) val msgProgressinfo: List<ProgressInfo> = emptyList()
        ) : ProtoBuf

        @Serializable
        internal class RNFCNotify(
            @JvmField @ProtoNumber(1) val sessionid: Long = 0L,
            @JvmField @ProtoNumber(2) val token: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoType(ProtoIntegerType.FIXED) @JvmField @ProtoNumber(3) val fixed32Ip: Int = 0,
            @JvmField @ProtoNumber(4) val port: Int = 0,
            @JvmField @ProtoNumber(5) val svrTaskId: Long = 0L
        ) : ProtoBuf
    }
}


@Serializable
internal class C2CType0x211SubC2CType0x8 : ProtoBuf {
    @Serializable
    internal class BusiReqHead(
        @JvmField @ProtoNumber(1) val int32Version: Int = 0,
        @JvmField @ProtoNumber(2) val int32Seq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class BusiRespHead(
        @JvmField @ProtoNumber(1) val int32Version: Int = 0,
        @JvmField @ProtoNumber(2) val int32Seq: Int = 0,
        @JvmField @ProtoNumber(3) val int32ReplyCode: Int = 0,
        @JvmField @ProtoNumber(4) val result: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Cell(
        @JvmField @ProtoNumber(1) val int32Mcc: Int = -1,
        @JvmField @ProtoNumber(2) val int32Mnc: Int = -1,
        @JvmField @ProtoNumber(3) val int32Lac: Int = -1,
        @JvmField @ProtoNumber(4) val int32Cellid: Int = -1,
        @JvmField @ProtoNumber(5) val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ConnType(
        @JvmField @ProtoNumber(1) val type: Int /* enum */ = 1,
        @JvmField @ProtoNumber(2) val desription: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GPS(
        @JvmField @ProtoNumber(1) val int32Lat: Int = 900000000,
        @JvmField @ProtoNumber(2) val int32Lon: Int = 900000000,
        @JvmField @ProtoNumber(3) val int32Alt: Int = -10000000,
        @JvmField @ProtoNumber(4) val eType: Int /* enum */ = 0
    ) : ProtoBuf

    @Serializable
    internal class IPAddrInfo(
        @JvmField @ProtoNumber(1) val int32Ip: Int = 0,
        @JvmField @ProtoNumber(2) val int32Mask: Int = 0,
        @JvmField @ProtoNumber(3) val int32Gateway: Int = 0,
        @JvmField @ProtoNumber(4) val int32Port: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class JudgeResult(
        @JvmField @ProtoNumber(1) val type: Int /* enum */ = 0,
        @JvmField @ProtoNumber(2) val ssid: String = "",
        @JvmField @ProtoNumber(3) val tips: String = "",
        @JvmField @ProtoNumber(4) val int32IdleTimeout: Int = 0,
        @JvmField @ProtoNumber(5) val idleWaiting: Int = 0,
        @JvmField @ProtoNumber(6) val forceWifi: Int = 0,
        @JvmField @ProtoNumber(7) val flagsWifipsw: Int = 0,
        @JvmField @ProtoNumber(8) val flagsNetcheck: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class LBSInfo(
        @JvmField @ProtoNumber(1) val msgGps: GPS? = null,
        @JvmField @ProtoNumber(2) val msgWifis: List<Wifi> = emptyList(),
        @JvmField @ProtoNumber(3) val msgCells: List<Cell> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @JvmField @ProtoNumber(1) val msgType: Int /* enum */ = 1,
        @JvmField @ProtoNumber(2) val msgCcNotifylist: NotifyList? = null,
        @JvmField @ProtoNumber(3) val msgCcnfAbiQuery: NearFieldAbiQuery? = null,
        @JvmField @ProtoNumber(4) val msgCcPushJudgeResult: PushJudgeResult? = null,
        @JvmField @ProtoNumber(5) val msgCcnfFilesendReq: NearFieldFileSendReq? = null,
        @JvmField @ProtoNumber(6) val msgCcnfFilestateSync: NearFieldFileStateSync? = null
    ) : ProtoBuf

    @Serializable
    internal class NearFieldAbiQuery(
        @JvmField @ProtoNumber(1) val toUin: Long = 0L,
        @JvmField @ProtoNumber(2) val fromUin: Long = 0L,
        @JvmField @ProtoNumber(3) val boolNeedTips: Boolean = false,
        @JvmField @ProtoNumber(4) val int32Timeout: Int = 0,
        @JvmField @ProtoNumber(5) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(6) val int32PeerIp: Int = 0,
        @JvmField @ProtoNumber(7) val int32PeerPort: Int = 0,
        @JvmField @ProtoNumber(8) val peerExtra: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NearFieldFileInfo(
        @JvmField @ProtoNumber(1) val fileName: String = "",
        @JvmField @ProtoNumber(2) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(3) val fileMd5: String = "",
        @JvmField @ProtoNumber(4) val fileUrl: String = "",
        @JvmField @ProtoNumber(5) val fileThumbMd5: String = "",
        @JvmField @ProtoNumber(6) val fileThumbUrl: String = "",
        @JvmField @ProtoNumber(7) val sessionId: Long = 0L,
        @JvmField @ProtoNumber(8) val int32Timeout: Int = 0,
        @JvmField @ProtoNumber(9) val groupId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class NearFieldFileSendReq(
        @JvmField @ProtoNumber(1) val toUin: Long = 0L,
        @JvmField @ProtoNumber(2) val msgFileList: List<NearFieldFileInfo> = emptyList(),
        @JvmField @ProtoNumber(3) val int32Ip: Int = 0,
        @JvmField @ProtoNumber(4) val int32UdpPort: Int = 0,
        @JvmField @ProtoNumber(5) val ssid: String = "",
        @JvmField @ProtoNumber(6) val int32ConnWifiapTimeout: Int = 0,
        @JvmField @ProtoNumber(7) val forceWifi: Int = 0,
        @JvmField @ProtoNumber(8) val wifipsw: String = ""
    ) : ProtoBuf

    @Serializable
    internal class NearFieldFileStateSync(
        @JvmField @ProtoNumber(1) val eType: Int /* enum */ = 1,
        @JvmField @ProtoNumber(2) val sessionId: Long = 0L,
        @JvmField @ProtoNumber(3) val fromUin: Long = 0L,
        @JvmField @ProtoNumber(4) val int32ErrorCode: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class NearfieldInfo(
        @JvmField @ProtoNumber(1) val msgLbsInfo: LBSInfo? = null,
        @JvmField @ProtoNumber(2) val msgConnType: ConnType? = null,
        @JvmField @ProtoNumber(3) val msgIpInfo: IPAddrInfo? = null,
        @JvmField @ProtoNumber(4) val msgWifiDetail: WifiDetailInfo? = null,
        @JvmField @ProtoNumber(5) val msgWifiAbi: WifiAbility? = null,
        @JvmField @ProtoNumber(6) val extra: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NotifyList(
        @JvmField @ProtoNumber(1) val notifyType: Int /* enum */ = 0,
        @JvmField @ProtoNumber(2) val msgUpdateList: List<UpdateInfo> = emptyList(),
        @JvmField @ProtoNumber(3) val sessionId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PushJudgeResult(
        @JvmField @ProtoNumber(1) val msgHead: BusiRespHead? = null,
        @JvmField @ProtoNumber(2) val toUin: Long = 0L,
        @JvmField @ProtoNumber(3) val msgResult: JudgeResult? = null,
        @JvmField @ProtoNumber(4) val int32PeerIp: Int = 0,
        @JvmField @ProtoNumber(5) val int32PeerPort: Int = 0,
        @JvmField @ProtoNumber(6) val peerExtra: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqAIOJudge(
        @JvmField @ProtoNumber(1) val msgHead: BusiReqHead? = null,
        @JvmField @ProtoNumber(2) val toUin: Long = 0L,
        @JvmField @ProtoNumber(3) val msgNearfieldInfo: NearfieldInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqExit(
        @JvmField @ProtoNumber(1) val msgHead: BusiReqHead? = null,
        @JvmField @ProtoNumber(2) val sessionId: Int = 0,
        @JvmField @ProtoNumber(3) val msgNearfieldInfo: NearfieldInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqGetList(
        @JvmField @ProtoNumber(1) val msgHead: BusiReqHead? = null,
        @JvmField @ProtoNumber(2) val msgNearfieldInfo: NearfieldInfo? = null,
        @JvmField @ProtoNumber(3) val sessionId: Int = 0,
        @JvmField @ProtoNumber(4) val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqReportNearFieldAbi(
        @JvmField @ProtoNumber(1) val msgHead: BusiReqHead? = null,
        @JvmField @ProtoNumber(2) val fromUin: Long = 0L,
        @JvmField @ProtoNumber(3) val msgNearfieldInfo: NearfieldInfo? = null,
        @JvmField @ProtoNumber(4) val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RespAIOJudge(
        @JvmField @ProtoNumber(1) val msgHead: BusiRespHead? = null,
        @JvmField @ProtoNumber(2) val msgResult: JudgeResult? = null,
        @JvmField @ProtoNumber(3) val timeout: Int = 0,
        @JvmField @ProtoNumber(4) val toUin: Long = 0L,
        @JvmField @ProtoNumber(5) val int32PeerIp: Int = 0,
        @JvmField @ProtoNumber(6) val int32PeerPort: Int = 0,
        @JvmField @ProtoNumber(7) val peerExtra: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RespExit(
        @JvmField @ProtoNumber(1) val msgHead: BusiRespHead? = null
    ) : ProtoBuf

    @Serializable
    internal class RespGetList(
        @JvmField @ProtoNumber(1) val msgHead: BusiRespHead? = null,
        @JvmField @ProtoNumber(2) val msgUserList: List<UserProfile> = emptyList(),
        @JvmField @ProtoNumber(3) val sessionId: Int = 0,
        @JvmField @ProtoNumber(4) val int32UpdateInterval: Int = 0,
        @JvmField @ProtoNumber(5) val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class UpdateInfo(
        @JvmField @ProtoNumber(1) val type: Int /* enum */ = 1,
        @JvmField @ProtoNumber(2) val msgUser: UserProfile? = null
    ) : ProtoBuf

    @Serializable
    internal class UserAbility(
        @JvmField @ProtoNumber(1) val int32SysQlver: Int = 0,
        @JvmField @ProtoNumber(2) val int32SysTerm: Int = 0,
        @JvmField @ProtoNumber(3) val int32SysApp: Int = 0,
        @JvmField @ProtoNumber(10) val int32AbsWifiHost: Int = 0,
        @JvmField @ProtoNumber(11) val int32AbsWifiClient: Int = 0,
        @JvmField @ProtoNumber(12) val int32AbsWifiForcedcreate: Int = 0,
        @JvmField @ProtoNumber(13) val int32AbsWifiForcedconnect: Int = 0,
        @JvmField @ProtoNumber(14) val int32AbsWifiPassword: Int = 0,
        @JvmField @ProtoNumber(20) val int32AbsNetReachablecheck: Int = 0,
        @JvmField @ProtoNumber(21) val int32AbsNetSpeedTest: Int = 0,
        @JvmField @ProtoNumber(30) val int32AbsUiPromptOnclick: Int = 0,
        @JvmField @ProtoNumber(31) val int32AbsUiPromptPassive: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class UserExtraInfo(
        @JvmField @ProtoNumber(1) val ability: UserAbility? = null
    ) : ProtoBuf

    @Serializable
    internal class UserProfile(
        @JvmField @ProtoNumber(1) val uin: Long = 0L,
        @JvmField @ProtoNumber(2) val int32FaceId: Int = 0,
        @JvmField @ProtoNumber(3) val int32Sex: Int = 0,
        @JvmField @ProtoNumber(4) val int32Age: Int = 0,
        @JvmField @ProtoNumber(5) val nick: String = "",
        @JvmField @ProtoNumber(6) val tmpTalkSig: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(7) val msgResult: JudgeResult? = null,
        @JvmField @ProtoNumber(8) val int32Ip: Int = 0,
        @JvmField @ProtoNumber(9) val int32Port: Int = 0,
        @JvmField @ProtoNumber(10) val tip: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(11) val extra: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Wifi(
        @JvmField @ProtoNumber(1) val mac: Long = 0L,
        @JvmField @ProtoNumber(2) val int32Rssi: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class WifiAbility(
        @JvmField @ProtoNumber(1) val boolEstablishAbi: Boolean = false,
        @JvmField @ProtoNumber(2) val boolAutoConnectAbi: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class WifiDetailInfo(
        @JvmField @ProtoNumber(1) val boolSelfEstablish: Boolean = false,
        @JvmField @ProtoNumber(2) val ssid: String = "",
        @JvmField @ProtoNumber(3) val mac: String = ""
    ) : ProtoBuf
}


@Serializable
internal class C2CType0x211SubC2CType0x9 : ProtoBuf {
    @Serializable
    internal class MsgBody(
        @JvmField @ProtoNumber(1) val service: String = "",
        @JvmField @ProtoNumber(2) val cMD: Int = 0,
        @JvmField @ProtoNumber(3) val msgPrinter: MsgPrinter? = null
    ) : ProtoBuf {
        @Serializable
        internal class HPPrinterStateInfo(
            @JvmField @ProtoNumber(1) val printerDin: Long = 0L,
            @JvmField @ProtoNumber(2) val printerQrPicUrl: String = "",
            @JvmField @ProtoNumber(3) val printerQrOpenUrl: String = "",
            @JvmField @ProtoNumber(4) val printerTipUrl: String = ""
        ) : ProtoBuf

        @Serializable
        internal class MsgPrinter(
            @JvmField @ProtoNumber(1) val stringPrinter: List<String> = emptyList(),
            @JvmField @ProtoNumber(2) val printSessionId: Long = 0L,
            @JvmField @ProtoNumber(3) val printResult: Int = 0,
            @JvmField @ProtoNumber(4) val resultMsg: String = "",
            @JvmField @ProtoNumber(5) val uint64SessionId: List<Long> = emptyList(),
            @JvmField @ProtoNumber(6) val msgSupportFileInfo: List<SupportFileInfo> = emptyList(),
            @JvmField @ProtoNumber(7) val hpPrinterStateInfo: HPPrinterStateInfo? = null
        ) : ProtoBuf

        @Serializable
        internal class SupportFileInfo(
            @JvmField @ProtoNumber(1) val fileSuffix: String = "",
            @JvmField @ProtoNumber(2) val copies: Int = 0,
            @JvmField @ProtoNumber(3) val duplex: Int = 0
        ) : ProtoBuf
    }
}


@Serializable
internal class C2CType0x211SubC2CType0xb : ProtoBuf {
    @Serializable
    internal class MsgBody(
        @JvmField @ProtoNumber(1) val msgMsgHeader: MsgHeader? = null,
        @JvmField @ProtoNumber(2) val msgRejectMotify: RejectNotify? = null
    ) : ProtoBuf {
        @Serializable
        internal class MsgHeader(
            @JvmField @ProtoNumber(1) val bodyType: Int /* enum */ = 101,
            @JvmField @ProtoNumber(2) val sessionType: Int = 0,
            @JvmField @ProtoNumber(3) val toUin: Long = 0L,
            @JvmField @ProtoNumber(4) val toMobile: String = "",
            @JvmField @ProtoNumber(5) val roomId: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class RejectNotify(
            @JvmField @ProtoNumber(1) val enumRejectReason: Int /* enum */ = 201,
            @JvmField @ProtoNumber(2) val msg: String = "",
            @JvmField @ProtoNumber(3) val ringFilename: String = ""
        ) : ProtoBuf
    }
}
