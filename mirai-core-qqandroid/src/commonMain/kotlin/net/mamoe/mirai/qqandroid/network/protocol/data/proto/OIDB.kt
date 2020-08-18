package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class Oidb0x8a0 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val msgKickResult: List<KickResult>? = null
    ) : ProtoBuf

    @Serializable
    internal class KickResult(
        @ProtoNumber(1) @JvmField val optUint32Result: Int = 0,
        @ProtoNumber(2) @JvmField val optUint64MemberUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class KickMemberInfo(
        @ProtoNumber(1) @JvmField val optUint32Operate: Int = 0,
        @ProtoNumber(2) @JvmField val optUint64MemberUin: Long = 0L,
        @ProtoNumber(3) @JvmField val optUint32Flag: Int = 0,
        @ProtoNumber(4) @JvmField val optBytesMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val msgKickList: List<KickMemberInfo>? = null,
        @ProtoNumber(3) @JvmField val kickList: List<Long>? = null,
        @ProtoNumber(4) @JvmField val kickFlag: Int = 0,
        @ProtoNumber(5) @JvmField val kickMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


@Serializable
internal class Oidb0x8fc : ProtoBuf {
    @Serializable
    internal class CardNameElem(
        @ProtoNumber(1) @JvmField val enumCardType: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommCardNameBuf(
        @ProtoNumber(1) @JvmField val richCardName: List<RichCardNameElem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val showFlag: Int = 0,
        @ProtoNumber(3) @JvmField val memLevelInfo: List<MemberInfo>? = null,
        @ProtoNumber(4) @JvmField val levelName: List<LevelName>? = null,
        @ProtoNumber(5) @JvmField val updateTime: Int = 0,
        @ProtoNumber(6) @JvmField val officeMode: Int = 0,
        @ProtoNumber(7) @JvmField val groupOpenAppid: Int = 0,
        @ProtoNumber(8) @JvmField val msgClientInfo: ClientInfo? = null,
        @ProtoNumber(9) @JvmField val authKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MemberInfo(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val point: Int = 0,
        @ProtoNumber(3) @JvmField val activeDay: Int = 0,
        @ProtoNumber(4) @JvmField val level: Int = 0,
        @ProtoNumber(5) @JvmField val specialTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val specialTitleExpireTime: Int = 0,
        @ProtoNumber(7) @JvmField val uinName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val memberCardName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val phone: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val email: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val remark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val gender: Int = 0,
        @ProtoNumber(13) @JvmField val job: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val tribeLevel: Int = 0,
        @ProtoNumber(15) @JvmField val tribePoint: Int = 0,
        @ProtoNumber(16) @JvmField val richCardName: List<CardNameElem>? = null,
        @ProtoNumber(17) @JvmField val commRichCardName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RichCardNameElem(
        @ProtoNumber(1) @JvmField val ctrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val errInfo: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoNumber(1) @JvmField val implat: Int = 0,
        @ProtoNumber(2) @JvmField val ingClientver: String = ""
    ) : ProtoBuf

    @Serializable
    internal class LevelName(
        @ProtoNumber(1) @JvmField val level: Int = 0,
        @ProtoNumber(2) @JvmField val name: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x88d : ProtoBuf {
    @Serializable
    internal class GroupExInfoOnly(
        @ProtoNumber(1) @JvmField val tribeId: Int = 0,
        @ProtoNumber(2) @JvmField val moneyForAddGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqGroupInfo(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val stgroupinfo: GroupInfo? = null,
        @ProtoNumber(3) @JvmField val lastGetGroupNameTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspGroupInfo(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val result: Int = 0,
        @ProtoNumber(3) @JvmField val stgroupinfo: GroupInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class GroupGeoInfo(
        @ProtoNumber(1) @JvmField val owneruin: Long = 0L,
        @ProtoNumber(2) @JvmField val settime: Int = 0,
        @ProtoNumber(3) @JvmField val cityid: Int = 0,
        @ProtoNumber(4) @JvmField val int64Longitude: Long = 0L,
        @ProtoNumber(5) @JvmField val int64Latitude: Long = 0L,
        @ProtoNumber(6) @JvmField val geocontent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val poiId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TagRecord(
        @ProtoNumber(1) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(2) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(3) @JvmField val tagId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val setTime: Long = 0L,
        @ProtoNumber(5) @JvmField val goodNum: Int = 0,
        @ProtoNumber(6) @JvmField val badNum: Int = 0,
        @ProtoNumber(7) @JvmField val tagLen: Int = 0,
        @ProtoNumber(8) @JvmField val tagValue: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoNumber(1) @JvmField val groupOwner: Long? = null,
        @ProtoNumber(2) @JvmField val groupCreateTime: Int? = null,
        @ProtoNumber(3) @JvmField val groupFlag: Int? = null,
        @ProtoNumber(4) @JvmField val groupFlagExt: Int? = null,
        @ProtoNumber(5) @JvmField val groupMemberMaxNum: Int? = null,
        @ProtoNumber(6) @JvmField val groupMemberNum: Int? = null,
        @ProtoNumber(7) @JvmField val groupOption: Int? = null,
        @ProtoNumber(8) @JvmField val groupClassExt: Int? = null,
        @ProtoNumber(9) @JvmField val groupSpecialClass: Int? = null,
        @ProtoNumber(10) @JvmField val groupLevel: Int? = null,
        @ProtoNumber(11) @JvmField val groupFace: Int? = null,
        @ProtoNumber(12) @JvmField val groupDefaultPage: Int? = null,
        @ProtoNumber(13) @JvmField val groupInfoSeq: Int? = null,
        @ProtoNumber(14) @JvmField val groupRoamingTime: Int? = null,
        @ProtoNumber(15) var groupName: String? = null,
        @ProtoNumber(16) var groupMemo: String? = null,
        @ProtoNumber(17) @JvmField val ingGroupFingerMemo: String? = null,
        @ProtoNumber(18) @JvmField val ingGroupClassText: String? = null,
        @ProtoNumber(19) @JvmField val groupAllianceCode: List<Int>? = null,
        @ProtoNumber(20) @JvmField val groupExtraAdmNum: Int? = null,
        @ProtoNumber(21) var groupUin: Long? = null,
        @ProtoNumber(22) @JvmField val groupCurMsgSeq: Int? = null,
        @ProtoNumber(23) @JvmField val groupLastMsgTime: Int? = null,
        @ProtoNumber(24) @JvmField val ingGroupQuestion: String? = null,
        @ProtoNumber(25) @JvmField val ingGroupAnswer: String? = null,
        @ProtoNumber(26) @JvmField val groupVisitorMaxNum: Int? = null,
        @ProtoNumber(27) @JvmField val groupVisitorCurNum: Int? = null,
        @ProtoNumber(28) @JvmField val levelNameSeq: Int? = null,
        @ProtoNumber(29) @JvmField val groupAdminMaxNum: Int? = null,
        @ProtoNumber(30) @JvmField val groupAioSkinTimestamp: Int? = null,
        @ProtoNumber(31) @JvmField val groupBoardSkinTimestamp: Int? = null,
        @ProtoNumber(32) @JvmField val ingGroupAioSkinUrl: String? = null,
        @ProtoNumber(33) @JvmField val ingGroupBoardSkinUrl: String? = null,
        @ProtoNumber(34) @JvmField val groupCoverSkinTimestamp: Int? = null,
        @ProtoNumber(35) @JvmField val ingGroupCoverSkinUrl: String? = null,
        @ProtoNumber(36) @JvmField val groupGrade: Int? = null,
        @ProtoNumber(37) @JvmField val activeMemberNum: Int? = null,
        @ProtoNumber(38) @JvmField val certificationType: Int? = null,
        @ProtoNumber(39) @JvmField val ingCertificationText: String? = null,
        @ProtoNumber(40) @JvmField val ingGroupRichFingerMemo: String? = null,
        @ProtoNumber(41) @JvmField val tagRecord: List<TagRecord>? = null,
        @ProtoNumber(42) @JvmField val groupGeoInfo: GroupGeoInfo? = null,
        @ProtoNumber(43) @JvmField val headPortraitSeq: Int? = null,
        @ProtoNumber(44) @JvmField val msgHeadPortrait: GroupHeadPortrait? = null,
        @ProtoNumber(45) @JvmField val shutupTimestamp: Int? = null,
        @ProtoNumber(46) @JvmField val shutupTimestampMe: Int? = null,
        @ProtoNumber(47) @JvmField val createSourceFlag: Int? = null,
        @ProtoNumber(48) @JvmField val cmduinMsgSeq: Int? = null,
        @ProtoNumber(49) @JvmField val cmduinJoinTime: Int? = null,
        @ProtoNumber(50) @JvmField val cmduinUinFlag: Int? = null,
        @ProtoNumber(51) @JvmField val cmduinFlagEx: Int? = null,
        @ProtoNumber(52) @JvmField val cmduinNewMobileFlag: Int? = null,
        @ProtoNumber(53) @JvmField val cmduinReadMsgSeq: Int? = null,
        @ProtoNumber(54) @JvmField val cmduinLastMsgTime: Int? = null,
        @ProtoNumber(55) @JvmField val groupTypeFlag: Int? = null,
        @ProtoNumber(56) @JvmField val appPrivilegeFlag: Int? = null,
        @ProtoNumber(57) @JvmField val stGroupExInfo: GroupExInfoOnly? = null,
        @ProtoNumber(58) @JvmField val groupSecLevel: Int? = null,
        @ProtoNumber(59) @JvmField val groupSecLevelInfo: Int? = null,
        @ProtoNumber(60) @JvmField val cmduinPrivilege: Int? = null,
        @ProtoNumber(61) @JvmField val ingPoidInfo: ByteArray? = null,
        @ProtoNumber(62) @JvmField val cmduinFlagEx2: Int? = null,
        @ProtoNumber(63) @JvmField val confUin: Long? = null,
        @ProtoNumber(64) @JvmField val confMaxMsgSeq: Int? = null,
        @ProtoNumber(65) @JvmField val confToGroupTime: Int? = null,
        @ProtoNumber(66) @JvmField val passwordRedbagTime: Int? = null,
        @ProtoNumber(67) @JvmField val subscriptionUin: Long? = null,
        @ProtoNumber(68) @JvmField val memberListChangeSeq: Int? = null,
        @ProtoNumber(69) @JvmField val membercardSeq: Int? = null,
        @ProtoNumber(70) @JvmField val rootId: Long? = null,
        @ProtoNumber(71) @JvmField val parentId: Long? = null,
        @ProtoNumber(72) @JvmField val teamSeq: Int? = null,
        @ProtoNumber(73) @JvmField val historyMsgBeginTime: Long? = null,
        @ProtoNumber(74) @JvmField val inviteNoAuthNumLimit: Long? = null,
        @ProtoNumber(75) @JvmField val cmduinHistoryMsgSeq: Int? = null,
        @ProtoNumber(76) @JvmField val cmduinJoinMsgSeq: Int? = null,
        @ProtoNumber(77) @JvmField val groupFlagext3: Int? = null,
        @ProtoNumber(78) @JvmField val groupOpenAppid: Int? = null,
        @ProtoNumber(79) @JvmField val isConfGroup: Int? = null,
        @ProtoNumber(80) @JvmField val isModifyConfGroupFace: Int? = null,
        @ProtoNumber(81) @JvmField val isModifyConfGroupName: Int? = null,
        @ProtoNumber(82) @JvmField val noFingerOpenFlag: Int? = null,
        @ProtoNumber(83) @JvmField val noCodeFingerOpenFlag: Int? = null,
        @ProtoNumber(84) @JvmField val autoAgreeJoinGroupUserNumForNormalGroup: Int? = null,
        @ProtoNumber(85) @JvmField val autoAgreeJoinGroupUserNumForConfGroup: Int? = null,
        @ProtoNumber(86) @JvmField val isAllowConfGroupMemberNick: Int? = null,
        @ProtoNumber(87) @JvmField val isAllowConfGroupMemberAtAll: Int? = null,
        @ProtoNumber(88) @JvmField val isAllowConfGroupMemberModifyGroupName: Int? = null,
        @ProtoNumber(89) @JvmField val longGroupName: String? = null,
        @ProtoNumber(90) @JvmField val cmduinJoinRealMsgSeq: Int? = null,
        @ProtoNumber(91) @JvmField val isGroupFreeze: Int? = null,
        @ProtoNumber(92) @JvmField val msgLimitFrequency: Int? = null,
        @ProtoNumber(93) @JvmField val joinGroupAuth: ByteArray? = null,
        @ProtoNumber(94) @JvmField val hlGuildAppid: Int? = null,
        @ProtoNumber(95) @JvmField val hlGuildSubType: Int? = null,
        @ProtoNumber(96) @JvmField val hlGuildOrgid: Int? = null,
        @ProtoNumber(97) @JvmField val isAllowHlGuildBinary: Int? = null,
        @ProtoNumber(98) @JvmField val cmduinRingtoneId: Int? = null,
        @ProtoNumber(99) @JvmField val groupFlagext4: Int? = null,
        @ProtoNumber(100) @JvmField val groupFreezeReason: Int? = null,
        @ProtoNumber(101) var groupCode: Long? = null // mirai 添加
    ) : ProtoBuf

    @Serializable
    internal class GroupHeadPortraitInfo(
        @ProtoNumber(1) @JvmField val uint32PicId: Int = 0,
        @ProtoNumber(2) @JvmField val leftX: Int = 0,
        @ProtoNumber(3) @JvmField val leftY: Int = 0,
        @ProtoNumber(4) @JvmField val rightX: Int = 0,
        @ProtoNumber(5) @JvmField val rightY: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val stzrspgroupinfo: List<RspGroupInfo>? = null,
        @ProtoNumber(2) @JvmField val errorinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val stzreqgroupinfo: List<ReqGroupInfo>? = null,
        @ProtoNumber(3) @JvmField val pcClientVersion: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupHeadPortrait(
        @ProtoNumber(1) @JvmField val picCnt: Int = 0,
        @ProtoNumber(2) @JvmField val msgInfo: List<GroupHeadPortraitInfo>? = null,
        @ProtoNumber(3) @JvmField val defaultId: Int = 0,
        @ProtoNumber(4) @JvmField val verifyingPicCnt: Int = 0,
        @ProtoNumber(5) @JvmField val msgVerifyingpicInfo: List<GroupHeadPortraitInfo>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x89a : ProtoBuf {
    @Serializable
    internal class GroupNewGuidelinesInfo(
        @ProtoNumber(1) @JvmField val boolEnabled: Boolean = false,
        @ProtoNumber(2) @JvmField val ingContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Groupinfo(
        @ProtoNumber(1) @JvmField val groupExtAdmNum: Int? = null,
        @ProtoNumber(2) @JvmField val flag: Int? = null,
        @ProtoNumber(3) @JvmField val ingGroupName: ByteArray? = null,
        @ProtoNumber(4) @JvmField val ingGroupMemo: ByteArray? = null,
        @ProtoNumber(5) @JvmField val ingGroupFingerMemo: ByteArray? = null,
        @ProtoNumber(6) @JvmField val ingGroupAioSkinUrl: ByteArray? = null,
        @ProtoNumber(7) @JvmField val ingGroupBoardSkinUrl: ByteArray? = null,
        @ProtoNumber(8) @JvmField val ingGroupCoverSkinUrl: ByteArray? = null,
        @ProtoNumber(9) @JvmField val groupGrade: Int? = null,
        @ProtoNumber(10) @JvmField val activeMemberNum: Int? = null,
        @ProtoNumber(11) @JvmField val certificationType: Int? = null,
        @ProtoNumber(12) @JvmField val ingCertificationText: ByteArray? = null,
        @ProtoNumber(13) @JvmField val ingGroupRichFingerMemo: ByteArray? = null,
        @ProtoNumber(14) @JvmField val stGroupNewguidelines: GroupNewGuidelinesInfo? = null,
        @ProtoNumber(15) @JvmField val groupFace: Int? = null,
        @ProtoNumber(16) @JvmField val addOption: Int? = null,
        @ProtoNumber(17) @JvmField val shutupTime: Int? = null,
        @ProtoNumber(18) @JvmField val groupTypeFlag: Int? = null,
        @ProtoNumber(19) @JvmField val stringGroupTag: List<ByteArray>? = null,
        @ProtoNumber(20) @JvmField val msgGroupGeoInfo: GroupGeoInfo? = null,
        @ProtoNumber(21) @JvmField val groupClassExt: Int? = null,
        @ProtoNumber(22) @JvmField val ingGroupClassText: ByteArray? = null,
        @ProtoNumber(23) @JvmField val appPrivilegeFlag: Int? = null,
        @ProtoNumber(24) @JvmField val appPrivilegeMask: Int? = null,
        @ProtoNumber(25) @JvmField val stGroupExInfo: GroupExInfoOnly? = null,
        @ProtoNumber(26) @JvmField val groupSecLevel: Int? = null,
        @ProtoNumber(27) @JvmField val groupSecLevelInfo: Int? = null,
        @ProtoNumber(28) @JvmField val subscriptionUin: Long? = null,
        @ProtoNumber(29) @JvmField val allowMemberInvite: Int? = null,
        @ProtoNumber(30) @JvmField val ingGroupQuestion: ByteArray? = null,
        @ProtoNumber(31) @JvmField val ingGroupAnswer: ByteArray? = null,
        @ProtoNumber(32) @JvmField val groupFlagext3: Int? = null,
        @ProtoNumber(33) @JvmField val groupFlagext3Mask: Int? = null,
        @ProtoNumber(34) @JvmField val groupOpenAppid: Int? = null,
        @ProtoNumber(35) @JvmField val noFingerOpenFlag: Int? = null,
        @ProtoNumber(36) @JvmField val noCodeFingerOpenFlag: Int? = null,
        @ProtoNumber(37) @JvmField val rootId: Long? = null,
        @ProtoNumber(38) @JvmField val msgLimitFrequency: Int? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val errorinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupExInfoOnly(
        @ProtoNumber(1) @JvmField val tribeId: Int = 0,
        @ProtoNumber(2) @JvmField val moneyForAddGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupGeoInfo(
        @ProtoNumber(1) @JvmField val cityId: Int = 0,
        @ProtoNumber(2) @JvmField val longtitude: Long = 0L,
        @ProtoNumber(3) @JvmField val latitude: Long = 0L,
        @ProtoNumber(4) @JvmField val ingGeoContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val poiId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val stGroupInfo: Groupinfo? = null,
        @ProtoNumber(3) @JvmField val originalOperatorUin: Long = 0L,
        @ProtoNumber(4) @JvmField val reqGroupOpenAppid: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cb : ProtoBuf {
    @Serializable
    internal class ConfigItem(
        @ProtoNumber(1) @JvmField val id: Int = 0,
        @ProtoNumber(2) @JvmField val config: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val timeStamp: Int = 0,
        @ProtoNumber(2) @JvmField val timeGap: Int = 0,
        @ProtoNumber(3) @JvmField val commentConfigs: List<CommentConfig>? = null,
        @ProtoNumber(4) @JvmField val attendTipsToA: String = "",
        @ProtoNumber(5) @JvmField val firstMsgTips: String = "",
        @ProtoNumber(6) @JvmField val cancleConfig: List<ConfigItem>? = null,
        @ProtoNumber(7) @JvmField val msgDateRequest: DateRequest? = null,
        @ProtoNumber(8) @JvmField val msgHotLocale: List<ByteArray>? = null,//List<AppointDefine.LocaleInfo>
        @ProtoNumber(9) @JvmField val msgTopicList: List<TopicConfig>? = null,
        @ProtoNumber(10) @JvmField val travelMsgTips: String = "",
        @ProtoNumber(11) @JvmField val travelProfileTips: String = "",
        @ProtoNumber(12) @JvmField val travelAttenTips: String = "",
        @ProtoNumber(13) @JvmField val topicDefault: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommentConfig(
        @ProtoNumber(1) @JvmField val appointSubject: Int = 0,
        @ProtoNumber(2) @JvmField val msgConfigs: List<ConfigItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val timeStamp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DateRequest(
        @ProtoNumber(1) @JvmField val time: Int = 0,
        @ProtoNumber(2) @JvmField val errMsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TopicConfig(
        @ProtoNumber(1) @JvmField val topicId: Int = 0,
        @ProtoNumber(2) @JvmField val topicName: String = "",
        @ProtoNumber(3) @JvmField val deadline: Int = 0,
        @ProtoNumber(4) @JvmField val errDeadline: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x87a : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val country: String = "",
        @ProtoNumber(2) @JvmField val telephone: String = "",
        @ProtoNumber(3) @JvmField val resendInterval: Int = 0,
        @ProtoNumber(4) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val country: String = "",
        @ProtoNumber(2) @JvmField val telephone: String = "",
        @ProtoNumber(3) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val enumButype: Int /* enum */ = 0
    ) : ProtoBuf
}

@Serializable
internal class GroupAppPb : ProtoBuf {
    @Serializable
    internal class ClientInfo(
        @ProtoNumber(1) @JvmField val platform: Int = 0,
        @ProtoNumber(2) @JvmField val version: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val fullList: AppList? = null,
        @ProtoNumber(2) @JvmField val groupGrayList: AppList? = null,
        @ProtoNumber(3) @JvmField val redPointList: AppList? = null,
        @ProtoNumber(4) @JvmField val cacheInterval: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AppList(
        @ProtoNumber(1) @JvmField val hash: String = "",
        @ProtoNumber(2) @JvmField val infos: List<AppInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class AppInfo(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val icon: String = "",
        @ProtoNumber(3) @JvmField val name: String = "",
        @ProtoNumber(4) @JvmField val url: String = "",
        @ProtoNumber(5) @JvmField val isGray: Int = 0,
        @ProtoNumber(6) @JvmField val iconSimpleDay: String = "",
        @ProtoNumber(7) @JvmField val iconSimpleNight: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val client: ClientInfo? = null,
        @ProtoNumber(2) @JvmField val groupId: Long = 0L,
        @ProtoNumber(3) @JvmField val groupType: Int = 0,
        @ProtoNumber(4) @JvmField val fullListHash: String = "",
        @ProtoNumber(5) @JvmField val groupGrayListHash: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc34 : ProtoBuf {
    @Serializable
    internal class RspBody : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fd : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgComment: AppointDefine.DateComment? = null,
        @ProtoNumber(2) @JvmField val maxFetchCount: Int = 0,
        @ProtoNumber(3) @JvmField val lastCommentId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgComment: List<AppointDefine.DateComment>? = null,
        @ProtoNumber(2) @JvmField val errorTips: String = "",
        @ProtoNumber(3) @JvmField val clearCacheFlag: Int = 0,
        @ProtoNumber(4) @JvmField val commentWording: String = "",
        @ProtoNumber(5) @JvmField val commentNum: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbcb : ProtoBuf {
    @Serializable
    internal class CheckUrlReqItem(
        @ProtoNumber(1) @JvmField val url: String = "",
        @ProtoNumber(2) @JvmField val refer: String = "",
        @ProtoNumber(3) @JvmField val plateform: String = "",
        @ProtoNumber(4) @JvmField val qqPfTo: String = "",
        @ProtoNumber(5) @JvmField val msgType: Int = 0,
        @ProtoNumber(6) @JvmField val msgFrom: Int = 0,
        @ProtoNumber(7) @JvmField val msgChatid: Long = 0L,
        @ProtoNumber(8) @JvmField val serviceType: Long = 0L,
        @ProtoNumber(9) @JvmField val sendUin: Long = 0L,
        @ProtoNumber(10) @JvmField val reqType: String = ""
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlRsp(
        @ProtoNumber(1) @JvmField val results: List<UrlCheckResult>? = null,
        @ProtoNumber(2) @JvmField val nextReqDuration: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(9) @JvmField val notUseCache: Int = 0,
        @ProtoNumber(10) @JvmField val checkUrlReq: CheckUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val wording: String = "",
        @ProtoNumber(10) @JvmField val checkUrlRsp: CheckUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlReq(
        @ProtoNumber(1) @JvmField val url: List<String> = listOf(),
        @ProtoNumber(2) @JvmField val refer: String = "",
        @ProtoNumber(3) @JvmField val plateform: String = "",
        @ProtoNumber(4) @JvmField val qqPfTo: String = "",
        @ProtoNumber(5) @JvmField val msgType: Int = 0,
        @ProtoNumber(6) @JvmField val msgFrom: Int = 0,
        @ProtoNumber(7) @JvmField val msgChatid: Long = 0L,
        @ProtoNumber(8) @JvmField val serviceType: Long = 0L,
        @ProtoNumber(9) @JvmField val sendUin: Long = 0L,
        @ProtoNumber(10) @JvmField val reqType: String = "",
        @ProtoNumber(11) @JvmField val originalUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UrlCheckResult(
        @ProtoNumber(1) @JvmField val url: String = "",
        @ProtoNumber(2) @JvmField val result: Int = 0,
        @ProtoNumber(3) @JvmField val jumpResult: Int = 0,
        @ProtoNumber(4) @JvmField val jumpUrl: String = "",
        @ProtoNumber(5) @JvmField val level: Int = 0,
        @ProtoNumber(6) @JvmField val subLevel: Int = 0,
        @ProtoNumber(7) @JvmField val umrtype: Int = 0,
        @ProtoNumber(8) @JvmField val retFrom: Int = 0,
        @ProtoNumber(9) @JvmField val operationBit: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbfe : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val receiveStatus: Int = 0,
        @ProtoNumber(2) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val flag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbe8 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val enumOpCode: Int /* enum */ = 1,
        @ProtoNumber(3) @JvmField val rspOfPopupFlag: Int = 0,
        @ProtoNumber(4) @JvmField val popupCountNow: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PopupResult(
        @ProtoNumber(1) @JvmField val popupResult: Int = 0,
        @ProtoNumber(2) @JvmField val popupFieldid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val enumOpCode: Int /* enum */ = 1,
        @ProtoNumber(3) @JvmField val reqOfPopupFlag: Int = 0,
        @ProtoNumber(4) @JvmField val rstOfPopupFlag: Int = 0,
        @ProtoNumber(5) @JvmField val mqq808WelcomepageFlag: Int = 0,
        @ProtoNumber(6) @JvmField val msgPopupResult: List<PopupResult>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7de : ProtoBuf {
    @Serializable
    internal class UserProfile(
        @ProtoNumber(1) @JvmField val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoNumber(2) @JvmField val msgAppointsInfo: AppointDefine.AppointInfo? = null,
        @ProtoNumber(3) @JvmField val msgVistorInfo: List<AppointDefine.StrangerInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgHead: BusiRespHead? = null,
        @ProtoNumber(2) @JvmField val msgUserList: List<UserProfile>? = null,
        @ProtoNumber(3) @JvmField val ended: Int = 0,
        @ProtoNumber(4) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BusiRespHead(
        @ProtoNumber(1) @JvmField val int32Version: Int = 1,
        @ProtoNumber(2) @JvmField val int32Seq: Int = 0,
        @ProtoNumber(3) @JvmField val int32ReplyCode: Int = 0,
        @ProtoNumber(4) @JvmField val result: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgHead: BusiReqHead? = null,
        @ProtoNumber(2) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoNumber(3) @JvmField val time: Int = 0,
        @ProtoNumber(4) @JvmField val subject: Int = 0,
        @ProtoNumber(5) @JvmField val gender: Int = 0,
        @ProtoNumber(6) @JvmField val ageLow: Int = 0,
        @ProtoNumber(7) @JvmField val ageUp: Int = 0,
        @ProtoNumber(8) @JvmField val profession: Int = 0,
        @ProtoNumber(9) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val msgDestination: AppointDefine.LocaleInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class BusiReqHead(
        @ProtoNumber(1) @JvmField val int32Version: Int = 1,
        @ProtoNumber(2) @JvmField val int32Seq: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7a8 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val reqUin: Long = 0L,
        @ProtoNumber(11) @JvmField val onlyObtained: Int = 0,
        @ProtoNumber(12) @JvmField val readReport: Int = 0,
        @ProtoNumber(13) @JvmField val sortType: Int = 0,
        @ProtoNumber(14) @JvmField val onlyNew: Int = 0,
        @ProtoNumber(15) @JvmField val filterMedalIds: List<Int>? = null,
        @ProtoNumber(16) @JvmField val onlySummary: Int = 0,
        @ProtoNumber(17) @JvmField val doScan: Int = 0,
        @ProtoNumber(18) @JvmField val startTimestamp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val nick: String = "",
        @ProtoNumber(2) @JvmField val metalRank: Int = 0,
        @ProtoNumber(3) @JvmField val friCount: Int = 0,
        @ProtoNumber(4) @JvmField val metalCount: Int = 0,
        @ProtoNumber(5) @JvmField val metalTotal: Int = 0,
        @ProtoNumber(6) @JvmField val msgMedal: List<Common.MedalInfo>? = null,
        @ProtoNumber(8) @JvmField val totalPoint: Int = 0,
        @ProtoNumber(9) @JvmField val int32NewCount: Int = 0,
        @ProtoNumber(10) @JvmField val int32UpgradeCount: Int = 0,
        @ProtoNumber(11) @JvmField val promptParams: String = "",
        @ProtoNumber(12) @JvmField val now: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalNews(
        @ProtoNumber(1) @JvmField val friUin: Long = 0L,
        @ProtoNumber(2) @JvmField val friNick: String = "",
        @ProtoNumber(3) @JvmField val msgMedal: Common.MedalInfo? = null
    ) : ProtoBuf
}


@Serializable
internal class Cmd0x5fe : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoNumber(2) @JvmField val commentId: String = "",
        @ProtoNumber(3) @JvmField val fetchOldCount: Int = 0,
        @ProtoNumber(4) @JvmField val fetchNewCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgComment: List<AppointDefine.DateComment>? = null,
        @ProtoNumber(2) @JvmField val errorTips: String = "",
        @ProtoNumber(3) @JvmField val fetchOldOver: Int = 0,
        @ProtoNumber(4) @JvmField val fetchNewOver: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc35 : ProtoBuf {
    @Serializable
    internal class RspBody : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgExposeInfo: List<ExposeItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ExposeItem(
        @ProtoNumber(1) @JvmField val friend: Long = 0L,
        @ProtoNumber(2) @JvmField val pageId: Int = 0,
        @ProtoNumber(3) @JvmField val entranceId: Int = 0,
        @ProtoNumber(4) @JvmField val actionId: Int = 0,
        @ProtoNumber(5) @JvmField val exposeCount: Int = 0,
        @ProtoNumber(6) @JvmField val exposeTime: Int = 0,
        @ProtoNumber(7) @JvmField val algoBuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val addition: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc0d : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val completedTaskStamp: Long = 0L,
        @ProtoNumber(2) @JvmField val errMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val taskType: Int = 0,
        @ProtoNumber(3) @JvmField val taskPoint: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class OidbSso : ProtoBuf {
    @Serializable
    internal class OIDBSSOPkg(
        @ProtoNumber(1) @JvmField val command: Int = 0,
        @ProtoNumber(2) @JvmField val serviceType: Int = 0,
        @ProtoNumber(3) @JvmField val result: Int = 0,
        @ProtoNumber(4) @JvmField val bodybuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val errorMsg: String = "",
        @ProtoNumber(6) @JvmField val clientVersion: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xc83 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(101) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(102) @JvmField val toUin: Long = 0L,
        @ProtoNumber(103) @JvmField val op: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(101) @JvmField val result: Int = 0,
        @ProtoNumber(102) @JvmField val retryInterval: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xccb : ProtoBuf {
    @Serializable
    internal class GroupMsgInfo(
        @ProtoNumber(1) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(2) @JvmField val roamFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val destUin: Long = 0L,
        @ProtoNumber(3) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(4) @JvmField val c2cMsg: List<C2cMsgInfo>? = null,
        @ProtoNumber(5) @JvmField val groupMsg: List<GroupMsgInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val destUin: Long = 0L,
        @ProtoNumber(3) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(4) @JvmField val c2cMsg: List<C2cMsgInfo>? = null,
        @ProtoNumber(5) @JvmField val groupMsg: List<GroupMsgInfo>? = null,
        @ProtoNumber(6) @JvmField val resId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class C2cMsgInfo(
        @ProtoNumber(1) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(2) @JvmField val msgTime: Int = 0,
        @ProtoNumber(3) @JvmField val msgRandom: Int = 0,
        @ProtoNumber(4) @JvmField val roamFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd84 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x5e1 : ProtoBuf {
    @Serializable
    internal class UdcUinData(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(4) @JvmField val openid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20002) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20003) @JvmField val country: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20004) @JvmField val province: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20009) @JvmField val gender: Int = 0,
        @ProtoNumber(20014) @JvmField val allow: Int = 0,
        @ProtoNumber(20015) @JvmField val faceId: Int = 0,
        @ProtoNumber(20020) @JvmField val city: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20027) @JvmField val commonPlace1: Int = 0,
        @ProtoNumber(20030) @JvmField val mss3Bitmapextra: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20031) @JvmField val birthday: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20032) @JvmField val cityId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(20033) @JvmField val lang1: Int = 0,
        @ProtoNumber(20034) @JvmField val lang2: Int = 0,
        @ProtoNumber(20035) @JvmField val lang3: Int = 0,
        @ProtoNumber(20041) @JvmField val cityZoneId: Int = 0,
        @ProtoNumber(20056) @JvmField val oin: Int = 0,
        @ProtoNumber(20059) @JvmField val bubbleId: Int = 0,
        @ProtoNumber(21001) @JvmField val mss2Identity: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(21002) @JvmField val mss1Service: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(21003) @JvmField val lflag: Int = 0,
        @ProtoNumber(21004) @JvmField val extFlag: Int = 0,
        @ProtoNumber(21006) @JvmField val basicSvrFlag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(21007) @JvmField val basicCliFlag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(24101) @JvmField val pengyouRealname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(24103) @JvmField val pengyouGender: Int = 0,
        @ProtoNumber(24118) @JvmField val pengyouFlag: Int = 0,
        @ProtoNumber(26004) @JvmField val fullBirthday: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(26005) @JvmField val fullAge: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(26010) @JvmField val simpleUpdateTime: Int = 0,
        @ProtoNumber(26011) @JvmField val mssUpdateTime: Int = 0,
        @ProtoNumber(27022) @JvmField val groupMemCreditFlag: Int = 0,
        @ProtoNumber(27025) @JvmField val faceAddonId: Long = 0L,
        @ProtoNumber(27026) @JvmField val musicGene: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(40323) @JvmField val fileShareBit: Int = 0,
        @ProtoNumber(40404) @JvmField val recommendPrivacyCtrl: Int = 0,
        @ProtoNumber(40505) @JvmField val oldFriendChat: Int = 0,
        @ProtoNumber(40602) @JvmField val businessBit: Int = 0,
        @ProtoNumber(41305) @JvmField val crmBit: Int = 0,
        @ProtoNumber(41810) @JvmField val forbidFileshareBit: Int = 0,
        @ProtoNumber(42333) @JvmField val userLoginGuardFace: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(11) @JvmField val msgUinData: List<UdcUinData>? = null,
        @ProtoNumber(12) @JvmField val uint64UnfinishedUins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uint64Uins: List<Long>? = null,
        @ProtoNumber(2) @JvmField val startTime: Int = 0,
        @ProtoNumber(3) @JvmField val maxPackageSize: Int = 0,
        @ProtoNumber(4) @JvmField val bytesOpenid: List<ByteArray>? = null,
        @ProtoNumber(5) @JvmField val appid: Int = 0,
        @ProtoNumber(20002) @JvmField val reqNick: Int = 0,
        @ProtoNumber(20003) @JvmField val reqCountry: Int = 0,
        @ProtoNumber(20004) @JvmField val reqProvince: Int = 0,
        @ProtoNumber(20009) @JvmField val reqGender: Int = 0,
        @ProtoNumber(20014) @JvmField val reqAllow: Int = 0,
        @ProtoNumber(20015) @JvmField val reqFaceId: Int = 0,
        @ProtoNumber(20020) @JvmField val reqCity: Int = 0,
        @ProtoNumber(20027) @JvmField val reqCommonPlace1: Int = 0,
        @ProtoNumber(20030) @JvmField val reqMss3Bitmapextra: Int = 0,
        @ProtoNumber(20031) @JvmField val reqBirthday: Int = 0,
        @ProtoNumber(20032) @JvmField val reqCityId: Int = 0,
        @ProtoNumber(20033) @JvmField val reqLang1: Int = 0,
        @ProtoNumber(20034) @JvmField val reqLang2: Int = 0,
        @ProtoNumber(20035) @JvmField val reqLang3: Int = 0,
        @ProtoNumber(20041) @JvmField val reqCityZoneId: Int = 0,
        @ProtoNumber(20056) @JvmField val reqOin: Int = 0,
        @ProtoNumber(20059) @JvmField val reqBubbleId: Int = 0,
        @ProtoNumber(21001) @JvmField val reqMss2Identity: Int = 0,
        @ProtoNumber(21002) @JvmField val reqMss1Service: Int = 0,
        @ProtoNumber(21003) @JvmField val reqLflag: Int = 0,
        @ProtoNumber(21004) @JvmField val reqExtFlag: Int = 0,
        @ProtoNumber(21006) @JvmField val reqBasicSvrFlag: Int = 0,
        @ProtoNumber(21007) @JvmField val reqBasicCliFlag: Int = 0,
        @ProtoNumber(24101) @JvmField val reqPengyouRealname: Int = 0,
        @ProtoNumber(24103) @JvmField val reqPengyouGender: Int = 0,
        @ProtoNumber(24118) @JvmField val reqPengyouFlag: Int = 0,
        @ProtoNumber(26004) @JvmField val reqFullBirthday: Int = 0,
        @ProtoNumber(26005) @JvmField val reqFullAge: Int = 0,
        @ProtoNumber(26010) @JvmField val reqSimpleUpdateTime: Int = 0,
        @ProtoNumber(26011) @JvmField val reqMssUpdateTime: Int = 0,
        @ProtoNumber(27022) @JvmField val reqGroupMemCreditFlag: Int = 0,
        @ProtoNumber(27025) @JvmField val reqFaceAddonId: Int = 0,
        @ProtoNumber(27026) @JvmField val reqMusicGene: Int = 0,
        @ProtoNumber(40323) @JvmField val reqFileShareBit: Int = 0,
        @ProtoNumber(40404) @JvmField val reqRecommendPrivacyCtrlBit: Int = 0,
        @ProtoNumber(40505) @JvmField val reqOldFriendChatBit: Int = 0,
        @ProtoNumber(40602) @JvmField val reqBusinessBit: Int = 0,
        @ProtoNumber(41305) @JvmField val reqCrmBit: Int = 0,
        @ProtoNumber(41810) @JvmField val reqForbidFileshareBit: Int = 0,
        @ProtoNumber(42333) @JvmField val userLoginGuardFace: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc90 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val communityBid: List<Long>? = null,
        @ProtoNumber(2) @JvmField val page: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommunityWebInfo(
        @ProtoNumber(1) @JvmField val communityInfoItem: List<CommunityConfigInfo>? = null,
        @ProtoNumber(2) @JvmField val page: Int = 0,
        @ProtoNumber(3) @JvmField val end: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val communityInfoItem: List<CommunityConfigInfo>? = null,
        @ProtoNumber(2) @JvmField val jumpConcernCommunityUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val communityTitleWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val moreUrlWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val webCommunityInfo: CommunityWebInfo? = null,
        @ProtoNumber(6) @JvmField val jumpCommunityChannelUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommunityConfigInfo(
        @ProtoNumber(1) @JvmField val jumpHomePageUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val picUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val dynamicCount: Int = 0,
        @ProtoNumber(5) @JvmField val communityBid: Long = 0L,
        @ProtoNumber(6) @JvmField val followStatus: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xd8a : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val retcode: Int = 0,
        @ProtoNumber(2) @JvmField val res: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val cmd: Int = 0,
        @ProtoNumber(3) @JvmField val body: String = "",
        @ProtoNumber(4) @JvmField val clientInfo: ClientInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoNumber(1) @JvmField val implat: Int = 0,
        @ProtoNumber(2) @JvmField val ingClientver: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb6f : ProtoBuf {
    @Serializable
    internal class ReportFreqRspBody(
        @ProtoNumber(1) @JvmField val identity: Identity? = null,
        @ProtoNumber(4) @JvmField val remainTimes: Long = 0L,
        @ProtoNumber(5) @JvmField val expireTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Identity(
        @ProtoNumber(1) @JvmField val apiName: String = "",
        @ProtoNumber(2) @JvmField val appid: Int = 0,
        @ProtoNumber(3) @JvmField val apptype: Int = 0,
        @ProtoNumber(4) @JvmField val bizid: Int = 0,
        @ProtoNumber(10) @JvmField val intExt1: Long = 0L,
        @ProtoNumber(20) @JvmField val ext1: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ThresholdInfo(
        @ProtoNumber(1) @JvmField val thresholdPerMinute: Long = 0L,
        @ProtoNumber(2) @JvmField val thresholdPerDay: Long = 0L,
        @ProtoNumber(3) @JvmField val thresholdPerHour: Long = 0L,
        @ProtoNumber(4) @JvmField val thresholdPerWeek: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val reportFreqRsp: ReportFreqRspBody? = null
    ) : ProtoBuf

    @Serializable
    internal class ReportFreqReqBody(
        @ProtoNumber(1) @JvmField val identity: Identity? = null,
        @ProtoNumber(2) @JvmField val invokeTimes: Long = 1L
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val reportFreqReq: ReportFreqReqBody? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7dc : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val seq: Int = 0,
        @ProtoNumber(2) @JvmField val wording: String = "",
        @ProtoNumber(3) @JvmField val msgAppointInfo: List<AppointDefine.AppointInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val seq: Int = 0,
        @ProtoNumber(2) @JvmField val msgAppointment: AppointDefine.AppointContent? = null,
        @ProtoNumber(3) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoNumber(4) @JvmField val overwrite: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cd : ProtoBuf {
    @Serializable
    internal class AppointBrife(
        @ProtoNumber(1) @JvmField val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoNumber(2) @JvmField val msgAppointsInfo: AppointDefine.AppointInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val stamp: Int = 0,
        @ProtoNumber(2) @JvmField val over: Int = 0,
        @ProtoNumber(3) @JvmField val next: Int = 0,
        @ProtoNumber(4) @JvmField val msgAppointsInfo: List<AppointBrife>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val stamp: Int = 0,
        @ProtoNumber(2) @JvmField val start: Int = 0,
        @ProtoNumber(3) @JvmField val want: Int = 0,
        @ProtoNumber(4) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoNumber(5) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoNumber(6) @JvmField val appointOperation: Int = 0,
        @ProtoNumber(100) @JvmField val requestUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc0c : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val isTaskCompleted: Int = 0,
        @ProtoNumber(2) @JvmField val taskPoint: Int = 0,
        @ProtoNumber(3) @JvmField val guideWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val needShowProgress: Int = 0,
        @ProtoNumber(5) @JvmField val originalProgress: Int = 0,
        @ProtoNumber(6) @JvmField val nowProgress: Int = 0,
        @ProtoNumber(7) @JvmField val totalProgress: Int = 0,
        @ProtoNumber(8) @JvmField val needExecTask: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class VideoSrcType(
        @ProtoNumber(1) @JvmField val sourceType: Int = 0,
        @ProtoNumber(2) @JvmField val videoFromType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val taskType: Int = 0,
        @ProtoNumber(3) @JvmField val rowkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val feedsId: Long = 0L,
        @ProtoNumber(5) @JvmField val msgVideoFromType: VideoSrcType? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fb : ProtoBuf {
    @Serializable
    internal class ReqInfo(
        @ProtoNumber(3) @JvmField val time: Int = 0,
        @ProtoNumber(4) @JvmField val subject: Int = 0,
        @ProtoNumber(5) @JvmField val gender: Int = 0,
        @ProtoNumber(6) @JvmField val ageLow: Int = 0,
        @ProtoNumber(7) @JvmField val ageUp: Int = 0,
        @ProtoNumber(8) @JvmField val profession: Int = 0,
        @ProtoNumber(9) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val msgDestination: AppointDefine.LocaleInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgHead: BusiReqHead? = null,
        @ProtoNumber(2) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoNumber(3) @JvmField val reqInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BusiRespHead(
        @ProtoNumber(1) @JvmField val int32Version: Int = 1,
        @ProtoNumber(2) @JvmField val int32Seq: Int = 0,
        @ProtoNumber(3) @JvmField val int32ReplyCode: Int = 0,
        @ProtoNumber(4) @JvmField val result: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UserProfile(
        @ProtoNumber(1) @JvmField val int64Id: Long = 0L,
        @ProtoNumber(2) @JvmField val int32IdType: Int = 0,
        @ProtoNumber(3) @JvmField val url: String = "",
        @ProtoNumber(4) @JvmField val int32PicType: Int = 0,
        @ProtoNumber(5) @JvmField val int32SubPicType: Int = 0,
        @ProtoNumber(6) @JvmField val title: String = "",
        @ProtoNumber(7) @JvmField val content: String = "",
        @ProtoNumber(8) @JvmField val content2: String = "",
        @ProtoNumber(9) @JvmField val picUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class BusiReqHead(
        @ProtoNumber(1) @JvmField val int32Version: Int = 1,
        @ProtoNumber(2) @JvmField val int32Seq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgHead: BusiRespHead? = null,
        @ProtoNumber(2) @JvmField val msgUserList: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb61 : ProtoBuf {
    @Serializable
    internal class GetAppinfoReq(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val appType: Int = 0,
        @ProtoNumber(3) @JvmField val platform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetPkgUrlReq(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val appType: Int = 0,
        @ProtoNumber(3) @JvmField val appVersion: Int = 0,
        @ProtoNumber(4) @JvmField val platform: Int = 0,
        @ProtoNumber(5) @JvmField val sysVersion: String = "",
        @ProtoNumber(6) @JvmField val qqVersion: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val wording: String = "",
        @ProtoNumber(2) @JvmField val nextReqDuration: Int = 0,
        @ProtoNumber(10) @JvmField val getAppinfoRsp: GetAppinfoRsp? = null,
        @ProtoNumber(11) @JvmField val getMqqappUrlRsp: GetPkgUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(10) @JvmField val getAppinfoReq: GetAppinfoReq? = null,
        @ProtoNumber(11) @JvmField val getMqqappUrlReq: GetPkgUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetAppinfoRsp(
        @ProtoNumber(1) @JvmField val appinfo: Qqconnect.Appinfo? = null
    ) : ProtoBuf

    @Serializable
    internal class GetPkgUrlRsp(
        @ProtoNumber(1) @JvmField val appVersion: Int = 0,
        @ProtoNumber(2) @JvmField val pkgUrl: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb60 : ProtoBuf {
    @Serializable
    internal class GetPrivilegeReq(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val appType: Int = 3
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlReq(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val appType: Int = 0,
        @ProtoNumber(3) @JvmField val url: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoNumber(1) @JvmField val platform: Int = 0,
        @ProtoNumber(2) @JvmField val sdkVersion: String = "",
        @ProtoNumber(3) @JvmField val androidPackageName: String = "",
        @ProtoNumber(4) @JvmField val androidSignature: String = "",
        @ProtoNumber(5) @JvmField val iosBundleId: String = "",
        @ProtoNumber(6) @JvmField val pcSign: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val wording: String = "",
        @ProtoNumber(10) @JvmField val getPrivilegeRsp: GetPrivilegeRsp? = null,
        @ProtoNumber(11) @JvmField val checkUrlRsp: CheckUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlRsp(
        @ProtoNumber(1) @JvmField val isAuthed: Boolean = false,
        @ProtoNumber(2) @JvmField val nextReqDuration: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val clientInfo: ClientInfo? = null,
        @ProtoNumber(10) @JvmField val getPrivilegeReq: GetPrivilegeReq? = null,
        @ProtoNumber(11) @JvmField val checkUrlReq: CheckUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetPrivilegeRsp(
        @ProtoNumber(1) @JvmField val apiGroups: List<Int>? = null,
        @ProtoNumber(2) @JvmField val nextReqDuration: Int = 0,
        @ProtoNumber(3) @JvmField val apiNames: List<String> = listOf()
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fc : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val lastEventId: Long = 0L,
        @ProtoNumber(2) @JvmField val readEventId: Long = 0L,
        @ProtoNumber(3) @JvmField val fetchCount: Int = 0,
        @ProtoNumber(4) @JvmField val lastNearbyEventId: Long = 0L,
        @ProtoNumber(5) @JvmField val readNearbyEventId: Long = 0L,
        @ProtoNumber(6) @JvmField val fetchNearbyEventCount: Int = 0,
        @ProtoNumber(7) @JvmField val lastFeedEventId: Long = 0L,
        @ProtoNumber(8) @JvmField val readFeedEventId: Long = 0L,
        @ProtoNumber(9) @JvmField val fetchFeedEventCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgEventList: List<AppointDefine.DateEvent>? = null,
        @ProtoNumber(2) @JvmField val actAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoNumber(3) @JvmField val maxEventId: Long = 0L,
        @ProtoNumber(4) @JvmField val errorTips: String = "",
        @ProtoNumber(5) @JvmField val msgNearbyEventList: List<AppointDefine.NearbyEvent>? = null,
        @ProtoNumber(6) @JvmField val msgFeedEventList: List<AppointDefine.FeedEvent>? = null,
        @ProtoNumber(7) @JvmField val maxFreshEventId: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc33 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val nextGap: Int = 0,
        @ProtoNumber(3) @JvmField val newUser: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody : ProtoBuf
}

@Serializable
internal class Oidb0xc0b : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val isOpenCoinEntry: Int = 0,
        @ProtoNumber(2) @JvmField val canGetCoinCount: Int = 0,
        @ProtoNumber(3) @JvmField val coinIconUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val lastCompletedTaskStamp: Long = 0L,
        @ProtoNumber(6) @JvmField val cmsWording: List<KanDianCMSActivityInfo>? = null,
        @ProtoNumber(7) @JvmField val lastCmsActivityStamp: Long = 0L,
        @ProtoNumber(8) @JvmField val msgKandianCoinRemind: KanDianCoinRemind? = null,
        @ProtoNumber(9) @JvmField val msgKandianTaskRemind: KanDianTaskRemind? = null
    ) : ProtoBuf

    @Serializable
    internal class KanDianCoinRemind(
        @ProtoNumber(1) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KanDianTaskRemind(
        @ProtoNumber(1) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val taskType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class KanDianCMSActivityInfo(
        @ProtoNumber(1) @JvmField val activityId: Long = 0L,
        @ProtoNumber(2) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val pictureUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xc85 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(101) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(102) @JvmField val toUin: Long = 0L,
        @ProtoNumber(103) @JvmField val op: Int = 0,
        @ProtoNumber(104) @JvmField val intervalDays: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class InteractionDetailInfo(
        @ProtoNumber(101) @JvmField val continuousRecordDays: Int = 0,
        @ProtoNumber(102) @JvmField val sendDayTime: Int = 0,
        @ProtoNumber(103) @JvmField val recvDayTime: Int = 0,
        @ProtoNumber(104) @JvmField val sendRecord: String = "",
        @ProtoNumber(105) @JvmField val recvRecord: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(101) @JvmField val result: Int = 0,
        @ProtoNumber(102) @JvmField val recentInteractionTime: Int = 0,
        @ProtoNumber(103) @JvmField val interactionDetailInfo: InteractionDetailInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7ce : ProtoBuf {
    @Serializable
    internal class AppintDetail(
        @ProtoNumber(1) @JvmField val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoNumber(2) @JvmField val msgAppointsInfo: AppointDefine.AppointInfo? = null,
        @ProtoNumber(3) @JvmField val score: Int = 0,
        @ProtoNumber(4) @JvmField val joinOver: Int = 0,
        @ProtoNumber(5) @JvmField val joinNext: Int = 0,
        @ProtoNumber(6) @JvmField val msgStrangerInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoNumber(7) @JvmField val viewOver: Int = 0,
        @ProtoNumber(8) @JvmField val viewNext: Int = 0,
        @ProtoNumber(9) @JvmField val msgVistorInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoNumber(10) @JvmField val meJoin: Int = 0,
        @ProtoNumber(12) @JvmField val canProfile: Int = 0,
        @ProtoNumber(13) @JvmField val profileErrmsg: String = "",
        @ProtoNumber(14) @JvmField val canAio: Int = 0,
        @ProtoNumber(15) @JvmField val aioErrmsg: String = "",
        @ProtoNumber(16) @JvmField val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(17) @JvmField val uin: Long = 0L,
        @ProtoNumber(18) @JvmField val limited: Int = 0,
        @ProtoNumber(19) @JvmField val msgCommentList: List<AppointDefine.DateComment>? = null,
        @ProtoNumber(20) @JvmField val commentOver: Int = 0,
        @ProtoNumber(23) @JvmField val meInvited: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgAppointsInfo: List<AppintDetail>? = null,
        @ProtoNumber(2) @JvmField val secureFlag: Int = 0,
        @ProtoNumber(3) @JvmField val secureTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val appointIds: List<AppointDefine.AppointID>? = null,
        @ProtoNumber(2) @JvmField val joinStart: Int = 0,
        @ProtoNumber(3) @JvmField val joinWant: Int = 0,
        @ProtoNumber(4) @JvmField val viewStart: Int = 0,
        @ProtoNumber(5) @JvmField val viewWant: Int = 0,
        @ProtoNumber(6) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoNumber(7) @JvmField val uint64Uins: List<Long>? = null,
        @ProtoNumber(8) @JvmField val viewCommentCount: Int = 0,
        @ProtoNumber(100) @JvmField val requestUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7db : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val wording: String = "",
        @ProtoNumber(2) @JvmField val msgAppointInfo: AppointDefine.AppointInfo? = null,
        @ProtoNumber(3) @JvmField val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val appointAction: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoNumber(2) @JvmField val appointAction: Int = 0,
        @ProtoNumber(3) @JvmField val overwrite: Int = 0,
        @ProtoNumber(4) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc6c : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgGroupInfo: List<GroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoNumber(1) @JvmField val groupUin: Long = 0L,
        @ProtoNumber(2) @JvmField val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody : ProtoBuf
}

@Serializable
internal class Oidb0xc05 : ProtoBuf {
    @Serializable
    internal class GetAuthAppListReq(
        @ProtoNumber(1) @JvmField val start: Int = 0,
        @ProtoNumber(2) @JvmField val limit: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val wording: String = "",
        @ProtoNumber(10) @JvmField val getCreateAppListRsp: GetCreateAppListRsp? = null,
        @ProtoNumber(11) @JvmField val getAuthAppListRsp: GetAuthAppListRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class GetCreateAppListRsp(
        @ProtoNumber(1) @JvmField val totalCount: Int = 0,
        @ProtoNumber(2) @JvmField val appinfos: List<Qqconnect.Appinfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class GetAuthAppListRsp(
        @ProtoNumber(1) @JvmField val totalCount: Int = 0,
        @ProtoNumber(2) @JvmField val appinfos: List<Qqconnect.Appinfo>? = null,
        @ProtoNumber(3) @JvmField val curIndex: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(10) @JvmField val getCreateAppListReq: GetCreateAppListReq? = null,
        @ProtoNumber(11) @JvmField val getAuthAppListReq: GetAuthAppListReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetCreateAppListReq(
        @ProtoNumber(1) @JvmField val start: Int = 0,
        @ProtoNumber(2) @JvmField val limit: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7da : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoNumber(2) @JvmField val appointOperation: Int = 0,
        @ProtoNumber(3) @JvmField val operationReason: Int = 0,
        @ProtoNumber(4) @JvmField val overwrite: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val wording: String = "",
        @ProtoNumber(2) @JvmField val msgAppointInfo: List<AppointDefine.AppointInfo>? = null,
        @ProtoNumber(3) @JvmField val operationReason: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Qqconnect : ProtoBuf {
    @Serializable
    internal class MobileAppInfo(
        @ProtoNumber(11) @JvmField val androidAppInfo: List<AndroidAppInfo>? = null,
        @ProtoNumber(12) @JvmField val iosAppInfo: List<IOSAppInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class TemplateMsgConfig(
        @ProtoNumber(1) @JvmField val serviceMsgUin: Long = 0L,
        @ProtoNumber(2) @JvmField val publicMsgUin: Long = 0L,
        @ProtoNumber(3) @JvmField val campMsgUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class Appinfo(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val appType: Int = 0,
        @ProtoNumber(3) @JvmField val platform: Int = 0,
        @ProtoNumber(4) @JvmField val appName: String = "",
        @ProtoNumber(5) @JvmField val appKey: String = "",
        @ProtoNumber(6) @JvmField val appState: Int = 0,
        @ProtoNumber(7) @JvmField val iphoneUrlScheme: String = "",
        @ProtoNumber(8) @JvmField val androidPackName: String = "",
        @ProtoNumber(9) @JvmField val iconUrl: String = "",
        @ProtoNumber(10) @JvmField val sourceUrl: String = "",
        @ProtoNumber(11) @JvmField val iconSmallUrl: String = "",
        @ProtoNumber(12) @JvmField val iconMiddleUrl: String = "",
        @ProtoNumber(13) @JvmField val tencentDocsAppinfo: TencentDocsAppinfo? = null,
        @ProtoNumber(21) @JvmField val developerUin: Long = 0L,
        @ProtoNumber(22) @JvmField val appClass: Int = 0,
        @ProtoNumber(23) @JvmField val appSubclass: Int = 0,
        @ProtoNumber(24) @JvmField val remark: String = "",
        @ProtoNumber(25) @JvmField val iconMiniUrl: String = "",
        @ProtoNumber(26) @JvmField val authTime: Long = 0L,
        @ProtoNumber(27) @JvmField val appUrl: String = "",
        @ProtoNumber(28) @JvmField val universalLink: String = "",
        @ProtoNumber(29) @JvmField val qqconnectFeature: Int = 0,
        @ProtoNumber(30) @JvmField val isHatchery: Int = 0,
        @ProtoNumber(31) @JvmField val testUinList: List<Long>? = null,
        @ProtoNumber(100) @JvmField val templateMsgConfig: TemplateMsgConfig? = null,
        @ProtoNumber(101) @JvmField val miniAppInfo: MiniAppInfo? = null,
        @ProtoNumber(102) @JvmField val webAppInfo: WebAppInfo? = null,
        @ProtoNumber(103) @JvmField val mobileAppInfo: MobileAppInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ConnectClientInfo(
        @ProtoNumber(1) @JvmField val platform: Int = 0,
        @ProtoNumber(2) @JvmField val sdkVersion: String = "",
        @ProtoNumber(3) @JvmField val systemName: String = "",
        @ProtoNumber(4) @JvmField val systemVersion: String = "",
        @ProtoNumber(21) @JvmField val androidPackageName: String = "",
        @ProtoNumber(22) @JvmField val androidSignature: String = "",
        @ProtoNumber(31) @JvmField val iosBundleId: String = "",
        @ProtoNumber(32) @JvmField val iosDeviceId: String = "",
        @ProtoNumber(33) @JvmField val iosAppToken: String = "",
        @ProtoNumber(41) @JvmField val pcSign: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TencentDocsAppinfo(
        @ProtoNumber(1) @JvmField val openTypes: String = "",
        @ProtoNumber(2) @JvmField val opts: String = "",
        @ProtoNumber(3) @JvmField val ejs: String = "",
        @ProtoNumber(4) @JvmField val callbackUrlTest: String = "",
        @ProtoNumber(5) @JvmField val callbackUrl: String = "",
        @ProtoNumber(6) @JvmField val domain: String = "",
        @ProtoNumber(7) @JvmField val userinfoCallback: String = "",
        @ProtoNumber(8) @JvmField val userinfoCallbackTest: String = ""
    ) : ProtoBuf

    @Serializable
    internal class WebAppInfo(
        @ProtoNumber(1) @JvmField val websiteUrl: String = "",
        @ProtoNumber(2) @JvmField val provider: String = "",
        @ProtoNumber(3) @JvmField val icp: String = "",
        @ProtoNumber(4) @JvmField val callbackUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class IOSAppInfo(
        @ProtoNumber(1) @JvmField val bundleId: String = "",
        @ProtoNumber(2) @JvmField val urlScheme: String = "",
        @ProtoNumber(3) @JvmField val storeId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MsgUinInfo(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgType: Int = 0,
        @ProtoNumber(3) @JvmField val appid: Int = 0,
        @ProtoNumber(4) @JvmField val appType: Int = 0,
        @ProtoNumber(5) @JvmField val ctime: Int = 0,
        @ProtoNumber(6) @JvmField val mtime: Int = 0,
        @ProtoNumber(7) @JvmField val mpType: Int = 0,
        @ProtoNumber(100) @JvmField val nick: String = "",
        @ProtoNumber(101) @JvmField val faceUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MiniAppInfo(
        @ProtoNumber(1) @JvmField val superUin: Long = 0L,
        @ProtoNumber(11) @JvmField val ownerType: Int = 0,
        @ProtoNumber(12) @JvmField val ownerName: String = "",
        @ProtoNumber(13) @JvmField val ownerIdCardType: Int = 0,
        @ProtoNumber(14) @JvmField val ownerIdCard: String = "",
        @ProtoNumber(15) @JvmField val ownerStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AndroidAppInfo(
        @ProtoNumber(1) @JvmField val packName: String = "",
        @ProtoNumber(2) @JvmField val packSign: String = "",
        @ProtoNumber(3) @JvmField val apkDownUrl: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Sync : ProtoBuf {
    @Serializable
    internal class SyncAppointmentReq(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgAppointment: AppointDefine.AppointContent? = null,
        @ProtoNumber(3) @JvmField val msgGpsInfo: AppointDefine.GPS? = null
    ) : ProtoBuf

    @Serializable
    internal class SyncAppointmentRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc26 : ProtoBuf {
    @Serializable
    internal class RgoupLabel(
        @ProtoNumber(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val enumType: Int /* enum */ = 1,
        @ProtoNumber(3) @JvmField val textColor: RgroupColor? = null,
        @ProtoNumber(4) @JvmField val edgingColor: RgroupColor? = null,
        @ProtoNumber(5) @JvmField val labelAttr: Int = 0,
        @ProtoNumber(6) @JvmField val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AddFriendSource(
        @ProtoNumber(1) @JvmField val source: Int = 0,
        @ProtoNumber(2) @JvmField val subSource: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Label(
        @ProtoNumber(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val textColor: Color? = null,
        @ProtoNumber(3) @JvmField val edgingColor: Color? = null,
        @ProtoNumber(4) @JvmField val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class EntryDelay(
        @ProtoNumber(1) @JvmField val emEntry: Int /* enum */ = 1,
        @ProtoNumber(2) @JvmField val delay: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgPersons: List<MayKnowPerson>? = null,
        @ProtoNumber(2) @JvmField val entryInuse: List<Int> = listOf(),
        @ProtoNumber(3) @JvmField val entryClose: List<Int> = listOf(),
        @ProtoNumber(4) @JvmField val nextGap: Int = 0,
        @ProtoNumber(5) @JvmField val timestamp: Int = 0,
        @ProtoNumber(6) @JvmField val msgUp: Int = 0,
        @ProtoNumber(7) @JvmField val entryDelays: List<EntryDelay>? = null,
        @ProtoNumber(8) @JvmField val listSwitch: Int = 0,
        @ProtoNumber(9) @JvmField val addPageListSwitch: Int = 0,
        @ProtoNumber(10) @JvmField val emRspDataType: Int /* enum */ = 1,
        @ProtoNumber(11) @JvmField val msgRgroupItems: List<RecommendInfo>? = null,
        @ProtoNumber(12) @JvmField val boolIsNewuser: Boolean = false,
        @ProtoNumber(13) @JvmField val msgTables: List<TabInfo>? = null,
        @ProtoNumber(14) @JvmField val cookies: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TabInfo(
        @ProtoNumber(1) @JvmField val tabId: Int = 0,
        @ProtoNumber(2) @JvmField val recommendCount: Int = 0,
        @ProtoNumber(3) @JvmField val tableName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val iconUrlSelect: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val iconUrlUnselect: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val backgroundColorSelect: Color? = null,
        @ProtoNumber(7) @JvmField val backgroundColorUnselect: Color? = null
    ) : ProtoBuf

    @Serializable
    internal class MayKnowPerson(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgIosSource: AddFriendSource? = null,
        @ProtoNumber(3) @JvmField val msgAndroidSource: AddFriendSource? = null,
        @ProtoNumber(4) @JvmField val reason: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val additive: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val remark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(8) @JvmField val country: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val province: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val city: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val age: Int = 0,
        @ProtoNumber(12) @JvmField val catelogue: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val alghrithm: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(14) @JvmField val richbuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(15) @JvmField val qzone: Int = 0,
        @ProtoNumber(16) @JvmField val gender: Int = 0,
        @ProtoNumber(17) @JvmField val mobileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(18) @JvmField val token: String = "",
        @ProtoNumber(19) @JvmField val onlineState: Int = 0,
        @ProtoNumber(20) @JvmField val msgLabels: List<Label>? = null,
        @ProtoNumber(21) @JvmField val sourceid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RecommendInfo(
        @ProtoNumber(1) @JvmField val woring: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgGroups: List<RgroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RgroupInfo(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val ownerUin: Long = 0L,
        @ProtoNumber(3) @JvmField val groupName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val groupMemo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val memberNum: Int = 0,
        @ProtoNumber(6) @JvmField val groupLabel: List<RgoupLabel>? = null,
        @ProtoNumber(7) @JvmField val groupFlagExt: Int = 0,
        @ProtoNumber(8) @JvmField val groupFlag: Int = 0,
        @ProtoNumber(9) @JvmField val source: Int /* enum */ = 1,
        @ProtoNumber(10) @JvmField val tagWording: RgoupLabel? = null,
        @ProtoNumber(11) @JvmField val algorithm: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(12) @JvmField val joinGroupAuth: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(13) @JvmField val activity: Int = 0,
        @ProtoNumber(14) @JvmField val memberMaxNum: Int = 0,
        @ProtoNumber(15) @JvmField val int32UinPrivilege: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val filterUins: List<Long>? = null,
        @ProtoNumber(2) @JvmField val phoneBook: Int = 0,
        @ProtoNumber(3) @JvmField val expectedUins: List<Long>? = null,
        @ProtoNumber(4) @JvmField val emEntry: Int /* enum */ = 1,
        @ProtoNumber(5) @JvmField val fetchRgroup: Int = 0,
        @ProtoNumber(6) @JvmField val tabId: Int = 0,
        @ProtoNumber(7) @JvmField val want: Int = 80,
        @ProtoNumber(8) @JvmField val cookies: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RgroupColor(
        @ProtoNumber(1) @JvmField val r: Int = 0,
        @ProtoNumber(2) @JvmField val g: Int = 0,
        @ProtoNumber(3) @JvmField val b: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Color(
        @ProtoNumber(1) @JvmField val r: Int = 0,
        @ProtoNumber(2) @JvmField val g: Int = 0,
        @ProtoNumber(3) @JvmField val b: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xac6 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val results: List<OperateResult>? = null,
        @ProtoNumber(4) @JvmField val metalCount: Int = 0,
        @ProtoNumber(5) @JvmField val metalTotal: Int = 0,
        @ProtoNumber(9) @JvmField val int32NewCount: Int = 0,
        @ProtoNumber(10) @JvmField val int32UpgradeCount: Int = 0,
        @ProtoNumber(11) @JvmField val promptParams: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val medals: List<MedalReport>? = null,
        @ProtoNumber(2) @JvmField val clean: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalReport(
        @ProtoNumber(1) @JvmField val id: Int = 0,
        @ProtoNumber(2) @JvmField val level: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class OperateResult(
        @ProtoNumber(1) @JvmField val id: Int = 0,
        @ProtoNumber(2) @JvmField val int32Result: Int = 0,
        @ProtoNumber(3) @JvmField val errmsg: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd32 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val openid: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class XmitInfo(
        @ProtoNumber(1) @JvmField val signature: String = "",
        @ProtoNumber(2) @JvmField val appid: String = "",
        @ProtoNumber(3) @JvmField val groupid: String = "",
        @ProtoNumber(4) @JvmField val nonce: String = "",
        @ProtoNumber(5) @JvmField val timestamp: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cf : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val stamp: Int = 0,
        @ProtoNumber(2) @JvmField val start: Int = 0,
        @ProtoNumber(3) @JvmField val want: Int = 0,
        @ProtoNumber(4) @JvmField val reqValidOnly: Int = 0,
        @ProtoNumber(5) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoNumber(6) @JvmField val appointOperation: Int = 0,
        @ProtoNumber(100) @JvmField val requestUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val stamp: Int = 0,
        @ProtoNumber(2) @JvmField val over: Int = 0,
        @ProtoNumber(3) @JvmField val next: Int = 0,
        @ProtoNumber(4) @JvmField val msgAppointsInfo: List<AppointDefine.AppointInfo>? = null,
        @ProtoNumber(5) @JvmField val unreadCount: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xac7 : ProtoBuf {
    @Serializable
    internal class DeviceInfo(
        @ProtoNumber(1) @JvmField val din: Long = 0L,
        @ProtoNumber(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val extd: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val cmd: Int = 0,
        @ProtoNumber(2) @JvmField val din: Long = 0L,
        @ProtoNumber(3) @JvmField val extd: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val msgBinderSig: BinderSig? = null
    ) : ProtoBuf

    @Serializable
    internal class ReceiveMessageDevices(
        @ProtoNumber(1) @JvmField val devices: List<DeviceInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class BinderSig(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val uin: Long = 0L,
        @ProtoNumber(3) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fa : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgStrangerInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoNumber(2) @JvmField val reachStart: Int = 0,
        @ProtoNumber(3) @JvmField val reachEnd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val appointIds: AppointDefine.AppointID? = null,
        @ProtoNumber(2) @JvmField val referIdx: Int = 0,
        @ProtoNumber(3) @JvmField val getReferRec: Int = 0,
        @ProtoNumber(4) @JvmField val reqNextCount: Int = 0,
        @ProtoNumber(5) @JvmField val reqPrevCount: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class FavoriteCKVData : ProtoBuf {
    @Serializable
    internal class PicInfo(
        @ProtoNumber(1) @JvmField val uri: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val sha1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val note: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val width: Int = 0,
        @ProtoNumber(7) @JvmField val height: Int = 0,
        @ProtoNumber(8) @JvmField val size: Int = 0,
        @ProtoNumber(9) @JvmField val type: Int = 0,
        @ProtoNumber(10) @JvmField val msgOwner: Author? = null,
        @ProtoNumber(11) @JvmField val picId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KandianFavoriteItem(
        @ProtoNumber(1) @JvmField val msgFavoriteExtInfo: KandianFavoriteBizData? = null,
        @ProtoNumber(2) @JvmField val bytesCid: List<ByteArray>? = null,
        @ProtoNumber(3) @JvmField val type: Int = 0,
        @ProtoNumber(4) @JvmField val status: Int = 0,
        @ProtoNumber(5) @JvmField val msgAuthor: Author? = null,
        @ProtoNumber(6) @JvmField val createTime: Long = 0L,
        @ProtoNumber(7) @JvmField val favoriteTime: Long = 0L,
        @ProtoNumber(8) @JvmField val modifyTime: Long = 0L,
        @ProtoNumber(9) @JvmField val dataSyncTime: Long = 0L,
        @ProtoNumber(10) @JvmField val msgFavoriteSummary: FavoriteSummary? = null
    ) : ProtoBuf

    @Serializable
    internal class LinkSummary(
        @ProtoNumber(1) @JvmField val uri: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val publisher: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val brief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val msgPicInfo: List<PicInfo>? = null,
        @ProtoNumber(6) @JvmField val type: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val resourceUri: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class UserFavoriteList(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val modifyTs: Long = 0L,
        @ProtoNumber(100) @JvmField val msgFavoriteItems: List<FavoriteItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class FavoriteSummary(
        @ProtoNumber(2) @JvmField val msgLinkSummary: LinkSummary? = null
    ) : ProtoBuf

    @Serializable
    internal class FavoriteItem(
        @ProtoNumber(1) @JvmField val favoriteSource: Int = 0,
        @ProtoNumber(100) @JvmField val msgKandianFavoriteItem: KandianFavoriteItem? = null
    ) : ProtoBuf

    @Serializable
    internal class Author(
        @ProtoNumber(1) @JvmField val type: Int = 0,
        @ProtoNumber(2) @JvmField val numId: Long = 0L,
        @ProtoNumber(3) @JvmField val strId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val groupId: Long = 0L,
        @ProtoNumber(5) @JvmField val groupName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KandianFavoriteBizData(
        @ProtoNumber(1) @JvmField val rowkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val type: Int = 0,
        @ProtoNumber(3) @JvmField val videoDuration: Int = 0,
        @ProtoNumber(4) @JvmField val picNum: Int = 0,
        @ProtoNumber(5) @JvmField val accountId: Long = 0L,
        @ProtoNumber(6) @JvmField val accountName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val videoType: Int = 0,
        @ProtoNumber(8) @JvmField val feedsId: Long = 0L,
        @ProtoNumber(9) @JvmField val feedsType: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5ff : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val errorTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoNumber(2) @JvmField val commentId: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xccd : ProtoBuf {
    @Serializable
    internal class Result(
        @ProtoNumber(1) @JvmField val appid: Int = 0,
        @ProtoNumber(2) @JvmField val errcode: Int = 0,
        @ProtoNumber(3) @JvmField val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val int64Uin: Long = 0L,
        @ProtoNumber(2) @JvmField val appids: List<Int>? = null,
        @ProtoNumber(3) @JvmField val platform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val errcode: Int = 0,
        @ProtoNumber(2) @JvmField val results: List<Result>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc36 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uint64Uins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody : ProtoBuf
}

@Serializable
internal class Oidb0x87c : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val country: String = "",
        @ProtoNumber(2) @JvmField val telephone: String = "",
        @ProtoNumber(3) @JvmField val smsCode: String = "",
        @ProtoNumber(4) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val enumButype: Int /* enum */ = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val country: String = "",
        @ProtoNumber(2) @JvmField val telephone: String = "",
        @ProtoNumber(3) @JvmField val keyType: Int = 0,
        @ProtoNumber(4) @JvmField val key: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xbf2 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val phoneAddrBook: List<PhoneAddrBook>? = null,
        @ProtoNumber(2) @JvmField val end: Int = 0,
        @ProtoNumber(3) @JvmField val nextIndex: Long = 0
    ) : ProtoBuf

    @Serializable
    internal class PhoneAddrBook(
        @ProtoNumber(1) @JvmField val phone: String = "",
        @ProtoNumber(2) @JvmField val nick: String = "",
        @ProtoNumber(3) @JvmField val headUrl: String = "",
        @ProtoNumber(4) @JvmField val longNick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val startIndex: Long = 0L,
        @ProtoNumber(3) @JvmField val num: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x6cd : ProtoBuf {
    @Serializable
    internal class RedpointInfo(
        @ProtoNumber(1) @JvmField val taskid: Int = 0,
        @ProtoNumber(2) @JvmField val curSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val pullSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val readSeq: Long = 0L,
        @ProtoNumber(5) @JvmField val pullTimes: Int = 0,
        @ProtoNumber(6) @JvmField val lastPullTime: Int = 0,
        @ProtoNumber(7) @JvmField val int32RemainedTime: Int = 0,
        @ProtoNumber(8) @JvmField val lastRecvTime: Int = 0,
        @ProtoNumber(9) @JvmField val fromId: Long = 0L,
        @ProtoNumber(10) @JvmField val enumRedpointType: Int /* enum */ = 1,
        @ProtoNumber(11) @JvmField val msgRedpointExtraInfo: RepointExtraInfo? = null,
        @ProtoNumber(12) @JvmField val configVersion: String = "",
        @ProtoNumber(13) @JvmField val doActivity: Int = 0,
        @ProtoNumber(14) @JvmField val msgUnreadMsg: List<MessageRec>? = null
    ) : ProtoBuf

    @Serializable
    internal class PullRedpointReq(
        @ProtoNumber(1) @JvmField val taskid: Int = 0,
        @ProtoNumber(2) @JvmField val lastPullSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgRedpoint: List<RedpointInfo>? = null,
        @ProtoNumber(2) @JvmField val unfinishedRedpoint: List<PullRedpointReq>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val lastPullRedpoint: List<PullRedpointReq>? = null,
        @ProtoNumber(2) @JvmField val unfinishedRedpoint: List<PullRedpointReq>? = null,
        @ProtoNumber(3) @JvmField val msgPullSingleTask: PullRedpointReq? = null,
        @ProtoNumber(4) @JvmField val retMsgRec: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MessageRec(
        @ProtoNumber(1) @JvmField val seq: Long = 0L,
        @ProtoNumber(2) @JvmField val time: Int = 0,
        @ProtoNumber(3) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RepointExtraInfo(
        @ProtoNumber(1) @JvmField val count: Int = 0,
        @ProtoNumber(2) @JvmField val iconUrl: String = "",
        @ProtoNumber(3) @JvmField val tips: String = "",
        @ProtoNumber(4) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd55 : ProtoBuf {
    @Serializable
    internal class CheckUserRsp(
        @ProtoNumber(1) @JvmField val openidUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CheckMiniAppRsp : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val appid: Long = 0L,
        @ProtoNumber(2) @JvmField val appType: Int = 0,
        @ProtoNumber(3) @JvmField val srcId: Int = 0,
        @ProtoNumber(4) @JvmField val rawUrl: String = "",
        @ProtoNumber(11) @JvmField val checkAppSignReq: CheckAppSignReq? = null,
        @ProtoNumber(12) @JvmField val checkUserReq: CheckUserReq? = null,
        @ProtoNumber(13) @JvmField val checkMiniAppReq: CheckMiniAppReq? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckAppSignReq(
        @ProtoNumber(1) @JvmField val clientInfo: Qqconnect.ConnectClientInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val wording: String = "",
        @ProtoNumber(11) @JvmField val checkAppSignRsp: CheckAppSignRsp? = null,
        @ProtoNumber(12) @JvmField val checkUserRsp: CheckUserRsp? = null,
        @ProtoNumber(13) @JvmField val checkMiniAppRsp: CheckMiniAppRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUserReq(
        @ProtoNumber(1) @JvmField val openid: String = "",
        @ProtoNumber(2) @JvmField val needCheckSameUser: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CheckMiniAppReq(
        @ProtoNumber(1) @JvmField val miniAppAppid: Long = 0L,
        @ProtoNumber(2) @JvmField val needCheckBind: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CheckAppSignRsp(
        @ProtoNumber(1) @JvmField val iosAppToken: String = "",
        @ProtoNumber(2) @JvmField val iosUniversalLink: String = "",
        @ProtoNumber(11) @JvmField val optimizeSwitch: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x8b4 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val gc: Long = 0L,
        @ProtoNumber(2) @JvmField val guin: Long = 0L,
        @ProtoNumber(3) @JvmField val flag: Int = 0,
        @ProtoNumber(21) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(22) @JvmField val start: Int = 0,
        @ProtoNumber(23) @JvmField val cnt: Int = 0,
        @ProtoNumber(24) @JvmField val tag: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoNumber(1) @JvmField val gc: Long = 0L,
        @ProtoNumber(2) @JvmField val groupName: String = "",
        @ProtoNumber(3) @JvmField val faceUrl: String = "",
        @ProtoNumber(4) @JvmField val setDisplayTime: Int = 0,
        // @SerialId(5) @JvmField val groupLabel: List<GroupLabel.Label>? = null,
        @ProtoNumber(6) @JvmField val textIntro: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val richIntro: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TagInfo(
        @ProtoNumber(1) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(2) @JvmField val start: Int = 0,
        @ProtoNumber(3) @JvmField val cnt: Int = 0,
        @ProtoNumber(4) @JvmField val timestamp: Int = 0,
        @ProtoNumber(5) @JvmField val _0x7ddSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val flag: Int = 0,
        @ProtoNumber(21) @JvmField val tag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(22) @JvmField val groupInfo: List<GroupInfo>? = null,
        @ProtoNumber(23) @JvmField val textLabel: List<ByteArray>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x682 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgChatinfo: List<ChatInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class ChatInfo(
        @ProtoNumber(1) @JvmField val touin: Long = 0L,
        @ProtoNumber(2) @JvmField val chatflag: Int = 0,
        @ProtoNumber(3) @JvmField val goldflag: Int = 0,
        @ProtoNumber(4) @JvmField val totalexpcount: Int = 0,
        @ProtoNumber(5) @JvmField val curexpcount: Int = 0,
        @ProtoNumber(6) @JvmField val totalFlag: Int = 0,
        @ProtoNumber(7) @JvmField val curdayFlag: Int = 0,
        @ProtoNumber(8) @JvmField val expressTipsMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(9) @JvmField val expressMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val uint64Touinlist: List<Long>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x6f5 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val qqVersion: String = "",
        @ProtoNumber(2) @JvmField val qqPlatform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TaskInfo(
        @ProtoNumber(1) @JvmField val taskId: Int = 0,
        @ProtoNumber(2) @JvmField val appid: Int = 0,
        @ProtoNumber(3) @JvmField val passthroughLevel: Int = 0,
        @ProtoNumber(4) @JvmField val showLevel: Int = 0,
        @ProtoNumber(5) @JvmField val extra: Int = 0,
        @ProtoNumber(6) @JvmField val priority: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val configVersion: String = "",
        @ProtoNumber(2) @JvmField val taskInfo: List<TaskInfo>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb7e : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val topItem: List<DiandianTopConfig>? = null
    ) : ProtoBuf

    @Serializable
    internal class DiandianTopConfig(
        @ProtoNumber(1) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val subTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val subTitleColor: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val picUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val type: Int = 0,
        @ProtoNumber(7) @JvmField val topicId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody : ProtoBuf
}

@Serializable
internal class Oidb0xc2f : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val msgGetFollowUserRecommendListRsp: GetFollowUserRecommendListRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class GetFollowUserRecommendListReq(
        @ProtoNumber(1) @JvmField val followedUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RecommendAccountInfo(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val accountType: Int = 0,
        @ProtoNumber(3) @JvmField val nickName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val headImgUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val isVip: Int = 0,
        @ProtoNumber(6) @JvmField val isStar: Int = 0,
        @ProtoNumber(7) @JvmField val recommendReason: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GetFollowUserRecommendListRsp(
        @ProtoNumber(1) @JvmField val msgRecommendList: List<RecommendAccountInfo>? = null,
        @ProtoNumber(2) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgGetFollowUserRecommendListReq: GetFollowUserRecommendListReq? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7ca : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoNumber(2) @JvmField val tinyid: Long = 0L,
        @ProtoNumber(3) @JvmField val opType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val peerUin: Long = 0L,
        @ProtoNumber(3) @JvmField val errorWording: String = "",
        @ProtoNumber(4) @JvmField val opType: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xd40 : ProtoBuf {
    @Serializable
    internal class DeviceInfo(
        @ProtoNumber(1) @JvmField val os: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val dev: DeviceInfo? = null,
        @ProtoNumber(2) @JvmField val src: Int = 0,
        @ProtoNumber(3) @JvmField val event: Int = 0,
        @ProtoNumber(4) @JvmField val redtype: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody : ProtoBuf
}

@Serializable
internal class Cmd0x6ce : ProtoBuf {
    @Serializable
    internal class RspBody : ProtoBuf

    @Serializable
    internal class ReadRedpointReq(
        @ProtoNumber(1) @JvmField val taskid: Int = 0,
        @ProtoNumber(2) @JvmField val readSeq: Long = 0L,
        @ProtoNumber(3) @JvmField val appid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val msgReadReq: List<ReadRedpointReq>? = null
    ) : ProtoBuf
}

