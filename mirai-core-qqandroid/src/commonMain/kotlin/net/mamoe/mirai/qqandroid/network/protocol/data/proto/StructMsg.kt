package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class QPayReminderMsg : ProtoBuf {
    @Serializable
    internal class GetInfoReq(
        @ProtoId(1) @JvmField val scene: String = "",
        @ProtoId(2) @JvmField val subCmd: String = "",
        @ProtoId(3) @JvmField val infoDate: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GetInfoRsp(
        @ProtoId(1) @JvmField val resultCode: Int = 0,
        @ProtoId(2) @JvmField val resultInfo: String = "",
        @ProtoId(3) @JvmField val urgency: Int = 0,
        @ProtoId(4) @JvmField val templateNo: Int = 0,
        @ProtoId(5) @JvmField val content: String = "",
        @ProtoId(6) @JvmField val infoDate: String = ""
    ) : ProtoBuf
}

internal class Structmsg : ProtoBuf {
    @Serializable
    internal class AddFrdSNInfo(
        @ProtoId(1) @JvmField val notSeeDynamic: Int = 0,
        @ProtoId(2) @JvmField val setSn: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FlagInfo(
        @ProtoId(1) @JvmField val grpMsgKickAdmin: Int = 0,
        @ProtoId(2) @JvmField val grpMsgHiddenGrp: Int = 0,
        @ProtoId(3) @JvmField val grpMsgWordingDown: Int = 0,
        @ProtoId(4) @JvmField val frdMsgGetBusiCard: Int = 0,
        @ProtoId(5) @JvmField val grpMsgGetOfficialAccount: Int = 0,
        @ProtoId(6) @JvmField val grpMsgGetPayInGroup: Int = 0,
        @ProtoId(7) @JvmField val frdMsgDiscuss2ManyChat: Int = 0,
        @ProtoId(8) @JvmField val grpMsgNotAllowJoinGrpInviteNotFrd: Int = 0,
        @ProtoId(9) @JvmField val frdMsgNeedWaitingMsg: Int = 0,
        @ProtoId(10) @JvmField val frdMsgUint32NeedAllUnreadMsg: Int = 0,
        @ProtoId(11) @JvmField val grpMsgNeedAutoAdminWording: Int = 0,
        @ProtoId(12) @JvmField val grpMsgGetTransferGroupMsgFlag: Int = 0,
        @ProtoId(13) @JvmField val grpMsgGetQuitPayGroupMsgFlag: Int = 0,
        @ProtoId(14) @JvmField val grpMsgSupportInviteAutoJoin: Int = 0,
        @ProtoId(15) @JvmField val grpMsgMaskInviteAutoJoin: Int = 0,
        @ProtoId(16) @JvmField val grpMsgGetDisbandedByAdmin: Int = 0,
        @ProtoId(17) @JvmField val grpMsgGetC2cInviteJoinGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class FriendInfo(
        @ProtoId(1) @JvmField val msgJointFriend: String = "",
        @ProtoId(2) @JvmField val msgBlacklist: String = ""
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) @JvmField val groupAuthType: Int = 0,
        @ProtoId(2) @JvmField val displayAction: Int = 0,
        @ProtoId(3) @JvmField val msgAlert: String = "",
        @ProtoId(4) @JvmField val msgDetailAlert: String = "",
        @ProtoId(5) @JvmField val msgOtherAdminDone: String = "",
        @ProtoId(6) @JvmField val appPrivilegeFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgInviteExt(
        @ProtoId(1) @JvmField val srcType: Int = 0,
        @ProtoId(2) @JvmField val srcCode: Long = 0L,
        @ProtoId(3) @JvmField val waitState: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MsgPayGroupExt(
        @ProtoId(1) @JvmField val joinGrpTime: Long = 0L,
        @ProtoId(2) @JvmField val quitGrpTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqNextSystemMsg(
        @ProtoId(1) @JvmField val msgNum: Int = 0,
        @ProtoId(2) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoId(3) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoId(4) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoId(5) @JvmField val flag: FlagInfo? = null,
        @ProtoId(6) @JvmField val language: Int = 0,
        @ProtoId(7) @JvmField val version: Int = 0,
        @ProtoId(8) @JvmField val friendMsgTypeFlag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsg(
        @ProtoId(1) @JvmField val msgNum: Int = 0,
        @ProtoId(2) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoId(3) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoId(4) @JvmField val version: Int = 0,
        @ProtoId(5) @JvmField val language: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgAction(
        @ProtoId(1) @JvmField val msgType: Int /* enum */ = 1,
        @ProtoId(2) @JvmField val msgSeq: Long = 0L,
        @ProtoId(3) @JvmField val reqUin: Long = 0L,
        @ProtoId(4) @JvmField val subType: Int = 0,
        @ProtoId(5) @JvmField val srcId: Int = 0,
        @ProtoId(6) @JvmField val subSrcId: Int = 0,
        @ProtoId(7) @JvmField val groupMsgType: Int = 0,
        @ProtoId(8) @JvmField val actionInfo: SystemMsgActionInfo? = null,
        @ProtoId(9) @JvmField val language: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgNew(
        @ProtoId(1) @JvmField val msgNum: Int = 0,
        @ProtoId(2) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoId(3) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoId(4) @JvmField val version: Int = 0,
        @ProtoId(5) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoId(6) @JvmField val flag: FlagInfo? = null,
        @ProtoId(7) @JvmField val language: Int = 0,
        @ProtoId(8) @JvmField val isGetFrdRibbon: Boolean = true,
        @ProtoId(9) @JvmField val isGetGrpRibbon: Boolean = true,
        @ProtoId(10) @JvmField val friendMsgTypeFlag: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqSystemMsgRead(
        @ProtoId(1) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoId(2) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoId(3) @JvmField val type: Int = 0,
        @ProtoId(4) @JvmField val checktype: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class RspHead(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val msgFail: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspNextSystemMsg(
        @ProtoId(1) @JvmField val head: RspHead? = null,
        @ProtoId(2) @JvmField val msgs: List<StructMsg>? = null,
        @ProtoId(3) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoId(4) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoId(5) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoId(100) @JvmField val gameNick: String = "",
        @ProtoId(101) @JvmField val undecidForQim: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(102) @JvmField val unReadCount3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsg(
        @ProtoId(1) @JvmField val head: RspHead? = null,
        @ProtoId(2) @JvmField val msgs: List<StructMsg>? = null,
        @ProtoId(3) @JvmField val unreadCount: Int = 0,
        @ProtoId(4) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoId(5) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoId(6) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoId(7) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoId(8) @JvmField val msgDisplay: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgAction(
        @ProtoId(1) @JvmField val head: RspHead? = null,
        @ProtoId(2) @JvmField val msgDetail: String = "",
        @ProtoId(3) @JvmField val type: Int = 0,
        @ProtoId(5) @JvmField val msgInvalidDecided: String = "",
        @ProtoId(6) @JvmField val remarkResult: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgNew(
        @ProtoId(1) @JvmField val head: RspHead? = null,
        @ProtoId(2) @JvmField val unreadFriendCount: Int = 0,
        @ProtoId(3) @JvmField val unreadGroupCount: Int = 0,
        @ProtoId(4) @JvmField val latestFriendSeq: Long = 0L,
        @ProtoId(5) @JvmField val latestGroupSeq: Long = 0L,
        @ProtoId(6) @JvmField val followingFriendSeq: Long = 0L,
        @ProtoId(7) @JvmField val followingGroupSeq: Long = 0L,
        @ProtoId(9) @JvmField val friendmsgs: List<StructMsg>? = null,
        @ProtoId(10) @JvmField val groupmsgs: List<StructMsg>? = null,
        @ProtoId(11) @JvmField val msgRibbonFriend: StructMsg? = null,
        @ProtoId(12) @JvmField val msgRibbonGroup: StructMsg? = null,
        @ProtoId(13) @JvmField val msgDisplay: String = "",
        @ProtoId(14) @JvmField val grpMsgDisplay: String = "",
        @ProtoId(15) @JvmField val over: Int = 0,
        @ProtoId(20) @JvmField val checktype: Int /* enum */ = 1,
        @ProtoId(100) @JvmField val gameNick: String = "",
        @ProtoId(101) @JvmField val undecidForQim: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(102) @JvmField val unReadCount3: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspSystemMsgRead(
        @ProtoId(1) @JvmField val head: RspHead? = null,
        @ProtoId(2) @JvmField val type: Int = 0,
        @ProtoId(3) @JvmField val checktype: Int /* enum */ = 1
    ) : ProtoBuf

    @Serializable
    internal class StructMsg(
        @ProtoId(1) @JvmField val version: Int = 0,
        @ProtoId(2) @JvmField val msgType: Int /* enum */ = 1,
        @ProtoId(3) @JvmField val msgSeq: Long = 0L,
        @ProtoId(4) @JvmField val msgTime: Long = 0L,
        @ProtoId(5) @JvmField val reqUin: Long = 0L,
        @ProtoId(6) @JvmField val unreadFlag: Int = 0,
        @ProtoId(50) @JvmField val msg: SystemMsg? = null
    ) : ProtoBuf

    @Serializable
    internal class SystemMsg(
        @ProtoId(1) @JvmField val subType: Int = 0,
        @ProtoId(2) @JvmField val msgTitle: String = "",
        @ProtoId(3) @JvmField val msgDescribe: String = "",
        @ProtoId(4) @JvmField val msgAdditional: String = "",
        @ProtoId(5) @JvmField val msgSource: String = "",
        @ProtoId(6) @JvmField val msgDecided: String = "",
        @ProtoId(7) @JvmField val srcId: Int = 0,
        @ProtoId(8) @JvmField val subSrcId: Int = 0,
        @ProtoId(9) @JvmField val actions: List<SystemMsgAction>? = null,
        @ProtoId(10) @JvmField val groupCode: Long = 0L,
        @ProtoId(11) @JvmField val actionUin: Long = 0L,
        @ProtoId(12) @JvmField val groupMsgType: Int = 0,
        @ProtoId(13) @JvmField val groupInviterRole: Int = 0,
        @ProtoId(14) @JvmField val friendInfo: FriendInfo? = null,
        @ProtoId(15) @JvmField val groupInfo: GroupInfo? = null,
        @ProtoId(16) @JvmField val actorUin: Long = 0L,
        @ProtoId(17) @JvmField val msgActorDescribe: String = "",
        @ProtoId(18) @JvmField val msgAdditionalList: String = "",
        @ProtoId(19) @JvmField val relation: Int = 0,
        @ProtoId(20) @JvmField val reqsubtype: Int = 0,
        @ProtoId(21) @JvmField val cloneUin: Long = 0L,
        @ProtoId(22) @JvmField val discussUin: Long = 0L,
        @ProtoId(23) @JvmField val eimGroupId: Long = 0L,
        @ProtoId(24) @JvmField val msgInviteExtinfo: MsgInviteExt? = null,
        @ProtoId(25) @JvmField val msgPayGroupExtinfo: MsgPayGroupExt? = null,
        @ProtoId(26) @JvmField val sourceFlag: Int = 0,
        @ProtoId(27) @JvmField val gameNick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(28) @JvmField val gameMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(29) @JvmField val groupFlagext3: Int = 0,
        @ProtoId(30) @JvmField val groupOwnerUin: Long = 0L,
        @ProtoId(31) @JvmField val doubtFlag: Int = 0,
        @ProtoId(32) @JvmField val warningTips: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(33) @JvmField val nameMore: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(50) @JvmField val reqUinFaceid: Int = 0,
        @ProtoId(51) @JvmField val reqUinNick: String = "",
        @ProtoId(52) @JvmField val groupName: String = "",
        @ProtoId(53) @JvmField val actionUinNick: String = "",
        @ProtoId(54) @JvmField val msgQna: String = "",
        @ProtoId(55) @JvmField val msgDetail: String = "",
        @ProtoId(57) @JvmField val groupExtFlag: Int = 0,
        @ProtoId(58) @JvmField val actorUinNick: String = "",
        @ProtoId(59) @JvmField val picUrl: String = "",
        @ProtoId(60) @JvmField val cloneUinNick: String = "",
        @ProtoId(61) @JvmField val reqUinBusinessCard: String = "",
        @ProtoId(63) @JvmField val eimGroupIdName: String = "",
        @ProtoId(64) @JvmField val reqUinPreRemark: String = "",
        @ProtoId(65) @JvmField val actionUinQqNick: String = "",
        @ProtoId(66) @JvmField val actionUinRemark: String = "",
        @ProtoId(67) @JvmField val reqUinGender: Int = 0,
        @ProtoId(68) @JvmField val reqUinAge: Int = 0,
        @ProtoId(69) @JvmField val c2cInviteJoinGroupFlag: Int = 0,
        @ProtoId(101) @JvmField val cardSwitch: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class SystemMsgAction(
        @ProtoId(1) @JvmField val name: String = "",
        @ProtoId(2) @JvmField val result: String = "",
        @ProtoId(3) @JvmField val action: Int = 0,
        @ProtoId(4) @JvmField val actionInfo: SystemMsgActionInfo? = null,
        @ProtoId(5) @JvmField val detailName: String = ""
    ) : ProtoBuf

    @Serializable
    internal class SystemMsgActionInfo(
        @ProtoId(1) @JvmField val type: Int /* enum */ = 1,
        @ProtoId(2) @JvmField val groupCode: Long = 0L,
        @ProtoId(3) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(50) @JvmField val msg: String = "",
        @ProtoId(51) @JvmField val groupId: Int = 0,
        @ProtoId(52) @JvmField val remark: String = "",
        @ProtoId(53) @JvmField val blacklist: Boolean = false,
        @ProtoId(54) @JvmField val addFrdSNInfo: AddFrdSNInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class Youtu : ProtoBuf {
    @Serializable
    internal class NameCardOcrRsp(
        @ProtoId(1) @JvmField val errorcode: Int = 0,
        @ProtoId(2) @JvmField val errormsg: String = "",
        @ProtoId(3) @JvmField val uin: String = "",
        @ProtoId(4) @JvmField val uinConfidence: Float = 0.0F,
        @ProtoId(5) @JvmField val phone: String = "",
        @ProtoId(6) @JvmField val phoneConfidence: Float = 0.0F,
        @ProtoId(7) @JvmField val name: String = "",
        @ProtoId(8) @JvmField val nameConfidence: Float = 0.0F,
        @ProtoId(9) @JvmField val image: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val sessionId: String = ""
    ) : ProtoBuf
}
