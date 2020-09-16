package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class QPayReminderMsg : ProtoBuf {
    @Serializable
    internal class GetInfoReq(
        @ProtoNumber(1) @JvmField val scene: String = "",
        @ProtoNumber(2) @JvmField val subCmd: String = "",
        @ProtoNumber(3) @JvmField val infoDate: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GetInfoRsp(
        @ProtoNumber(1) @JvmField val resultCode: Int = 0,
        @ProtoNumber(2) @JvmField val resultInfo: String = "",
        @ProtoNumber(3) @JvmField val urgency: Int = 0,
        @ProtoNumber(4) @JvmField val templateNo: Int = 0,
        @ProtoNumber(5) @JvmField val content: String = "",
        @ProtoNumber(6) @JvmField val infoDate: String = ""
    ) : ProtoBuf
}

internal class Structmsg : ProtoBuf {
    @Serializable
    internal class AddFrdSNInfo(
        @ProtoNumber(1) @JvmField val notSeeDynamic: Int = 0,
        @ProtoNumber(2) @JvmField val setSn: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FlagInfo(
        @ProtoNumber(1) @JvmField val grpMsgKickAdmin: Int = 0,
        @ProtoNumber(2) @JvmField val grpMsgHiddenGrp: Int = 0,
        @ProtoNumber(3) @JvmField val grpMsgWordingDown: Int = 0,
        @ProtoNumber(4) @JvmField val frdMsgGetBusiCard: Int = 0,
        @ProtoNumber(5) @JvmField val grpMsgGetOfficialAccount: Int = 0,
        @ProtoNumber(6) @JvmField val grpMsgGetPayInGroup: Int = 0,
        @ProtoNumber(7) @JvmField val frdMsgDiscuss2ManyChat: Int = 0,
        @ProtoNumber(8) @JvmField val grpMsgNotAllowJoinGrpInviteNotFrd: Int = 0,
        @ProtoNumber(9) @JvmField val frdMsgNeedWaitingMsg: Int = 0,
        @ProtoNumber(10) @JvmField val frdMsgUint32NeedAllUnreadMsg: Int = 0,
        @ProtoNumber(11) @JvmField val grpMsgNeedAutoAdminWording: Int = 0,
        @ProtoNumber(12) @JvmField val grpMsgGetTransferGroupMsgFlag: Int = 0,
        @ProtoNumber(13) @JvmField val grpMsgGetQuitPayGroupMsgFlag: Int = 0,
        @ProtoNumber(14) @JvmField val grpMsgSupportInviteAutoJoin: Int = 0,
        @ProtoNumber(15) @JvmField val grpMsgMaskInviteAutoJoin: Int = 0,
        @ProtoNumber(16) @JvmField val grpMsgGetDisbandedByAdmin: Int = 0,
        @ProtoNumber(17) @JvmField val grpMsgGetC2cInviteJoinGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FriendInfo(
        @ProtoNumber(1) @JvmField val msgJointFriend: String = "",
        @ProtoNumber(2) @JvmField val msgBlacklist: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoNumber(1) @JvmField val groupAuthType: Int = 0,
        @ProtoNumber(2) @JvmField val displayAction: Int = 0,
        @ProtoNumber(3) @JvmField val msgAlert: String = "",
        @ProtoNumber(4) @JvmField val msgDetailAlert: String = "",
        @ProtoNumber(5) @JvmField val msgOtherAdminDone: String = "",
        @ProtoNumber(6) @JvmField val appPrivilegeFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgInviteExt(
        @ProtoNumber(1) @JvmField val srcType: Int = 0,
        @ProtoNumber(2) @JvmField val srcCode: Long = 0L,
        @ProtoNumber(3) @JvmField val waitState: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgPayGroupExt(
        @ProtoNumber(1) @JvmField val joinGrpTime: Long = 0L,
        @ProtoNumber(2) @JvmField val quitGrpTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqNextSystemMsg(
        @ProtoNumber(1) @JvmField val msgNum: Int = 0,
        @ProtoNumber(2) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoNumber(5) @JvmField val flag: FlagInfo? = null,
        @ProtoNumber(6) @JvmField val language: Int = 0,
        @ProtoNumber(7) @JvmField val version: Int = 0,
        @ProtoNumber(8) @JvmField val friendMsgTypeFlag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsg(
        @ProtoNumber(1) @JvmField val msgNum: Int = 0,
        @ProtoNumber(2) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val version: Int = 0,
        @ProtoNumber(5) @JvmField val language: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgAction(
        @ProtoNumber(1) @JvmField val msgType: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val msgSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val reqUin: Long = 0L,
        @ProtoNumber(4) @JvmField val subType: Int = 0,
        @ProtoNumber(5) @JvmField val srcId: Int = 0,
        @ProtoNumber(6) @JvmField val subSrcId: Int = 0,
        @ProtoNumber(7) @JvmField val groupMsgType: Int = 0,
        @ProtoNumber(8) @JvmField val actionInfo: SystemMsgActionInfo? = null,
        @ProtoNumber(9) @JvmField val language: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgNew(
        @ProtoNumber(1) @JvmField val msgNum: Int = 0,
        @ProtoNumber(2) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val version: Int = 0,
        @ProtoNumber(5) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoNumber(6) @JvmField val flag: FlagInfo? = null,
        @ProtoNumber(7) @JvmField val language: Int = 0,
        @ProtoNumber(8) @JvmField val isGetFrdRibbon: Boolean = true,
        @ProtoNumber(9) @JvmField val isGetGrpRibbon: Boolean = true,
        @ProtoNumber(10) @JvmField val friendMsgTypeFlag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgRead(
        @ProtoNumber(1) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoNumber(2) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val type: Int = 0,
        @ProtoNumber(4) @JvmField val checktype: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class RspHead(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgFail: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspNextSystemMsg(
        @ProtoNumber(1) @JvmField val head: RspHead? = null,
        @ProtoNumber(2) @JvmField val msgs: List<StructMsg>? = null,
        @ProtoNumber(3) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoNumber(100) @JvmField val gameNick: String = "",
        @ProtoNumber(101) @JvmField val undecidForQim: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(102) @JvmField val unReadCount3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsg(
        @ProtoNumber(1) @JvmField val head: RspHead? = null,
        @ProtoNumber(2) @JvmField val msgs: List<StructMsg>? = null,
        @ProtoNumber(3) @JvmField val unreadCount: Int = 0,
        @ProtoNumber(4) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoNumber(7) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoNumber(8) @JvmField val msgDisplay: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgAction(
        @ProtoNumber(1) @JvmField val head: RspHead? = null,
        @ProtoNumber(2) @JvmField val msgDetail: String = "",
        @ProtoNumber(3) @JvmField val type: Int = 0,
        @ProtoNumber(5) @JvmField val msgInvalidDecided: String = "",
        @ProtoNumber(6) @JvmField val remarkResult: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgNew(
        @ProtoNumber(1) @JvmField val head: RspHead? = null,
        @ProtoNumber(2) @JvmField val unreadFriendCount: Int = 0,
        @ProtoNumber(3) @JvmField val unreadGroupCount: Int = 0,
        @ProtoNumber(4) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoNumber(6) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoNumber(7) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoNumber(9) @JvmField val friendmsgs: List<StructMsg>? = null,
        @ProtoNumber(10) @JvmField val groupmsgs: List<StructMsg>? = null,
        @ProtoNumber(11) @JvmField val msgRibbonFriend: StructMsg? = null,
        @ProtoNumber(12) @JvmField val msgRibbonGroup: StructMsg? = null,
        @ProtoNumber(13) @JvmField val msgDisplay: String = "",
        @ProtoNumber(14) @JvmField val grpMsgDisplay: String = "",
        @ProtoNumber(15) @JvmField val over: Int = 0,
        @ProtoNumber(20) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoNumber(100) @JvmField val gameNick: String = "",
        @ProtoNumber(101) @JvmField val undecidForQim: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(102) @JvmField val unReadCount3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgRead(
        @ProtoNumber(1) @JvmField val head: RspHead? = null,
        @ProtoNumber(2) @JvmField val type: Int = 0,
        @ProtoNumber(3) @JvmField val checktype: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class StructMsg(
        @ProtoNumber(1) @JvmField val version: Int = 0,
        @ProtoNumber(2) @JvmField val msgType: Int /* enum */ = 1,
        @ProtoNumber(3) @JvmField val msgSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val msgTime: Long = 0L,
        @ProtoNumber(5) @JvmField val reqUin: Long = 0L,
        @ProtoNumber(6) @JvmField val unreadFlag: Int = 0,
        @ProtoNumber(50) @JvmField val msg: SystemMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class SystemMsg(
        @ProtoNumber(1) @JvmField val subType: Int = 0,
        @ProtoNumber(2) @JvmField val msgTitle: String = "",
        @ProtoNumber(3) @JvmField val msgDescribe: String = "",
        @ProtoNumber(4) @JvmField val msgAdditional: String = "",
        @ProtoNumber(5) @JvmField val msgSource: String = "",
        @ProtoNumber(6) @JvmField val msgDecided: String = "",
        @ProtoNumber(7) @JvmField val srcId: Int = 0,
        @ProtoNumber(8) @JvmField val subSrcId: Int = 0,
        @ProtoNumber(9) @JvmField val actions: List<SystemMsgAction>? = null,
        @ProtoNumber(10) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(11) @JvmField val actionUin: Long = 0L,
        @ProtoNumber(12) @JvmField val groupMsgType: Int = 0,
        @ProtoNumber(13) @JvmField val groupInviterRole: Int = 0,
        @ProtoNumber(14) @JvmField val friendInfo: FriendInfo? = null,
        @ProtoNumber(15) @JvmField val groupInfo: GroupInfo? = null,
        @ProtoNumber(16) @JvmField val actorUin: Long = 0L,
        @ProtoNumber(17) @JvmField val msgActorDescribe: String = "",
        @ProtoNumber(18) @JvmField val msgAdditionalList: String = "",
        @ProtoNumber(19) @JvmField val relation: Int = 0,
        @ProtoNumber(20) @JvmField val reqsubtype: Int = 0,
        @ProtoNumber(21) @JvmField val cloneUin: Long = 0L,
        @ProtoNumber(22) @JvmField val discussUin: Long = 0L,
        @ProtoNumber(23) @JvmField val eimGroupId: Long = 0L,
        @ProtoNumber(24) @JvmField val msgInviteExtinfo: MsgInviteExt? = null,
        @ProtoNumber(25) @JvmField val msgPayGroupExtinfo: MsgPayGroupExt? = null,
        @ProtoNumber(26) @JvmField val sourceFlag: Int = 0,
        @ProtoNumber(27) @JvmField val gameNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(28) @JvmField val gameMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(29) @JvmField val groupFlagext3: Int = 0,
        @ProtoNumber(30) @JvmField val groupOwnerUin: Long = 0L,
        @ProtoNumber(31) @JvmField val doubtFlag: Int = 0,
        @ProtoNumber(32) @JvmField val warningTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(33) @JvmField val nameMore: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(50) @JvmField val reqUinFaceid: Int = 0,
        @ProtoNumber(51) @JvmField val reqUinNick: String = "",
        @ProtoNumber(52) @JvmField val groupName: String = "",
        @ProtoNumber(53) @JvmField val actionUinNick: String = "",
        @ProtoNumber(54) @JvmField val msgQna: String = "",
        @ProtoNumber(55) @JvmField val msgDetail: String = "",
        @ProtoNumber(57) @JvmField val groupExtFlag: Int = 0,
        @ProtoNumber(58) @JvmField val actorUinNick: String = "",
        @ProtoNumber(59) @JvmField val picUrl: String = "",
        @ProtoNumber(60) @JvmField val cloneUinNick: String = "",
        @ProtoNumber(61) @JvmField val reqUinBusinessCard: String = "",
        @ProtoNumber(63) @JvmField val eimGroupIdName: String = "",
        @ProtoNumber(64) @JvmField val reqUinPreRemark: String = "",
        @ProtoNumber(65) @JvmField val actionUinQqNick: String = "",
        @ProtoNumber(66) @JvmField val actionUinRemark: String = "",
        @ProtoNumber(67) @JvmField val reqUinGender: Int = 0,
        @ProtoNumber(68) @JvmField val reqUinAge: Int = 0,
        @ProtoNumber(69) @JvmField val c2cInviteJoinGroupFlag: Int = 0,
        @ProtoNumber(101) @JvmField val cardSwitch: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SystemMsgAction(
        @ProtoNumber(1) @JvmField val name: String = "",
        @ProtoNumber(2) @JvmField val result: String = "",
        @ProtoNumber(3) @JvmField val action: Int = 0,
        @ProtoNumber(4) @JvmField val actionInfo: SystemMsgActionInfo? = null,
        @ProtoNumber(5) @JvmField val detailName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class SystemMsgActionInfo(
        @ProtoNumber(1) @JvmField val type: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(3) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(50) @JvmField val msg: String = "",
        @ProtoNumber(51) @JvmField val groupId: Int = 0,
        @ProtoNumber(52) @JvmField val remark: String = "",
        @ProtoNumber(53) @JvmField val blacklist: Boolean = false,
        @ProtoNumber(54) @JvmField val addFrdSNInfo: AddFrdSNInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class Youtu : ProtoBuf {
    @Serializable
    internal class NameCardOcrRsp(
        @ProtoNumber(1) @JvmField val errorcode: Int = 0,
        @ProtoNumber(2) @JvmField val errormsg: String = "",
        @ProtoNumber(3) @JvmField val uin: String = "",
        @ProtoNumber(4) @JvmField val uinConfidence: Float = 0.0F,
        @ProtoNumber(5) @JvmField val phone: String = "",
        @ProtoNumber(6) @JvmField val phoneConfidence: Float = 0.0F,
        @ProtoNumber(7) @JvmField val name: String = "",
        @ProtoNumber(8) @JvmField val nameConfidence: Float = 0.0F,
        @ProtoNumber(9) @JvmField val image: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val sessionId: String = ""
    ) : ProtoBuf
}
