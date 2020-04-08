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
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
internal class Oidb0x8a0 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val optUint64GroupCode: Long = 0L,
        @ProtoId(2) val msgKickResult: List<KickResult>? = null
    ) : ProtoBuf

    @Serializable
    internal class KickResult(
        @ProtoId(1) val optUint32Result: Int = 0,
        @ProtoId(2) val optUint64MemberUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class KickMemberInfo(
        @ProtoId(1) val optUint32Operate: Int = 0,
        @ProtoId(2) val optUint64MemberUin: Long = 0L,
        @ProtoId(3) val optUint32Flag: Int = 0,
        @ProtoId(4) val optBytesMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val optUint64GroupCode: Long = 0L,
        @ProtoId(2) val msgKickList: List<KickMemberInfo>? = null,
        @ProtoId(3) val kickList: List<Long>? = null,
        @ProtoId(4) val kickFlag: Int = 0,
        @ProtoId(5) val kickMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


@Serializable
internal class Oidb0x8fc : ProtoBuf {
    @Serializable
    internal class CardNameElem(
        @ProtoId(1) val enumCardType: Int /* enum */ = 1,
        @ProtoId(2) val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommCardNameBuf(
        @ProtoId(1) val richCardName: List<RichCardNameElem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val showFlag: Int = 0,
        @ProtoId(3) val memLevelInfo: List<MemberInfo>? = null,
        @ProtoId(4) val levelName: List<LevelName>? = null,
        @ProtoId(5) val updateTime: Int = 0,
        @ProtoId(6) val officeMode: Int = 0,
        @ProtoId(7) val groupOpenAppid: Int = 0,
        @ProtoId(8) val msgClientInfo: ClientInfo? = null,
        @ProtoId(9) val authKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MemberInfo(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val point: Int = 0,
        @ProtoId(3) val activeDay: Int = 0,
        @ProtoId(4) val level: Int = 0,
        @ProtoId(5) val specialTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val specialTitleExpireTime: Int = 0,
        @ProtoId(7) val uinName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val memberCardName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val phone: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val email: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val remark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val gender: Int = 0,
        @ProtoId(13) val job: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val tribeLevel: Int = 0,
        @ProtoId(15) val tribePoint: Int = 0,
        @ProtoId(16) val richCardName: List<CardNameElem>? = null,
        @ProtoId(17) val commRichCardName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RichCardNameElem(
        @ProtoId(1) val ctrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val text: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val errInfo: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoId(1) val implat: Int = 0,
        @ProtoId(2) val ingClientver: String = ""
    ) : ProtoBuf

    @Serializable
    internal class LevelName(
        @ProtoId(1) val level: Int = 0,
        @ProtoId(2) val name: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x88d : ProtoBuf {
    @Serializable
    internal class GroupExInfoOnly(
        @ProtoId(1) val tribeId: Int = 0,
        @ProtoId(2) val moneyForAddGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqGroupInfo(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val stgroupinfo: GroupInfo? = null,
        @ProtoId(3) val lastGetGroupNameTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspGroupInfo(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val result: Int = 0,
        @ProtoId(3) val stgroupinfo: GroupInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class GroupGeoInfo(
        @ProtoId(1) val owneruin: Long = 0L,
        @ProtoId(2) val settime: Int = 0,
        @ProtoId(3) val cityid: Int = 0,
        @ProtoId(4) val int64Longitude: Long = 0L,
        @ProtoId(5) val int64Latitude: Long = 0L,
        @ProtoId(6) val geocontent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val poiId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TagRecord(
        @ProtoId(1) val fromUin: Long = 0L,
        @ProtoId(2) val groupCode: Long = 0L,
        @ProtoId(3) val tagId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val setTime: Long = 0L,
        @ProtoId(5) val goodNum: Int = 0,
        @ProtoId(6) val badNum: Int = 0,
        @ProtoId(7) val tagLen: Int = 0,
        @ProtoId(8) val tagValue: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) val groupOwner: Long? = null,
        @ProtoId(2) val groupCreateTime: Int? = null,
        @ProtoId(3) val groupFlag: Int? = null,
        @ProtoId(4) val groupFlagExt: Int? = null,
        @ProtoId(5) val groupMemberMaxNum: Int? = null,
        @ProtoId(6) val groupMemberNum: Int? = null,
        @ProtoId(7) val groupOption: Int? = null,
        @ProtoId(8) val groupClassExt: Int? = null,
        @ProtoId(9) val groupSpecialClass: Int? = null,
        @ProtoId(10) val groupLevel: Int? = null,
        @ProtoId(11) val groupFace: Int? = null,
        @ProtoId(12) val groupDefaultPage: Int? = null,
        @ProtoId(13) val groupInfoSeq: Int? = null,
        @ProtoId(14) val groupRoamingTime: Int? = null,
        @ProtoId(15) var groupName: String? = null,
        @ProtoId(16) var groupMemo: String? = null,
        @ProtoId(17) val ingGroupFingerMemo: String? = null,
        @ProtoId(18) val ingGroupClassText: String? = null,
        @ProtoId(19) val groupAllianceCode: List<Int>? = null,
        @ProtoId(20) val groupExtraAdmNum: Int? = null,
        @ProtoId(21) var groupUin: Long? = null,
        @ProtoId(22) val groupCurMsgSeq: Int? = null,
        @ProtoId(23) val groupLastMsgTime: Int? = null,
        @ProtoId(24) val ingGroupQuestion: String? = null,
        @ProtoId(25) val ingGroupAnswer: String? = null,
        @ProtoId(26) val groupVisitorMaxNum: Int? = null,
        @ProtoId(27) val groupVisitorCurNum: Int? = null,
        @ProtoId(28) val levelNameSeq: Int? = null,
        @ProtoId(29) val groupAdminMaxNum: Int? = null,
        @ProtoId(30) val groupAioSkinTimestamp: Int? = null,
        @ProtoId(31) val groupBoardSkinTimestamp: Int? = null,
        @ProtoId(32) val ingGroupAioSkinUrl: String? = null,
        @ProtoId(33) val ingGroupBoardSkinUrl: String? = null,
        @ProtoId(34) val groupCoverSkinTimestamp: Int? = null,
        @ProtoId(35) val ingGroupCoverSkinUrl: String? = null,
        @ProtoId(36) val groupGrade: Int? = null,
        @ProtoId(37) val activeMemberNum: Int? = null,
        @ProtoId(38) val certificationType: Int? = null,
        @ProtoId(39) val ingCertificationText: String? = null,
        @ProtoId(40) val ingGroupRichFingerMemo: String? = null,
        @ProtoId(41) val tagRecord: List<TagRecord>? = null,
        @ProtoId(42) val groupGeoInfo: GroupGeoInfo? = null,
        @ProtoId(43) val headPortraitSeq: Int? = null,
        @ProtoId(44) val msgHeadPortrait: GroupHeadPortrait? = null,
        @ProtoId(45) val shutupTimestamp: Int? = null,
        @ProtoId(46) val shutupTimestampMe: Int? = null,
        @ProtoId(47) val createSourceFlag: Int? = null,
        @ProtoId(48) val cmduinMsgSeq: Int? = null,
        @ProtoId(49) val cmduinJoinTime: Int? = null,
        @ProtoId(50) val cmduinUinFlag: Int? = null,
        @ProtoId(51) val cmduinFlagEx: Int? = null,
        @ProtoId(52) val cmduinNewMobileFlag: Int? = null,
        @ProtoId(53) val cmduinReadMsgSeq: Int? = null,
        @ProtoId(54) val cmduinLastMsgTime: Int? = null,
        @ProtoId(55) val groupTypeFlag: Int? = null,
        @ProtoId(56) val appPrivilegeFlag: Int? = null,
        @ProtoId(57) val stGroupExInfo: GroupExInfoOnly? = null,
        @ProtoId(58) val groupSecLevel: Int? = null,
        @ProtoId(59) val groupSecLevelInfo: Int? = null,
        @ProtoId(60) val cmduinPrivilege: Int? = null,
        @ProtoId(61) val ingPoidInfo: ByteArray? = null,
        @ProtoId(62) val cmduinFlagEx2: Int? = null,
        @ProtoId(63) val confUin: Long? = null,
        @ProtoId(64) val confMaxMsgSeq: Int? = null,
        @ProtoId(65) val confToGroupTime: Int? = null,
        @ProtoId(66) val passwordRedbagTime: Int? = null,
        @ProtoId(67) val subscriptionUin: Long? = null,
        @ProtoId(68) val memberListChangeSeq: Int? = null,
        @ProtoId(69) val membercardSeq: Int? = null,
        @ProtoId(70) val rootId: Long? = null,
        @ProtoId(71) val parentId: Long? = null,
        @ProtoId(72) val teamSeq: Int? = null,
        @ProtoId(73) val historyMsgBeginTime: Long? = null,
        @ProtoId(74) val inviteNoAuthNumLimit: Long? = null,
        @ProtoId(75) val cmduinHistoryMsgSeq: Int? = null,
        @ProtoId(76) val cmduinJoinMsgSeq: Int? = null,
        @ProtoId(77) val groupFlagext3: Int? = null,
        @ProtoId(78) val groupOpenAppid: Int? = null,
        @ProtoId(79) val isConfGroup: Int? = null,
        @ProtoId(80) val isModifyConfGroupFace: Int? = null,
        @ProtoId(81) val isModifyConfGroupName: Int? = null,
        @ProtoId(82) val noFingerOpenFlag: Int? = null,
        @ProtoId(83) val noCodeFingerOpenFlag: Int? = null,
        @ProtoId(84) val autoAgreeJoinGroupUserNumForNormalGroup: Int? = null,
        @ProtoId(85) val autoAgreeJoinGroupUserNumForConfGroup: Int? = null,
        @ProtoId(86) val isAllowConfGroupMemberNick: Int? = null,
        @ProtoId(87) val isAllowConfGroupMemberAtAll: Int? = null,
        @ProtoId(88) val isAllowConfGroupMemberModifyGroupName: Int? = null,
        @ProtoId(89) val longGroupName: String? = null,
        @ProtoId(90) val cmduinJoinRealMsgSeq: Int? = null,
        @ProtoId(91) val isGroupFreeze: Int? = null,
        @ProtoId(92) val msgLimitFrequency: Int? = null,
        @ProtoId(93) val joinGroupAuth: ByteArray? = null,
        @ProtoId(94) val hlGuildAppid: Int? = null,
        @ProtoId(95) val hlGuildSubType: Int? = null,
        @ProtoId(96) val hlGuildOrgid: Int? = null,
        @ProtoId(97) val isAllowHlGuildBinary: Int? = null,
        @ProtoId(98) val cmduinRingtoneId: Int? = null,
        @ProtoId(99) val groupFlagext4: Int? = null,
        @ProtoId(100) val groupFreezeReason: Int? = null,
        @ProtoId(101) var groupCode: Long? = null // mirai 添加
    ) : ProtoBuf

    @Serializable
    internal class GroupHeadPortraitInfo(
        @ProtoId(1) val uint32PicId: Int = 0,
        @ProtoId(2) val leftX: Int = 0,
        @ProtoId(3) val leftY: Int = 0,
        @ProtoId(4) val rightX: Int = 0,
        @ProtoId(5) val rightY: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val stzrspgroupinfo: List<RspGroupInfo>? = null,
        @ProtoId(2) val errorinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val stzreqgroupinfo: List<ReqGroupInfo>? = null,
        @ProtoId(3) val pcClientVersion: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupHeadPortrait(
        @ProtoId(1) val picCnt: Int = 0,
        @ProtoId(2) val msgInfo: List<GroupHeadPortraitInfo>? = null,
        @ProtoId(3) val defaultId: Int = 0,
        @ProtoId(4) val verifyingPicCnt: Int = 0,
        @ProtoId(5) val msgVerifyingpicInfo: List<GroupHeadPortraitInfo>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x89a : ProtoBuf {
    @Serializable
    internal class GroupNewGuidelinesInfo(
        @ProtoId(1) val boolEnabled: Boolean = false,
        @ProtoId(2) val ingContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Groupinfo(
        @ProtoId(1) val groupExtAdmNum: Int? = null,
        @ProtoId(2) val flag: Int? = null,
        @ProtoId(3) val ingGroupName: ByteArray? = null,
        @ProtoId(4) val ingGroupMemo: ByteArray? = null,
        @ProtoId(5) val ingGroupFingerMemo: ByteArray? = null,
        @ProtoId(6) val ingGroupAioSkinUrl: ByteArray? = null,
        @ProtoId(7) val ingGroupBoardSkinUrl: ByteArray? = null,
        @ProtoId(8) val ingGroupCoverSkinUrl: ByteArray? = null,
        @ProtoId(9) val groupGrade: Int? = null,
        @ProtoId(10) val activeMemberNum: Int? = null,
        @ProtoId(11) val certificationType: Int? = null,
        @ProtoId(12) val ingCertificationText: ByteArray? = null,
        @ProtoId(13) val ingGroupRichFingerMemo: ByteArray? = null,
        @ProtoId(14) val stGroupNewguidelines: GroupNewGuidelinesInfo? = null,
        @ProtoId(15) val groupFace: Int? = null,
        @ProtoId(16) val addOption: Int? = null,
        @ProtoId(17) val shutupTime: Int? = null,
        @ProtoId(18) val groupTypeFlag: Int? = null,
        @ProtoId(19) val stringGroupTag: List<ByteArray>? = null,
        @ProtoId(20) val msgGroupGeoInfo: GroupGeoInfo? = null,
        @ProtoId(21) val groupClassExt: Int? = null,
        @ProtoId(22) val ingGroupClassText: ByteArray? = null,
        @ProtoId(23) val appPrivilegeFlag: Int? = null,
        @ProtoId(24) val appPrivilegeMask: Int? = null,
        @ProtoId(25) val stGroupExInfo: GroupExInfoOnly? = null,
        @ProtoId(26) val groupSecLevel: Int? = null,
        @ProtoId(27) val groupSecLevelInfo: Int? = null,
        @ProtoId(28) val subscriptionUin: Long? = null,
        @ProtoId(29) val allowMemberInvite: Int? = null,
        @ProtoId(30) val ingGroupQuestion: ByteArray? = null,
        @ProtoId(31) val ingGroupAnswer: ByteArray? = null,
        @ProtoId(32) val groupFlagext3: Int? = null,
        @ProtoId(33) val groupFlagext3Mask: Int? = null,
        @ProtoId(34) val groupOpenAppid: Int? = null,
        @ProtoId(35) val noFingerOpenFlag: Int? = null,
        @ProtoId(36) val noCodeFingerOpenFlag: Int? = null,
        @ProtoId(37) val rootId: Long? = null,
        @ProtoId(38) val msgLimitFrequency: Int? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val errorinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupExInfoOnly(
        @ProtoId(1) val tribeId: Int = 0,
        @ProtoId(2) val moneyForAddGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupGeoInfo(
        @ProtoId(1) val cityId: Int = 0,
        @ProtoId(2) val longtitude: Long = 0L,
        @ProtoId(3) val latitude: Long = 0L,
        @ProtoId(4) val ingGeoContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val poiId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val stGroupInfo: Groupinfo? = null,
        @ProtoId(3) val originalOperatorUin: Long = 0L,
        @ProtoId(4) val reqGroupOpenAppid: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cb : ProtoBuf {
    @Serializable
    internal class ConfigItem(
        @ProtoId(1) val id: Int = 0,
        @ProtoId(2) val config: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val timeStamp: Int = 0,
        @ProtoId(2) val timeGap: Int = 0,
        @ProtoId(3) val commentConfigs: List<CommentConfig>? = null,
        @ProtoId(4) val attendTipsToA: String = "",
        @ProtoId(5) val firstMsgTips: String = "",
        @ProtoId(6) val cancleConfig: List<ConfigItem>? = null,
        @ProtoId(7) val msgDateRequest: DateRequest? = null,
        @ProtoId(8) val msgHotLocale: List<ByteArray>? = null,//List<AppointDefine.LocaleInfo>
        @ProtoId(9) val msgTopicList: List<TopicConfig>? = null,
        @ProtoId(10) val travelMsgTips: String = "",
        @ProtoId(11) val travelProfileTips: String = "",
        @ProtoId(12) val travelAttenTips: String = "",
        @ProtoId(13) val topicDefault: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommentConfig(
        @ProtoId(1) val appointSubject: Int = 0,
        @ProtoId(2) val msgConfigs: List<ConfigItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val timeStamp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DateRequest(
        @ProtoId(1) val time: Int = 0,
        @ProtoId(2) val errMsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TopicConfig(
        @ProtoId(1) val topicId: Int = 0,
        @ProtoId(2) val topicName: String = "",
        @ProtoId(3) val deadline: Int = 0,
        @ProtoId(4) val errDeadline: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x87a : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val country: String = "",
        @ProtoId(2) val telephone: String = "",
        @ProtoId(3) val resendInterval: Int = 0,
        @ProtoId(4) val guid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val country: String = "",
        @ProtoId(2) val telephone: String = "",
        @ProtoId(3) val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val enumButype: Int /* enum */ = 0
    ) : ProtoBuf
}

@Serializable
internal class GroupAppPb : ProtoBuf {
    @Serializable
    internal class ClientInfo(
        @ProtoId(1) val platform: Int = 0,
        @ProtoId(2) val version: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val fullList: AppList? = null,
        @ProtoId(2) val groupGrayList: AppList? = null,
        @ProtoId(3) val redPointList: AppList? = null,
        @ProtoId(4) val cacheInterval: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AppList(
        @ProtoId(1) val hash: String = "",
        @ProtoId(2) val infos: List<AppInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class AppInfo(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val icon: String = "",
        @ProtoId(3) val name: String = "",
        @ProtoId(4) val url: String = "",
        @ProtoId(5) val isGray: Int = 0,
        @ProtoId(6) val iconSimpleDay: String = "",
        @ProtoId(7) val iconSimpleNight: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val client: ClientInfo? = null,
        @ProtoId(2) val groupId: Long = 0L,
        @ProtoId(3) val groupType: Int = 0,
        @ProtoId(4) val fullListHash: String = "",
        @ProtoId(5) val groupGrayListHash: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc34 : ProtoBuf {
    @Serializable
    internal class RspBody : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fd : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgComment: AppointDefine.DateComment? = null,
        @ProtoId(2) val maxFetchCount: Int = 0,
        @ProtoId(3) val lastCommentId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgComment: List<AppointDefine.DateComment>? = null,
        @ProtoId(2) val errorTips: String = "",
        @ProtoId(3) val clearCacheFlag: Int = 0,
        @ProtoId(4) val commentWording: String = "",
        @ProtoId(5) val commentNum: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbcb : ProtoBuf {
    @Serializable
    internal class CheckUrlReqItem(
        @ProtoId(1) val url: String = "",
        @ProtoId(2) val refer: String = "",
        @ProtoId(3) val plateform: String = "",
        @ProtoId(4) val qqPfTo: String = "",
        @ProtoId(5) val msgType: Int = 0,
        @ProtoId(6) val msgFrom: Int = 0,
        @ProtoId(7) val msgChatid: Long = 0L,
        @ProtoId(8) val serviceType: Long = 0L,
        @ProtoId(9) val sendUin: Long = 0L,
        @ProtoId(10) val reqType: String = ""
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlRsp(
        @ProtoId(1) val results: List<UrlCheckResult>? = null,
        @ProtoId(2) val nextReqDuration: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(9) val notUseCache: Int = 0,
        @ProtoId(10) val checkUrlReq: CheckUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val wording: String = "",
        @ProtoId(10) val checkUrlRsp: CheckUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlReq(
        @ProtoId(1) val url: List<String> = listOf(),
        @ProtoId(2) val refer: String = "",
        @ProtoId(3) val plateform: String = "",
        @ProtoId(4) val qqPfTo: String = "",
        @ProtoId(5) val msgType: Int = 0,
        @ProtoId(6) val msgFrom: Int = 0,
        @ProtoId(7) val msgChatid: Long = 0L,
        @ProtoId(8) val serviceType: Long = 0L,
        @ProtoId(9) val sendUin: Long = 0L,
        @ProtoId(10) val reqType: String = "",
        @ProtoId(11) val originalUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UrlCheckResult(
        @ProtoId(1) val url: String = "",
        @ProtoId(2) val result: Int = 0,
        @ProtoId(3) val jumpResult: Int = 0,
        @ProtoId(4) val jumpUrl: String = "",
        @ProtoId(5) val level: Int = 0,
        @ProtoId(6) val subLevel: Int = 0,
        @ProtoId(7) val umrtype: Int = 0,
        @ProtoId(8) val retFrom: Int = 0,
        @ProtoId(9) val operationBit: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbfe : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val receiveStatus: Int = 0,
        @ProtoId(2) val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val flag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbe8 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val enumOpCode: Int /* enum */ = 1,
        @ProtoId(3) val rspOfPopupFlag: Int = 0,
        @ProtoId(4) val popupCountNow: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PopupResult(
        @ProtoId(1) val popupResult: Int = 0,
        @ProtoId(2) val popupFieldid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val enumOpCode: Int /* enum */ = 1,
        @ProtoId(3) val reqOfPopupFlag: Int = 0,
        @ProtoId(4) val rstOfPopupFlag: Int = 0,
        @ProtoId(5) val mqq808WelcomepageFlag: Int = 0,
        @ProtoId(6) val msgPopupResult: List<PopupResult>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7de : ProtoBuf {
    @Serializable
    internal class UserProfile(
        @ProtoId(1) val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) val msgAppointsInfo: AppointDefine.AppointInfo? = null,
        @ProtoId(3) val msgVistorInfo: List<AppointDefine.StrangerInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgHead: BusiRespHead? = null,
        @ProtoId(2) val msgUserList: List<UserProfile>? = null,
        @ProtoId(3) val ended: Int = 0,
        @ProtoId(4) val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BusiRespHead(
        @ProtoId(1) val int32Version: Int = 1,
        @ProtoId(2) val int32Seq: Int = 0,
        @ProtoId(3) val int32ReplyCode: Int = 0,
        @ProtoId(4) val result: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgHead: BusiReqHead? = null,
        @ProtoId(2) val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(3) val time: Int = 0,
        @ProtoId(4) val subject: Int = 0,
        @ProtoId(5) val gender: Int = 0,
        @ProtoId(6) val ageLow: Int = 0,
        @ProtoId(7) val ageUp: Int = 0,
        @ProtoId(8) val profession: Int = 0,
        @ProtoId(9) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val msgDestination: AppointDefine.LocaleInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class BusiReqHead(
        @ProtoId(1) val int32Version: Int = 1,
        @ProtoId(2) val int32Seq: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7a8 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val reqUin: Long = 0L,
        @ProtoId(11) val onlyObtained: Int = 0,
        @ProtoId(12) val readReport: Int = 0,
        @ProtoId(13) val sortType: Int = 0,
        @ProtoId(14) val onlyNew: Int = 0,
        @ProtoId(15) val filterMedalIds: List<Int>? = null,
        @ProtoId(16) val onlySummary: Int = 0,
        @ProtoId(17) val doScan: Int = 0,
        @ProtoId(18) val startTimestamp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val nick: String = "",
        @ProtoId(2) val metalRank: Int = 0,
        @ProtoId(3) val friCount: Int = 0,
        @ProtoId(4) val metalCount: Int = 0,
        @ProtoId(5) val metalTotal: Int = 0,
        @ProtoId(6) val msgMedal: List<Common.MedalInfo>? = null,
        @ProtoId(8) val totalPoint: Int = 0,
        @ProtoId(9) val int32NewCount: Int = 0,
        @ProtoId(10) val int32UpgradeCount: Int = 0,
        @ProtoId(11) val promptParams: String = "",
        @ProtoId(12) val now: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalNews(
        @ProtoId(1) val friUin: Long = 0L,
        @ProtoId(2) val friNick: String = "",
        @ProtoId(3) val msgMedal: Common.MedalInfo? = null
    ) : ProtoBuf
}


@Serializable
internal class Cmd0x5fe : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) val commentId: String = "",
        @ProtoId(3) val fetchOldCount: Int = 0,
        @ProtoId(4) val fetchNewCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgComment: List<AppointDefine.DateComment>? = null,
        @ProtoId(2) val errorTips: String = "",
        @ProtoId(3) val fetchOldOver: Int = 0,
        @ProtoId(4) val fetchNewOver: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc35 : ProtoBuf {
    @Serializable
    internal class RspBody : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val msgExposeInfo: List<ExposeItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ExposeItem(
        @ProtoId(1) val friend: Long = 0L,
        @ProtoId(2) val pageId: Int = 0,
        @ProtoId(3) val entranceId: Int = 0,
        @ProtoId(4) val actionId: Int = 0,
        @ProtoId(5) val exposeCount: Int = 0,
        @ProtoId(6) val exposeTime: Int = 0,
        @ProtoId(7) val algoBuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val addition: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc0d : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val completedTaskStamp: Long = 0L,
        @ProtoId(2) val errMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val taskType: Int = 0,
        @ProtoId(3) val taskPoint: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class OidbSso : ProtoBuf {
    @Serializable
    internal class OIDBSSOPkg(
        @ProtoId(1) val command: Int = 0,
        @ProtoId(2) val serviceType: Int = 0,
        @ProtoId(3) val result: Int = 0,
        @ProtoId(4) val bodybuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val errorMsg: String = "",
        @ProtoId(6) val clientVersion: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xc83 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(101) val fromUin: Long = 0L,
        @ProtoId(102) val toUin: Long = 0L,
        @ProtoId(103) val op: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(101) val result: Int = 0,
        @ProtoId(102) val retryInterval: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xccb : ProtoBuf {
    @Serializable
    internal class GroupMsgInfo(
        @ProtoId(1) val msgSeq: Int = 0,
        @ProtoId(2) val roamFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val destUin: Long = 0L,
        @ProtoId(3) val groupCode: Long = 0L,
        @ProtoId(4) val c2cMsg: List<C2cMsgInfo>? = null,
        @ProtoId(5) val groupMsg: List<GroupMsgInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val destUin: Long = 0L,
        @ProtoId(3) val groupCode: Long = 0L,
        @ProtoId(4) val c2cMsg: List<C2cMsgInfo>? = null,
        @ProtoId(5) val groupMsg: List<GroupMsgInfo>? = null,
        @ProtoId(6) val resId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class C2cMsgInfo(
        @ProtoId(1) val msgSeq: Int = 0,
        @ProtoId(2) val msgTime: Int = 0,
        @ProtoId(3) val msgRandom: Int = 0,
        @ProtoId(4) val roamFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd84 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x5e1 : ProtoBuf {
    @Serializable
    internal class UdcUinData(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(4) val openid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20002) val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20003) val country: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20004) val province: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20009) val gender: Int = 0,
        @ProtoId(20014) val allow: Int = 0,
        @ProtoId(20015) val faceId: Int = 0,
        @ProtoId(20020) val city: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20027) val commonPlace1: Int = 0,
        @ProtoId(20030) val mss3Bitmapextra: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20031) val birthday: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20032) val cityId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20033) val lang1: Int = 0,
        @ProtoId(20034) val lang2: Int = 0,
        @ProtoId(20035) val lang3: Int = 0,
        @ProtoId(20041) val cityZoneId: Int = 0,
        @ProtoId(20056) val oin: Int = 0,
        @ProtoId(20059) val bubbleId: Int = 0,
        @ProtoId(21001) val mss2Identity: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21002) val mss1Service: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21003) val lflag: Int = 0,
        @ProtoId(21004) val extFlag: Int = 0,
        @ProtoId(21006) val basicSvrFlag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21007) val basicCliFlag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(24101) val pengyouRealname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(24103) val pengyouGender: Int = 0,
        @ProtoId(24118) val pengyouFlag: Int = 0,
        @ProtoId(26004) val fullBirthday: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(26005) val fullAge: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(26010) val simpleUpdateTime: Int = 0,
        @ProtoId(26011) val mssUpdateTime: Int = 0,
        @ProtoId(27022) val groupMemCreditFlag: Int = 0,
        @ProtoId(27025) val faceAddonId: Long = 0L,
        @ProtoId(27026) val musicGene: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(40323) val fileShareBit: Int = 0,
        @ProtoId(40404) val recommendPrivacyCtrl: Int = 0,
        @ProtoId(40505) val oldFriendChat: Int = 0,
        @ProtoId(40602) val businessBit: Int = 0,
        @ProtoId(41305) val crmBit: Int = 0,
        @ProtoId(41810) val forbidFileshareBit: Int = 0,
        @ProtoId(42333) val userLoginGuardFace: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(11) val msgUinData: List<UdcUinData>? = null,
        @ProtoId(12) val uint64UnfinishedUins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uint64Uins: List<Long>? = null,
        @ProtoId(2) val startTime: Int = 0,
        @ProtoId(3) val maxPackageSize: Int = 0,
        @ProtoId(4) val bytesOpenid: List<ByteArray>? = null,
        @ProtoId(5) val appid: Int = 0,
        @ProtoId(20002) val reqNick: Int = 0,
        @ProtoId(20003) val reqCountry: Int = 0,
        @ProtoId(20004) val reqProvince: Int = 0,
        @ProtoId(20009) val reqGender: Int = 0,
        @ProtoId(20014) val reqAllow: Int = 0,
        @ProtoId(20015) val reqFaceId: Int = 0,
        @ProtoId(20020) val reqCity: Int = 0,
        @ProtoId(20027) val reqCommonPlace1: Int = 0,
        @ProtoId(20030) val reqMss3Bitmapextra: Int = 0,
        @ProtoId(20031) val reqBirthday: Int = 0,
        @ProtoId(20032) val reqCityId: Int = 0,
        @ProtoId(20033) val reqLang1: Int = 0,
        @ProtoId(20034) val reqLang2: Int = 0,
        @ProtoId(20035) val reqLang3: Int = 0,
        @ProtoId(20041) val reqCityZoneId: Int = 0,
        @ProtoId(20056) val reqOin: Int = 0,
        @ProtoId(20059) val reqBubbleId: Int = 0,
        @ProtoId(21001) val reqMss2Identity: Int = 0,
        @ProtoId(21002) val reqMss1Service: Int = 0,
        @ProtoId(21003) val reqLflag: Int = 0,
        @ProtoId(21004) val reqExtFlag: Int = 0,
        @ProtoId(21006) val reqBasicSvrFlag: Int = 0,
        @ProtoId(21007) val reqBasicCliFlag: Int = 0,
        @ProtoId(24101) val reqPengyouRealname: Int = 0,
        @ProtoId(24103) val reqPengyouGender: Int = 0,
        @ProtoId(24118) val reqPengyouFlag: Int = 0,
        @ProtoId(26004) val reqFullBirthday: Int = 0,
        @ProtoId(26005) val reqFullAge: Int = 0,
        @ProtoId(26010) val reqSimpleUpdateTime: Int = 0,
        @ProtoId(26011) val reqMssUpdateTime: Int = 0,
        @ProtoId(27022) val reqGroupMemCreditFlag: Int = 0,
        @ProtoId(27025) val reqFaceAddonId: Int = 0,
        @ProtoId(27026) val reqMusicGene: Int = 0,
        @ProtoId(40323) val reqFileShareBit: Int = 0,
        @ProtoId(40404) val reqRecommendPrivacyCtrlBit: Int = 0,
        @ProtoId(40505) val reqOldFriendChatBit: Int = 0,
        @ProtoId(40602) val reqBusinessBit: Int = 0,
        @ProtoId(41305) val reqCrmBit: Int = 0,
        @ProtoId(41810) val reqForbidFileshareBit: Int = 0,
        @ProtoId(42333) val userLoginGuardFace: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc90 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val communityBid: List<Long>? = null,
        @ProtoId(2) val page: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommunityWebInfo(
        @ProtoId(1) val communityInfoItem: List<CommunityConfigInfo>? = null,
        @ProtoId(2) val page: Int = 0,
        @ProtoId(3) val end: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val communityInfoItem: List<CommunityConfigInfo>? = null,
        @ProtoId(2) val jumpConcernCommunityUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val communityTitleWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val moreUrlWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val webCommunityInfo: CommunityWebInfo? = null,
        @ProtoId(6) val jumpCommunityChannelUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommunityConfigInfo(
        @ProtoId(1) val jumpHomePageUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val picUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val dynamicCount: Int = 0,
        @ProtoId(5) val communityBid: Long = 0L,
        @ProtoId(6) val followStatus: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xd8a : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val retcode: Int = 0,
        @ProtoId(2) val res: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val cmd: Int = 0,
        @ProtoId(3) val body: String = "",
        @ProtoId(4) val clientInfo: ClientInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoId(1) val implat: Int = 0,
        @ProtoId(2) val ingClientver: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb6f : ProtoBuf {
    @Serializable
    internal class ReportFreqRspBody(
        @ProtoId(1) val identity: Identity? = null,
        @ProtoId(4) val remainTimes: Long = 0L,
        @ProtoId(5) val expireTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Identity(
        @ProtoId(1) val apiName: String = "",
        @ProtoId(2) val appid: Int = 0,
        @ProtoId(3) val apptype: Int = 0,
        @ProtoId(4) val bizid: Int = 0,
        @ProtoId(10) val intExt1: Long = 0L,
        @ProtoId(20) val ext1: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ThresholdInfo(
        @ProtoId(1) val thresholdPerMinute: Long = 0L,
        @ProtoId(2) val thresholdPerDay: Long = 0L,
        @ProtoId(3) val thresholdPerHour: Long = 0L,
        @ProtoId(4) val thresholdPerWeek: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val reportFreqRsp: ReportFreqRspBody? = null
    ) : ProtoBuf

    @Serializable
    internal class ReportFreqReqBody(
        @ProtoId(1) val identity: Identity? = null,
        @ProtoId(2) val invokeTimes: Long = 1L
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val reportFreqReq: ReportFreqReqBody? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7dc : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val seq: Int = 0,
        @ProtoId(2) val wording: String = "",
        @ProtoId(3) val msgAppointInfo: List<AppointDefine.AppointInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val seq: Int = 0,
        @ProtoId(2) val msgAppointment: AppointDefine.AppointContent? = null,
        @ProtoId(3) val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(4) val overwrite: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cd : ProtoBuf {
    @Serializable
    internal class AppointBrife(
        @ProtoId(1) val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) val msgAppointsInfo: AppointDefine.AppointInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val stamp: Int = 0,
        @ProtoId(2) val over: Int = 0,
        @ProtoId(3) val next: Int = 0,
        @ProtoId(4) val msgAppointsInfo: List<AppointBrife>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val stamp: Int = 0,
        @ProtoId(2) val start: Int = 0,
        @ProtoId(3) val want: Int = 0,
        @ProtoId(4) val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(5) val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(6) val appointOperation: Int = 0,
        @ProtoId(100) val requestUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc0c : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val isTaskCompleted: Int = 0,
        @ProtoId(2) val taskPoint: Int = 0,
        @ProtoId(3) val guideWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val needShowProgress: Int = 0,
        @ProtoId(5) val originalProgress: Int = 0,
        @ProtoId(6) val nowProgress: Int = 0,
        @ProtoId(7) val totalProgress: Int = 0,
        @ProtoId(8) val needExecTask: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class VideoSrcType(
        @ProtoId(1) val sourceType: Int = 0,
        @ProtoId(2) val videoFromType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val taskType: Int = 0,
        @ProtoId(3) val rowkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val feedsId: Long = 0L,
        @ProtoId(5) val msgVideoFromType: VideoSrcType? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fb : ProtoBuf {
    @Serializable
    internal class ReqInfo(
        @ProtoId(3) val time: Int = 0,
        @ProtoId(4) val subject: Int = 0,
        @ProtoId(5) val gender: Int = 0,
        @ProtoId(6) val ageLow: Int = 0,
        @ProtoId(7) val ageUp: Int = 0,
        @ProtoId(8) val profession: Int = 0,
        @ProtoId(9) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val msgDestination: AppointDefine.LocaleInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgHead: BusiReqHead? = null,
        @ProtoId(2) val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(3) val reqInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BusiRespHead(
        @ProtoId(1) val int32Version: Int = 1,
        @ProtoId(2) val int32Seq: Int = 0,
        @ProtoId(3) val int32ReplyCode: Int = 0,
        @ProtoId(4) val result: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UserProfile(
        @ProtoId(1) val int64Id: Long = 0L,
        @ProtoId(2) val int32IdType: Int = 0,
        @ProtoId(3) val url: String = "",
        @ProtoId(4) val int32PicType: Int = 0,
        @ProtoId(5) val int32SubPicType: Int = 0,
        @ProtoId(6) val title: String = "",
        @ProtoId(7) val content: String = "",
        @ProtoId(8) val content2: String = "",
        @ProtoId(9) val picUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class BusiReqHead(
        @ProtoId(1) val int32Version: Int = 1,
        @ProtoId(2) val int32Seq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgHead: BusiRespHead? = null,
        @ProtoId(2) val msgUserList: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb61 : ProtoBuf {
    @Serializable
    internal class GetAppinfoReq(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val appType: Int = 0,
        @ProtoId(3) val platform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetPkgUrlReq(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val appType: Int = 0,
        @ProtoId(3) val appVersion: Int = 0,
        @ProtoId(4) val platform: Int = 0,
        @ProtoId(5) val sysVersion: String = "",
        @ProtoId(6) val qqVersion: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val wording: String = "",
        @ProtoId(2) val nextReqDuration: Int = 0,
        @ProtoId(10) val getAppinfoRsp: GetAppinfoRsp? = null,
        @ProtoId(11) val getMqqappUrlRsp: GetPkgUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(10) val getAppinfoReq: GetAppinfoReq? = null,
        @ProtoId(11) val getMqqappUrlReq: GetPkgUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetAppinfoRsp(
        @ProtoId(1) val appinfo: Qqconnect.Appinfo? = null
    ) : ProtoBuf

    @Serializable
    internal class GetPkgUrlRsp(
        @ProtoId(1) val appVersion: Int = 0,
        @ProtoId(2) val pkgUrl: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb60 : ProtoBuf {
    @Serializable
    internal class GetPrivilegeReq(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val appType: Int = 3
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlReq(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val appType: Int = 0,
        @ProtoId(3) val url: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoId(1) val platform: Int = 0,
        @ProtoId(2) val sdkVersion: String = "",
        @ProtoId(3) val androidPackageName: String = "",
        @ProtoId(4) val androidSignature: String = "",
        @ProtoId(5) val iosBundleId: String = "",
        @ProtoId(6) val pcSign: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val wording: String = "",
        @ProtoId(10) val getPrivilegeRsp: GetPrivilegeRsp? = null,
        @ProtoId(11) val checkUrlRsp: CheckUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlRsp(
        @ProtoId(1) val isAuthed: Boolean = false,
        @ProtoId(2) val nextReqDuration: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val clientInfo: ClientInfo? = null,
        @ProtoId(10) val getPrivilegeReq: GetPrivilegeReq? = null,
        @ProtoId(11) val checkUrlReq: CheckUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetPrivilegeRsp(
        @ProtoId(1) val apiGroups: List<Int>? = null,
        @ProtoId(2) val nextReqDuration: Int = 0,
        @ProtoId(3) val apiNames: List<String> = listOf()
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fc : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val lastEventId: Long = 0L,
        @ProtoId(2) val readEventId: Long = 0L,
        @ProtoId(3) val fetchCount: Int = 0,
        @ProtoId(4) val lastNearbyEventId: Long = 0L,
        @ProtoId(5) val readNearbyEventId: Long = 0L,
        @ProtoId(6) val fetchNearbyEventCount: Int = 0,
        @ProtoId(7) val lastFeedEventId: Long = 0L,
        @ProtoId(8) val readFeedEventId: Long = 0L,
        @ProtoId(9) val fetchFeedEventCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgEventList: List<AppointDefine.DateEvent>? = null,
        @ProtoId(2) val actAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(3) val maxEventId: Long = 0L,
        @ProtoId(4) val errorTips: String = "",
        @ProtoId(5) val msgNearbyEventList: List<AppointDefine.NearbyEvent>? = null,
        @ProtoId(6) val msgFeedEventList: List<AppointDefine.FeedEvent>? = null,
        @ProtoId(7) val maxFreshEventId: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc33 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val nextGap: Int = 0,
        @ProtoId(3) val newUser: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody : ProtoBuf
}

@Serializable
internal class Oidb0xc0b : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val isOpenCoinEntry: Int = 0,
        @ProtoId(2) val canGetCoinCount: Int = 0,
        @ProtoId(3) val coinIconUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val lastCompletedTaskStamp: Long = 0L,
        @ProtoId(6) val cmsWording: List<KanDianCMSActivityInfo>? = null,
        @ProtoId(7) val lastCmsActivityStamp: Long = 0L,
        @ProtoId(8) val msgKandianCoinRemind: KanDianCoinRemind? = null,
        @ProtoId(9) val msgKandianTaskRemind: KanDianTaskRemind? = null
    ) : ProtoBuf

    @Serializable
    internal class KanDianCoinRemind(
        @ProtoId(1) val wording: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KanDianTaskRemind(
        @ProtoId(1) val wording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val taskType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class KanDianCMSActivityInfo(
        @ProtoId(1) val activityId: Long = 0L,
        @ProtoId(2) val wording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val pictureUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xc85 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(101) val fromUin: Long = 0L,
        @ProtoId(102) val toUin: Long = 0L,
        @ProtoId(103) val op: Int = 0,
        @ProtoId(104) val intervalDays: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class InteractionDetailInfo(
        @ProtoId(101) val continuousRecordDays: Int = 0,
        @ProtoId(102) val sendDayTime: Int = 0,
        @ProtoId(103) val recvDayTime: Int = 0,
        @ProtoId(104) val sendRecord: String = "",
        @ProtoId(105) val recvRecord: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(101) val result: Int = 0,
        @ProtoId(102) val recentInteractionTime: Int = 0,
        @ProtoId(103) val interactionDetailInfo: InteractionDetailInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7ce : ProtoBuf {
    @Serializable
    internal class AppintDetail(
        @ProtoId(1) val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) val msgAppointsInfo: AppointDefine.AppointInfo? = null,
        @ProtoId(3) val score: Int = 0,
        @ProtoId(4) val joinOver: Int = 0,
        @ProtoId(5) val joinNext: Int = 0,
        @ProtoId(6) val msgStrangerInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoId(7) val viewOver: Int = 0,
        @ProtoId(8) val viewNext: Int = 0,
        @ProtoId(9) val msgVistorInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoId(10) val meJoin: Int = 0,
        @ProtoId(12) val canProfile: Int = 0,
        @ProtoId(13) val profileErrmsg: String = "",
        @ProtoId(14) val canAio: Int = 0,
        @ProtoId(15) val aioErrmsg: String = "",
        @ProtoId(16) val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(17) val uin: Long = 0L,
        @ProtoId(18) val limited: Int = 0,
        @ProtoId(19) val msgCommentList: List<AppointDefine.DateComment>? = null,
        @ProtoId(20) val commentOver: Int = 0,
        @ProtoId(23) val meInvited: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgAppointsInfo: List<AppintDetail>? = null,
        @ProtoId(2) val secureFlag: Int = 0,
        @ProtoId(3) val secureTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val appointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(2) val joinStart: Int = 0,
        @ProtoId(3) val joinWant: Int = 0,
        @ProtoId(4) val viewStart: Int = 0,
        @ProtoId(5) val viewWant: Int = 0,
        @ProtoId(6) val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(7) val uint64Uins: List<Long>? = null,
        @ProtoId(8) val viewCommentCount: Int = 0,
        @ProtoId(100) val requestUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7db : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val wording: String = "",
        @ProtoId(2) val msgAppointInfo: AppointDefine.AppointInfo? = null,
        @ProtoId(3) val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val appointAction: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) val appointAction: Int = 0,
        @ProtoId(3) val overwrite: Int = 0,
        @ProtoId(4) val msgAppointIds: List<AppointDefine.AppointID>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc6c : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val msgGroupInfo: List<GroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) val groupUin: Long = 0L,
        @ProtoId(2) val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody : ProtoBuf
}

@Serializable
internal class Oidb0xc05 : ProtoBuf {
    @Serializable
    internal class GetAuthAppListReq(
        @ProtoId(1) val start: Int = 0,
        @ProtoId(2) val limit: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val wording: String = "",
        @ProtoId(10) val getCreateAppListRsp: GetCreateAppListRsp? = null,
        @ProtoId(11) val getAuthAppListRsp: GetAuthAppListRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class GetCreateAppListRsp(
        @ProtoId(1) val totalCount: Int = 0,
        @ProtoId(2) val appinfos: List<Qqconnect.Appinfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class GetAuthAppListRsp(
        @ProtoId(1) val totalCount: Int = 0,
        @ProtoId(2) val appinfos: List<Qqconnect.Appinfo>? = null,
        @ProtoId(3) val curIndex: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(10) val getCreateAppListReq: GetCreateAppListReq? = null,
        @ProtoId(11) val getAuthAppListReq: GetAuthAppListReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetCreateAppListReq(
        @ProtoId(1) val start: Int = 0,
        @ProtoId(2) val limit: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7da : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(2) val appointOperation: Int = 0,
        @ProtoId(3) val operationReason: Int = 0,
        @ProtoId(4) val overwrite: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val wording: String = "",
        @ProtoId(2) val msgAppointInfo: List<AppointDefine.AppointInfo>? = null,
        @ProtoId(3) val operationReason: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Qqconnect : ProtoBuf {
    @Serializable
    internal class MobileAppInfo(
        @ProtoId(11) val androidAppInfo: List<AndroidAppInfo>? = null,
        @ProtoId(12) val iosAppInfo: List<IOSAppInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class TemplateMsgConfig(
        @ProtoId(1) val serviceMsgUin: Long = 0L,
        @ProtoId(2) val publicMsgUin: Long = 0L,
        @ProtoId(3) val campMsgUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class Appinfo(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val appType: Int = 0,
        @ProtoId(3) val platform: Int = 0,
        @ProtoId(4) val appName: String = "",
        @ProtoId(5) val appKey: String = "",
        @ProtoId(6) val appState: Int = 0,
        @ProtoId(7) val iphoneUrlScheme: String = "",
        @ProtoId(8) val androidPackName: String = "",
        @ProtoId(9) val iconUrl: String = "",
        @ProtoId(10) val sourceUrl: String = "",
        @ProtoId(11) val iconSmallUrl: String = "",
        @ProtoId(12) val iconMiddleUrl: String = "",
        @ProtoId(13) val tencentDocsAppinfo: TencentDocsAppinfo? = null,
        @ProtoId(21) val developerUin: Long = 0L,
        @ProtoId(22) val appClass: Int = 0,
        @ProtoId(23) val appSubclass: Int = 0,
        @ProtoId(24) val remark: String = "",
        @ProtoId(25) val iconMiniUrl: String = "",
        @ProtoId(26) val authTime: Long = 0L,
        @ProtoId(27) val appUrl: String = "",
        @ProtoId(28) val universalLink: String = "",
        @ProtoId(29) val qqconnectFeature: Int = 0,
        @ProtoId(30) val isHatchery: Int = 0,
        @ProtoId(31) val testUinList: List<Long>? = null,
        @ProtoId(100) val templateMsgConfig: TemplateMsgConfig? = null,
        @ProtoId(101) val miniAppInfo: MiniAppInfo? = null,
        @ProtoId(102) val webAppInfo: WebAppInfo? = null,
        @ProtoId(103) val mobileAppInfo: MobileAppInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ConnectClientInfo(
        @ProtoId(1) val platform: Int = 0,
        @ProtoId(2) val sdkVersion: String = "",
        @ProtoId(3) val systemName: String = "",
        @ProtoId(4) val systemVersion: String = "",
        @ProtoId(21) val androidPackageName: String = "",
        @ProtoId(22) val androidSignature: String = "",
        @ProtoId(31) val iosBundleId: String = "",
        @ProtoId(32) val iosDeviceId: String = "",
        @ProtoId(33) val iosAppToken: String = "",
        @ProtoId(41) val pcSign: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TencentDocsAppinfo(
        @ProtoId(1) val openTypes: String = "",
        @ProtoId(2) val opts: String = "",
        @ProtoId(3) val ejs: String = "",
        @ProtoId(4) val callbackUrlTest: String = "",
        @ProtoId(5) val callbackUrl: String = "",
        @ProtoId(6) val domain: String = "",
        @ProtoId(7) val userinfoCallback: String = "",
        @ProtoId(8) val userinfoCallbackTest: String = ""
    ) : ProtoBuf

    @Serializable
    internal class WebAppInfo(
        @ProtoId(1) val websiteUrl: String = "",
        @ProtoId(2) val provider: String = "",
        @ProtoId(3) val icp: String = "",
        @ProtoId(4) val callbackUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class IOSAppInfo(
        @ProtoId(1) val bundleId: String = "",
        @ProtoId(2) val urlScheme: String = "",
        @ProtoId(3) val storeId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MsgUinInfo(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val msgType: Int = 0,
        @ProtoId(3) val appid: Int = 0,
        @ProtoId(4) val appType: Int = 0,
        @ProtoId(5) val ctime: Int = 0,
        @ProtoId(6) val mtime: Int = 0,
        @ProtoId(7) val mpType: Int = 0,
        @ProtoId(100) val nick: String = "",
        @ProtoId(101) val faceUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MiniAppInfo(
        @ProtoId(1) val superUin: Long = 0L,
        @ProtoId(11) val ownerType: Int = 0,
        @ProtoId(12) val ownerName: String = "",
        @ProtoId(13) val ownerIdCardType: Int = 0,
        @ProtoId(14) val ownerIdCard: String = "",
        @ProtoId(15) val ownerStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AndroidAppInfo(
        @ProtoId(1) val packName: String = "",
        @ProtoId(2) val packSign: String = "",
        @ProtoId(3) val apkDownUrl: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Sync : ProtoBuf {
    @Serializable
    internal class SyncAppointmentReq(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val msgAppointment: AppointDefine.AppointContent? = null,
        @ProtoId(3) val msgGpsInfo: AppointDefine.GPS? = null
    ) : ProtoBuf

    @Serializable
    internal class SyncAppointmentRsp(
        @ProtoId(1) val result: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc26 : ProtoBuf {
    @Serializable
    internal class RgoupLabel(
        @ProtoId(1) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val enumType: Int /* enum */ = 1,
        @ProtoId(3) val textColor: RgroupColor? = null,
        @ProtoId(4) val edgingColor: RgroupColor? = null,
        @ProtoId(5) val labelAttr: Int = 0,
        @ProtoId(6) val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AddFriendSource(
        @ProtoId(1) val source: Int = 0,
        @ProtoId(2) val subSource: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Label(
        @ProtoId(1) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val textColor: Color? = null,
        @ProtoId(3) val edgingColor: Color? = null,
        @ProtoId(4) val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class EntryDelay(
        @ProtoId(1) val emEntry: Int /* enum */ = 1,
        @ProtoId(2) val delay: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgPersons: List<MayKnowPerson>? = null,
        @ProtoId(2) val entryInuse: List<Int> = listOf(),
        @ProtoId(3) val entryClose: List<Int> = listOf(),
        @ProtoId(4) val nextGap: Int = 0,
        @ProtoId(5) val timestamp: Int = 0,
        @ProtoId(6) val msgUp: Int = 0,
        @ProtoId(7) val entryDelays: List<EntryDelay>? = null,
        @ProtoId(8) val listSwitch: Int = 0,
        @ProtoId(9) val addPageListSwitch: Int = 0,
        @ProtoId(10) val emRspDataType: Int /* enum */ = 1,
        @ProtoId(11) val msgRgroupItems: List<RecommendInfo>? = null,
        @ProtoId(12) val boolIsNewuser: Boolean = false,
        @ProtoId(13) val msgTables: List<TabInfo>? = null,
        @ProtoId(14) val cookies: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TabInfo(
        @ProtoId(1) val tabId: Int = 0,
        @ProtoId(2) val recommendCount: Int = 0,
        @ProtoId(3) val tableName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val iconUrlSelect: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val iconUrlUnselect: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val backgroundColorSelect: Color? = null,
        @ProtoId(7) val backgroundColorUnselect: Color? = null
    ) : ProtoBuf

    @Serializable
    internal class MayKnowPerson(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val msgIosSource: AddFriendSource? = null,
        @ProtoId(3) val msgAndroidSource: AddFriendSource? = null,
        @ProtoId(4) val reason: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val additive: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val remark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) val country: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val province: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val city: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val age: Int = 0,
        @ProtoId(12) val catelogue: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val alghrithm: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) val richbuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) val qzone: Int = 0,
        @ProtoId(16) val gender: Int = 0,
        @ProtoId(17) val mobileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(18) val token: String = "",
        @ProtoId(19) val onlineState: Int = 0,
        @ProtoId(20) val msgLabels: List<Label>? = null,
        @ProtoId(21) val sourceid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RecommendInfo(
        @ProtoId(1) val woring: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val msgGroups: List<RgroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RgroupInfo(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val ownerUin: Long = 0L,
        @ProtoId(3) val groupName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val groupMemo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val memberNum: Int = 0,
        @ProtoId(6) val groupLabel: List<RgoupLabel>? = null,
        @ProtoId(7) val groupFlagExt: Int = 0,
        @ProtoId(8) val groupFlag: Int = 0,
        @ProtoId(9) val source: Int /* enum */ = 1,
        @ProtoId(10) val tagWording: RgoupLabel? = null,
        @ProtoId(11) val algorithm: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) val joinGroupAuth: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) val activity: Int = 0,
        @ProtoId(14) val memberMaxNum: Int = 0,
        @ProtoId(15) val int32UinPrivilege: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val filterUins: List<Long>? = null,
        @ProtoId(2) val phoneBook: Int = 0,
        @ProtoId(3) val expectedUins: List<Long>? = null,
        @ProtoId(4) val emEntry: Int /* enum */ = 1,
        @ProtoId(5) val fetchRgroup: Int = 0,
        @ProtoId(6) val tabId: Int = 0,
        @ProtoId(7) val want: Int = 80,
        @ProtoId(8) val cookies: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RgroupColor(
        @ProtoId(1) val r: Int = 0,
        @ProtoId(2) val g: Int = 0,
        @ProtoId(3) val b: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Color(
        @ProtoId(1) val r: Int = 0,
        @ProtoId(2) val g: Int = 0,
        @ProtoId(3) val b: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xac6 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val results: List<OperateResult>? = null,
        @ProtoId(4) val metalCount: Int = 0,
        @ProtoId(5) val metalTotal: Int = 0,
        @ProtoId(9) val int32NewCount: Int = 0,
        @ProtoId(10) val int32UpgradeCount: Int = 0,
        @ProtoId(11) val promptParams: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val medals: List<MedalReport>? = null,
        @ProtoId(2) val clean: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalReport(
        @ProtoId(1) val id: Int = 0,
        @ProtoId(2) val level: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class OperateResult(
        @ProtoId(1) val id: Int = 0,
        @ProtoId(2) val int32Result: Int = 0,
        @ProtoId(3) val errmsg: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd32 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val openid: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class XmitInfo(
        @ProtoId(1) val signature: String = "",
        @ProtoId(2) val appid: String = "",
        @ProtoId(3) val groupid: String = "",
        @ProtoId(4) val nonce: String = "",
        @ProtoId(5) val timestamp: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cf : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val stamp: Int = 0,
        @ProtoId(2) val start: Int = 0,
        @ProtoId(3) val want: Int = 0,
        @ProtoId(4) val reqValidOnly: Int = 0,
        @ProtoId(5) val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(6) val appointOperation: Int = 0,
        @ProtoId(100) val requestUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val stamp: Int = 0,
        @ProtoId(2) val over: Int = 0,
        @ProtoId(3) val next: Int = 0,
        @ProtoId(4) val msgAppointsInfo: List<AppointDefine.AppointInfo>? = null,
        @ProtoId(5) val unreadCount: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xac7 : ProtoBuf {
    @Serializable
    internal class DeviceInfo(
        @ProtoId(1) val din: Long = 0L,
        @ProtoId(2) val name: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val extd: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val cmd: Int = 0,
        @ProtoId(2) val din: Long = 0L,
        @ProtoId(3) val extd: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val msgBinderSig: BinderSig? = null
    ) : ProtoBuf

    @Serializable
    internal class ReceiveMessageDevices(
        @ProtoId(1) val devices: List<DeviceInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class BinderSig(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val uin: Long = 0L,
        @ProtoId(3) val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fa : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgStrangerInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoId(2) val reachStart: Int = 0,
        @ProtoId(3) val reachEnd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val appointIds: AppointDefine.AppointID? = null,
        @ProtoId(2) val referIdx: Int = 0,
        @ProtoId(3) val getReferRec: Int = 0,
        @ProtoId(4) val reqNextCount: Int = 0,
        @ProtoId(5) val reqPrevCount: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class FavoriteCKVData : ProtoBuf {
    @Serializable
    internal class PicInfo(
        @ProtoId(1) val uri: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val sha1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val note: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val width: Int = 0,
        @ProtoId(7) val height: Int = 0,
        @ProtoId(8) val size: Int = 0,
        @ProtoId(9) val type: Int = 0,
        @ProtoId(10) val msgOwner: Author? = null,
        @ProtoId(11) val picId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KandianFavoriteItem(
        @ProtoId(1) val msgFavoriteExtInfo: KandianFavoriteBizData? = null,
        @ProtoId(2) val bytesCid: List<ByteArray>? = null,
        @ProtoId(3) val type: Int = 0,
        @ProtoId(4) val status: Int = 0,
        @ProtoId(5) val msgAuthor: Author? = null,
        @ProtoId(6) val createTime: Long = 0L,
        @ProtoId(7) val favoriteTime: Long = 0L,
        @ProtoId(8) val modifyTime: Long = 0L,
        @ProtoId(9) val dataSyncTime: Long = 0L,
        @ProtoId(10) val msgFavoriteSummary: FavoriteSummary? = null
    ) : ProtoBuf

    @Serializable
    internal class LinkSummary(
        @ProtoId(1) val uri: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val publisher: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val brief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val msgPicInfo: List<PicInfo>? = null,
        @ProtoId(6) val type: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val resourceUri: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class UserFavoriteList(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val modifyTs: Long = 0L,
        @ProtoId(100) val msgFavoriteItems: List<FavoriteItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class FavoriteSummary(
        @ProtoId(2) val msgLinkSummary: LinkSummary? = null
    ) : ProtoBuf

    @Serializable
    internal class FavoriteItem(
        @ProtoId(1) val favoriteSource: Int = 0,
        @ProtoId(100) val msgKandianFavoriteItem: KandianFavoriteItem? = null
    ) : ProtoBuf

    @Serializable
    internal class Author(
        @ProtoId(1) val type: Int = 0,
        @ProtoId(2) val numId: Long = 0L,
        @ProtoId(3) val strId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val groupId: Long = 0L,
        @ProtoId(5) val groupName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KandianFavoriteBizData(
        @ProtoId(1) val rowkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val type: Int = 0,
        @ProtoId(3) val videoDuration: Int = 0,
        @ProtoId(4) val picNum: Int = 0,
        @ProtoId(5) val accountId: Long = 0L,
        @ProtoId(6) val accountName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val videoType: Int = 0,
        @ProtoId(8) val feedsId: Long = 0L,
        @ProtoId(9) val feedsType: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5ff : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val errorTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) val commentId: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xccd : ProtoBuf {
    @Serializable
    internal class Result(
        @ProtoId(1) val appid: Int = 0,
        @ProtoId(2) val errcode: Int = 0,
        @ProtoId(3) val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val int64Uin: Long = 0L,
        @ProtoId(2) val appids: List<Int>? = null,
        @ProtoId(3) val platform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val errcode: Int = 0,
        @ProtoId(2) val results: List<Result>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc36 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uint64Uins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody : ProtoBuf
}

@Serializable
internal class Oidb0x87c : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val country: String = "",
        @ProtoId(2) val telephone: String = "",
        @ProtoId(3) val smsCode: String = "",
        @ProtoId(4) val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val enumButype: Int /* enum */ = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val country: String = "",
        @ProtoId(2) val telephone: String = "",
        @ProtoId(3) val keyType: Int = 0,
        @ProtoId(4) val key: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val guid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xbf2 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val phoneAddrBook: List<PhoneAddrBook>? = null,
        @ProtoId(2) val end: Int = 0,
        @ProtoId(3) val nextIndex: Long = 0
    ) : ProtoBuf

    @Serializable
    internal class PhoneAddrBook(
        @ProtoId(1) val phone: String = "",
        @ProtoId(2) val nick: String = "",
        @ProtoId(3) val headUrl: String = "",
        @ProtoId(4) val longNick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val startIndex: Long = 0L,
        @ProtoId(3) val num: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x6cd : ProtoBuf {
    @Serializable
    internal class RedpointInfo(
        @ProtoId(1) val taskid: Int = 0,
        @ProtoId(2) val curSeq: Long = 0L,
        @ProtoId(3) val pullSeq: Long = 0L,
        @ProtoId(4) val readSeq: Long = 0L,
        @ProtoId(5) val pullTimes: Int = 0,
        @ProtoId(6) val lastPullTime: Int = 0,
        @ProtoId(7) val int32RemainedTime: Int = 0,
        @ProtoId(8) val lastRecvTime: Int = 0,
        @ProtoId(9) val fromId: Long = 0L,
        @ProtoId(10) val enumRedpointType: Int /* enum */ = 1,
        @ProtoId(11) val msgRedpointExtraInfo: RepointExtraInfo? = null,
        @ProtoId(12) val configVersion: String = "",
        @ProtoId(13) val doActivity: Int = 0,
        @ProtoId(14) val msgUnreadMsg: List<MessageRec>? = null
    ) : ProtoBuf

    @Serializable
    internal class PullRedpointReq(
        @ProtoId(1) val taskid: Int = 0,
        @ProtoId(2) val lastPullSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgRedpoint: List<RedpointInfo>? = null,
        @ProtoId(2) val unfinishedRedpoint: List<PullRedpointReq>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val lastPullRedpoint: List<PullRedpointReq>? = null,
        @ProtoId(2) val unfinishedRedpoint: List<PullRedpointReq>? = null,
        @ProtoId(3) val msgPullSingleTask: PullRedpointReq? = null,
        @ProtoId(4) val retMsgRec: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MessageRec(
        @ProtoId(1) val seq: Long = 0L,
        @ProtoId(2) val time: Int = 0,
        @ProtoId(3) val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RepointExtraInfo(
        @ProtoId(1) val count: Int = 0,
        @ProtoId(2) val iconUrl: String = "",
        @ProtoId(3) val tips: String = "",
        @ProtoId(4) val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd55 : ProtoBuf {
    @Serializable
    internal class CheckUserRsp(
        @ProtoId(1) val openidUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CheckMiniAppRsp : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val appid: Long = 0L,
        @ProtoId(2) val appType: Int = 0,
        @ProtoId(3) val srcId: Int = 0,
        @ProtoId(4) val rawUrl: String = "",
        @ProtoId(11) val checkAppSignReq: CheckAppSignReq? = null,
        @ProtoId(12) val checkUserReq: CheckUserReq? = null,
        @ProtoId(13) val checkMiniAppReq: CheckMiniAppReq? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckAppSignReq(
        @ProtoId(1) val clientInfo: Qqconnect.ConnectClientInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val wording: String = "",
        @ProtoId(11) val checkAppSignRsp: CheckAppSignRsp? = null,
        @ProtoId(12) val checkUserRsp: CheckUserRsp? = null,
        @ProtoId(13) val checkMiniAppRsp: CheckMiniAppRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUserReq(
        @ProtoId(1) val openid: String = "",
        @ProtoId(2) val needCheckSameUser: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CheckMiniAppReq(
        @ProtoId(1) val miniAppAppid: Long = 0L,
        @ProtoId(2) val needCheckBind: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CheckAppSignRsp(
        @ProtoId(1) val iosAppToken: String = "",
        @ProtoId(2) val iosUniversalLink: String = "",
        @ProtoId(11) val optimizeSwitch: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x8b4 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val gc: Long = 0L,
        @ProtoId(2) val guin: Long = 0L,
        @ProtoId(3) val flag: Int = 0,
        @ProtoId(21) val dstUin: Long = 0L,
        @ProtoId(22) val start: Int = 0,
        @ProtoId(23) val cnt: Int = 0,
        @ProtoId(24) val tag: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) val gc: Long = 0L,
        @ProtoId(2) val groupName: String = "",
        @ProtoId(3) val faceUrl: String = "",
        @ProtoId(4) val setDisplayTime: Int = 0,
        // @SerialId(5) val groupLabel: List<GroupLabel.Label>? = null,
        @ProtoId(6) val textIntro: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val richIntro: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TagInfo(
        @ProtoId(1) val dstUin: Long = 0L,
        @ProtoId(2) val start: Int = 0,
        @ProtoId(3) val cnt: Int = 0,
        @ProtoId(4) val timestamp: Int = 0,
        @ProtoId(5) val _0x7ddSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val flag: Int = 0,
        @ProtoId(21) val tag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(22) val groupInfo: List<GroupInfo>? = null,
        @ProtoId(23) val textLabel: List<ByteArray>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x682 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgChatinfo: List<ChatInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class ChatInfo(
        @ProtoId(1) val touin: Long = 0L,
        @ProtoId(2) val chatflag: Int = 0,
        @ProtoId(3) val goldflag: Int = 0,
        @ProtoId(4) val totalexpcount: Int = 0,
        @ProtoId(5) val curexpcount: Int = 0,
        @ProtoId(6) val totalFlag: Int = 0,
        @ProtoId(7) val curdayFlag: Int = 0,
        @ProtoId(8) val expressTipsMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) val expressMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val uint64Touinlist: List<Long>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x6f5 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val qqVersion: String = "",
        @ProtoId(2) val qqPlatform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TaskInfo(
        @ProtoId(1) val taskId: Int = 0,
        @ProtoId(2) val appid: Int = 0,
        @ProtoId(3) val passthroughLevel: Int = 0,
        @ProtoId(4) val showLevel: Int = 0,
        @ProtoId(5) val extra: Int = 0,
        @ProtoId(6) val priority: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val configVersion: String = "",
        @ProtoId(2) val taskInfo: List<TaskInfo>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb7e : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val topItem: List<DiandianTopConfig>? = null
    ) : ProtoBuf

    @Serializable
    internal class DiandianTopConfig(
        @ProtoId(1) val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val subTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val subTitleColor: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val picUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val type: Int = 0,
        @ProtoId(7) val topicId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody : ProtoBuf
}

@Serializable
internal class Oidb0xc2f : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) val msgGetFollowUserRecommendListRsp: GetFollowUserRecommendListRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class GetFollowUserRecommendListReq(
        @ProtoId(1) val followedUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RecommendAccountInfo(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val accountType: Int = 0,
        @ProtoId(3) val nickName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val headImgUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val isVip: Int = 0,
        @ProtoId(6) val isStar: Int = 0,
        @ProtoId(7) val recommendReason: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GetFollowUserRecommendListRsp(
        @ProtoId(1) val msgRecommendList: List<RecommendAccountInfo>? = null,
        @ProtoId(2) val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgGetFollowUserRecommendListReq: GetFollowUserRecommendListReq? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7ca : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) val tinyid: Long = 0L,
        @ProtoId(3) val opType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val peerUin: Long = 0L,
        @ProtoId(3) val errorWording: String = "",
        @ProtoId(4) val opType: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xd40 : ProtoBuf {
    @Serializable
    internal class DeviceInfo(
        @ProtoId(1) val os: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val dev: DeviceInfo? = null,
        @ProtoId(2) val src: Int = 0,
        @ProtoId(3) val event: Int = 0,
        @ProtoId(4) val redtype: Int = 0
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
        @ProtoId(1) val taskid: Int = 0,
        @ProtoId(2) val readSeq: Long = 0L,
        @ProtoId(3) val appid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) val msgReadReq: List<ReadRedpointReq>? = null
    ) : ProtoBuf
}

