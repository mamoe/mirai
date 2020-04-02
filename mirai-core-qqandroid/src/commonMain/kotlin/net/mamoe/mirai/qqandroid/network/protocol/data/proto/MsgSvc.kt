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
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
internal class MsgSvc : ProtoBuf {
    @Serializable
    internal class Grp(
        @ProtoId(1) val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val syncCookie: ByteArray? = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val syncFlag: SyncFlag,
        @ProtoId(5) val uinPairMsgs: List<MsgComm.UinPairMsg>? = null,
        @ProtoId(6) val bindUin: Long = 0L,
        @ProtoId(7) val msgRspType: Int = 0,
        @ProtoId(8) val pubAccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val isPartialSync: Boolean = false,
        @ProtoId(10) val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawReq(
        @ProtoId(1) val subCmd: Int = 0,
        @ProtoId(2) val groupType: Int = 0,
        @ProtoId(3) val groupCode: Long = 0L,
        @ProtoId(4) val msgList: List<MessageInfo>? = null,
        @ProtoId(5) val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageInfo(
            @ProtoId(1) val msgSeq: Int = 0,
            @ProtoId(2) val msgRandom: Int = 0,
            @ProtoId(3) val msgType: Int = 0
        )
    }

    @Serializable
    internal class PbGroupReadedReportReq(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class BusinessWPATmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val sigt: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class C2C(
        @ProtoId(1) val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgReq(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val beginSeq: Long = 0L,
        @ProtoId(3) val endSeq: Long = 0L,
        @ProtoId(4) val filter: Int /* enum */ = 0,
        @ProtoId(5) val memberSeq: Long = 0L,
        @ProtoId(6) val publicGroup: Boolean = false,
        @ProtoId(7) val shieldFlag: Int = 0,
        @ProtoId(8) val saveTrafficFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmReq(
        @ProtoId(1) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AccostTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val confUin: Long = 0L,
        @ProtoId(4) val memberSeq: Long = 0L,
        @ProtoId(5) val confSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class NearByAssistantTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class MsgSendInfo(
        @ProtoId(1) val receiver: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PubGroupTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val groupUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AddressListTmp(
        @ProtoId(1) val fromPhone: String = "",
        @ProtoId(2) val toPhone: String = "",
        @ProtoId(3) val toUin: Long = 0L,
        @ProtoId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val fromContactSize: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DisTmp(
        @ProtoId(1) val disUin: Long = 0L,
        @ProtoId(2) val toUin: Long = 0L
    )

    @Serializable
    internal class PbMsgWithDrawResp(
        @ProtoId(1) val c2cWithDraw: List<PbC2CMsgWithDrawResp>? = null,
        @ProtoId(2) val groupWithDraw: List<PbGroupMsgWithDrawResp>? = null
    ) : ProtoBuf

    @Serializable
    internal class AuthTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbMsgWithDrawReq(
        @ProtoId(1) val c2cWithDraw: List<PbC2CMsgWithDrawReq>? = null,
        @ProtoId(2) val groupWithDraw: List<PbGroupMsgWithDrawReq>? = null
    ) : ProtoBuf

    internal enum class SyncFlag {
        START,
        CONTINUE,
        STOP
    }

    @Serializable
    internal class PbGetMsgReq(
        @ProtoId(1) val syncFlag: SyncFlag,
        @ProtoId(2) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val rambleFlag: Int = 1,
        @ProtoId(4) val latestRambleNumber: Int = 20,
        @ProtoId(5) val otherRambleNumber: Int = 3,
        @ProtoId(6) val onlineSyncFlag: Int = 1,
        @ProtoId(7) val contextFlag: Int = 0,
        @ProtoId(8) val whisperSessionId: Int = 0,
        @ProtoId(9) val msgReqType: Int = 0,
        @ProtoId(10) val pubaccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val serverBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgReq(
        @ProtoId(1) val peerUin: Long = 0L,
        @ProtoId(2) val lastMsgtime: Long = 0L,
        @ProtoId(3) val random: Long = 0L,
        @ProtoId(4) val readCnt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GrpTmp(
        @ProtoId(1) val groupUin: Long = 0L,
        @ProtoId(2) val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val discussUin: Long = 0L,
        @ProtoId(4) val returnEndSeq: Long = 0L,
        @ProtoId(5) val returnBeginSeq: Long = 0L,
        @ProtoId(6) val msg: List<MsgComm.Msg>? = null,
        @ProtoId(7) val lastGetTime: Long = 0L,
        @ProtoId(8) val discussInfoSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CommTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val c2cType: Int = 0,
        @ProtoId(3) val svrType: Int = 0,
        @ProtoId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val subCmd: Int = 0,
        @ProtoId(4) val groupType: Int = 0,
        @ProtoId(5) val groupCode: Long = 0L,
        @ProtoId(6) val failedMsgList: List<MessageResult>? = null,
        @ProtoId(7) val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageResult(
            @ProtoId(1) val result: Int = 0,
            @ProtoId(2) val msgSeq: Int = 0,
            @ProtoId(3) val msgTime: Int = 0,
            @ProtoId(4) val msgRandom: Int = 0,
            @ProtoId(5) val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) val msgType: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class PbC2CReadedReportResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumReq : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawReq(
        @ProtoId(1) val msgInfo: List<MsgInfo>? = null,
        @ProtoId(2) val longMessageFlag: Int = 0,
        @ProtoId(3) val reserved: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgInfo(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val toUin: Long = 0L,
            @ProtoId(3) val msgSeq: Int = 0,
            @ProtoId(4) val msgUid: Long = 0L,
            @ProtoId(5) val msgTime: Long = 0L,
            @ProtoId(6) val msgRandom: Int = 0,
            @ProtoId(7) val pkgNum: Int = 0,
            @ProtoId(8) val pkgIndex: Int = 0,
            @ProtoId(9) val divSeq: Int = 0,
            @ProtoId(10) val msgType: Int = 0,
            @ProtoId(20) val routingHead: RoutingHead? = null
        )
    }

    @Serializable
    internal class PbDelRoamMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Dis(
        @ProtoId(1) val disUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TransSvrInfo(
        @ProtoId(1) val subType: Int = 0,
        @ProtoId(2) val int32RetCode: Int = 0,
        @ProtoId(3) val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val transInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val groupInfoResp: List<GroupInfoResp>? = null
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoResp(
            @ProtoId(1) val groupCode: Long = 0L,
            @ProtoId(2) val memberSeq: Long = 0L,
            @ProtoId(3) val groupSeq: Long = 0L
        )
    }

    @Serializable
    internal class PbSendMsgReq(
        @ProtoId(1) val routingHead: RoutingHead? = null,
        @ProtoId(2) val contentHead: MsgComm.ContentHead? = null,
        @ProtoId(3) val msgBody: ImMsgBody.MsgBody = ImMsgBody.MsgBody(),
        @ProtoId(4) val msgSeq: Int = 0,
        @ProtoId(5) val msgRand: Int = 0,
        @ProtoId(6) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val appShare: MsgComm.AppShareInfo? = null,
        @ProtoId(8) val msgVia: Int = 0,
        @ProtoId(9) val dataStatist: Int = 0,
        @ProtoId(10) val multiMsgAssist: MultiMsgAssist? = null,
        @ProtoId(11) val inputNotifyInfo: PbInputNotifyInfo? = null,
        @ProtoId(12) val msgCtrl: MsgCtrl.MsgCtrl? = null,
        @ProtoId(13) val receiptReq: ImReceipt.ReceiptReq? = null,
        @ProtoId(14) val multiSendSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransMsg(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val c2cCmd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudResp(
        @ProtoId(1) val msg: List<MsgComm.Msg>? = null,
        @ProtoId(2) val serializeRspbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbInputNotifyInfo(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val ime: Int = 0,
        @ProtoId(3) val notifyFlag: Int = 0,
        @ProtoId(4) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val iosPushWording: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbUnReadMsgSeqResp(
        @ProtoId(1) val c2cUnreadInfo: PbC2CUnReadMsgNumResp? = null,
        @ProtoId(2) val binduinUnreadInfo: List<PbBindUinUnReadMsgNumResp>? = null,
        @ProtoId(3) val groupUnreadInfo: PbPullGroupMsgSeqResp? = null,
        @ProtoId(4) val discussUnreadInfo: PbPullDiscussMsgSeqResp? = null,
        @ProtoId(5) val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgReq(
        @ProtoId(1) val msgItems: List<MsgItem>? = null
    ) : ProtoBuf {
        @Serializable
        internal class MsgItem(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val toUin: Long = 0L,
            @ProtoId(3) val msgType: Int = 0,
            @ProtoId(4) val msgSeq: Int = 0,
            @ProtoId(5) val msgUid: Long = 0L,
            @ProtoId(7) val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class MultiMsgAssist(
        @ProtoId(1) val repeatedRouting: List<RoutingHead>? = null,
        @ProtoId(2) val msgUse: Int /* enum */ = 1,
        @ProtoId(3) val tempId: Long = 0L,
        @ProtoId(4) val vedioLen: Long = 0L,
        @ProtoId(5) val redbagId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val redbagAmount: Long = 0L,
        @ProtoId(7) val hasReadbag: Int = 0,
        @ProtoId(8) val hasVedio: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportReq(
        @ProtoId(1) val grpReadReport: List<PbGroupReadedReportReq>? = null,
        @ProtoId(2) val disReadReport: List<PbDiscussReadedReportReq>? = null,
        @ProtoId(3) val c2cReadReport: PbC2CReadedReportReq? = null,
        @ProtoId(4) val bindUinReadReport: PbBindUinMsgReadedConfirmReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val peerUin: Long = 0L,
        @ProtoId(4) val lastMsgtime: Long = 0L,
        @ProtoId(5) val random: Long = 0L,
        @ProtoId(6) val msg: List<MsgComm.Msg>? = null,
        @ProtoId(7) val iscomplete: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinGetMsgReq(
        @ProtoId(1) val bindUin: Long = 0L,
        @ProtoId(2) val bindUinSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val syncFlag: Int /* enum */ = 0,
        @ProtoId(4) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NearByDatingTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class BsnsTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RoutingHead(
        @ProtoId(1) val c2c: C2C? = null,
        @ProtoId(2) val grp: Grp? = null,
        @ProtoId(3) val grpTmp: GrpTmp? = null,
        @ProtoId(4) val dis: Dis? = null,
        @ProtoId(5) val disTmp: DisTmp? = null,
        @ProtoId(6) val wpaTmp: WPATmp? = null,
        @ProtoId(7) val secretFile: SecretFileHead? = null,
        @ProtoId(8) val publicPlat: PublicPlat? = null,
        @ProtoId(9) val transMsg: TransMsg? = null,
        @ProtoId(10) val addressList: AddressListTmp? = null,
        @ProtoId(11) val richStatusTmp: RichStatusTmp? = null,
        @ProtoId(12) val transCmd: TransCmd? = null,
        @ProtoId(13) val accostTmp: AccostTmp? = null,
        @ProtoId(14) val pubGroupTmp: PubGroupTmp? = null,
        @ProtoId(15) val trans0x211: Trans0x211? = null,
        @ProtoId(16) val businessWpaTmp: BusinessWPATmp? = null,
        @ProtoId(17) val authTmp: AuthTmp? = null,
        @ProtoId(18) val bsnsTmp: BsnsTmp? = null,
        @ProtoId(19) val qqQuerybusinessTmp: QQQueryBusinessTmp? = null,
        @ProtoId(20) val nearbyDatingTmp: NearByDatingTmp? = null,
        @ProtoId(21) val nearbyAssistantTmp: NearByAssistantTmp? = null,
        @ProtoId(22) val commTmp: CommTmp? = null
    ) : ProtoBuf

    @Serializable
    internal class TransResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val respTag: Int = 0,
        @ProtoId(4) val respBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbSendMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val sendTime: Int = 0,
        @ProtoId(4) val svrbusyWaitTime: Int = 0,
        @ProtoId(5) val msgSendInfo: MsgSendInfo? = null,
        @ProtoId(6) val errtype: Int = 0,
        @ProtoId(7) val transSvrInfo: TransSvrInfo? = null,
        @ProtoId(8) val receiptResp: ImReceipt.ReceiptResp? = null,
        @ProtoId(9) val textAnalysisResult: Int = 0
    ) : ProtoBuf, Packet

    @Serializable
    internal class PbBindUinUnReadMsgNumResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val bindUin: Long = 0L,
        @ProtoId(4) val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgReq(
        @ProtoId(1) val discussUin: Long = 0L,
        @ProtoId(2) val endSeq: Long = 0L,
        @ProtoId(3) val beginSeq: Long = 0L,
        @ProtoId(4) val lastGetTime: Long = 0L,
        @ProtoId(5) val discussInfoSeq: Long = 0L,
        @ProtoId(6) val filter: Int /* enum */ = 0,
        @ProtoId(7) val memberSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val msgStatus: List<MsgStatus>? = null,
        @ProtoId(4) val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgStatus(
            @ProtoId(1) val msgInfo: PbC2CMsgWithDrawReq.MsgInfo? = null,
            @ProtoId(2) val status: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class SecretFileHead(
        @ProtoId(1) val secretFileMsg: SubMsgType0xc1.MsgBody? = null,
        @ProtoId(2) val secretFileStatus: SubMsgType0x1a.MsgBody? = null
    )

    @Serializable
    internal class PbGetRoamMsgReq(
        @ProtoId(1) val peerUin: Long = 0L,
        @ProtoId(2) val lastMsgtime: Long = 0L,
        @ProtoId(3) val random: Long = 0L,
        @ProtoId(4) val readCnt: Int = 0,
        @ProtoId(5) val checkPwd: Int = 0,
        @ProtoId(6) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val pwd: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val subcmd: Int = 0,
        @ProtoId(9) val beginMsgtime: Long = 0L,
        @ProtoId(10) val reqType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransCmd(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportResp(
        @ProtoId(1) val grpReadReport: List<PbGroupReadedReportResp>? = null,
        @ProtoId(2) val disReadReport: List<PbDiscussReadedReportResp>? = null,
        @ProtoId(3) val c2cReadReport: PbC2CReadedReportResp? = null,
        @ProtoId(4) val bindUinReadReport: PbBindUinMsgReadedConfirmResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val thirdqqRespInfo: List<ThirdQQRespInfo>? = null,
        @ProtoId(4) val interval: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQRespInfo(
            @ProtoId(1) val thirdUin: Long = 0L,
            @ProtoId(2) val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val msgNum: Int = 0,
            @ProtoId(4) val msgFlag: Int = 0,
            @ProtoId(5) val redbagTime: Int = 0,
            @ProtoId(6) val status: Int = 0,
            @ProtoId(7) val lastMsgTime: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class RichStatusTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQQueryBusinessTmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDelRoamMsgReq(
        @ProtoId(1) val c2cMsg: C2CMsg? = null,
        @ProtoId(2) val grpMsg: GrpMsg? = null,
        @ProtoId(3) val disMsg: DisMsg? = null
    ) : ProtoBuf {
        @Serializable
        internal class GrpMsg(
            @ProtoId(1) val groupCode: Long = 0L,
            @ProtoId(2) val msgSeq: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class C2CMsg(
            @ProtoId(1) val fromUin: Long = 0L,
            @ProtoId(2) val peerUin: Long = 0L,
            @ProtoId(3) val msgTime: Int = 0,
            @ProtoId(4) val msgRandom: Int = 0,
            @ProtoId(5) val msgSeq: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DisMsg(
            @ProtoId(1) val discussUin: Long = 0L,
            @ProtoId(2) val msgSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbUnReadMsgSeqReq(
        @ProtoId(1) val c2cUnreadInfo: PbC2CUnReadMsgNumReq? = null,
        @ProtoId(2) val binduinUnreadInfo: List<PbBindUinUnReadMsgNumReq>? = null,
        @ProtoId(3) val groupUnreadInfo: PbPullGroupMsgSeqReq? = null,
        @ProtoId(4) val discussUnreadInfo: PbPullDiscussMsgSeqReq? = null,
        @ProtoId(5) val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbPullDiscussMsgSeqResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val discussInfoResp: List<DiscussInfoResp>? = null
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoResp(
            @ProtoId(1) val confUin: Long = 0L,
            @ProtoId(2) val memberSeq: Long = 0L,
            @ProtoId(3) val confSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbPullDiscussMsgSeqReq(
        @ProtoId(1) val discussInfoReq: List<DiscussInfoReq>? = null
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoReq(
            @ProtoId(1) val confUin: Long = 0L,
            @ProtoId(2) val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class WPATmp(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PublicPlat(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetRoamMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val peerUin: Long = 0L,
        @ProtoId(4) val lastMsgtime: Long = 0L,
        @ProtoId(5) val random: Long = 0L,
        @ProtoId(6) val msg: List<MsgComm.Msg>? = null,
        @ProtoId(7) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportReq(
        @ProtoId(1) val confUin: Long = 0L,
        @ProtoId(2) val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CReadedReportReq(
        @ProtoId(1) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val pairInfo: List<UinPairReadInfo>? = null
    ) : ProtoBuf {
        @Serializable
        internal class UinPairReadInfo(
            @ProtoId(1) val peerUin: Long = 0L,
            @ProtoId(2) val lastReadTime: Int = 0,
            @ProtoId(3) val crmSig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class Trans0x211(
        @ProtoId(1) val toUin: Long = 0L,
        @ProtoId(2) val ccCmd: Int = 0,
        @ProtoId(3) val instCtrl: ImMsgHead.InstCtrl? = null,
        @ProtoId(4) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val c2cType: Int = 0,
        @ProtoId(6) val serviceType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudReq(
        @ProtoId(1) val serializeReqbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinUnReadMsgNumReq(
        @ProtoId(1) val bindUin: Long = 0L,
        @ProtoId(2) val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqReq(
        @ProtoId(1) val groupInfoReq: List<GroupInfoReq>? = null
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoReq(
            @ProtoId(1) val groupCode: Long = 0L,
            @ProtoId(2) val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class TransReq(
        @ProtoId(1) val command: Int = 0,
        @ProtoId(2) val reqTag: Int = 0,
        @ProtoId(3) val reqBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupReadedReportResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val groupCode: Long = 0L,
        @ProtoId(4) val memberSeq: Long = 0L,
        @ProtoId(5) val groupMsgSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgResp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val errmsg: String = "",
        @ProtoId(3) val groupCode: Long = 0L,
        @ProtoId(4) val returnBeginSeq: Long = 0L,
        @ProtoId(5) val returnEndSeq: Long = 0L,
        @ProtoId(6) val msg: List<MsgComm.Msg>? = null
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumReq(
        @ProtoId(1) val thirdqqReqInfo: List<ThirdQQReqInfo>? = null,
        @ProtoId(2) val source: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQReqInfo(
            @ProtoId(1) val thirdUin: Long = 0L,
            @ProtoId(2) val thirdUinSig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}

@Serializable
internal class MsgCtrl {
    @Serializable
    internal class MsgCtrl(
        @ProtoId(1) val msgFlag: Int = 0,
        @ProtoId(2) val resvResvInfo: ResvResvInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ResvResvInfo(
        @ProtoId(1) val flag: Int = 0,
        @ProtoId(2) val reserv1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val reserv2: Long = 0L,
        @ProtoId(4) val reserv3: Long = 0L,
        @ProtoId(5) val createTime: Int = 0,
        @ProtoId(6) val picHeight: Int = 0,
        @ProtoId(7) val picWidth: Int = 0,
        @ProtoId(8) val resvFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class SubMsgType0xc1 {
    @Serializable
    internal class NotOnlineImage(
        @ProtoId(1) val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fileLen: Int = 0,
        @ProtoId(3) val downloadPath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val imgType: Int = 0,
        @ProtoId(6) val previewsImage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val picMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val picHeight: Int = 0,
        @ProtoId(9) val picWidth: Int = 0,
        @ProtoId(10) val resId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val downloadUrl: String = "",
        @ProtoId(13) val original: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @ProtoId(1) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fromUin: Long = 0L,
        @ProtoId(3) val toUin: Long = 0L,
        @ProtoId(4) val status: Int = 0,
        @ProtoId(5) val ttl: Int = 0,
        @ProtoId(6) val type: Int = 0,
        @ProtoId(7) val encryptPreheadLength: Int = 0,
        @ProtoId(8) val encryptType: Int = 0,
        @ProtoId(9) val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val readTimes: Int = 0,
        @ProtoId(11) val leftTime: Int = 0,
        @ProtoId(12) val notOnlineImage: NotOnlineImage? = null
    ) : ProtoBuf
}

@Serializable
internal class SubMsgType0x1a {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val fromUin_int32: Int = 0,
        @ProtoId(3) val toUin_int32: Int = 0,
        @ProtoId(4) val status: Int = 0,
        @ProtoId(5) val ttl: Int = 0,
        @ProtoId(6) val ingDesc: String = "",
        @ProtoId(7) val type: Int = 0,
        @ProtoId(8) val captureTimes: Int = 0,
        @ProtoId(9) val fromUin: Long = 0L,
        @ProtoId(10) val toUin: Long = 0L
    ) : ProtoBuf
}