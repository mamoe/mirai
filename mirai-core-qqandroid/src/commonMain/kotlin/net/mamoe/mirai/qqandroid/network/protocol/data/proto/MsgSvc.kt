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
import kotlin.jvm.JvmField

@Serializable
internal class MsgSvc : ProtoBuf {
    @Serializable
    internal class Grp(
        @ProtoId(1) @JvmField val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val syncCookie: ByteArray? = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val syncFlag: SyncFlag,
        @ProtoId(5) @JvmField val uinPairMsgs: List<MsgComm.UinPairMsg>? = null,
        @ProtoId(6) @JvmField val bindUin: Long = 0L,
        @ProtoId(7) @JvmField val msgRspType: Int = 0,
        @ProtoId(8) @JvmField val pubAccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val isPartialSync: Boolean = false,
        @ProtoId(10) @JvmField val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawReq(
        @ProtoId(1) @JvmField val subCmd: Int = 0,
        @ProtoId(2) @JvmField val groupType: Int = 0,
        @ProtoId(3) @JvmField val groupCode: Long = 0L,
        @ProtoId(4) @JvmField val msgList: List<MessageInfo>? = null,
        @ProtoId(5) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageInfo(
            @ProtoId(1) @JvmField val msgSeq: Int = 0,
            @ProtoId(2) @JvmField val msgRandom: Int = 0,
            @ProtoId(3) @JvmField val msgType: Int = 0
        )
    }

    @Serializable
    internal class PbGroupReadedReportReq(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class BusinessWPATmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val sigt: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class C2C(
        @ProtoId(1) @JvmField val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgReq(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val beginSeq: Long = 0L,
        @ProtoId(3) @JvmField val endSeq: Long = 0L,
        @ProtoId(4) @JvmField val filter: Int /* enum */ = 0,
        @ProtoId(5) @JvmField val memberSeq: Long = 0L,
        @ProtoId(6) @JvmField val publicGroup: Boolean = false,
        @ProtoId(7) @JvmField val shieldFlag: Int = 0,
        @ProtoId(8) @JvmField val saveTrafficFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmReq(
        @ProtoId(1) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AccostTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val confUin: Long = 0L,
        @ProtoId(4) @JvmField val memberSeq: Long = 0L,
        @ProtoId(5) @JvmField val confSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class NearByAssistantTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class MsgSendInfo(
        @ProtoId(1) @JvmField val receiver: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PubGroupTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val groupUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AddressListTmp(
        @ProtoId(1) @JvmField val fromPhone: String = "",
        @ProtoId(2) @JvmField val toPhone: String = "",
        @ProtoId(3) @JvmField val toUin: Long = 0L,
        @ProtoId(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val fromContactSize: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DisTmp(
        @ProtoId(1) @JvmField val disUin: Long = 0L,
        @ProtoId(2) @JvmField val toUin: Long = 0L
    )

    @Serializable
    internal class PbMsgWithDrawResp(
        @ProtoId(1) @JvmField val c2cWithDraw: List<PbC2CMsgWithDrawResp>? = null,
        @ProtoId(2) @JvmField val groupWithDraw: List<PbGroupMsgWithDrawResp>? = null
    ) : ProtoBuf

    @Serializable
    internal class AuthTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbMsgWithDrawReq(
        @ProtoId(1) @JvmField val c2cWithDraw: List<PbC2CMsgWithDrawReq>? = null,
        @ProtoId(2) @JvmField val groupWithDraw: List<PbGroupMsgWithDrawReq>? = null
    ) : ProtoBuf

    internal enum class SyncFlag {
        START,
        CONTINUE,
        STOP
    }

    @Serializable
    internal class PbGetMsgReq(
        @ProtoId(1) @JvmField val syncFlag: SyncFlag,
        @ProtoId(2) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val rambleFlag: Int = 1,
        @ProtoId(4) @JvmField val latestRambleNumber: Int = 20,
        @ProtoId(5) @JvmField val otherRambleNumber: Int = 3,
        @ProtoId(6) @JvmField val onlineSyncFlag: Int = 1,
        @ProtoId(7) @JvmField val contextFlag: Int = 0,
        @ProtoId(8) @JvmField val whisperSessionId: Int = 0,
        @ProtoId(9) @JvmField val msgReqType: Int = 0,
        @ProtoId(10) @JvmField val pubaccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val serverBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgReq(
        @ProtoId(1) @JvmField val peerUin: Long = 0L,
        @ProtoId(2) @JvmField val lastMsgtime: Long = 0L,
        @ProtoId(3) @JvmField val random: Long = 0L,
        @ProtoId(4) @JvmField val readCnt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GrpTmp(
        @ProtoId(1) @JvmField val groupUin: Long = 0L,
        @ProtoId(2) @JvmField val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val discussUin: Long = 0L,
        @ProtoId(4) @JvmField val returnEndSeq: Long = 0L,
        @ProtoId(5) @JvmField val returnBeginSeq: Long = 0L,
        @ProtoId(6) @JvmField val msg: List<MsgComm.Msg>? = null,
        @ProtoId(7) @JvmField val lastGetTime: Long = 0L,
        @ProtoId(8) @JvmField val discussInfoSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CommTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val c2cType: Int = 0,
        @ProtoId(3) @JvmField val svrType: Int = 0,
        @ProtoId(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val subCmd: Int = 0,
        @ProtoId(4) @JvmField val groupType: Int = 0,
        @ProtoId(5) @JvmField val groupCode: Long = 0L,
        @ProtoId(6) @JvmField val failedMsgList: List<MessageResult>? = null,
        @ProtoId(7) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageResult(
            @ProtoId(1) @JvmField val result: Int = 0,
            @ProtoId(2) @JvmField val msgSeq: Int = 0,
            @ProtoId(3) @JvmField val msgTime: Int = 0,
            @ProtoId(4) @JvmField val msgRandom: Int = 0,
            @ProtoId(5) @JvmField val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(6) @JvmField val msgType: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class PbC2CReadedReportResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumReq : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawReq(
        @ProtoId(1) @JvmField val msgInfo: List<MsgInfo>? = null,
        @ProtoId(2) @JvmField val longMessageFlag: Int = 0,
        @ProtoId(3) @JvmField val reserved: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgInfo(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val toUin: Long = 0L,
            @ProtoId(3) @JvmField val msgSeq: Int = 0,
            @ProtoId(4) @JvmField val msgUid: Long = 0L,
            @ProtoId(5) @JvmField val msgTime: Long = 0L,
            @ProtoId(6) @JvmField val msgRandom: Int = 0,
            @ProtoId(7) @JvmField val pkgNum: Int = 0,
            @ProtoId(8) @JvmField val pkgIndex: Int = 0,
            @ProtoId(9) @JvmField val divSeq: Int = 0,
            @ProtoId(10) @JvmField val msgType: Int = 0,
            @ProtoId(20) @JvmField val routingHead: RoutingHead? = null
        )
    }

    @Serializable
    internal class PbDelRoamMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Dis(
        @ProtoId(1) @JvmField val disUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TransSvrInfo(
        @ProtoId(1) @JvmField val subType: Int = 0,
        @ProtoId(2) @JvmField val int32RetCode: Int = 0,
        @ProtoId(3) @JvmField val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val transInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val groupInfoResp: List<GroupInfoResp>? = null
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoResp(
            @ProtoId(1) @JvmField val groupCode: Long = 0L,
            @ProtoId(2) @JvmField val memberSeq: Long = 0L,
            @ProtoId(3) @JvmField val groupSeq: Long = 0L
        )
    }

    @Serializable
    internal class PbSendMsgReq(
        @ProtoId(1) @JvmField val routingHead: RoutingHead? = null,
        @ProtoId(2) @JvmField val contentHead: MsgComm.ContentHead? = null,
        @ProtoId(3) @JvmField val msgBody: ImMsgBody.MsgBody = ImMsgBody.MsgBody(),
        @ProtoId(4) @JvmField val msgSeq: Int = 0,
        @ProtoId(5) @JvmField val msgRand: Int = 0,
        @ProtoId(6) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val appShare: MsgComm.AppShareInfo? = null,
        @ProtoId(8) @JvmField val msgVia: Int = 0,
        @ProtoId(9) @JvmField val dataStatist: Int = 0,
        @ProtoId(10) @JvmField val multiMsgAssist: MultiMsgAssist? = null,
        @ProtoId(11) @JvmField val inputNotifyInfo: PbInputNotifyInfo? = null,
        @ProtoId(12) @JvmField val msgCtrl: MsgCtrl.MsgCtrl? = null,
        @ProtoId(13) @JvmField val receiptReq: ImReceipt.ReceiptReq? = null,
        @ProtoId(14) @JvmField val multiSendSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransMsg(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val c2cCmd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudResp(
        @ProtoId(1) @JvmField val msg: List<MsgComm.Msg>? = null,
        @ProtoId(2) @JvmField val serializeRspbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbInputNotifyInfo(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val ime: Int = 0,
        @ProtoId(3) @JvmField val notifyFlag: Int = 0,
        @ProtoId(4) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val iosPushWording: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbUnReadMsgSeqResp(
        @ProtoId(1) @JvmField val c2cUnreadInfo: PbC2CUnReadMsgNumResp? = null,
        @ProtoId(2) @JvmField val binduinUnreadInfo: List<PbBindUinUnReadMsgNumResp>? = null,
        @ProtoId(3) @JvmField val groupUnreadInfo: PbPullGroupMsgSeqResp? = null,
        @ProtoId(4) @JvmField val discussUnreadInfo: PbPullDiscussMsgSeqResp? = null,
        @ProtoId(5) @JvmField val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgReq(
        @ProtoId(1) @JvmField val msgItems: List<MsgItem>? = null
    ) : ProtoBuf {
        @Serializable
        internal class MsgItem(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val toUin: Long = 0L,
            @ProtoId(3) @JvmField val msgType: Int = 0,
            @ProtoId(4) @JvmField val msgSeq: Int = 0,
            @ProtoId(5) @JvmField val msgUid: Long = 0L,
            @ProtoId(7) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class MultiMsgAssist(
        @ProtoId(1) @JvmField val repeatedRouting: List<RoutingHead>? = null,
        @ProtoId(2) @JvmField val msgUse: Int /* enum */ = 1,
        @ProtoId(3) @JvmField val tempId: Long = 0L,
        @ProtoId(4) @JvmField val vedioLen: Long = 0L,
        @ProtoId(5) @JvmField val redbagId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val redbagAmount: Long = 0L,
        @ProtoId(7) @JvmField val hasReadbag: Int = 0,
        @ProtoId(8) @JvmField val hasVedio: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportReq(
        @ProtoId(1) @JvmField val grpReadReport: List<PbGroupReadedReportReq>? = null,
        @ProtoId(2) @JvmField val disReadReport: List<PbDiscussReadedReportReq>? = null,
        @ProtoId(3) @JvmField val c2cReadReport: PbC2CReadedReportReq? = null,
        @ProtoId(4) @JvmField val bindUinReadReport: PbBindUinMsgReadedConfirmReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val peerUin: Long = 0L,
        @ProtoId(4) @JvmField val lastMsgtime: Long = 0L,
        @ProtoId(5) @JvmField val random: Long = 0L,
        @ProtoId(6) @JvmField val msg: List<MsgComm.Msg>? = null,
        @ProtoId(7) @JvmField val iscomplete: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinGetMsgReq(
        @ProtoId(1) @JvmField val bindUin: Long = 0L,
        @ProtoId(2) @JvmField val bindUinSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val syncFlag: Int /* enum */ = 0,
        @ProtoId(4) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NearByDatingTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class BsnsTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RoutingHead(
        @ProtoId(1) @JvmField val c2c: C2C? = null,
        @ProtoId(2) @JvmField val grp: Grp? = null,
        @ProtoId(3) @JvmField val grpTmp: GrpTmp? = null,
        @ProtoId(4) @JvmField val dis: Dis? = null,
        @ProtoId(5) @JvmField val disTmp: DisTmp? = null,
        @ProtoId(6) @JvmField val wpaTmp: WPATmp? = null,
        @ProtoId(7) @JvmField val secretFile: SecretFileHead? = null,
        @ProtoId(8) @JvmField val publicPlat: PublicPlat? = null,
        @ProtoId(9) @JvmField val transMsg: TransMsg? = null,
        @ProtoId(10) @JvmField val addressList: AddressListTmp? = null,
        @ProtoId(11) @JvmField val richStatusTmp: RichStatusTmp? = null,
        @ProtoId(12) @JvmField val transCmd: TransCmd? = null,
        @ProtoId(13) @JvmField val accostTmp: AccostTmp? = null,
        @ProtoId(14) @JvmField val pubGroupTmp: PubGroupTmp? = null,
        @ProtoId(15) @JvmField val trans0x211: Trans0x211? = null,
        @ProtoId(16) @JvmField val businessWpaTmp: BusinessWPATmp? = null,
        @ProtoId(17) @JvmField val authTmp: AuthTmp? = null,
        @ProtoId(18) @JvmField val bsnsTmp: BsnsTmp? = null,
        @ProtoId(19) @JvmField val qqQuerybusinessTmp: QQQueryBusinessTmp? = null,
        @ProtoId(20) @JvmField val nearbyDatingTmp: NearByDatingTmp? = null,
        @ProtoId(21) @JvmField val nearbyAssistantTmp: NearByAssistantTmp? = null,
        @ProtoId(22) @JvmField val commTmp: CommTmp? = null
    ) : ProtoBuf

    @Serializable
    internal class TransResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val respTag: Int = 0,
        @ProtoId(4) @JvmField val respBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbSendMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val sendTime: Int = 0,
        @ProtoId(4) @JvmField val svrbusyWaitTime: Int = 0,
        @ProtoId(5) @JvmField val msgSendInfo: MsgSendInfo? = null,
        @ProtoId(6) @JvmField val errtype: Int = 0,
        @ProtoId(7) @JvmField val transSvrInfo: TransSvrInfo? = null,
        @ProtoId(8) @JvmField val receiptResp: ImReceipt.ReceiptResp? = null,
        @ProtoId(9) @JvmField val textAnalysisResult: Int = 0
    ) : ProtoBuf, Packet

    @Serializable
    internal class PbBindUinUnReadMsgNumResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val bindUin: Long = 0L,
        @ProtoId(4) @JvmField val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgReq(
        @ProtoId(1) @JvmField val discussUin: Long = 0L,
        @ProtoId(2) @JvmField val endSeq: Long = 0L,
        @ProtoId(3) @JvmField val beginSeq: Long = 0L,
        @ProtoId(4) @JvmField val lastGetTime: Long = 0L,
        @ProtoId(5) @JvmField val discussInfoSeq: Long = 0L,
        @ProtoId(6) @JvmField val filter: Int /* enum */ = 0,
        @ProtoId(7) @JvmField val memberSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val msgStatus: List<MsgStatus>? = null,
        @ProtoId(4) @JvmField val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgStatus(
            @ProtoId(1) @JvmField val msgInfo: PbC2CMsgWithDrawReq.MsgInfo? = null,
            @ProtoId(2) @JvmField val status: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class SecretFileHead(
        @ProtoId(1) @JvmField val secretFileMsg: SubMsgType0xc1.MsgBody? = null
        //    @ProtoId(2) @JvmField val secretFileStatus: SubMsgType0x1a.MsgBody? = null
    )

    @Serializable
    internal class PbGetRoamMsgReq(
        @ProtoId(1) @JvmField val peerUin: Long = 0L,
        @ProtoId(2) @JvmField val lastMsgtime: Long = 0L,
        @ProtoId(3) @JvmField val random: Long = 0L,
        @ProtoId(4) @JvmField val readCnt: Int = 0,
        @ProtoId(5) @JvmField val checkPwd: Int = 0,
        @ProtoId(6) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val pwd: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val subcmd: Int = 0,
        @ProtoId(9) @JvmField val beginMsgtime: Long = 0L,
        @ProtoId(10) @JvmField val reqType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransCmd(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportResp(
        @ProtoId(1) @JvmField val grpReadReport: List<PbGroupReadedReportResp>? = null,
        @ProtoId(2) @JvmField val disReadReport: List<PbDiscussReadedReportResp>? = null,
        @ProtoId(3) @JvmField val c2cReadReport: PbC2CReadedReportResp? = null,
        @ProtoId(4) @JvmField val bindUinReadReport: PbBindUinMsgReadedConfirmResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val thirdqqRespInfo: List<ThirdQQRespInfo>? = null,
        @ProtoId(4) @JvmField val interval: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQRespInfo(
            @ProtoId(1) @JvmField val thirdUin: Long = 0L,
            @ProtoId(2) @JvmField val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val msgNum: Int = 0,
            @ProtoId(4) @JvmField val msgFlag: Int = 0,
            @ProtoId(5) @JvmField val redbagTime: Int = 0,
            @ProtoId(6) @JvmField val status: Int = 0,
            @ProtoId(7) @JvmField val lastMsgTime: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class RichStatusTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQQueryBusinessTmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDelRoamMsgReq(
        @ProtoId(1) @JvmField val c2cMsg: C2CMsg? = null,
        @ProtoId(2) @JvmField val grpMsg: GrpMsg? = null,
        @ProtoId(3) @JvmField val disMsg: DisMsg? = null
    ) : ProtoBuf {
        @Serializable
        internal class GrpMsg(
            @ProtoId(1) @JvmField val groupCode: Long = 0L,
            @ProtoId(2) @JvmField val msgSeq: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class C2CMsg(
            @ProtoId(1) @JvmField val fromUin: Long = 0L,
            @ProtoId(2) @JvmField val peerUin: Long = 0L,
            @ProtoId(3) @JvmField val msgTime: Int = 0,
            @ProtoId(4) @JvmField val msgRandom: Int = 0,
            @ProtoId(5) @JvmField val msgSeq: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DisMsg(
            @ProtoId(1) @JvmField val discussUin: Long = 0L,
            @ProtoId(2) @JvmField val msgSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbUnReadMsgSeqReq(
        @ProtoId(1) @JvmField val c2cUnreadInfo: PbC2CUnReadMsgNumReq? = null,
        @ProtoId(2) @JvmField val binduinUnreadInfo: List<PbBindUinUnReadMsgNumReq>? = null,
        @ProtoId(3) @JvmField val groupUnreadInfo: PbPullGroupMsgSeqReq? = null,
        @ProtoId(4) @JvmField val discussUnreadInfo: PbPullDiscussMsgSeqReq? = null,
        @ProtoId(5) @JvmField val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbPullDiscussMsgSeqResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val discussInfoResp: List<DiscussInfoResp>? = null
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoResp(
            @ProtoId(1) @JvmField val confUin: Long = 0L,
            @ProtoId(2) @JvmField val memberSeq: Long = 0L,
            @ProtoId(3) @JvmField val confSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbPullDiscussMsgSeqReq(
        @ProtoId(1) @JvmField val discussInfoReq: List<DiscussInfoReq>? = null
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoReq(
            @ProtoId(1) @JvmField val confUin: Long = 0L,
            @ProtoId(2) @JvmField val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class WPATmp(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PublicPlat(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetRoamMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val peerUin: Long = 0L,
        @ProtoId(4) @JvmField val lastMsgtime: Long = 0L,
        @ProtoId(5) @JvmField val random: Long = 0L,
        @ProtoId(6) @JvmField val msg: List<MsgComm.Msg>? = null,
        @ProtoId(7) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportReq(
        @ProtoId(1) @JvmField val confUin: Long = 0L,
        @ProtoId(2) @JvmField val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CReadedReportReq(
        @ProtoId(1) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val pairInfo: List<UinPairReadInfo>? = null
    ) : ProtoBuf {
        @Serializable
        internal class UinPairReadInfo(
            @ProtoId(1) @JvmField val peerUin: Long = 0L,
            @ProtoId(2) @JvmField val lastReadTime: Int = 0,
            @ProtoId(3) @JvmField val crmSig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class Trans0x211(
        @ProtoId(1) @JvmField val toUin: Long = 0L,
        @ProtoId(2) @JvmField val ccCmd: Int = 0,
        @ProtoId(3) @JvmField val instCtrl: ImMsgHead.InstCtrl? = null,
        @ProtoId(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val c2cType: Int = 0,
        @ProtoId(6) @JvmField val serviceType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudReq(
        @ProtoId(1) @JvmField val serializeReqbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinUnReadMsgNumReq(
        @ProtoId(1) @JvmField val bindUin: Long = 0L,
        @ProtoId(2) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqReq(
        @ProtoId(1) @JvmField val groupInfoReq: List<GroupInfoReq>? = null
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoReq(
            @ProtoId(1) @JvmField val groupCode: Long = 0L,
            @ProtoId(2) @JvmField val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class TransReq(
        @ProtoId(1) @JvmField val command: Int = 0,
        @ProtoId(2) @JvmField val reqTag: Int = 0,
        @ProtoId(3) @JvmField val reqBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupReadedReportResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val groupCode: Long = 0L,
        @ProtoId(4) @JvmField val memberSeq: Long = 0L,
        @ProtoId(5) @JvmField val groupMsgSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgResp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val errmsg: String = "",
        @ProtoId(3) @JvmField val groupCode: Long = 0L,
        @ProtoId(4) @JvmField val returnBeginSeq: Long = 0L,
        @ProtoId(5) @JvmField val returnEndSeq: Long = 0L,
        @ProtoId(6) @JvmField val msg: List<MsgComm.Msg>? = null
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumReq(
        @ProtoId(1) @JvmField val thirdqqReqInfo: List<ThirdQQReqInfo>? = null,
        @ProtoId(2) @JvmField val source: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQReqInfo(
            @ProtoId(1) @JvmField val thirdUin: Long = 0L,
            @ProtoId(2) @JvmField val thirdUinSig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoId(3) @JvmField val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}

@Serializable
internal class MsgCtrl {
    @Serializable
    internal class MsgCtrl(
        @ProtoId(1) @JvmField val msgFlag: Int = 0,
        @ProtoId(2) @JvmField val resvResvInfo: ResvResvInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ResvResvInfo(
        @ProtoId(1) @JvmField val flag: Int = 0,
        @ProtoId(2) @JvmField val reserv1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val reserv2: Long = 0L,
        @ProtoId(4) @JvmField val reserv3: Long = 0L,
        @ProtoId(5) @JvmField val createTime: Int = 0,
        @ProtoId(6) @JvmField val picHeight: Int = 0,
        @ProtoId(7) @JvmField val picWidth: Int = 0,
        @ProtoId(8) @JvmField val resvFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class SubMsgType0xc1 {
    @Serializable
    internal class NotOnlineImage(
        @ProtoId(1) @JvmField val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val fileLen: Int = 0,
        @ProtoId(3) @JvmField val downloadPath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val imgType: Int = 0,
        @ProtoId(6) @JvmField val previewsImage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val picMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val picHeight: Int = 0,
        @ProtoId(9) @JvmField val picWidth: Int = 0,
        @ProtoId(10) @JvmField val resId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val downloadUrl: String = "",
        @ProtoId(13) @JvmField val original: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @ProtoId(1) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val fromUin: Long = 0L,
        @ProtoId(3) @JvmField val toUin: Long = 0L,
        @ProtoId(4) @JvmField val status: Int = 0,
        @ProtoId(5) @JvmField val ttl: Int = 0,
        @ProtoId(6) @JvmField val type: Int = 0,
        @ProtoId(7) @JvmField val encryptPreheadLength: Int = 0,
        @ProtoId(8) @JvmField val encryptType: Int = 0,
        @ProtoId(9) @JvmField val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val readTimes: Int = 0,
        @ProtoId(11) @JvmField val leftTime: Int = 0,
        @ProtoId(12) @JvmField val notOnlineImage: NotOnlineImage? = null
    ) : ProtoBuf
}

/*
@Serializable
internal class SubMsgType0x1a {
    @Serializable
    internal class MsgBody(
        @ProtoId(1) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val fromUin_int32: Int = 0,
        @ProtoId(3) @JvmField val toUin_int32: Int = 0,
        @ProtoId(4) @JvmField val status: Int = 0,
        @ProtoId(5) @JvmField val ttl: Int = 0,
        @ProtoId(6) @JvmField val ingDesc: String = "",
        @ProtoId(7) @JvmField val type: Int = 0,
        @ProtoId(8) @JvmField val captureTimes: Int = 0,
        @ProtoId(9) @JvmField val fromUin: Long = 0L,
        @ProtoId(10) @JvmField val toUin: Long = 0L
    ) : ProtoBuf
}*/