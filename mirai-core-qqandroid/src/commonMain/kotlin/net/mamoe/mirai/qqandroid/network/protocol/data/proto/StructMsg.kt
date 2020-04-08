package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
internal class QPayReminderMsg : ProtoBuf {
    @Serializable
    internal class GetInfoReq(
        @ProtoId(1) val scene: String = "",
        @ProtoId(2) val subCmd: String = "",
        @ProtoId(3) val infoDate: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GetInfoRsp(
        @ProtoId(1) val resultCode: Int = 0,
        @ProtoId(2) val resultInfo: String = "",
        @ProtoId(3) val urgency: Int = 0,
        @ProtoId(4) val templateNo: Int = 0,
        @ProtoId(5) val content: String = "",
        @ProtoId(6) val infoDate: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Structmsg : ProtoBuf {
    @Serializable
    internal class AddFrdSNInfo(
        @ProtoId(1) val notSeeDynamic: Int = 0,
        @ProtoId(2) val setSn: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FlagInfo(
        @ProtoId(1) val grpMsgKickAdmin: Int = 0,
        @ProtoId(2) val grpMsgHiddenGrp: Int = 0,
        @ProtoId(3) val grpMsgWordingDown: Int = 0,
        @ProtoId(4) val frdMsgGetBusiCard: Int = 0,
        @ProtoId(5) val grpMsgGetOfficialAccount: Int = 0,
        @ProtoId(6) val grpMsgGetPayInGroup: Int = 0,
        @ProtoId(7) val frdMsgDiscuss2ManyChat: Int = 0,
        @ProtoId(8) val grpMsgNotAllowJoinGrpInviteNotFrd: Int = 0,
        @ProtoId(9) val frdMsgNeedWaitingMsg: Int = 0,
        @ProtoId(10) val frdMsgUint32NeedAllUnreadMsg: Int = 0,
        @ProtoId(11) val grpMsgNeedAutoAdminWording: Int = 0,
        @ProtoId(12) val grpMsgGetTransferGroupMsgFlag: Int = 0,
        @ProtoId(13) val grpMsgGetQuitPayGroupMsgFlag: Int = 0,
        @ProtoId(14) val grpMsgSupportInviteAutoJoin: Int = 0,
        @ProtoId(15) val grpMsgMaskInviteAutoJoin: Int = 0,
        @ProtoId(16) val grpMsgGetDisbandedByAdmin: Int = 0,
        @ProtoId(17) val grpMsgGetC2cInviteJoinGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FriendInfo(
        @ProtoId(1) val msgJointFriend: String = "",
        @ProtoId(2) val msgBlacklist: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) val groupAuthType: Int = 0,
        @ProtoId(2) val displayAction: Int = 0,
        @ProtoId(3) val msgAlert: String = "",
        @ProtoId(4) val msgDetailAlert: String = "",
        @ProtoId(5) val msgOtherAdminDone: String = "",
        @ProtoId(6) val appPrivilegeFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgInviteExt(
        @ProtoId(1) val srcType: Int = 0,
        @ProtoId(2) val srcCode: Long = 0L,
        @ProtoId(3) val waitState: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgPayGroupExt(
        @ProtoId(1) val joinGrpTime: Long = 0L,
        @ProtoId(2) val quitGrpTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqNextSystemMsg(
        @ProtoId(1) val msgNum: Int = 0,
        @ProtoId(2) val followingFriendSeq: Long = 0L,
        @ProtoId(3) val followingGroupSeq: Long = 0L,
        @ProtoId(4) val checktype: Int /* enum */ = 1,
        @ProtoId(5) val flag: FlagInfo? = null,
        @ProtoId(6) val language: Int = 0,
        @ProtoId(7) val version: Int = 0,
        @ProtoId(8) val friendMsgTypeFlag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsg(
        @ProtoId(1) val msgNum: Int = 0,
        @ProtoId(2) val latestFriendSeq: Long = 0L,
        @ProtoId(3) val latestGroupSeq: Long = 0L,
        @ProtoId(4) val version: Int = 0,
        @ProtoId(5) val language: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgAction(
        @ProtoId(1) val msgType: Int /* enum */ = 1,
        @ProtoId(2) val msgSeq: Long = 0L,
        @ProtoId(3) val reqUin: Long = 0L,
        @ProtoId(4) val subType: Int = 0,
        @ProtoId(5) val srcId: Int = 0,
        @ProtoId(6) val subSrcId: Int = 0,
        @ProtoId(7) val groupMsgType: Int = 0,
        @ProtoId(8) val actionInfo: SystemMsgActionInfo? = null,
        @ProtoId(9) val language: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgNew(
        @ProtoId(1) val msgNum: Int = 0,
        @ProtoId(2) val latestFriendSeq: Long = 0L,
        @ProtoId(3) val latestGroupSeq: Long = 0L,
        @ProtoId(4) val version: Int = 0,
        @ProtoId(5) val checktype: Int /* enum */ = 1,
        @ProtoId(6) val flag: FlagInfo? = null,
        @ProtoId(7) val language: Int = 0,
        @ProtoId(8) val isGetFrdRibbon: Boolean = true,
        @ProtoId(9) val isGetGrpRibbon: Boolean = true,
        @ProtoId(10) val friendMsgTypeFlag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgRead(
        @ProtoId(1) val latestFriendSeq: Long = 0L,
        @ProtoId(2) val latestGroupSeq: Long = 0L,
        @ProtoId(3) val type: Int = 0,
        @ProtoId(4) val checktype: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class RspHead(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val msgFail: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspNextSystemMsg(
        @ProtoId(1) val head: RspHead? = null,
        @ProtoId(2) val msgs: List<StructMsg>? = null,
        @ProtoId(3) val followingFriendSeq: Long = 0L,
        @ProtoId(4) val followingGroupSeq: Long = 0L,
        @ProtoId(5) val checktype: Int /* enum */ = 1,
        @ProtoId(100) val gameNick: String = "",
        @ProtoId(101) val undecidForQim: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(102) val unReadCount3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsg(
        @ProtoId(1) val head: RspHead? = null,
        @ProtoId(2) val msgs: List<StructMsg>? = null,
        @ProtoId(3) val unreadCount: Int = 0,
        @ProtoId(4) val latestFriendSeq: Long = 0L,
        @ProtoId(5) val latestGroupSeq: Long = 0L,
        @ProtoId(6) val followingFriendSeq: Long = 0L,
        @ProtoId(7) val followingGroupSeq: Long = 0L,
        @ProtoId(8) val msgDisplay: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgAction(
        @ProtoId(1) val head: RspHead? = null,
        @ProtoId(2) val msgDetail: String = "",
        @ProtoId(3) val type: Int = 0,
        @ProtoId(5) val msgInvalidDecided: String = "",
        @ProtoId(6) val remarkResult: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgNew(
        @ProtoId(1) val head: RspHead? = null,
        @ProtoId(2) val unreadFriendCount: Int = 0,
        @ProtoId(3) val unreadGroupCount: Int = 0,
        @ProtoId(4) val latestFriendSeq: Long = 0L,
        @ProtoId(5) val latestGroupSeq: Long = 0L,
        @ProtoId(6) val followingFriendSeq: Long = 0L,
        @ProtoId(7) val followingGroupSeq: Long = 0L,
        @ProtoId(9) val friendmsgs: List<StructMsg>? = null,
        @ProtoId(10) val groupmsgs: List<StructMsg>? = null,
        @ProtoId(11) val msgRibbonFriend: StructMsg? = null,
        @ProtoId(12) val msgRibbonGroup: StructMsg? = null,
        @ProtoId(13) val msgDisplay: String = "",
        @ProtoId(14) val grpMsgDisplay: String = "",
        @ProtoId(15) val over: Int = 0,
        @ProtoId(20) val checktype: Int /* enum */ = 1,
        @ProtoId(100) val gameNick: String = "",
        @ProtoId(101) val undecidForQim: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(102) val unReadCount3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgRead(
        @ProtoId(1) val head: RspHead? = null,
        @ProtoId(2) val type: Int = 0,
        @ProtoId(3) val checktype: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class StructMsg(
        @ProtoId(1) val version: Int = 0,
        @ProtoId(2) val msgType: Int /* enum */ = 1,
        @ProtoId(3) val msgSeq: Long = 0L,
        @ProtoId(4) val msgTime: Long = 0L,
        @ProtoId(5) val reqUin: Long = 0L,
        @ProtoId(6) val unreadFlag: Int = 0,
        @ProtoId(50) val msg: SystemMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class SystemMsg(
        @ProtoId(1) val subType: Int = 0,
        @ProtoId(2) val msgTitle: String = "",
        @ProtoId(3) val msgDescribe: String = "",
        @ProtoId(4) val msgAdditional: String = "",
        @ProtoId(5) val msgSource: String = "",
        @ProtoId(6) val msgDecided: String = "",
        @ProtoId(7) val srcId: Int = 0,
        @ProtoId(8) val subSrcId: Int = 0,
        @ProtoId(9) val actions: List<SystemMsgAction>? = null,
        @ProtoId(10) val groupCode: Long = 0L,
        @ProtoId(11) val actionUin: Long = 0L,
        @ProtoId(12) val groupMsgType: Int = 0,
        @ProtoId(13) val groupInviterRole: Int = 0,
        @ProtoId(14) val friendInfo: FriendInfo? = null,
        @ProtoId(15) val groupInfo: GroupInfo? = null,
        @ProtoId(16) val actorUin: Long = 0L,
        @ProtoId(17) val msgActorDescribe: String = "",
        @ProtoId(18) val msgAdditionalList: String = "",
        @ProtoId(19) val relation: Int = 0,
        @ProtoId(20) val reqsubtype: Int = 0,
        @ProtoId(21) val cloneUin: Long = 0L,
        @ProtoId(22) val discussUin: Long = 0L,
        @ProtoId(23) val eimGroupId: Long = 0L,
        @ProtoId(24) val msgInviteExtinfo: MsgInviteExt? = null,
        @ProtoId(25) val msgPayGroupExtinfo: MsgPayGroupExt? = null,
        @ProtoId(26) val sourceFlag: Int = 0,
        @ProtoId(27) val gameNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(28) val gameMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(29) val groupFlagext3: Int = 0,
        @ProtoId(30) val groupOwnerUin: Long = 0L,
        @ProtoId(31) val doubtFlag: Int = 0,
        @ProtoId(32) val warningTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(33) val nameMore: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(50) val reqUinFaceid: Int = 0,
        @ProtoId(51) val reqUinNick: String = "",
        @ProtoId(52) val groupName: String = "",
        @ProtoId(53) val actionUinNick: String = "",
        @ProtoId(54) val msgQna: String = "",
        @ProtoId(55) val msgDetail: String = "",
        @ProtoId(57) val groupExtFlag: Int = 0,
        @ProtoId(58) val actorUinNick: String = "",
        @ProtoId(59) val picUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(60) val cloneUinNick: String = "",
        @ProtoId(61) val reqUinBusinessCard: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(63) val eimGroupIdName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(64) val reqUinPreRemark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(65) val actionUinQqNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(66) val actionUinRemark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(67) val reqUinGender: Int = 0,
        @ProtoId(68) val reqUinAge: Int = 0,
        @ProtoId(69) val c2cInviteJoinGroupFlag: Int = 0,
        @ProtoId(101) val cardSwitch: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SystemMsgAction(
        @ProtoId(1) val name: String = "",
        @ProtoId(2) val result: String = "",
        @ProtoId(3) val action: Int = 0,
        @ProtoId(4) val actionInfo: SystemMsgActionInfo? = null,
        @ProtoId(5) val detailName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class SystemMsgActionInfo(
        @ProtoId(1) val type: Int /* enum */ = 1,
        @ProtoId(2) val groupCode: Long = 0L,
        @ProtoId(3) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(50) val msg: String = "",
        @ProtoId(51) val groupId: Int = 0,
        @ProtoId(52) val remark: String = "",
        @ProtoId(53) val blacklist: Boolean = false,
        @ProtoId(54) val addFrdSNInfo: AddFrdSNInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class Youtu : ProtoBuf {
    @Serializable
    internal class NameCardOcrRsp(
        @ProtoId(1) val errorcode: Int = 0,
        @ProtoId(2) val errormsg: String = "",
        @ProtoId(3) val uin: String = "",
        @ProtoId(4) val uinConfidence: Float = 0.0F,
        @ProtoId(5) val phone: String = "",
        @ProtoId(6) val phoneConfidence: Float = 0.0F,
        @ProtoId(7) val name: String = "",
        @ProtoId(8) val nameConfidence: Float = 0.0F,
        @ProtoId(9) val image: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val sessionId: String = ""
    ) : ProtoBuf
}
