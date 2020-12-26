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
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

@Serializable
internal class MsgSvc : ProtoBuf {
    @Serializable
    internal class Grp(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val syncCookie: ByteArray? = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val syncFlag: SyncFlag = SyncFlag.CONTINUE,
        @ProtoNumber(5) @JvmField val uinPairMsgs: List<MsgComm.UinPairMsg> = emptyList(),
        @ProtoNumber(6) @JvmField val bindUin: Long = 0L,
        @ProtoNumber(7) @JvmField val msgRspType: Int = 0,
        @ProtoNumber(8) @JvmField val pubAccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val isPartialSync: Boolean = false,
        @ProtoNumber(10) @JvmField val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawReq(
        @ProtoNumber(1) @JvmField val subCmd: Int = 0,
        @ProtoNumber(2) @JvmField val groupType: Int = 0,
        @ProtoNumber(3) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(4) @JvmField val msgList: List<MessageInfo> = emptyList(),
        @ProtoNumber(5) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageInfo(
            @ProtoNumber(1) @JvmField val msgSeq: Int = 0,
            @ProtoNumber(2) @JvmField val msgRandom: Int = 0,
            @ProtoNumber(3) @JvmField val msgType: Int = 0
        )
    }

    @Serializable
    internal class PbGroupReadedReportReq(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class BusinessWPATmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val sigt: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class C2C(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgReq(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val beginSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val endSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val filter: Int /* enum */ = 0,
        @ProtoNumber(5) @JvmField val memberSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val publicGroup: Boolean = false,
        @ProtoNumber(7) @JvmField val shieldFlag: Int = 0,
        @ProtoNumber(8) @JvmField val saveTrafficFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmReq(
        @ProtoNumber(1) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AccostTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val confUin: Long = 0L,
        @ProtoNumber(4) @JvmField val memberSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val confSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class NearByAssistantTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class MsgSendInfo(
        @ProtoNumber(1) @JvmField val receiver: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PubGroupTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val groupUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class AddressListTmp(
        @ProtoNumber(1) @JvmField val fromPhone: String = "",
        @ProtoNumber(2) @JvmField val toPhone: String = "",
        @ProtoNumber(3) @JvmField val toUin: Long = 0L,
        @ProtoNumber(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val fromContactSize: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DisTmp(
        @ProtoNumber(1) @JvmField val disUin: Long = 0L,
        @ProtoNumber(2) @JvmField val toUin: Long = 0L
    )

    @Serializable
    internal class PbMsgWithDrawResp(
        @ProtoNumber(1) @JvmField val c2cWithDraw: List<PbC2CMsgWithDrawResp> = emptyList(),
        @ProtoNumber(2) @JvmField val groupWithDraw: List<PbGroupMsgWithDrawResp> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class AuthTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbMsgWithDrawReq(
        @ProtoNumber(1) @JvmField val c2cWithDraw: List<PbC2CMsgWithDrawReq> = emptyList(),
        @ProtoNumber(2) @JvmField val groupWithDraw: List<PbGroupMsgWithDrawReq> = emptyList()
    ) : ProtoBuf

    internal enum class SyncFlag {
        START,
        CONTINUE,
        STOP
    }

    @Serializable
    internal class PbGetMsgReq(
        @ProtoNumber(1) @JvmField val syncFlag: SyncFlag,
        @ProtoNumber(2) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val rambleFlag: Int = 1,
        @ProtoNumber(4) @JvmField val latestRambleNumber: Int = 20,
        @ProtoNumber(5) @JvmField val otherRambleNumber: Int = 3,
        @ProtoNumber(6) @JvmField val onlineSyncFlag: Int = 1,
        @ProtoNumber(7) @JvmField val contextFlag: Int = 0,
        @ProtoNumber(8) @JvmField val whisperSessionId: Int = 0,
        @ProtoNumber(9) @JvmField val msgReqType: Int = 0,
        @ProtoNumber(10) @JvmField val pubaccountCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val serverBuf: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgReq(
        @ProtoNumber(1) @JvmField val peerUin: Long = 0L,
        @ProtoNumber(2) @JvmField val lastMsgtime: Long = 0L,
        @ProtoNumber(3) @JvmField val random: Long = 0L,
        @ProtoNumber(4) @JvmField val readCnt: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GrpTmp(
        @ProtoNumber(1) @JvmField val groupUin: Long = 0L,
        @ProtoNumber(2) @JvmField val toUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val discussUin: Long = 0L,
        @ProtoNumber(4) @JvmField val returnEndSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val returnBeginSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val msg: List<MsgComm.Msg> = emptyList(),
        @ProtoNumber(7) @JvmField val lastGetTime: Long = 0L,
        @ProtoNumber(8) @JvmField val discussInfoSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CommTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val c2cType: Int = 0,
        @ProtoNumber(3) @JvmField val svrType: Int = 0,
        @ProtoNumber(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupMsgWithDrawResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val subCmd: Int = 0,
        @ProtoNumber(4) @JvmField val groupType: Int = 0,
        @ProtoNumber(5) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(6) @JvmField val failedMsgList: List<MessageResult> = emptyList(),
        @ProtoNumber(7) @JvmField val userdef: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf {
        @Serializable
        internal class MessageResult(
            @ProtoNumber(1) @JvmField val result: Int = 0,
            @ProtoNumber(2) @JvmField val msgSeq: Int = 0,
            @ProtoNumber(3) @JvmField val msgTime: Int = 0,
            @ProtoNumber(4) @JvmField val msgRandom: Int = 0,
            @ProtoNumber(5) @JvmField val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(6) @JvmField val msgType: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class PbC2CReadedReportResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumReq : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawReq(
        @ProtoNumber(1) @JvmField val msgInfo: List<MsgInfo> = emptyList(),
        @ProtoNumber(2) @JvmField val longMessageFlag: Int = 0,
        @ProtoNumber(3) @JvmField val reserved: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgInfo(
            @ProtoNumber(1) @JvmField val fromUin: Long = 0L,
            @ProtoNumber(2) @JvmField val toUin: Long = 0L,
            @ProtoNumber(3) @JvmField val msgSeq: Int = 0,
            @ProtoNumber(4) @JvmField val msgUid: Long = 0L,
            @ProtoNumber(5) @JvmField val msgTime: Long = 0L,
            @ProtoNumber(6) @JvmField val msgRandom: Int = 0,
            @ProtoNumber(7) @JvmField val pkgNum: Int = 0,
            @ProtoNumber(8) @JvmField val pkgIndex: Int = 0,
            @ProtoNumber(9) @JvmField val divSeq: Int = 0,
            @ProtoNumber(10) @JvmField val msgType: Int = 0,
            @ProtoNumber(20) @JvmField val routingHead: RoutingHead? = null
        )
    }

    @Serializable
    internal class PbDelRoamMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Dis(
        @ProtoNumber(1) @JvmField val disUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TransSvrInfo(
        @ProtoNumber(1) @JvmField val subType: Int = 0,
        @ProtoNumber(2) @JvmField val int32RetCode: Int = 0,
        @ProtoNumber(3) @JvmField val errMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val transInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val groupInfoResp: List<GroupInfoResp> = emptyList()
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoResp(
            @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
            @ProtoNumber(2) @JvmField val memberSeq: Long = 0L,
            @ProtoNumber(3) @JvmField val groupSeq: Long = 0L
        )
    }

    @Serializable
    internal class PbSendMsgReq(
        @ProtoNumber(1) @JvmField val routingHead: RoutingHead? = null,
        @ProtoNumber(2) @JvmField val contentHead: MsgComm.ContentHead? = null,
        @ProtoNumber(3) @JvmField val msgBody: ImMsgBody.MsgBody = ImMsgBody.MsgBody(),
        @ProtoNumber(4) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(5) @JvmField val msgRand: Int = 0,
        @ProtoNumber(6) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val appShare: MsgComm.AppShareInfo? = null,
        @ProtoNumber(8) @JvmField val msgVia: Int = 0,
        @ProtoNumber(9) @JvmField val dataStatist: Int = 0,
        @ProtoNumber(10) @JvmField val multiMsgAssist: MultiMsgAssist? = null,
        @ProtoNumber(11) @JvmField val inputNotifyInfo: PbInputNotifyInfo? = null,
        @ProtoNumber(12) @JvmField val msgCtrl: MsgCtrl.MsgCtrl? = null,
        @ProtoNumber(13) @JvmField val receiptReq: ImReceipt.ReceiptReq? = null,
        @ProtoNumber(14) @JvmField val multiSendSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransMsg(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val c2cCmd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudResp(
        @ProtoNumber(1) @JvmField val msg: List<MsgComm.Msg> = emptyList(),
        @ProtoNumber(2) @JvmField val serializeRspbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbInputNotifyInfo(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val ime: Int = 0,
        @ProtoNumber(3) @JvmField val notifyFlag: Int = 0,
        @ProtoNumber(4) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val iosPushWording: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbUnReadMsgSeqResp(
        @ProtoNumber(1) @JvmField val c2cUnreadInfo: PbC2CUnReadMsgNumResp? = null,
        @ProtoNumber(2) @JvmField val binduinUnreadInfo: List<PbBindUinUnReadMsgNumResp> = emptyList(),
        @ProtoNumber(3) @JvmField val groupUnreadInfo: PbPullGroupMsgSeqResp? = null,
        @ProtoNumber(4) @JvmField val discussUnreadInfo: PbPullDiscussMsgSeqResp? = null,
        @ProtoNumber(5) @JvmField val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbDeleteMsgReq(
        @ProtoNumber(1) @JvmField val msgItems: List<MsgItem> = emptyList()
    ) : ProtoBuf {
        @Serializable
        internal class MsgItem(
            @ProtoNumber(1) @JvmField val fromUin: Long = 0L,
            @ProtoNumber(2) @JvmField val toUin: Long = 0L,
            @ProtoNumber(3) @JvmField val msgType: Int = 0,
            @ProtoNumber(4) @JvmField val msgSeq: Int = 0,
            @ProtoNumber(5) @JvmField val msgUid: Long = 0L,
            @ProtoNumber(7) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class MultiMsgAssist(
        @ProtoNumber(1) @JvmField val repeatedRouting: List<RoutingHead> = emptyList(),
        @ProtoNumber(2) @JvmField val msgUse: Int /* enum */ = 1,
        @ProtoNumber(3) @JvmField val tempId: Long = 0L,
        @ProtoNumber(4) @JvmField val vedioLen: Long = 0L,
        @ProtoNumber(5) @JvmField val redbagId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val redbagAmount: Long = 0L,
        @ProtoNumber(7) @JvmField val hasReadbag: Int = 0,
        @ProtoNumber(8) @JvmField val hasVedio: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportReq(
        @ProtoNumber(1) @JvmField val grpReadReport: List<PbGroupReadedReportReq> = emptyList(),
        @ProtoNumber(2) @JvmField val disReadReport: List<PbDiscussReadedReportReq> = emptyList(),
        @ProtoNumber(3) @JvmField val c2cReadReport: PbC2CReadedReportReq? = null,
        @ProtoNumber(4) @JvmField val bindUinReadReport: PbBindUinMsgReadedConfirmReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbGetOneDayRoamMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val peerUin: Long = 0L,
        @ProtoNumber(4) @JvmField val lastMsgtime: Long = 0L,
        @ProtoNumber(5) @JvmField val random: Long = 0L,
        @ProtoNumber(6) @JvmField val msg: List<MsgComm.Msg> = emptyList(),
        @ProtoNumber(7) @JvmField val iscomplete: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinGetMsgReq(
        @ProtoNumber(1) @JvmField val bindUin: Long = 0L,
        @ProtoNumber(2) @JvmField val bindUinSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val syncFlag: Int /* enum */ = 0,
        @ProtoNumber(4) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class NearByDatingTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val reply: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class BsnsTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RoutingHead(
        @ProtoNumber(1) @JvmField val c2c: C2C? = null,
        @ProtoNumber(2) @JvmField val grp: Grp? = null,
        @ProtoNumber(3) @JvmField val grpTmp: GrpTmp? = null,
        @ProtoNumber(4) @JvmField val dis: Dis? = null,
        @ProtoNumber(5) @JvmField val disTmp: DisTmp? = null,
        @ProtoNumber(6) @JvmField val wpaTmp: WPATmp? = null,
        @ProtoNumber(7) @JvmField val secretFile: SecretFileHead? = null,
        @ProtoNumber(8) @JvmField val publicPlat: PublicPlat? = null,
        @ProtoNumber(9) @JvmField val transMsg: TransMsg? = null,
        @ProtoNumber(10) @JvmField val addressList: AddressListTmp? = null,
        @ProtoNumber(11) @JvmField val richStatusTmp: RichStatusTmp? = null,
        @ProtoNumber(12) @JvmField val transCmd: TransCmd? = null,
        @ProtoNumber(13) @JvmField val accostTmp: AccostTmp? = null,
        @ProtoNumber(14) @JvmField val pubGroupTmp: PubGroupTmp? = null,
        @ProtoNumber(15) @JvmField val trans0x211: Trans0x211? = null,
        @ProtoNumber(16) @JvmField val businessWpaTmp: BusinessWPATmp? = null,
        @ProtoNumber(17) @JvmField val authTmp: AuthTmp? = null,
        @ProtoNumber(18) @JvmField val bsnsTmp: BsnsTmp? = null,
        @ProtoNumber(19) @JvmField val qqQuerybusinessTmp: QQQueryBusinessTmp? = null,
        @ProtoNumber(20) @JvmField val nearbyDatingTmp: NearByDatingTmp? = null,
        @ProtoNumber(21) @JvmField val nearbyAssistantTmp: NearByAssistantTmp? = null,
        @ProtoNumber(22) @JvmField val commTmp: CommTmp? = null
    ) : ProtoBuf

    @Serializable
    internal class TransResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val respTag: Int = 0,
        @ProtoNumber(4) @JvmField val respBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbSendMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val sendTime: Int = 0,
        @ProtoNumber(4) @JvmField val svrbusyWaitTime: Int = 0,
        @ProtoNumber(5) @JvmField val msgSendInfo: MsgSendInfo? = null,
        @ProtoNumber(6) @JvmField val errtype: Int = 0,
        @ProtoNumber(7) @JvmField val transSvrInfo: TransSvrInfo? = null,
        @ProtoNumber(8) @JvmField val receiptResp: ImReceipt.ReceiptResp? = null,
        @ProtoNumber(9) @JvmField val textAnalysisResult: Int = 0
    ) : ProtoBuf, Packet

    @Serializable
    internal class PbBindUinUnReadMsgNumResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val bindUin: Long = 0L,
        @ProtoNumber(4) @JvmField val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbGetDiscussMsgReq(
        @ProtoNumber(1) @JvmField val discussUin: Long = 0L,
        @ProtoNumber(2) @JvmField val endSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val beginSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val lastGetTime: Long = 0L,
        @ProtoNumber(5) @JvmField val discussInfoSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val filter: Int /* enum */ = 0,
        @ProtoNumber(7) @JvmField val memberSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CMsgWithDrawResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val msgStatus: List<MsgStatus> = emptyList(),
        @ProtoNumber(4) @JvmField val subCmd: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class MsgStatus(
            @ProtoNumber(1) @JvmField val msgInfo: PbC2CMsgWithDrawReq.MsgInfo? = null,
            @ProtoNumber(2) @JvmField val status: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class SecretFileHead(
        @ProtoNumber(1) @JvmField val secretFileMsg: SubMsgType0xc1.MsgBody? = null
        //    @ProtoNumber(2) @JvmField val secretFileStatus: SubMsgType0x1a.MsgBody? = null
    )

    @Serializable
    internal class PbGetRoamMsgReq(
        @ProtoNumber(1) @JvmField val peerUin: Long = 0L,
        @ProtoNumber(2) @JvmField val lastMsgtime: Long = 0L,
        @ProtoNumber(3) @JvmField val random: Long = 0L,
        @ProtoNumber(4) @JvmField val readCnt: Int = 0,
        @ProtoNumber(5) @JvmField val checkPwd: Int = 0,
        @ProtoNumber(6) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val pwd: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val subcmd: Int = 0,
        @ProtoNumber(9) @JvmField val beginMsgtime: Long = 0L,
        @ProtoNumber(10) @JvmField val reqType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TransCmd(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbMsgReadedReportResp(
        @ProtoNumber(1) @JvmField val grpReadReport: List<PbGroupReadedReportResp> = emptyList(),
        @ProtoNumber(2) @JvmField val disReadReport: List<PbDiscussReadedReportResp> = emptyList(),
        @ProtoNumber(3) @JvmField val c2cReadReport: PbC2CReadedReportResp? = null,
        @ProtoNumber(4) @JvmField val bindUinReadReport: PbBindUinMsgReadedConfirmResp? = null
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val thirdqqRespInfo: List<ThirdQQRespInfo> = emptyList(),
        @ProtoNumber(4) @JvmField val interval: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQRespInfo(
            @ProtoNumber(1) @JvmField val thirdUin: Long = 0L,
            @ProtoNumber(2) @JvmField val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(3) @JvmField val msgNum: Int = 0,
            @ProtoNumber(4) @JvmField val msgFlag: Int = 0,
            @ProtoNumber(5) @JvmField val redbagTime: Int = 0,
            @ProtoNumber(6) @JvmField val status: Int = 0,
            @ProtoNumber(7) @JvmField val lastMsgTime: Int = 0
        ) : ProtoBuf
    }

    @Serializable
    internal class RichStatusTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class QQQueryBusinessTmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDelRoamMsgReq(
        @ProtoNumber(1) @JvmField val c2cMsg: C2CMsg? = null,
        @ProtoNumber(2) @JvmField val grpMsg: GrpMsg? = null,
        @ProtoNumber(3) @JvmField val disMsg: DisMsg? = null
    ) : ProtoBuf {
        @Serializable
        internal class GrpMsg(
            @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
            @ProtoNumber(2) @JvmField val msgSeq: Long = 0L
        ) : ProtoBuf

        @Serializable
        internal class C2CMsg(
            @ProtoNumber(1) @JvmField val fromUin: Long = 0L,
            @ProtoNumber(2) @JvmField val peerUin: Long = 0L,
            @ProtoNumber(3) @JvmField val msgTime: Int = 0,
            @ProtoNumber(4) @JvmField val msgRandom: Int = 0,
            @ProtoNumber(5) @JvmField val msgSeq: Int = 0
        ) : ProtoBuf

        @Serializable
        internal class DisMsg(
            @ProtoNumber(1) @JvmField val discussUin: Long = 0L,
            @ProtoNumber(2) @JvmField val msgSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbUnReadMsgSeqReq(
        @ProtoNumber(1) @JvmField val c2cUnreadInfo: PbC2CUnReadMsgNumReq? = null,
        @ProtoNumber(2) @JvmField val binduinUnreadInfo: List<PbBindUinUnReadMsgNumReq> = emptyList(),
        @ProtoNumber(3) @JvmField val groupUnreadInfo: PbPullGroupMsgSeqReq? = null,
        @ProtoNumber(4) @JvmField val discussUnreadInfo: PbPullDiscussMsgSeqReq? = null,
        @ProtoNumber(5) @JvmField val thirdqqUnreadInfo: PbThirdQQUnReadMsgNumReq? = null
    ) : ProtoBuf

    @Serializable
    internal class PbPullDiscussMsgSeqResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val discussInfoResp: List<DiscussInfoResp> = emptyList()
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoResp(
            @ProtoNumber(1) @JvmField val confUin: Long = 0L,
            @ProtoNumber(2) @JvmField val memberSeq: Long = 0L,
            @ProtoNumber(3) @JvmField val confSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class PbPullDiscussMsgSeqReq(
        @ProtoNumber(1) @JvmField val discussInfoReq: List<DiscussInfoReq> = emptyList()
    ) : ProtoBuf {
        @Serializable
        internal class DiscussInfoReq(
            @ProtoNumber(1) @JvmField val confUin: Long = 0L,
            @ProtoNumber(2) @JvmField val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class WPATmp(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PublicPlat(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinMsgReadedConfirmResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val bindUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetRoamMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val peerUin: Long = 0L,
        @ProtoNumber(4) @JvmField val lastMsgtime: Long = 0L,
        @ProtoNumber(5) @JvmField val random: Long = 0L,
        @ProtoNumber(6) @JvmField val msg: List<MsgComm.Msg> = emptyList(),
        @ProtoNumber(7) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbDiscussReadedReportReq(
        @ProtoNumber(1) @JvmField val confUin: Long = 0L,
        @ProtoNumber(2) @JvmField val lastReadSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbC2CReadedReportReq(
        @ProtoNumber(1) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val pairInfo: List<UinPairReadInfo> = emptyList()
    ) : ProtoBuf {
        @Serializable
        internal class UinPairReadInfo(
            @ProtoNumber(1) @JvmField val peerUin: Long = 0L,
            @ProtoNumber(2) @JvmField val lastReadTime: Int = 0,
            @ProtoNumber(3) @JvmField val crmSig: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }

    @Serializable
    internal class Trans0x211(
        @ProtoNumber(1) @JvmField val toUin: Long = 0L,
        @ProtoNumber(2) @JvmField val ccCmd: Int = 0,
        @ProtoNumber(3) @JvmField val instCtrl: ImMsgHead.InstCtrl? = null,
        @ProtoNumber(4) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val c2cType: Int = 0,
        @ProtoNumber(6) @JvmField val serviceType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbSearchRoamMsgInCloudReq(
        @ProtoNumber(1) @JvmField val serializeReqbody: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbBindUinUnReadMsgNumReq(
        @ProtoNumber(1) @JvmField val bindUin: Long = 0L,
        @ProtoNumber(2) @JvmField val syncCookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbC2CUnReadMsgNumResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val msgNum: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PbPullGroupMsgSeqReq(
        @ProtoNumber(1) @JvmField val groupInfoReq: List<GroupInfoReq> = emptyList()
    ) : ProtoBuf {
        @Serializable
        internal class GroupInfoReq(
            @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
            @ProtoNumber(2) @JvmField val lastSeq: Long = 0L
        ) : ProtoBuf
    }

    @Serializable
    internal class TransReq(
        @ProtoNumber(1) @JvmField val command: Int = 0,
        @ProtoNumber(2) @JvmField val reqTag: Int = 0,
        @ProtoNumber(3) @JvmField val reqBuff: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class PbGroupReadedReportResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(4) @JvmField val memberSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val groupMsgSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbGetGroupMsgResp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val errmsg: String = "",
        @ProtoNumber(3) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(4) @JvmField val returnBeginSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val returnEndSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val msg: List<MsgComm.Msg> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class PbThirdQQUnReadMsgNumReq(
        @ProtoNumber(1) @JvmField val thirdqqReqInfo: List<ThirdQQReqInfo> = emptyList(),
        @ProtoNumber(2) @JvmField val source: Int = 0
    ) : ProtoBuf {
        @Serializable
        internal class ThirdQQReqInfo(
            @ProtoNumber(1) @JvmField val thirdUin: Long = 0L,
            @ProtoNumber(2) @JvmField val thirdUinSig: ByteArray = EMPTY_BYTE_ARRAY,
            @ProtoNumber(3) @JvmField val thirdUinCookie: ByteArray = EMPTY_BYTE_ARRAY
        ) : ProtoBuf
    }
}

@Serializable
internal class MsgCtrl {
    @Serializable
    internal class MsgCtrl(
        @ProtoNumber(1) @JvmField val msgFlag: Int = 0,
        @ProtoNumber(2) @JvmField val resvResvInfo: ResvResvInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ResvResvInfo(
        @ProtoNumber(1) @JvmField val flag: Int = 0,
        @ProtoNumber(2) @JvmField val reserv1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val reserv2: Long = 0L,
        @ProtoNumber(4) @JvmField val reserv3: Long = 0L,
        @ProtoNumber(5) @JvmField val createTime: Int = 0,
        @ProtoNumber(6) @JvmField val picHeight: Int = 0,
        @ProtoNumber(7) @JvmField val picWidth: Int = 0,
        @ProtoNumber(8) @JvmField val resvFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class SubMsgType0xc1 {
    @Serializable
    internal class NotOnlineImage(
        @ProtoNumber(1) @JvmField val filePath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fileLen: Int = 0,
        @ProtoNumber(3) @JvmField val downloadPath: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val oldVerSendFile: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val imgType: Int = 0,
        @ProtoNumber(6) @JvmField val previewsImage: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val picMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val picHeight: Int = 0,
        @ProtoNumber(9) @JvmField val picWidth: Int = 0,
        @ProtoNumber(10) @JvmField val resId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val flag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val downloadUrl: String = "",
        @ProtoNumber(13) @JvmField val original: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgBody(
        @ProtoNumber(1) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(3) @JvmField val toUin: Long = 0L,
        @ProtoNumber(4) @JvmField val status: Int = 0,
        @ProtoNumber(5) @JvmField val ttl: Int = 0,
        @ProtoNumber(6) @JvmField val type: Int = 0,
        @ProtoNumber(7) @JvmField val encryptPreheadLength: Int = 0,
        @ProtoNumber(8) @JvmField val encryptType: Int = 0,
        @ProtoNumber(9) @JvmField val encryptKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val readTimes: Int = 0,
        @ProtoNumber(11) @JvmField val leftTime: Int = 0,
        @ProtoNumber(12) @JvmField val notOnlineImage: NotOnlineImage? = null
    ) : ProtoBuf
}

/*
@Serializable
internal class SubMsgType0x1a {
    @Serializable
    internal class MsgBody(
        @ProtoNumber(1) @JvmField val fileKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val fromUin_int32: Int = 0,
        @ProtoNumber(3) @JvmField val toUin_int32: Int = 0,
        @ProtoNumber(4) @JvmField val status: Int = 0,
        @ProtoNumber(5) @JvmField val ttl: Int = 0,
        @ProtoNumber(6) @JvmField val ingDesc: String = "",
        @ProtoNumber(7) @JvmField val type: Int = 0,
        @ProtoNumber(8) @JvmField val captureTimes: Int = 0,
        @ProtoNumber(9) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(10) @JvmField val toUin: Long = 0L
    ) : ProtoBuf
}*/