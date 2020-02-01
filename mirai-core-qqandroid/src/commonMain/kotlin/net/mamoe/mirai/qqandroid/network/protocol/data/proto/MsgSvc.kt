package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

@Serializable
internal class MsgSvc : ProtoBuf {
    @Serializable
    internal class Grp(
        @SerialId(1) val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val syncFlag: SyncFlag,
        @SerialId(5) val uinPairMsgs: List<MsgComm.UinPairMsg>? = null,
        @SerialId(6) val bindUin: Long = 0L,
        @SerialId(7) val msgRspType: Int = 0,
        @SerialId(8) val pubAccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(9) val isPartialSync: Boolean = false,
        @SerialId(10) val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawReq(
        @SerialId(1) val subCmd: Int = 0,
        @SerialId(2) val groupType: Int = 0,
        @SerialId(3) val groupCode: Long = 0L,
        @SerialId(4) val msgList: List<MessageInfo>? = null,
        @SerialId(5) val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageInfo(
            @SerialId(1) val msgSeq: Int = 0,
            @SerialId(2) val msgRandom: Int = 0,
            @SerialId(3) val msgType: Int = 0
        )
    }

    @Serializable
    internal class PbGroupReadedReportReq(
        @SerialId(1) val groupCode: Long = 0L,
        @SerialId(2) val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class BusinessWPATmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val sigt: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class C2C(
        @SerialId(1) val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgReq(
        @SerialId(1) val groupCode: Long = 0L,
        @SerialId(2) val beginSeq: Long = 0L,
        @SerialId(3) val endSeq: Long = 0L,
        @SerialId(4) val filter: Int /* enum */ = 0,
        @SerialId(5) val memberSeq: Long = 0L,
        @SerialId(6) val publicGroup: Boolean = false,
        @SerialId(7) val shieldFlag: Int = 0,
        @SerialId(8) val saveTrafficFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmReq(
        @SerialId(1) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AccostTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val confUin: Long = 0L,
        @SerialId(4) val memberSeq: Long = 0L,
        @SerialId(5) val confSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class NearByAssistantTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal data class MsgSendInfo(
        @SerialId(1) val receiver: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PubGroupTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val groupUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AddressListTmp(
        @SerialId(1) val fromPhone: String = "",
        @SerialId(2) val toPhone: String = "",
        @SerialId(3) val toUin: Long = 0L,
        @SerialId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val fromContactSize: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DisTmp(
        @SerialId(1) val disUin: Long = 0L,
        @SerialId(2) val toUin: Long = 0L
    )

    @Serializable
    internal class PbMsgWithDrawResp(
        @SerialId(1) val c2cWithDraw: List<PbC2CMsgWithDrawResp>? = null,
        @SerialId(2) val groupWithDraw: List<PbGroupMsgWithDrawResp>? = null
    ) : ProtoBuf

    @Serializable
    internal class AuthTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbMsgWithDrawReq(
        @SerialId(1) val c2cWithDraw: List<PbC2CMsgWithDrawReq>? = null,
        @SerialId(2) val groupWithDraw: List<PbGroupMsgWithDrawReq>? = null
    ) : ProtoBuf

    internal enum class SyncFlag {
        START,
        CONTINUE,
        STOP
    }

    @Serializable
    internal class PbGetMsgReq(
        @SerialId(1) val syncFlag: SyncFlag,
        @SerialId(2) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val rambleFlag: Int = 1,
        @SerialId(4) val latestRambleNumber: Int = 20,
        @SerialId(5) val otherRambleNumber: Int = 3,
        @SerialId(6) val onlineSyncFlag: Int = 1,
        @SerialId(7) val contextFlag: Int = 0,
        @SerialId(8) val whisperSessionId: Int = 0,
        @SerialId(9) val msgReqType: Int = 0,
        @SerialId(10) val pubaccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val serverBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgReq(
        @SerialId(1) val peerUin: Long = 0L,
        @SerialId(2) val lastMsgtime: Long = 0L,
        @SerialId(3) val random: Long = 0L,
        @SerialId(4) val readCnt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GrpTmp(
        @SerialId(1) val groupUin: Long = 0L,
        @SerialId(2) val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val discussUin: Long = 0L,
        @SerialId(4) val returnEndSeq: Long = 0L,
        @SerialId(5) val returnBeginSeq: Long = 0L,
        @SerialId(6) val msg: List<MsgComm.Msg>? = null,
        @SerialId(7) val lastGetTime: Long = 0L,
        @SerialId(8) val discussInfoSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CommTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val c2cType: Int = 0,
        @SerialId(3) val svrType: Int = 0,
        @SerialId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val subCmd: Int = 0,
        @SerialId(4) val groupType: Int = 0,
        @SerialId(5) val groupCode: Long = 0L,
        @SerialId(6) val failedMsgList: List<MessageResult>? = null,
        @SerialId(7) val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageResult(
            @SerialId(1) val result: Int = 0,
            @SerialId(2) val msgSeq: Int = 0,
            @SerialId(3) val msgTime: Int = 0,
            @SerialId(4) val msgRandom: Int = 0,
            @SerialId(5) val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @SerialId(6) val msgType: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class PbC2CReadedReportResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumReq : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawReq(
        @SerialId(1) val msgInfo: List<MsgInfo>? = null,
        @SerialId(2) val longMessageFlag: Int = 0,
        @SerialId(3) val reserved: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgInfo(
            @SerialId(1) val fromUin: Long = 0L,
            @SerialId(2) val toUin: Long = 0L,
            @SerialId(3) val msgSeq: Int = 0,
            @SerialId(4) val msgUid: Long = 0L,
            @SerialId(5) val msgTime: Long = 0L,
            @SerialId(6) val msgRandom: Int = 0,
            @SerialId(7) val pkgNum: Int = 0,
            @SerialId(8) val pkgIndex: Int = 0,
            @SerialId(9) val divSeq: Int = 0,
            @SerialId(10) val msgType: Int = 0,
            @SerialId(20) val routingHead: RoutingHead? = null
        )
    }

    @Serializable
    internal class PbDelRoamMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Dis(
        @SerialId(1) val disUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TransSvrInfo(
        @SerialId(1) val subType: Int = 0,
        @SerialId(2) val int32RetCode: Int = 0,
        @SerialId(3) val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val transInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val groupInfoResp: List<GroupInfoResp>? = null
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoResp(
            @SerialId(1) val groupCode: Long = 0L,
            @SerialId(2) val memberSeq: Long = 0L,
            @SerialId(3) val groupSeq: Long = 0L
        )
    }

    @Serializable
    internal class PbSendMsgReq(
        @SerialId(1) val routingHead: RoutingHead? = null,
        @SerialId(2) val contentHead: MsgComm.ContentHead? = null,
        @SerialId(3) val msgBody: ImMsgBody.MsgBody = ImMsgBody.MsgBody(),
        @SerialId(4) val msgSeq: Int = 0,
        @SerialId(5) val msgRand: Int = 0,
        @SerialId(6) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val appShare: MsgComm.AppShareInfo? = null,
        @SerialId(8) val msgVia: Int = 0,
        @SerialId(9) val dataStatist: Int = 0,
        @SerialId(10) val multiMsgAssist: MultiMsgAssist? = null,
        @SerialId(11) val inputNotifyInfo: PbInputNotifyInfo? = null,
        @SerialId(12) val msgCtrl: MsgCtrl.MsgCtrl? = null,
        @SerialId(13) val receiptReq: ImReceipt.ReceiptReq? = null,
        @SerialId(14) val multiSendSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransMsg(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val c2cCmd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudResp(
        @SerialId(1) val msg: List<MsgComm.Msg>? = null,
        @SerialId(2) val serializeRspbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbInputNotifyInfo(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val ime: Int = 0,
        @SerialId(3) val notifyFlag: Int = 0,
        @SerialId(4) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val iosPushWording: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbUnReadMsgSeqResp(
        @SerialId(1) val c2cUnreadInfo: PbC2CUnReadMsgNumResp? = null,
        @SerialId(2) val binduinUnreadInfo: List<PbBindUinUnReadMsgNumResp>? = null,
        @SerialId(3) val groupUnreadInfo: PbPullGroupMsgSeqResp? = null,
        @SerialId(4) val discussUnreadInfo: PbPullDiscussMsgSeqResp? = null,
        @SerialId(5) val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgReq(
        @SerialId(1) val msgItems: List<MsgItem>? = null
    ) : ProtoBuf {
        @Serializable
        internal class MsgItem(
            @SerialId(1) val fromUin: Long = 0L,
            @SerialId(2) val toUin: Long = 0L,
            @SerialId(3) val msgType: Int = 0,
            @SerialId(4) val msgSeq: Int = 0,
            @SerialId(5) val msgUid: Long = 0L,
            @SerialId(7) val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class MultiMsgAssist(
        @SerialId(1) val repeatedRouting: List<RoutingHead>? = null,
        @SerialId(2) val msgUse: Int /* enum */ = 1,
        @SerialId(3) val tempId: Long = 0L,
        @SerialId(4) val vedioLen: Long = 0L,
        @SerialId(5) val redbagId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val redbagAmount: Long = 0L,
        @SerialId(7) val hasReadbag: Int = 0,
        @SerialId(8) val hasVedio: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportReq(
        @SerialId(1) val grpReadReport: List<PbGroupReadedReportReq>? = null,
        @SerialId(2) val disReadReport: List<PbDiscussReadedReportReq>? = null,
        @SerialId(3) val c2cReadReport: PbC2CReadedReportReq? = null,
        @SerialId(4) val bindUinReadReport: PbBindUinMsgReadedConfirmReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val peerUin: Long = 0L,
        @SerialId(4) val lastMsgtime: Long = 0L,
        @SerialId(5) val random: Long = 0L,
        @SerialId(6) val msg: List<MsgComm.Msg>? = null,
        @SerialId(7) val iscomplete: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinGetMsgReq(
        @SerialId(1) val bindUin: Long = 0L,
        @SerialId(2) val bindUinSig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val syncFlag: Int /* enum */ = 0,
        @SerialId(4) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NearByDatingTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class BsnsTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RoutingHead(
        @SerialId(1) val c2c: C2C? = null,
        @SerialId(2) val grp: Grp? = null,
        @SerialId(3) val grpTmp: GrpTmp? = null,
        @SerialId(4) val dis: Dis? = null,
        @SerialId(5) val disTmp: DisTmp? = null,
        @SerialId(6) val wpaTmp: WPATmp? = null,
        @SerialId(7) val secretFile: SecretFileHead? = null,
        @SerialId(8) val publicPlat: PublicPlat? = null,
        @SerialId(9) val transMsg: TransMsg? = null,
        @SerialId(10) val addressList: AddressListTmp? = null,
        @SerialId(11) val richStatusTmp: RichStatusTmp? = null,
        @SerialId(12) val transCmd: TransCmd? = null,
        @SerialId(13) val accostTmp: AccostTmp? = null,
        @SerialId(14) val pubGroupTmp: PubGroupTmp? = null,
        @SerialId(15) val trans0x211: Trans0x211? = null,
        @SerialId(16) val businessWpaTmp: BusinessWPATmp? = null,
        @SerialId(17) val authTmp: AuthTmp? = null,
        @SerialId(18) val bsnsTmp: BsnsTmp? = null,
        @SerialId(19) val qqQuerybusinessTmp: QQQueryBusinessTmp? = null,
        @SerialId(20) val nearbyDatingTmp: NearByDatingTmp? = null,
        @SerialId(21) val nearbyAssistantTmp: NearByAssistantTmp? = null,
        @SerialId(22) val commTmp: CommTmp? = null
    ) : ProtoBuf

    @Serializable
    internal class TransResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val respTag: Int = 0,
        @SerialId(4) val respBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal data class PbSendMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val sendTime: Int = 0,
        @SerialId(4) val svrbusyWaitTime: Int = 0,
        @SerialId(5) val msgSendInfo: MsgSendInfo? = null,
        @SerialId(6) val errtype: Int = 0,
        @SerialId(7) val transSvrInfo: TransSvrInfo? = null,
        @SerialId(8) val receiptResp: ImReceipt.ReceiptResp? = null,
        @SerialId(9) val textAnalysisResult: Int = 0
    ) : ProtoBuf, Packet

    @Serializable
    internal class PbBindUinUnReadMsgNumResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val bindUin: Long = 0L,
        @SerialId(4) val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgReq(
        @SerialId(1) val discussUin: Long = 0L,
        @SerialId(2) val endSeq: Long = 0L,
        @SerialId(3) val beginSeq: Long = 0L,
        @SerialId(4) val lastGetTime: Long = 0L,
        @SerialId(5) val discussInfoSeq: Long = 0L,
        @SerialId(6) val filter: Int /* enum */ = 0,
        @SerialId(7) val memberSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val msgStatus: List<MsgStatus>? = null,
        @SerialId(4) val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgStatus(
            @SerialId(1) val msgInfo: PbC2CMsgWithDrawReq.MsgInfo? = null,
            @SerialId(2) val status: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class SecretFileHead(
        @SerialId(1) val secretFileMsg: SubMsgType0xc1.MsgBody? = null,
        @SerialId(2) val secretFileStatus: SubMsgType0x1a.MsgBody? = null
    )

    @Serializable
    internal class PbGetRoamMsgReq(
        @SerialId(1) val peerUin: Long = 0L,
        @SerialId(2) val lastMsgtime: Long = 0L,
        @SerialId(3) val random: Long = 0L,
        @SerialId(4) val readCnt: Int = 0,
        @SerialId(5) val checkPwd: Int = 0,
        @SerialId(6) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val pwd: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val subcmd: Int = 0,
        @SerialId(9) val beginMsgtime: Long = 0L,
        @SerialId(10) val reqType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransCmd(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportResp(
        @SerialId(1) val grpReadReport: List<PbGroupReadedReportResp>? = null,
        @SerialId(2) val disReadReport: List<PbDiscussReadedReportResp>? = null,
        @SerialId(3) val c2cReadReport: PbC2CReadedReportResp? = null,
        @SerialId(4) val bindUinReadReport: PbBindUinMsgReadedConfirmResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val thirdqqRespInfo: List<ThirdQQRespInfo>? = null,
        @SerialId(4) val interval: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQRespInfo(
            @SerialId(1) val thirdUin: Long = 0L,
            @SerialId(2) val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY,
            @SerialId(3) val msgNum: Int = 0,
            @SerialId(4) val msgFlag: Int = 0,
            @SerialId(5) val redbagTime: Int = 0,
            @SerialId(6) val status: Int = 0,
            @SerialId(7) val lastMsgTime: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class RichStatusTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQQueryBusinessTmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDelRoamMsgReq(
        @SerialId(1) val c2cMsg: C2CMsg? = null,
        @SerialId(2) val grpMsg: GrpMsg? = null,
        @SerialId(3) val disMsg: DisMsg? = null
    ) : ProtoBuf {
        @Serializable
        internal class GrpMsg(
            @SerialId(1) val groupCode: Long = 0L,
            @SerialId(2) val msgSeq: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class C2CMsg(
            @SerialId(1) val fromUin: Long = 0L,
            @SerialId(2) val peerUin: Long = 0L,
            @SerialId(3) val msgTime: Int = 0,
            @SerialId(4) val msgRandom: Int = 0,
            @SerialId(5) val msgSeq: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DisMsg(
            @SerialId(1) val discussUin: Long = 0L,
            @SerialId(2) val msgSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbUnReadMsgSeqReq(
        @SerialId(1) val c2cUnreadInfo: PbC2CUnReadMsgNumReq? = null,
        @SerialId(2) val binduinUnreadInfo: List<PbBindUinUnReadMsgNumReq>? = null,
        @SerialId(3) val groupUnreadInfo: PbPullGroupMsgSeqReq? = null,
        @SerialId(4) val discussUnreadInfo: PbPullDiscussMsgSeqReq? = null,
        @SerialId(5) val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbPullDiscussMsgSeqResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val discussInfoResp: List<DiscussInfoResp>? = null
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoResp(
            @SerialId(1) val confUin: Long = 0L,
            @SerialId(2) val memberSeq: Long = 0L,
            @SerialId(3) val confSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbPullDiscussMsgSeqReq(
        @SerialId(1) val discussInfoReq: List<DiscussInfoReq>? = null
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoReq(
            @SerialId(1) val confUin: Long = 0L,
            @SerialId(2) val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class WPATmp(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PublicPlat(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetRoamMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val peerUin: Long = 0L,
        @SerialId(4) val lastMsgtime: Long = 0L,
        @SerialId(5) val random: Long = 0L,
        @SerialId(6) val msg: List<MsgComm.Msg>? = null,
        @SerialId(7) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportReq(
        @SerialId(1) val confUin: Long = 0L,
        @SerialId(2) val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CReadedReportReq(
        @SerialId(1) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val pairInfo: List<UinPairReadInfo>? = null
    ) : ProtoBuf {
        @Serializable
        internal class UinPairReadInfo(
            @SerialId(1) val peerUin: Long = 0L,
            @SerialId(2) val lastReadTime: Int = 0,
            @SerialId(3) val crmSig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class Trans0x211(
        @SerialId(1) val toUin: Long = 0L,
        @SerialId(2) val ccCmd: Int = 0,
        @SerialId(3) val instCtrl: ImMsgHead.InstCtrl? = null,
        @SerialId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val c2cType: Int = 0,
        @SerialId(6) val serviceType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudReq(
        @SerialId(1) val serializeReqbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinUnReadMsgNumReq(
        @SerialId(1) val bindUin: Long = 0L,
        @SerialId(2) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqReq(
        @SerialId(1) val groupInfoReq: List<GroupInfoReq>? = null
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoReq(
            @SerialId(1) val groupCode: Long = 0L,
            @SerialId(2) val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class TransReq(
        @SerialId(1) val command: Int = 0,
        @SerialId(2) val reqTag: Int = 0,
        @SerialId(3) val reqBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupReadedReportResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val groupCode: Long = 0L,
        @SerialId(4) val memberSeq: Long = 0L,
        @SerialId(5) val groupMsgSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgResp(
        @SerialId(1) val result: Int = 0,
        @SerialId(2) val errmsg: String = "",
        @SerialId(3) val groupCode: Long = 0L,
        @SerialId(4) val returnBeginSeq: Long = 0L,
        @SerialId(5) val returnEndSeq: Long = 0L,
        @SerialId(6) val msg: List<MsgComm.Msg>? = null
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumReq(
        @SerialId(1) val thirdqqReqInfo: List<ThirdQQReqInfo>? = null,
        @SerialId(2) val source: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQReqInfo(
            @SerialId(1) val thirdUin: Long = 0L,
            @SerialId(2) val thirdUinSig: ByteArray = EMPTY_BYTE_ARRAY,
            @SerialId(3) val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}

@Serializable
internal class MsgCtrl {
    @Serializable
    internal class MsgCtrl(
        @SerialId(1) val msgFlag: Int = 0,
        @SerialId(2) val resvResvInfo: ResvResvInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ResvResvInfo(
        @SerialId(1) val flag: Int = 0,
        @SerialId(2) val reserv1: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val reserv2: Long = 0L,
        @SerialId(4) val reserv3: Long = 0L,
        @SerialId(5) val createTime: Int = 0,
        @SerialId(6) val picHeight: Int = 0,
        @SerialId(7) val picWidth: Int = 0,
        @SerialId(8) val resvFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class SubMsgType0xc1 {
    @Serializable
    internal class NotOnlineImage(
        @SerialId(1) val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fileLen: Int = 0,
        @SerialId(3) val downloadPath: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(4) val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val imgType: Int = 0,
        @SerialId(6) val previewsImage: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(7) val picMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(8) val picHeight: Int = 0,
        @SerialId(9) val picWidth: Int = 0,
        @SerialId(10) val resId: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(11) val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(12) val downloadUrl: String = "",
        @SerialId(13) val original: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @SerialId(1) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fromUin: Long = 0L,
        @SerialId(3) val toUin: Long = 0L,
        @SerialId(4) val status: Int = 0,
        @SerialId(5) val ttl: Int = 0,
        @SerialId(6) val type: Int = 0,
        @SerialId(7) val encryptPreheadLength: Int = 0,
        @SerialId(8) val encryptType: Int = 0,
        @SerialId(9) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(10) val readTimes: Int = 0,
        @SerialId(11) val leftTime: Int = 0,
        @SerialId(12) val notOnlineImage: NotOnlineImage? = null
    ) : ProtoBuf
}

@Serializable
internal class SubMsgType0x1a {
    @Serializable
    internal class MsgBody(
        @SerialId(1) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(2) val fromUin_int32: Int = 0,
        @SerialId(3) val toUin_int32: Int = 0,
        @SerialId(4) val status: Int = 0,
        @SerialId(5) val ttl: Int = 0,
        @SerialId(6) val ingDesc: String = "",
        @SerialId(7) val type: Int = 0,
        @SerialId(8) val captureTimes: Int = 0,
        @SerialId(9) val fromUin: Long = 0L,
        @SerialId(10) val toUin: Long = 0L
    ) : ProtoBuf
}