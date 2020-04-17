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
import kotlin.jvm.JvmField

@Serializable
internal class Oidb0x8a0 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoId(2) @JvmField val msgKickResult: List<KickResult>? = null
    ) : ProtoBuf

    @Serializable
    internal class KickResult(
        @ProtoId(1) @JvmField val optUint32Result: Int = 0,
        @ProtoId(2) @JvmField val optUint64MemberUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class KickMemberInfo(
        @ProtoId(1) @JvmField val optUint32Operate: Int = 0,
        @ProtoId(2) @JvmField val optUint64MemberUin: Long = 0L,
        @ProtoId(3) @JvmField val optUint32Flag: Int = 0,
        @ProtoId(4) @JvmField val optBytesMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val optUint64GroupCode: Long = 0L,
        @ProtoId(2) @JvmField val msgKickList: List<KickMemberInfo>? = null,
        @ProtoId(3) @JvmField val kickList: List<Long>? = null,
        @ProtoId(4) @JvmField val kickFlag: Int = 0,
        @ProtoId(5) @JvmField val kickMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}


@Serializable
internal class Oidb0x8fc : ProtoBuf {
    @Serializable
    internal class CardNameElem(
        @ProtoId(1) @JvmField val enumCardType: Int /* enum */ = 1,
        @ProtoId(2) @JvmField val value: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommCardNameBuf(
        @ProtoId(1) @JvmField val richCardName: List<RichCardNameElem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val showFlag: Int = 0,
        @ProtoId(3) @JvmField val memLevelInfo: List<MemberInfo>? = null,
        @ProtoId(4) @JvmField val levelName: List<LevelName>? = null,
        @ProtoId(5) @JvmField val updateTime: Int = 0,
        @ProtoId(6) @JvmField val officeMode: Int = 0,
        @ProtoId(7) @JvmField val groupOpenAppid: Int = 0,
        @ProtoId(8) @JvmField val msgClientInfo: ClientInfo? = null,
        @ProtoId(9) @JvmField val authKey: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MemberInfo(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val point: Int = 0,
        @ProtoId(3) @JvmField val activeDay: Int = 0,
        @ProtoId(4) @JvmField val level: Int = 0,
        @ProtoId(5) @JvmField val specialTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val specialTitleExpireTime: Int = 0,
        @ProtoId(7) @JvmField val uinName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val memberCardName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val phone: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val email: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val remark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val gender: Int = 0,
        @ProtoId(13) @JvmField val job: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) @JvmField val tribeLevel: Int = 0,
        @ProtoId(15) @JvmField val tribePoint: Int = 0,
        @ProtoId(16) @JvmField val richCardName: List<CardNameElem>? = null,
        @ProtoId(17) @JvmField val commRichCardName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RichCardNameElem(
        @ProtoId(1) @JvmField val ctrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val text: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val errInfo: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoId(1) @JvmField val implat: Int = 0,
        @ProtoId(2) @JvmField val ingClientver: String = ""
    ) : ProtoBuf

    @Serializable
    internal class LevelName(
        @ProtoId(1) @JvmField val level: Int = 0,
        @ProtoId(2) @JvmField val name: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x88d : ProtoBuf {
    @Serializable
    internal class GroupExInfoOnly(
        @ProtoId(1) @JvmField val tribeId: Int = 0,
        @ProtoId(2) @JvmField val moneyForAddGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqGroupInfo(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val stgroupinfo: GroupInfo? = null,
        @ProtoId(3) @JvmField val lastGetGroupNameTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspGroupInfo(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val result: Int = 0,
        @ProtoId(3) @JvmField val stgroupinfo: GroupInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class GroupGeoInfo(
        @ProtoId(1) @JvmField val owneruin: Long = 0L,
        @ProtoId(2) @JvmField val settime: Int = 0,
        @ProtoId(3) @JvmField val cityid: Int = 0,
        @ProtoId(4) @JvmField val int64Longitude: Long = 0L,
        @ProtoId(5) @JvmField val int64Latitude: Long = 0L,
        @ProtoId(6) @JvmField val geocontent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val poiId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class TagRecord(
        @ProtoId(1) @JvmField val fromUin: Long = 0L,
        @ProtoId(2) @JvmField val groupCode: Long = 0L,
        @ProtoId(3) @JvmField val tagId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val setTime: Long = 0L,
        @ProtoId(5) @JvmField val goodNum: Int = 0,
        @ProtoId(6) @JvmField val badNum: Int = 0,
        @ProtoId(7) @JvmField val tagLen: Int = 0,
        @ProtoId(8) @JvmField val tagValue: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) @JvmField val groupOwner: Long? = null,
        @ProtoId(2) @JvmField val groupCreateTime: Int? = null,
        @ProtoId(3) @JvmField val groupFlag: Int? = null,
        @ProtoId(4) @JvmField val groupFlagExt: Int? = null,
        @ProtoId(5) @JvmField val groupMemberMaxNum: Int? = null,
        @ProtoId(6) @JvmField val groupMemberNum: Int? = null,
        @ProtoId(7) @JvmField val groupOption: Int? = null,
        @ProtoId(8) @JvmField val groupClassExt: Int? = null,
        @ProtoId(9) @JvmField val groupSpecialClass: Int? = null,
        @ProtoId(10) @JvmField val groupLevel: Int? = null,
        @ProtoId(11) @JvmField val groupFace: Int? = null,
        @ProtoId(12) @JvmField val groupDefaultPage: Int? = null,
        @ProtoId(13) @JvmField val groupInfoSeq: Int? = null,
        @ProtoId(14) @JvmField val groupRoamingTime: Int? = null,
        @ProtoId(15) var groupName: String? = null,
        @ProtoId(16) var groupMemo: String? = null,
        @ProtoId(17) @JvmField val ingGroupFingerMemo: String? = null,
        @ProtoId(18) @JvmField val ingGroupClassText: String? = null,
        @ProtoId(19) @JvmField val groupAllianceCode: List<Int>? = null,
        @ProtoId(20) @JvmField val groupExtraAdmNum: Int? = null,
        @ProtoId(21) var groupUin: Long? = null,
        @ProtoId(22) @JvmField val groupCurMsgSeq: Int? = null,
        @ProtoId(23) @JvmField val groupLastMsgTime: Int? = null,
        @ProtoId(24) @JvmField val ingGroupQuestion: String? = null,
        @ProtoId(25) @JvmField val ingGroupAnswer: String? = null,
        @ProtoId(26) @JvmField val groupVisitorMaxNum: Int? = null,
        @ProtoId(27) @JvmField val groupVisitorCurNum: Int? = null,
        @ProtoId(28) @JvmField val levelNameSeq: Int? = null,
        @ProtoId(29) @JvmField val groupAdminMaxNum: Int? = null,
        @ProtoId(30) @JvmField val groupAioSkinTimestamp: Int? = null,
        @ProtoId(31) @JvmField val groupBoardSkinTimestamp: Int? = null,
        @ProtoId(32) @JvmField val ingGroupAioSkinUrl: String? = null,
        @ProtoId(33) @JvmField val ingGroupBoardSkinUrl: String? = null,
        @ProtoId(34) @JvmField val groupCoverSkinTimestamp: Int? = null,
        @ProtoId(35) @JvmField val ingGroupCoverSkinUrl: String? = null,
        @ProtoId(36) @JvmField val groupGrade: Int? = null,
        @ProtoId(37) @JvmField val activeMemberNum: Int? = null,
        @ProtoId(38) @JvmField val certificationType: Int? = null,
        @ProtoId(39) @JvmField val ingCertificationText: String? = null,
        @ProtoId(40) @JvmField val ingGroupRichFingerMemo: String? = null,
        @ProtoId(41) @JvmField val tagRecord: List<TagRecord>? = null,
        @ProtoId(42) @JvmField val groupGeoInfo: GroupGeoInfo? = null,
        @ProtoId(43) @JvmField val headPortraitSeq: Int? = null,
        @ProtoId(44) @JvmField val msgHeadPortrait: GroupHeadPortrait? = null,
        @ProtoId(45) @JvmField val shutupTimestamp: Int? = null,
        @ProtoId(46) @JvmField val shutupTimestampMe: Int? = null,
        @ProtoId(47) @JvmField val createSourceFlag: Int? = null,
        @ProtoId(48) @JvmField val cmduinMsgSeq: Int? = null,
        @ProtoId(49) @JvmField val cmduinJoinTime: Int? = null,
        @ProtoId(50) @JvmField val cmduinUinFlag: Int? = null,
        @ProtoId(51) @JvmField val cmduinFlagEx: Int? = null,
        @ProtoId(52) @JvmField val cmduinNewMobileFlag: Int? = null,
        @ProtoId(53) @JvmField val cmduinReadMsgSeq: Int? = null,
        @ProtoId(54) @JvmField val cmduinLastMsgTime: Int? = null,
        @ProtoId(55) @JvmField val groupTypeFlag: Int? = null,
        @ProtoId(56) @JvmField val appPrivilegeFlag: Int? = null,
        @ProtoId(57) @JvmField val stGroupExInfo: GroupExInfoOnly? = null,
        @ProtoId(58) @JvmField val groupSecLevel: Int? = null,
        @ProtoId(59) @JvmField val groupSecLevelInfo: Int? = null,
        @ProtoId(60) @JvmField val cmduinPrivilege: Int? = null,
        @ProtoId(61) @JvmField val ingPoidInfo: ByteArray? = null,
        @ProtoId(62) @JvmField val cmduinFlagEx2: Int? = null,
        @ProtoId(63) @JvmField val confUin: Long? = null,
        @ProtoId(64) @JvmField val confMaxMsgSeq: Int? = null,
        @ProtoId(65) @JvmField val confToGroupTime: Int? = null,
        @ProtoId(66) @JvmField val passwordRedbagTime: Int? = null,
        @ProtoId(67) @JvmField val subscriptionUin: Long? = null,
        @ProtoId(68) @JvmField val memberListChangeSeq: Int? = null,
        @ProtoId(69) @JvmField val membercardSeq: Int? = null,
        @ProtoId(70) @JvmField val rootId: Long? = null,
        @ProtoId(71) @JvmField val parentId: Long? = null,
        @ProtoId(72) @JvmField val teamSeq: Int? = null,
        @ProtoId(73) @JvmField val historyMsgBeginTime: Long? = null,
        @ProtoId(74) @JvmField val inviteNoAuthNumLimit: Long? = null,
        @ProtoId(75) @JvmField val cmduinHistoryMsgSeq: Int? = null,
        @ProtoId(76) @JvmField val cmduinJoinMsgSeq: Int? = null,
        @ProtoId(77) @JvmField val groupFlagext3: Int? = null,
        @ProtoId(78) @JvmField val groupOpenAppid: Int? = null,
        @ProtoId(79) @JvmField val isConfGroup: Int? = null,
        @ProtoId(80) @JvmField val isModifyConfGroupFace: Int? = null,
        @ProtoId(81) @JvmField val isModifyConfGroupName: Int? = null,
        @ProtoId(82) @JvmField val noFingerOpenFlag: Int? = null,
        @ProtoId(83) @JvmField val noCodeFingerOpenFlag: Int? = null,
        @ProtoId(84) @JvmField val autoAgreeJoinGroupUserNumForNormalGroup: Int? = null,
        @ProtoId(85) @JvmField val autoAgreeJoinGroupUserNumForConfGroup: Int? = null,
        @ProtoId(86) @JvmField val isAllowConfGroupMemberNick: Int? = null,
        @ProtoId(87) @JvmField val isAllowConfGroupMemberAtAll: Int? = null,
        @ProtoId(88) @JvmField val isAllowConfGroupMemberModifyGroupName: Int? = null,
        @ProtoId(89) @JvmField val longGroupName: String? = null,
        @ProtoId(90) @JvmField val cmduinJoinRealMsgSeq: Int? = null,
        @ProtoId(91) @JvmField val isGroupFreeze: Int? = null,
        @ProtoId(92) @JvmField val msgLimitFrequency: Int? = null,
        @ProtoId(93) @JvmField val joinGroupAuth: ByteArray? = null,
        @ProtoId(94) @JvmField val hlGuildAppid: Int? = null,
        @ProtoId(95) @JvmField val hlGuildSubType: Int? = null,
        @ProtoId(96) @JvmField val hlGuildOrgid: Int? = null,
        @ProtoId(97) @JvmField val isAllowHlGuildBinary: Int? = null,
        @ProtoId(98) @JvmField val cmduinRingtoneId: Int? = null,
        @ProtoId(99) @JvmField val groupFlagext4: Int? = null,
        @ProtoId(100) @JvmField val groupFreezeReason: Int? = null,
        @ProtoId(101) var groupCode: Long? = null // mirai 添加
    ) : ProtoBuf

    @Serializable
    internal class GroupHeadPortraitInfo(
        @ProtoId(1) @JvmField val uint32PicId: Int = 0,
        @ProtoId(2) @JvmField val leftX: Int = 0,
        @ProtoId(3) @JvmField val leftY: Int = 0,
        @ProtoId(4) @JvmField val rightX: Int = 0,
        @ProtoId(5) @JvmField val rightY: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val stzrspgroupinfo: List<RspGroupInfo>? = null,
        @ProtoId(2) @JvmField val errorinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val stzreqgroupinfo: List<ReqGroupInfo>? = null,
        @ProtoId(3) @JvmField val pcClientVersion: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupHeadPortrait(
        @ProtoId(1) @JvmField val picCnt: Int = 0,
        @ProtoId(2) @JvmField val msgInfo: List<GroupHeadPortraitInfo>? = null,
        @ProtoId(3) @JvmField val defaultId: Int = 0,
        @ProtoId(4) @JvmField val verifyingPicCnt: Int = 0,
        @ProtoId(5) @JvmField val msgVerifyingpicInfo: List<GroupHeadPortraitInfo>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x89a : ProtoBuf {
    @Serializable
    internal class GroupNewGuidelinesInfo(
        @ProtoId(1) @JvmField val boolEnabled: Boolean = false,
        @ProtoId(2) @JvmField val ingContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Groupinfo(
        @ProtoId(1) @JvmField val groupExtAdmNum: Int? = null,
        @ProtoId(2) @JvmField val flag: Int? = null,
        @ProtoId(3) @JvmField val ingGroupName: ByteArray? = null,
        @ProtoId(4) @JvmField val ingGroupMemo: ByteArray? = null,
        @ProtoId(5) @JvmField val ingGroupFingerMemo: ByteArray? = null,
        @ProtoId(6) @JvmField val ingGroupAioSkinUrl: ByteArray? = null,
        @ProtoId(7) @JvmField val ingGroupBoardSkinUrl: ByteArray? = null,
        @ProtoId(8) @JvmField val ingGroupCoverSkinUrl: ByteArray? = null,
        @ProtoId(9) @JvmField val groupGrade: Int? = null,
        @ProtoId(10) @JvmField val activeMemberNum: Int? = null,
        @ProtoId(11) @JvmField val certificationType: Int? = null,
        @ProtoId(12) @JvmField val ingCertificationText: ByteArray? = null,
        @ProtoId(13) @JvmField val ingGroupRichFingerMemo: ByteArray? = null,
        @ProtoId(14) @JvmField val stGroupNewguidelines: GroupNewGuidelinesInfo? = null,
        @ProtoId(15) @JvmField val groupFace: Int? = null,
        @ProtoId(16) @JvmField val addOption: Int? = null,
        @ProtoId(17) @JvmField val shutupTime: Int? = null,
        @ProtoId(18) @JvmField val groupTypeFlag: Int? = null,
        @ProtoId(19) @JvmField val stringGroupTag: List<ByteArray>? = null,
        @ProtoId(20) @JvmField val msgGroupGeoInfo: GroupGeoInfo? = null,
        @ProtoId(21) @JvmField val groupClassExt: Int? = null,
        @ProtoId(22) @JvmField val ingGroupClassText: ByteArray? = null,
        @ProtoId(23) @JvmField val appPrivilegeFlag: Int? = null,
        @ProtoId(24) @JvmField val appPrivilegeMask: Int? = null,
        @ProtoId(25) @JvmField val stGroupExInfo: GroupExInfoOnly? = null,
        @ProtoId(26) @JvmField val groupSecLevel: Int? = null,
        @ProtoId(27) @JvmField val groupSecLevelInfo: Int? = null,
        @ProtoId(28) @JvmField val subscriptionUin: Long? = null,
        @ProtoId(29) @JvmField val allowMemberInvite: Int? = null,
        @ProtoId(30) @JvmField val ingGroupQuestion: ByteArray? = null,
        @ProtoId(31) @JvmField val ingGroupAnswer: ByteArray? = null,
        @ProtoId(32) @JvmField val groupFlagext3: Int? = null,
        @ProtoId(33) @JvmField val groupFlagext3Mask: Int? = null,
        @ProtoId(34) @JvmField val groupOpenAppid: Int? = null,
        @ProtoId(35) @JvmField val noFingerOpenFlag: Int? = null,
        @ProtoId(36) @JvmField val noCodeFingerOpenFlag: Int? = null,
        @ProtoId(37) @JvmField val rootId: Long? = null,
        @ProtoId(38) @JvmField val msgLimitFrequency: Int? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val errorinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupExInfoOnly(
        @ProtoId(1) @JvmField val tribeId: Int = 0,
        @ProtoId(2) @JvmField val moneyForAddGroup: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GroupGeoInfo(
        @ProtoId(1) @JvmField val cityId: Int = 0,
        @ProtoId(2) @JvmField val longtitude: Long = 0L,
        @ProtoId(3) @JvmField val latitude: Long = 0L,
        @ProtoId(4) @JvmField val ingGeoContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val poiId: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val stGroupInfo: Groupinfo? = null,
        @ProtoId(3) @JvmField val originalOperatorUin: Long = 0L,
        @ProtoId(4) @JvmField val reqGroupOpenAppid: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cb : ProtoBuf {
    @Serializable
    internal class ConfigItem(
        @ProtoId(1) @JvmField val id: Int = 0,
        @ProtoId(2) @JvmField val config: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val timeStamp: Int = 0,
        @ProtoId(2) @JvmField val timeGap: Int = 0,
        @ProtoId(3) @JvmField val commentConfigs: List<CommentConfig>? = null,
        @ProtoId(4) @JvmField val attendTipsToA: String = "",
        @ProtoId(5) @JvmField val firstMsgTips: String = "",
        @ProtoId(6) @JvmField val cancleConfig: List<ConfigItem>? = null,
        @ProtoId(7) @JvmField val msgDateRequest: DateRequest? = null,
        @ProtoId(8) @JvmField val msgHotLocale: List<ByteArray>? = null,//List<AppointDefine.LocaleInfo>
        @ProtoId(9) @JvmField val msgTopicList: List<TopicConfig>? = null,
        @ProtoId(10) @JvmField val travelMsgTips: String = "",
        @ProtoId(11) @JvmField val travelProfileTips: String = "",
        @ProtoId(12) @JvmField val travelAttenTips: String = "",
        @ProtoId(13) @JvmField val topicDefault: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommentConfig(
        @ProtoId(1) @JvmField val appointSubject: Int = 0,
        @ProtoId(2) @JvmField val msgConfigs: List<ConfigItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val timeStamp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DateRequest(
        @ProtoId(1) @JvmField val time: Int = 0,
        @ProtoId(2) @JvmField val errMsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TopicConfig(
        @ProtoId(1) @JvmField val topicId: Int = 0,
        @ProtoId(2) @JvmField val topicName: String = "",
        @ProtoId(3) @JvmField val deadline: Int = 0,
        @ProtoId(4) @JvmField val errDeadline: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x87a : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val country: String = "",
        @ProtoId(2) @JvmField val telephone: String = "",
        @ProtoId(3) @JvmField val resendInterval: Int = 0,
        @ProtoId(4) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val country: String = "",
        @ProtoId(2) @JvmField val telephone: String = "",
        @ProtoId(3) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val enumButype: Int /* enum */ = 0
    ) : ProtoBuf
}

@Serializable
internal class GroupAppPb : ProtoBuf {
    @Serializable
    internal class ClientInfo(
        @ProtoId(1) @JvmField val platform: Int = 0,
        @ProtoId(2) @JvmField val version: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val fullList: AppList? = null,
        @ProtoId(2) @JvmField val groupGrayList: AppList? = null,
        @ProtoId(3) @JvmField val redPointList: AppList? = null,
        @ProtoId(4) @JvmField val cacheInterval: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AppList(
        @ProtoId(1) @JvmField val hash: String = "",
        @ProtoId(2) @JvmField val infos: List<AppInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class AppInfo(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val icon: String = "",
        @ProtoId(3) @JvmField val name: String = "",
        @ProtoId(4) @JvmField val url: String = "",
        @ProtoId(5) @JvmField val isGray: Int = 0,
        @ProtoId(6) @JvmField val iconSimpleDay: String = "",
        @ProtoId(7) @JvmField val iconSimpleNight: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val client: ClientInfo? = null,
        @ProtoId(2) @JvmField val groupId: Long = 0L,
        @ProtoId(3) @JvmField val groupType: Int = 0,
        @ProtoId(4) @JvmField val fullListHash: String = "",
        @ProtoId(5) @JvmField val groupGrayListHash: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc34 : ProtoBuf {
    @Serializable
    internal class RspBody : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fd : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgComment: AppointDefine.DateComment? = null,
        @ProtoId(2) @JvmField val maxFetchCount: Int = 0,
        @ProtoId(3) @JvmField val lastCommentId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgComment: List<AppointDefine.DateComment>? = null,
        @ProtoId(2) @JvmField val errorTips: String = "",
        @ProtoId(3) @JvmField val clearCacheFlag: Int = 0,
        @ProtoId(4) @JvmField val commentWording: String = "",
        @ProtoId(5) @JvmField val commentNum: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbcb : ProtoBuf {
    @Serializable
    internal class CheckUrlReqItem(
        @ProtoId(1) @JvmField val url: String = "",
        @ProtoId(2) @JvmField val refer: String = "",
        @ProtoId(3) @JvmField val plateform: String = "",
        @ProtoId(4) @JvmField val qqPfTo: String = "",
        @ProtoId(5) @JvmField val msgType: Int = 0,
        @ProtoId(6) @JvmField val msgFrom: Int = 0,
        @ProtoId(7) @JvmField val msgChatid: Long = 0L,
        @ProtoId(8) @JvmField val serviceType: Long = 0L,
        @ProtoId(9) @JvmField val sendUin: Long = 0L,
        @ProtoId(10) @JvmField val reqType: String = ""
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlRsp(
        @ProtoId(1) @JvmField val results: List<UrlCheckResult>? = null,
        @ProtoId(2) @JvmField val nextReqDuration: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(9) @JvmField val notUseCache: Int = 0,
        @ProtoId(10) @JvmField val checkUrlReq: CheckUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val wording: String = "",
        @ProtoId(10) @JvmField val checkUrlRsp: CheckUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlReq(
        @ProtoId(1) @JvmField val url: List<String> = listOf(),
        @ProtoId(2) @JvmField val refer: String = "",
        @ProtoId(3) @JvmField val plateform: String = "",
        @ProtoId(4) @JvmField val qqPfTo: String = "",
        @ProtoId(5) @JvmField val msgType: Int = 0,
        @ProtoId(6) @JvmField val msgFrom: Int = 0,
        @ProtoId(7) @JvmField val msgChatid: Long = 0L,
        @ProtoId(8) @JvmField val serviceType: Long = 0L,
        @ProtoId(9) @JvmField val sendUin: Long = 0L,
        @ProtoId(10) @JvmField val reqType: String = "",
        @ProtoId(11) @JvmField val originalUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UrlCheckResult(
        @ProtoId(1) @JvmField val url: String = "",
        @ProtoId(2) @JvmField val result: Int = 0,
        @ProtoId(3) @JvmField val jumpResult: Int = 0,
        @ProtoId(4) @JvmField val jumpUrl: String = "",
        @ProtoId(5) @JvmField val level: Int = 0,
        @ProtoId(6) @JvmField val subLevel: Int = 0,
        @ProtoId(7) @JvmField val umrtype: Int = 0,
        @ProtoId(8) @JvmField val retFrom: Int = 0,
        @ProtoId(9) @JvmField val operationBit: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbfe : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val receiveStatus: Int = 0,
        @ProtoId(2) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val flag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xbe8 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val enumOpCode: Int /* enum */ = 1,
        @ProtoId(3) @JvmField val rspOfPopupFlag: Int = 0,
        @ProtoId(4) @JvmField val popupCountNow: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PopupResult(
        @ProtoId(1) @JvmField val popupResult: Int = 0,
        @ProtoId(2) @JvmField val popupFieldid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val enumOpCode: Int /* enum */ = 1,
        @ProtoId(3) @JvmField val reqOfPopupFlag: Int = 0,
        @ProtoId(4) @JvmField val rstOfPopupFlag: Int = 0,
        @ProtoId(5) @JvmField val mqq808WelcomepageFlag: Int = 0,
        @ProtoId(6) @JvmField val msgPopupResult: List<PopupResult>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7de : ProtoBuf {
    @Serializable
    internal class UserProfile(
        @ProtoId(1) @JvmField val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) @JvmField val msgAppointsInfo: AppointDefine.AppointInfo? = null,
        @ProtoId(3) @JvmField val msgVistorInfo: List<AppointDefine.StrangerInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgHead: BusiRespHead? = null,
        @ProtoId(2) @JvmField val msgUserList: List<UserProfile>? = null,
        @ProtoId(3) @JvmField val ended: Int = 0,
        @ProtoId(4) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BusiRespHead(
        @ProtoId(1) @JvmField val int32Version: Int = 1,
        @ProtoId(2) @JvmField val int32Seq: Int = 0,
        @ProtoId(3) @JvmField val int32ReplyCode: Int = 0,
        @ProtoId(4) @JvmField val result: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgHead: BusiReqHead? = null,
        @ProtoId(2) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(3) @JvmField val time: Int = 0,
        @ProtoId(4) @JvmField val subject: Int = 0,
        @ProtoId(5) @JvmField val gender: Int = 0,
        @ProtoId(6) @JvmField val ageLow: Int = 0,
        @ProtoId(7) @JvmField val ageUp: Int = 0,
        @ProtoId(8) @JvmField val profession: Int = 0,
        @ProtoId(9) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val msgDestination: AppointDefine.LocaleInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class BusiReqHead(
        @ProtoId(1) @JvmField val int32Version: Int = 1,
        @ProtoId(2) @JvmField val int32Seq: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7a8 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val reqUin: Long = 0L,
        @ProtoId(11) @JvmField val onlyObtained: Int = 0,
        @ProtoId(12) @JvmField val readReport: Int = 0,
        @ProtoId(13) @JvmField val sortType: Int = 0,
        @ProtoId(14) @JvmField val onlyNew: Int = 0,
        @ProtoId(15) @JvmField val filterMedalIds: List<Int>? = null,
        @ProtoId(16) @JvmField val onlySummary: Int = 0,
        @ProtoId(17) @JvmField val doScan: Int = 0,
        @ProtoId(18) @JvmField val startTimestamp: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val nick: String = "",
        @ProtoId(2) @JvmField val metalRank: Int = 0,
        @ProtoId(3) @JvmField val friCount: Int = 0,
        @ProtoId(4) @JvmField val metalCount: Int = 0,
        @ProtoId(5) @JvmField val metalTotal: Int = 0,
        @ProtoId(6) @JvmField val msgMedal: List<Common.MedalInfo>? = null,
        @ProtoId(8) @JvmField val totalPoint: Int = 0,
        @ProtoId(9) @JvmField val int32NewCount: Int = 0,
        @ProtoId(10) @JvmField val int32UpgradeCount: Int = 0,
        @ProtoId(11) @JvmField val promptParams: String = "",
        @ProtoId(12) @JvmField val now: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalNews(
        @ProtoId(1) @JvmField val friUin: Long = 0L,
        @ProtoId(2) @JvmField val friNick: String = "",
        @ProtoId(3) @JvmField val msgMedal: Common.MedalInfo? = null
    ) : ProtoBuf
}


@Serializable
internal class Cmd0x5fe : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) @JvmField val commentId: String = "",
        @ProtoId(3) @JvmField val fetchOldCount: Int = 0,
        @ProtoId(4) @JvmField val fetchNewCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgComment: List<AppointDefine.DateComment>? = null,
        @ProtoId(2) @JvmField val errorTips: String = "",
        @ProtoId(3) @JvmField val fetchOldOver: Int = 0,
        @ProtoId(4) @JvmField val fetchNewOver: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc35 : ProtoBuf {
    @Serializable
    internal class RspBody : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val msgExposeInfo: List<ExposeItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class ExposeItem(
        @ProtoId(1) @JvmField val friend: Long = 0L,
        @ProtoId(2) @JvmField val pageId: Int = 0,
        @ProtoId(3) @JvmField val entranceId: Int = 0,
        @ProtoId(4) @JvmField val actionId: Int = 0,
        @ProtoId(5) @JvmField val exposeCount: Int = 0,
        @ProtoId(6) @JvmField val exposeTime: Int = 0,
        @ProtoId(7) @JvmField val algoBuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val addition: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc0d : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val completedTaskStamp: Long = 0L,
        @ProtoId(2) @JvmField val errMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val taskType: Int = 0,
        @ProtoId(3) @JvmField val taskPoint: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class OidbSso : ProtoBuf {
    @Serializable
    internal class OIDBSSOPkg(
        @ProtoId(1) @JvmField val command: Int = 0,
        @ProtoId(2) @JvmField val serviceType: Int = 0,
        @ProtoId(3) @JvmField val result: Int = 0,
        @ProtoId(4) @JvmField val bodybuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val errorMsg: String = "",
        @ProtoId(6) @JvmField val clientVersion: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xc83 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(101) @JvmField val fromUin: Long = 0L,
        @ProtoId(102) @JvmField val toUin: Long = 0L,
        @ProtoId(103) @JvmField val op: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(101) @JvmField val result: Int = 0,
        @ProtoId(102) @JvmField val retryInterval: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xccb : ProtoBuf {
    @Serializable
    internal class GroupMsgInfo(
        @ProtoId(1) @JvmField val msgSeq: Int = 0,
        @ProtoId(2) @JvmField val roamFlag: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val type: Int = 0,
        @ProtoId(2) @JvmField val destUin: Long = 0L,
        @ProtoId(3) @JvmField val groupCode: Long = 0L,
        @ProtoId(4) @JvmField val c2cMsg: List<C2cMsgInfo>? = null,
        @ProtoId(5) @JvmField val groupMsg: List<GroupMsgInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val type: Int = 0,
        @ProtoId(2) @JvmField val destUin: Long = 0L,
        @ProtoId(3) @JvmField val groupCode: Long = 0L,
        @ProtoId(4) @JvmField val c2cMsg: List<C2cMsgInfo>? = null,
        @ProtoId(5) @JvmField val groupMsg: List<GroupMsgInfo>? = null,
        @ProtoId(6) @JvmField val resId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class C2cMsgInfo(
        @ProtoId(1) @JvmField val msgSeq: Int = 0,
        @ProtoId(2) @JvmField val msgTime: Int = 0,
        @ProtoId(3) @JvmField val msgRandom: Int = 0,
        @ProtoId(4) @JvmField val roamFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd84 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0x5e1 : ProtoBuf {
    @Serializable
    internal class UdcUinData(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(4) @JvmField val openid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20002) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20003) @JvmField val country: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20004) @JvmField val province: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20009) @JvmField val gender: Int = 0,
        @ProtoId(20014) @JvmField val allow: Int = 0,
        @ProtoId(20015) @JvmField val faceId: Int = 0,
        @ProtoId(20020) @JvmField val city: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20027) @JvmField val commonPlace1: Int = 0,
        @ProtoId(20030) @JvmField val mss3Bitmapextra: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20031) @JvmField val birthday: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20032) @JvmField val cityId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(20033) @JvmField val lang1: Int = 0,
        @ProtoId(20034) @JvmField val lang2: Int = 0,
        @ProtoId(20035) @JvmField val lang3: Int = 0,
        @ProtoId(20041) @JvmField val cityZoneId: Int = 0,
        @ProtoId(20056) @JvmField val oin: Int = 0,
        @ProtoId(20059) @JvmField val bubbleId: Int = 0,
        @ProtoId(21001) @JvmField val mss2Identity: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21002) @JvmField val mss1Service: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21003) @JvmField val lflag: Int = 0,
        @ProtoId(21004) @JvmField val extFlag: Int = 0,
        @ProtoId(21006) @JvmField val basicSvrFlag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(21007) @JvmField val basicCliFlag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(24101) @JvmField val pengyouRealname: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(24103) @JvmField val pengyouGender: Int = 0,
        @ProtoId(24118) @JvmField val pengyouFlag: Int = 0,
        @ProtoId(26004) @JvmField val fullBirthday: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(26005) @JvmField val fullAge: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(26010) @JvmField val simpleUpdateTime: Int = 0,
        @ProtoId(26011) @JvmField val mssUpdateTime: Int = 0,
        @ProtoId(27022) @JvmField val groupMemCreditFlag: Int = 0,
        @ProtoId(27025) @JvmField val faceAddonId: Long = 0L,
        @ProtoId(27026) @JvmField val musicGene: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(40323) @JvmField val fileShareBit: Int = 0,
        @ProtoId(40404) @JvmField val recommendPrivacyCtrl: Int = 0,
        @ProtoId(40505) @JvmField val oldFriendChat: Int = 0,
        @ProtoId(40602) @JvmField val businessBit: Int = 0,
        @ProtoId(41305) @JvmField val crmBit: Int = 0,
        @ProtoId(41810) @JvmField val forbidFileshareBit: Int = 0,
        @ProtoId(42333) @JvmField val userLoginGuardFace: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(11) @JvmField val msgUinData: List<UdcUinData>? = null,
        @ProtoId(12) @JvmField val uint64UnfinishedUins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uint64Uins: List<Long>? = null,
        @ProtoId(2) @JvmField val startTime: Int = 0,
        @ProtoId(3) @JvmField val maxPackageSize: Int = 0,
        @ProtoId(4) @JvmField val bytesOpenid: List<ByteArray>? = null,
        @ProtoId(5) @JvmField val appid: Int = 0,
        @ProtoId(20002) @JvmField val reqNick: Int = 0,
        @ProtoId(20003) @JvmField val reqCountry: Int = 0,
        @ProtoId(20004) @JvmField val reqProvince: Int = 0,
        @ProtoId(20009) @JvmField val reqGender: Int = 0,
        @ProtoId(20014) @JvmField val reqAllow: Int = 0,
        @ProtoId(20015) @JvmField val reqFaceId: Int = 0,
        @ProtoId(20020) @JvmField val reqCity: Int = 0,
        @ProtoId(20027) @JvmField val reqCommonPlace1: Int = 0,
        @ProtoId(20030) @JvmField val reqMss3Bitmapextra: Int = 0,
        @ProtoId(20031) @JvmField val reqBirthday: Int = 0,
        @ProtoId(20032) @JvmField val reqCityId: Int = 0,
        @ProtoId(20033) @JvmField val reqLang1: Int = 0,
        @ProtoId(20034) @JvmField val reqLang2: Int = 0,
        @ProtoId(20035) @JvmField val reqLang3: Int = 0,
        @ProtoId(20041) @JvmField val reqCityZoneId: Int = 0,
        @ProtoId(20056) @JvmField val reqOin: Int = 0,
        @ProtoId(20059) @JvmField val reqBubbleId: Int = 0,
        @ProtoId(21001) @JvmField val reqMss2Identity: Int = 0,
        @ProtoId(21002) @JvmField val reqMss1Service: Int = 0,
        @ProtoId(21003) @JvmField val reqLflag: Int = 0,
        @ProtoId(21004) @JvmField val reqExtFlag: Int = 0,
        @ProtoId(21006) @JvmField val reqBasicSvrFlag: Int = 0,
        @ProtoId(21007) @JvmField val reqBasicCliFlag: Int = 0,
        @ProtoId(24101) @JvmField val reqPengyouRealname: Int = 0,
        @ProtoId(24103) @JvmField val reqPengyouGender: Int = 0,
        @ProtoId(24118) @JvmField val reqPengyouFlag: Int = 0,
        @ProtoId(26004) @JvmField val reqFullBirthday: Int = 0,
        @ProtoId(26005) @JvmField val reqFullAge: Int = 0,
        @ProtoId(26010) @JvmField val reqSimpleUpdateTime: Int = 0,
        @ProtoId(26011) @JvmField val reqMssUpdateTime: Int = 0,
        @ProtoId(27022) @JvmField val reqGroupMemCreditFlag: Int = 0,
        @ProtoId(27025) @JvmField val reqFaceAddonId: Int = 0,
        @ProtoId(27026) @JvmField val reqMusicGene: Int = 0,
        @ProtoId(40323) @JvmField val reqFileShareBit: Int = 0,
        @ProtoId(40404) @JvmField val reqRecommendPrivacyCtrlBit: Int = 0,
        @ProtoId(40505) @JvmField val reqOldFriendChatBit: Int = 0,
        @ProtoId(40602) @JvmField val reqBusinessBit: Int = 0,
        @ProtoId(41305) @JvmField val reqCrmBit: Int = 0,
        @ProtoId(41810) @JvmField val reqForbidFileshareBit: Int = 0,
        @ProtoId(42333) @JvmField val userLoginGuardFace: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc90 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val communityBid: List<Long>? = null,
        @ProtoId(2) @JvmField val page: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CommunityWebInfo(
        @ProtoId(1) @JvmField val communityInfoItem: List<CommunityConfigInfo>? = null,
        @ProtoId(2) @JvmField val page: Int = 0,
        @ProtoId(3) @JvmField val end: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val communityInfoItem: List<CommunityConfigInfo>? = null,
        @ProtoId(2) @JvmField val jumpConcernCommunityUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val communityTitleWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val moreUrlWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val webCommunityInfo: CommunityWebInfo? = null,
        @ProtoId(6) @JvmField val jumpCommunityChannelUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CommunityConfigInfo(
        @ProtoId(1) @JvmField val jumpHomePageUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val picUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val dynamicCount: Int = 0,
        @ProtoId(5) @JvmField val communityBid: Long = 0L,
        @ProtoId(6) @JvmField val followStatus: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xd8a : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val retcode: Int = 0,
        @ProtoId(2) @JvmField val res: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val cmd: Int = 0,
        @ProtoId(3) @JvmField val body: String = "",
        @ProtoId(4) @JvmField val clientInfo: ClientInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoId(1) @JvmField val implat: Int = 0,
        @ProtoId(2) @JvmField val ingClientver: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb6f : ProtoBuf {
    @Serializable
    internal class ReportFreqRspBody(
        @ProtoId(1) @JvmField val identity: Identity? = null,
        @ProtoId(4) @JvmField val remainTimes: Long = 0L,
        @ProtoId(5) @JvmField val expireTime: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Identity(
        @ProtoId(1) @JvmField val apiName: String = "",
        @ProtoId(2) @JvmField val appid: Int = 0,
        @ProtoId(3) @JvmField val apptype: Int = 0,
        @ProtoId(4) @JvmField val bizid: Int = 0,
        @ProtoId(10) @JvmField val intExt1: Long = 0L,
        @ProtoId(20) @JvmField val ext1: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ThresholdInfo(
        @ProtoId(1) @JvmField val thresholdPerMinute: Long = 0L,
        @ProtoId(2) @JvmField val thresholdPerDay: Long = 0L,
        @ProtoId(3) @JvmField val thresholdPerHour: Long = 0L,
        @ProtoId(4) @JvmField val thresholdPerWeek: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val reportFreqRsp: ReportFreqRspBody? = null
    ) : ProtoBuf

    @Serializable
    internal class ReportFreqReqBody(
        @ProtoId(1) @JvmField val identity: Identity? = null,
        @ProtoId(2) @JvmField val invokeTimes: Long = 1L
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val reportFreqReq: ReportFreqReqBody? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7dc : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val seq: Int = 0,
        @ProtoId(2) @JvmField val wording: String = "",
        @ProtoId(3) @JvmField val msgAppointInfo: List<AppointDefine.AppointInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val seq: Int = 0,
        @ProtoId(2) @JvmField val msgAppointment: AppointDefine.AppointContent? = null,
        @ProtoId(3) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(4) @JvmField val overwrite: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cd : ProtoBuf {
    @Serializable
    internal class AppointBrife(
        @ProtoId(1) @JvmField val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) @JvmField val msgAppointsInfo: AppointDefine.AppointInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val stamp: Int = 0,
        @ProtoId(2) @JvmField val over: Int = 0,
        @ProtoId(3) @JvmField val next: Int = 0,
        @ProtoId(4) @JvmField val msgAppointsInfo: List<AppointBrife>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val stamp: Int = 0,
        @ProtoId(2) @JvmField val start: Int = 0,
        @ProtoId(3) @JvmField val want: Int = 0,
        @ProtoId(4) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(5) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(6) @JvmField val appointOperation: Int = 0,
        @ProtoId(100) @JvmField val requestUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc0c : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val isTaskCompleted: Int = 0,
        @ProtoId(2) @JvmField val taskPoint: Int = 0,
        @ProtoId(3) @JvmField val guideWording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val needShowProgress: Int = 0,
        @ProtoId(5) @JvmField val originalProgress: Int = 0,
        @ProtoId(6) @JvmField val nowProgress: Int = 0,
        @ProtoId(7) @JvmField val totalProgress: Int = 0,
        @ProtoId(8) @JvmField val needExecTask: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class VideoSrcType(
        @ProtoId(1) @JvmField val sourceType: Int = 0,
        @ProtoId(2) @JvmField val videoFromType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val taskType: Int = 0,
        @ProtoId(3) @JvmField val rowkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val feedsId: Long = 0L,
        @ProtoId(5) @JvmField val msgVideoFromType: VideoSrcType? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fb : ProtoBuf {
    @Serializable
    internal class ReqInfo(
        @ProtoId(3) @JvmField val time: Int = 0,
        @ProtoId(4) @JvmField val subject: Int = 0,
        @ProtoId(5) @JvmField val gender: Int = 0,
        @ProtoId(6) @JvmField val ageLow: Int = 0,
        @ProtoId(7) @JvmField val ageUp: Int = 0,
        @ProtoId(8) @JvmField val profession: Int = 0,
        @ProtoId(9) @JvmField val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val msgDestination: AppointDefine.LocaleInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgHead: BusiReqHead? = null,
        @ProtoId(2) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(3) @JvmField val reqInfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class BusiRespHead(
        @ProtoId(1) @JvmField val int32Version: Int = 1,
        @ProtoId(2) @JvmField val int32Seq: Int = 0,
        @ProtoId(3) @JvmField val int32ReplyCode: Int = 0,
        @ProtoId(4) @JvmField val result: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UserProfile(
        @ProtoId(1) @JvmField val int64Id: Long = 0L,
        @ProtoId(2) @JvmField val int32IdType: Int = 0,
        @ProtoId(3) @JvmField val url: String = "",
        @ProtoId(4) @JvmField val int32PicType: Int = 0,
        @ProtoId(5) @JvmField val int32SubPicType: Int = 0,
        @ProtoId(6) @JvmField val title: String = "",
        @ProtoId(7) @JvmField val content: String = "",
        @ProtoId(8) @JvmField val content2: String = "",
        @ProtoId(9) @JvmField val picUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class BusiReqHead(
        @ProtoId(1) @JvmField val int32Version: Int = 1,
        @ProtoId(2) @JvmField val int32Seq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgHead: BusiRespHead? = null,
        @ProtoId(2) @JvmField val msgUserList: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb61 : ProtoBuf {
    @Serializable
    internal class GetAppinfoReq(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val appType: Int = 0,
        @ProtoId(3) @JvmField val platform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class GetPkgUrlReq(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val appType: Int = 0,
        @ProtoId(3) @JvmField val appVersion: Int = 0,
        @ProtoId(4) @JvmField val platform: Int = 0,
        @ProtoId(5) @JvmField val sysVersion: String = "",
        @ProtoId(6) @JvmField val qqVersion: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val wording: String = "",
        @ProtoId(2) @JvmField val nextReqDuration: Int = 0,
        @ProtoId(10) @JvmField val getAppinfoRsp: GetAppinfoRsp? = null,
        @ProtoId(11) @JvmField val getMqqappUrlRsp: GetPkgUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(10) @JvmField val getAppinfoReq: GetAppinfoReq? = null,
        @ProtoId(11) @JvmField val getMqqappUrlReq: GetPkgUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetAppinfoRsp(
        @ProtoId(1) @JvmField val appinfo: Qqconnect.Appinfo? = null
    ) : ProtoBuf

    @Serializable
    internal class GetPkgUrlRsp(
        @ProtoId(1) @JvmField val appVersion: Int = 0,
        @ProtoId(2) @JvmField val pkgUrl: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb60 : ProtoBuf {
    @Serializable
    internal class GetPrivilegeReq(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val appType: Int = 3
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlReq(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val appType: Int = 0,
        @ProtoId(3) @JvmField val url: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @ProtoId(1) @JvmField val platform: Int = 0,
        @ProtoId(2) @JvmField val sdkVersion: String = "",
        @ProtoId(3) @JvmField val androidPackageName: String = "",
        @ProtoId(4) @JvmField val androidSignature: String = "",
        @ProtoId(5) @JvmField val iosBundleId: String = "",
        @ProtoId(6) @JvmField val pcSign: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val wording: String = "",
        @ProtoId(10) @JvmField val getPrivilegeRsp: GetPrivilegeRsp? = null,
        @ProtoId(11) @JvmField val checkUrlRsp: CheckUrlRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUrlRsp(
        @ProtoId(1) @JvmField val isAuthed: Boolean = false,
        @ProtoId(2) @JvmField val nextReqDuration: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val clientInfo: ClientInfo? = null,
        @ProtoId(10) @JvmField val getPrivilegeReq: GetPrivilegeReq? = null,
        @ProtoId(11) @JvmField val checkUrlReq: CheckUrlReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetPrivilegeRsp(
        @ProtoId(1) @JvmField val apiGroups: List<Int>? = null,
        @ProtoId(2) @JvmField val nextReqDuration: Int = 0,
        @ProtoId(3) @JvmField val apiNames: List<String> = listOf()
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fc : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val lastEventId: Long = 0L,
        @ProtoId(2) @JvmField val readEventId: Long = 0L,
        @ProtoId(3) @JvmField val fetchCount: Int = 0,
        @ProtoId(4) @JvmField val lastNearbyEventId: Long = 0L,
        @ProtoId(5) @JvmField val readNearbyEventId: Long = 0L,
        @ProtoId(6) @JvmField val fetchNearbyEventCount: Int = 0,
        @ProtoId(7) @JvmField val lastFeedEventId: Long = 0L,
        @ProtoId(8) @JvmField val readFeedEventId: Long = 0L,
        @ProtoId(9) @JvmField val fetchFeedEventCount: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgEventList: List<AppointDefine.DateEvent>? = null,
        @ProtoId(2) @JvmField val actAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(3) @JvmField val maxEventId: Long = 0L,
        @ProtoId(4) @JvmField val errorTips: String = "",
        @ProtoId(5) @JvmField val msgNearbyEventList: List<AppointDefine.NearbyEvent>? = null,
        @ProtoId(6) @JvmField val msgFeedEventList: List<AppointDefine.FeedEvent>? = null,
        @ProtoId(7) @JvmField val maxFreshEventId: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc33 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val nextGap: Int = 0,
        @ProtoId(3) @JvmField val newUser: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody : ProtoBuf
}

@Serializable
internal class Oidb0xc0b : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val isOpenCoinEntry: Int = 0,
        @ProtoId(2) @JvmField val canGetCoinCount: Int = 0,
        @ProtoId(3) @JvmField val coinIconUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val lastCompletedTaskStamp: Long = 0L,
        @ProtoId(6) @JvmField val cmsWording: List<KanDianCMSActivityInfo>? = null,
        @ProtoId(7) @JvmField val lastCmsActivityStamp: Long = 0L,
        @ProtoId(8) @JvmField val msgKandianCoinRemind: KanDianCoinRemind? = null,
        @ProtoId(9) @JvmField val msgKandianTaskRemind: KanDianTaskRemind? = null
    ) : ProtoBuf

    @Serializable
    internal class KanDianCoinRemind(
        @ProtoId(1) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KanDianTaskRemind(
        @ProtoId(1) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val taskType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class KanDianCMSActivityInfo(
        @ProtoId(1) @JvmField val activityId: Long = 0L,
        @ProtoId(2) @JvmField val wording: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val pictureUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xc85 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(101) @JvmField val fromUin: Long = 0L,
        @ProtoId(102) @JvmField val toUin: Long = 0L,
        @ProtoId(103) @JvmField val op: Int = 0,
        @ProtoId(104) @JvmField val intervalDays: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class InteractionDetailInfo(
        @ProtoId(101) @JvmField val continuousRecordDays: Int = 0,
        @ProtoId(102) @JvmField val sendDayTime: Int = 0,
        @ProtoId(103) @JvmField val recvDayTime: Int = 0,
        @ProtoId(104) @JvmField val sendRecord: String = "",
        @ProtoId(105) @JvmField val recvRecord: String = ""
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(101) @JvmField val result: Int = 0,
        @ProtoId(102) @JvmField val recentInteractionTime: Int = 0,
        @ProtoId(103) @JvmField val interactionDetailInfo: InteractionDetailInfo? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7ce : ProtoBuf {
    @Serializable
    internal class AppintDetail(
        @ProtoId(1) @JvmField val msgPublisherInfo: AppointDefine.PublisherInfo? = null,
        @ProtoId(2) @JvmField val msgAppointsInfo: AppointDefine.AppointInfo? = null,
        @ProtoId(3) @JvmField val score: Int = 0,
        @ProtoId(4) @JvmField val joinOver: Int = 0,
        @ProtoId(5) @JvmField val joinNext: Int = 0,
        @ProtoId(6) @JvmField val msgStrangerInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoId(7) @JvmField val viewOver: Int = 0,
        @ProtoId(8) @JvmField val viewNext: Int = 0,
        @ProtoId(9) @JvmField val msgVistorInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoId(10) @JvmField val meJoin: Int = 0,
        @ProtoId(12) @JvmField val canProfile: Int = 0,
        @ProtoId(13) @JvmField val profileErrmsg: String = "",
        @ProtoId(14) @JvmField val canAio: Int = 0,
        @ProtoId(15) @JvmField val aioErrmsg: String = "",
        @ProtoId(16) @JvmField val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(17) @JvmField val uin: Long = 0L,
        @ProtoId(18) @JvmField val limited: Int = 0,
        @ProtoId(19) @JvmField val msgCommentList: List<AppointDefine.DateComment>? = null,
        @ProtoId(20) @JvmField val commentOver: Int = 0,
        @ProtoId(23) @JvmField val meInvited: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgAppointsInfo: List<AppintDetail>? = null,
        @ProtoId(2) @JvmField val secureFlag: Int = 0,
        @ProtoId(3) @JvmField val secureTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val appointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(2) @JvmField val joinStart: Int = 0,
        @ProtoId(3) @JvmField val joinWant: Int = 0,
        @ProtoId(4) @JvmField val viewStart: Int = 0,
        @ProtoId(5) @JvmField val viewWant: Int = 0,
        @ProtoId(6) @JvmField val msgLbsInfo: AppointDefine.LBSInfo? = null,
        @ProtoId(7) @JvmField val uint64Uins: List<Long>? = null,
        @ProtoId(8) @JvmField val viewCommentCount: Int = 0,
        @ProtoId(100) @JvmField val requestUin: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7db : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val wording: String = "",
        @ProtoId(2) @JvmField val msgAppointInfo: AppointDefine.AppointInfo? = null,
        @ProtoId(3) @JvmField val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val appointAction: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) @JvmField val appointAction: Int = 0,
        @ProtoId(3) @JvmField val overwrite: Int = 0,
        @ProtoId(4) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc6c : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val msgGroupInfo: List<GroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) @JvmField val groupUin: Long = 0L,
        @ProtoId(2) @JvmField val groupCode: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody : ProtoBuf
}

@Serializable
internal class Oidb0xc05 : ProtoBuf {
    @Serializable
    internal class GetAuthAppListReq(
        @ProtoId(1) @JvmField val start: Int = 0,
        @ProtoId(2) @JvmField val limit: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val wording: String = "",
        @ProtoId(10) @JvmField val getCreateAppListRsp: GetCreateAppListRsp? = null,
        @ProtoId(11) @JvmField val getAuthAppListRsp: GetAuthAppListRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class GetCreateAppListRsp(
        @ProtoId(1) @JvmField val totalCount: Int = 0,
        @ProtoId(2) @JvmField val appinfos: List<Qqconnect.Appinfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class GetAuthAppListRsp(
        @ProtoId(1) @JvmField val totalCount: Int = 0,
        @ProtoId(2) @JvmField val appinfos: List<Qqconnect.Appinfo>? = null,
        @ProtoId(3) @JvmField val curIndex: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(10) @JvmField val getCreateAppListReq: GetCreateAppListReq? = null,
        @ProtoId(11) @JvmField val getAuthAppListReq: GetAuthAppListReq? = null
    ) : ProtoBuf

    @Serializable
    internal class GetCreateAppListReq(
        @ProtoId(1) @JvmField val start: Int = 0,
        @ProtoId(2) @JvmField val limit: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7da : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(2) @JvmField val appointOperation: Int = 0,
        @ProtoId(3) @JvmField val operationReason: Int = 0,
        @ProtoId(4) @JvmField val overwrite: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val wording: String = "",
        @ProtoId(2) @JvmField val msgAppointInfo: List<AppointDefine.AppointInfo>? = null,
        @ProtoId(3) @JvmField val operationReason: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Qqconnect : ProtoBuf {
    @Serializable
    internal class MobileAppInfo(
        @ProtoId(11) @JvmField val androidAppInfo: List<AndroidAppInfo>? = null,
        @ProtoId(12) @JvmField val iosAppInfo: List<IOSAppInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class TemplateMsgConfig(
        @ProtoId(1) @JvmField val serviceMsgUin: Long = 0L,
        @ProtoId(2) @JvmField val publicMsgUin: Long = 0L,
        @ProtoId(3) @JvmField val campMsgUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class Appinfo(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val appType: Int = 0,
        @ProtoId(3) @JvmField val platform: Int = 0,
        @ProtoId(4) @JvmField val appName: String = "",
        @ProtoId(5) @JvmField val appKey: String = "",
        @ProtoId(6) @JvmField val appState: Int = 0,
        @ProtoId(7) @JvmField val iphoneUrlScheme: String = "",
        @ProtoId(8) @JvmField val androidPackName: String = "",
        @ProtoId(9) @JvmField val iconUrl: String = "",
        @ProtoId(10) @JvmField val sourceUrl: String = "",
        @ProtoId(11) @JvmField val iconSmallUrl: String = "",
        @ProtoId(12) @JvmField val iconMiddleUrl: String = "",
        @ProtoId(13) @JvmField val tencentDocsAppinfo: TencentDocsAppinfo? = null,
        @ProtoId(21) @JvmField val developerUin: Long = 0L,
        @ProtoId(22) @JvmField val appClass: Int = 0,
        @ProtoId(23) @JvmField val appSubclass: Int = 0,
        @ProtoId(24) @JvmField val remark: String = "",
        @ProtoId(25) @JvmField val iconMiniUrl: String = "",
        @ProtoId(26) @JvmField val authTime: Long = 0L,
        @ProtoId(27) @JvmField val appUrl: String = "",
        @ProtoId(28) @JvmField val universalLink: String = "",
        @ProtoId(29) @JvmField val qqconnectFeature: Int = 0,
        @ProtoId(30) @JvmField val isHatchery: Int = 0,
        @ProtoId(31) @JvmField val testUinList: List<Long>? = null,
        @ProtoId(100) @JvmField val templateMsgConfig: TemplateMsgConfig? = null,
        @ProtoId(101) @JvmField val miniAppInfo: MiniAppInfo? = null,
        @ProtoId(102) @JvmField val webAppInfo: WebAppInfo? = null,
        @ProtoId(103) @JvmField val mobileAppInfo: MobileAppInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class ConnectClientInfo(
        @ProtoId(1) @JvmField val platform: Int = 0,
        @ProtoId(2) @JvmField val sdkVersion: String = "",
        @ProtoId(3) @JvmField val systemName: String = "",
        @ProtoId(4) @JvmField val systemVersion: String = "",
        @ProtoId(21) @JvmField val androidPackageName: String = "",
        @ProtoId(22) @JvmField val androidSignature: String = "",
        @ProtoId(31) @JvmField val iosBundleId: String = "",
        @ProtoId(32) @JvmField val iosDeviceId: String = "",
        @ProtoId(33) @JvmField val iosAppToken: String = "",
        @ProtoId(41) @JvmField val pcSign: String = ""
    ) : ProtoBuf

    @Serializable
    internal class TencentDocsAppinfo(
        @ProtoId(1) @JvmField val openTypes: String = "",
        @ProtoId(2) @JvmField val opts: String = "",
        @ProtoId(3) @JvmField val ejs: String = "",
        @ProtoId(4) @JvmField val callbackUrlTest: String = "",
        @ProtoId(5) @JvmField val callbackUrl: String = "",
        @ProtoId(6) @JvmField val domain: String = "",
        @ProtoId(7) @JvmField val userinfoCallback: String = "",
        @ProtoId(8) @JvmField val userinfoCallbackTest: String = ""
    ) : ProtoBuf

    @Serializable
    internal class WebAppInfo(
        @ProtoId(1) @JvmField val websiteUrl: String = "",
        @ProtoId(2) @JvmField val provider: String = "",
        @ProtoId(3) @JvmField val icp: String = "",
        @ProtoId(4) @JvmField val callbackUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class IOSAppInfo(
        @ProtoId(1) @JvmField val bundleId: String = "",
        @ProtoId(2) @JvmField val urlScheme: String = "",
        @ProtoId(3) @JvmField val storeId: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MsgUinInfo(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val msgType: Int = 0,
        @ProtoId(3) @JvmField val appid: Int = 0,
        @ProtoId(4) @JvmField val appType: Int = 0,
        @ProtoId(5) @JvmField val ctime: Int = 0,
        @ProtoId(6) @JvmField val mtime: Int = 0,
        @ProtoId(7) @JvmField val mpType: Int = 0,
        @ProtoId(100) @JvmField val nick: String = "",
        @ProtoId(101) @JvmField val faceUrl: String = ""
    ) : ProtoBuf

    @Serializable
    internal class MiniAppInfo(
        @ProtoId(1) @JvmField val superUin: Long = 0L,
        @ProtoId(11) @JvmField val ownerType: Int = 0,
        @ProtoId(12) @JvmField val ownerName: String = "",
        @ProtoId(13) @JvmField val ownerIdCardType: Int = 0,
        @ProtoId(14) @JvmField val ownerIdCard: String = "",
        @ProtoId(15) @JvmField val ownerStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AndroidAppInfo(
        @ProtoId(1) @JvmField val packName: String = "",
        @ProtoId(2) @JvmField val packSign: String = "",
        @ProtoId(3) @JvmField val apkDownUrl: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Sync : ProtoBuf {
    @Serializable
    internal class SyncAppointmentReq(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val msgAppointment: AppointDefine.AppointContent? = null,
        @ProtoId(3) @JvmField val msgGpsInfo: AppointDefine.GPS? = null
    ) : ProtoBuf

    @Serializable
    internal class SyncAppointmentRsp(
        @ProtoId(1) @JvmField val result: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc26 : ProtoBuf {
    @Serializable
    internal class RgoupLabel(
        @ProtoId(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val enumType: Int /* enum */ = 1,
        @ProtoId(3) @JvmField val textColor: RgroupColor? = null,
        @ProtoId(4) @JvmField val edgingColor: RgroupColor? = null,
        @ProtoId(5) @JvmField val labelAttr: Int = 0,
        @ProtoId(6) @JvmField val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class AddFriendSource(
        @ProtoId(1) @JvmField val source: Int = 0,
        @ProtoId(2) @JvmField val subSource: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Label(
        @ProtoId(1) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val textColor: Color? = null,
        @ProtoId(3) @JvmField val edgingColor: Color? = null,
        @ProtoId(4) @JvmField val labelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class EntryDelay(
        @ProtoId(1) @JvmField val emEntry: Int /* enum */ = 1,
        @ProtoId(2) @JvmField val delay: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgPersons: List<MayKnowPerson>? = null,
        @ProtoId(2) @JvmField val entryInuse: List<Int> = listOf(),
        @ProtoId(3) @JvmField val entryClose: List<Int> = listOf(),
        @ProtoId(4) @JvmField val nextGap: Int = 0,
        @ProtoId(5) @JvmField val timestamp: Int = 0,
        @ProtoId(6) @JvmField val msgUp: Int = 0,
        @ProtoId(7) @JvmField val entryDelays: List<EntryDelay>? = null,
        @ProtoId(8) @JvmField val listSwitch: Int = 0,
        @ProtoId(9) @JvmField val addPageListSwitch: Int = 0,
        @ProtoId(10) @JvmField val emRspDataType: Int /* enum */ = 1,
        @ProtoId(11) @JvmField val msgRgroupItems: List<RecommendInfo>? = null,
        @ProtoId(12) @JvmField val boolIsNewuser: Boolean = false,
        @ProtoId(13) @JvmField val msgTables: List<TabInfo>? = null,
        @ProtoId(14) @JvmField val cookies: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TabInfo(
        @ProtoId(1) @JvmField val tabId: Int = 0,
        @ProtoId(2) @JvmField val recommendCount: Int = 0,
        @ProtoId(3) @JvmField val tableName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val iconUrlSelect: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val iconUrlUnselect: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val backgroundColorSelect: Color? = null,
        @ProtoId(7) @JvmField val backgroundColorUnselect: Color? = null
    ) : ProtoBuf

    @Serializable
    internal class MayKnowPerson(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val msgIosSource: AddFriendSource? = null,
        @ProtoId(3) @JvmField val msgAndroidSource: AddFriendSource? = null,
        @ProtoId(4) @JvmField val reason: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val additive: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val nick: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val remark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(8) @JvmField val country: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val province: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) @JvmField val city: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) @JvmField val age: Int = 0,
        @ProtoId(12) @JvmField val catelogue: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) @JvmField val alghrithm: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(14) @JvmField val richbuffer: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(15) @JvmField val qzone: Int = 0,
        @ProtoId(16) @JvmField val gender: Int = 0,
        @ProtoId(17) @JvmField val mobileName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(18) @JvmField val token: String = "",
        @ProtoId(19) @JvmField val onlineState: Int = 0,
        @ProtoId(20) @JvmField val msgLabels: List<Label>? = null,
        @ProtoId(21) @JvmField val sourceid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RecommendInfo(
        @ProtoId(1) @JvmField val woring: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val msgGroups: List<RgroupInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class RgroupInfo(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val ownerUin: Long = 0L,
        @ProtoId(3) @JvmField val groupName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val groupMemo: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val memberNum: Int = 0,
        @ProtoId(6) @JvmField val groupLabel: List<RgoupLabel>? = null,
        @ProtoId(7) @JvmField val groupFlagExt: Int = 0,
        @ProtoId(8) @JvmField val groupFlag: Int = 0,
        @ProtoId(9) @JvmField val source: Int /* enum */ = 1,
        @ProtoId(10) @JvmField val tagWording: RgoupLabel? = null,
        @ProtoId(11) @JvmField val algorithm: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(12) @JvmField val joinGroupAuth: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(13) @JvmField val activity: Int = 0,
        @ProtoId(14) @JvmField val memberMaxNum: Int = 0,
        @ProtoId(15) @JvmField val int32UinPrivilege: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val filterUins: List<Long>? = null,
        @ProtoId(2) @JvmField val phoneBook: Int = 0,
        @ProtoId(3) @JvmField val expectedUins: List<Long>? = null,
        @ProtoId(4) @JvmField val emEntry: Int /* enum */ = 1,
        @ProtoId(5) @JvmField val fetchRgroup: Int = 0,
        @ProtoId(6) @JvmField val tabId: Int = 0,
        @ProtoId(7) @JvmField val want: Int = 80,
        @ProtoId(8) @JvmField val cookies: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RgroupColor(
        @ProtoId(1) @JvmField val r: Int = 0,
        @ProtoId(2) @JvmField val g: Int = 0,
        @ProtoId(3) @JvmField val b: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Color(
        @ProtoId(1) @JvmField val r: Int = 0,
        @ProtoId(2) @JvmField val g: Int = 0,
        @ProtoId(3) @JvmField val b: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xac6 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val results: List<OperateResult>? = null,
        @ProtoId(4) @JvmField val metalCount: Int = 0,
        @ProtoId(5) @JvmField val metalTotal: Int = 0,
        @ProtoId(9) @JvmField val int32NewCount: Int = 0,
        @ProtoId(10) @JvmField val int32UpgradeCount: Int = 0,
        @ProtoId(11) @JvmField val promptParams: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val medals: List<MedalReport>? = null,
        @ProtoId(2) @JvmField val clean: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MedalReport(
        @ProtoId(1) @JvmField val id: Int = 0,
        @ProtoId(2) @JvmField val level: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class OperateResult(
        @ProtoId(1) @JvmField val id: Int = 0,
        @ProtoId(2) @JvmField val int32Result: Int = 0,
        @ProtoId(3) @JvmField val errmsg: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd32 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val openid: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val xmitinfo: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class XmitInfo(
        @ProtoId(1) @JvmField val signature: String = "",
        @ProtoId(2) @JvmField val appid: String = "",
        @ProtoId(3) @JvmField val groupid: String = "",
        @ProtoId(4) @JvmField val nonce: String = "",
        @ProtoId(5) @JvmField val timestamp: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7cf : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val stamp: Int = 0,
        @ProtoId(2) @JvmField val start: Int = 0,
        @ProtoId(3) @JvmField val want: Int = 0,
        @ProtoId(4) @JvmField val reqValidOnly: Int = 0,
        @ProtoId(5) @JvmField val msgAppointIds: List<AppointDefine.AppointID>? = null,
        @ProtoId(6) @JvmField val appointOperation: Int = 0,
        @ProtoId(100) @JvmField val requestUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val stamp: Int = 0,
        @ProtoId(2) @JvmField val over: Int = 0,
        @ProtoId(3) @JvmField val next: Int = 0,
        @ProtoId(4) @JvmField val msgAppointsInfo: List<AppointDefine.AppointInfo>? = null,
        @ProtoId(5) @JvmField val unreadCount: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xac7 : ProtoBuf {
    @Serializable
    internal class DeviceInfo(
        @ProtoId(1) @JvmField val din: Long = 0L,
        @ProtoId(2) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val extd: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val cmd: Int = 0,
        @ProtoId(2) @JvmField val din: Long = 0L,
        @ProtoId(3) @JvmField val extd: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val msgBinderSig: BinderSig? = null
    ) : ProtoBuf

    @Serializable
    internal class ReceiveMessageDevices(
        @ProtoId(1) @JvmField val devices: List<DeviceInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class BinderSig(
        @ProtoId(1) @JvmField val type: Int = 0,
        @ProtoId(2) @JvmField val uin: Long = 0L,
        @ProtoId(3) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5fa : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgStrangerInfo: List<AppointDefine.StrangerInfo>? = null,
        @ProtoId(2) @JvmField val reachStart: Int = 0,
        @ProtoId(3) @JvmField val reachEnd: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val appointIds: AppointDefine.AppointID? = null,
        @ProtoId(2) @JvmField val referIdx: Int = 0,
        @ProtoId(3) @JvmField val getReferRec: Int = 0,
        @ProtoId(4) @JvmField val reqNextCount: Int = 0,
        @ProtoId(5) @JvmField val reqPrevCount: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class FavoriteCKVData : ProtoBuf {
    @Serializable
    internal class PicInfo(
        @ProtoId(1) @JvmField val uri: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val md5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val sha1: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val name: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val note: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val width: Int = 0,
        @ProtoId(7) @JvmField val height: Int = 0,
        @ProtoId(8) @JvmField val size: Int = 0,
        @ProtoId(9) @JvmField val type: Int = 0,
        @ProtoId(10) @JvmField val msgOwner: Author? = null,
        @ProtoId(11) @JvmField val picId: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KandianFavoriteItem(
        @ProtoId(1) @JvmField val msgFavoriteExtInfo: KandianFavoriteBizData? = null,
        @ProtoId(2) @JvmField val bytesCid: List<ByteArray>? = null,
        @ProtoId(3) @JvmField val type: Int = 0,
        @ProtoId(4) @JvmField val status: Int = 0,
        @ProtoId(5) @JvmField val msgAuthor: Author? = null,
        @ProtoId(6) @JvmField val createTime: Long = 0L,
        @ProtoId(7) @JvmField val favoriteTime: Long = 0L,
        @ProtoId(8) @JvmField val modifyTime: Long = 0L,
        @ProtoId(9) @JvmField val dataSyncTime: Long = 0L,
        @ProtoId(10) @JvmField val msgFavoriteSummary: FavoriteSummary? = null
    ) : ProtoBuf

    @Serializable
    internal class LinkSummary(
        @ProtoId(1) @JvmField val uri: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val publisher: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val brief: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val msgPicInfo: List<PicInfo>? = null,
        @ProtoId(6) @JvmField val type: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val resourceUri: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class UserFavoriteList(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val modifyTs: Long = 0L,
        @ProtoId(100) @JvmField val msgFavoriteItems: List<FavoriteItem>? = null
    ) : ProtoBuf

    @Serializable
    internal class FavoriteSummary(
        @ProtoId(2) @JvmField val msgLinkSummary: LinkSummary? = null
    ) : ProtoBuf

    @Serializable
    internal class FavoriteItem(
        @ProtoId(1) @JvmField val favoriteSource: Int = 0,
        @ProtoId(100) @JvmField val msgKandianFavoriteItem: KandianFavoriteItem? = null
    ) : ProtoBuf

    @Serializable
    internal class Author(
        @ProtoId(1) @JvmField val type: Int = 0,
        @ProtoId(2) @JvmField val numId: Long = 0L,
        @ProtoId(3) @JvmField val strId: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val groupId: Long = 0L,
        @ProtoId(5) @JvmField val groupName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class KandianFavoriteBizData(
        @ProtoId(1) @JvmField val rowkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val type: Int = 0,
        @ProtoId(3) @JvmField val videoDuration: Int = 0,
        @ProtoId(4) @JvmField val picNum: Int = 0,
        @ProtoId(5) @JvmField val accountId: Long = 0L,
        @ProtoId(6) @JvmField val accountName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val videoType: Int = 0,
        @ProtoId(8) @JvmField val feedsId: Long = 0L,
        @ProtoId(9) @JvmField val feedsType: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x5ff : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val errorTips: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) @JvmField val commentId: String = ""
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xccd : ProtoBuf {
    @Serializable
    internal class Result(
        @ProtoId(1) @JvmField val appid: Int = 0,
        @ProtoId(2) @JvmField val errcode: Int = 0,
        @ProtoId(3) @JvmField val errmsg: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val int64Uin: Long = 0L,
        @ProtoId(2) @JvmField val appids: List<Int>? = null,
        @ProtoId(3) @JvmField val platform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val errcode: Int = 0,
        @ProtoId(2) @JvmField val results: List<Result>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xc36 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uint64Uins: List<Long>? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody : ProtoBuf
}

@Serializable
internal class Oidb0x87c : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val country: String = "",
        @ProtoId(2) @JvmField val telephone: String = "",
        @ProtoId(3) @JvmField val smsCode: String = "",
        @ProtoId(4) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val enumButype: Int /* enum */ = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val country: String = "",
        @ProtoId(2) @JvmField val telephone: String = "",
        @ProtoId(3) @JvmField val keyType: Int = 0,
        @ProtoId(4) @JvmField val key: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val guid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xbf2 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val phoneAddrBook: List<PhoneAddrBook>? = null,
        @ProtoId(2) @JvmField val end: Int = 0,
        @ProtoId(3) @JvmField val nextIndex: Long = 0
    ) : ProtoBuf

    @Serializable
    internal class PhoneAddrBook(
        @ProtoId(1) @JvmField val phone: String = "",
        @ProtoId(2) @JvmField val nick: String = "",
        @ProtoId(3) @JvmField val headUrl: String = "",
        @ProtoId(4) @JvmField val longNick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val startIndex: Long = 0L,
        @ProtoId(3) @JvmField val num: Long = 0L
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x6cd : ProtoBuf {
    @Serializable
    internal class RedpointInfo(
        @ProtoId(1) @JvmField val taskid: Int = 0,
        @ProtoId(2) @JvmField val curSeq: Long = 0L,
        @ProtoId(3) @JvmField val pullSeq: Long = 0L,
        @ProtoId(4) @JvmField val readSeq: Long = 0L,
        @ProtoId(5) @JvmField val pullTimes: Int = 0,
        @ProtoId(6) @JvmField val lastPullTime: Int = 0,
        @ProtoId(7) @JvmField val int32RemainedTime: Int = 0,
        @ProtoId(8) @JvmField val lastRecvTime: Int = 0,
        @ProtoId(9) @JvmField val fromId: Long = 0L,
        @ProtoId(10) @JvmField val enumRedpointType: Int /* enum */ = 1,
        @ProtoId(11) @JvmField val msgRedpointExtraInfo: RepointExtraInfo? = null,
        @ProtoId(12) @JvmField val configVersion: String = "",
        @ProtoId(13) @JvmField val doActivity: Int = 0,
        @ProtoId(14) @JvmField val msgUnreadMsg: List<MessageRec>? = null
    ) : ProtoBuf

    @Serializable
    internal class PullRedpointReq(
        @ProtoId(1) @JvmField val taskid: Int = 0,
        @ProtoId(2) @JvmField val lastPullSeq: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgRedpoint: List<RedpointInfo>? = null,
        @ProtoId(2) @JvmField val unfinishedRedpoint: List<PullRedpointReq>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val lastPullRedpoint: List<PullRedpointReq>? = null,
        @ProtoId(2) @JvmField val unfinishedRedpoint: List<PullRedpointReq>? = null,
        @ProtoId(3) @JvmField val msgPullSingleTask: PullRedpointReq? = null,
        @ProtoId(4) @JvmField val retMsgRec: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MessageRec(
        @ProtoId(1) @JvmField val seq: Long = 0L,
        @ProtoId(2) @JvmField val time: Int = 0,
        @ProtoId(3) @JvmField val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class RepointExtraInfo(
        @ProtoId(1) @JvmField val count: Int = 0,
        @ProtoId(2) @JvmField val iconUrl: String = "",
        @ProtoId(3) @JvmField val tips: String = "",
        @ProtoId(4) @JvmField val data: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xd55 : ProtoBuf {
    @Serializable
    internal class CheckUserRsp(
        @ProtoId(1) @JvmField val openidUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class CheckMiniAppRsp : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val appid: Long = 0L,
        @ProtoId(2) @JvmField val appType: Int = 0,
        @ProtoId(3) @JvmField val srcId: Int = 0,
        @ProtoId(4) @JvmField val rawUrl: String = "",
        @ProtoId(11) @JvmField val checkAppSignReq: CheckAppSignReq? = null,
        @ProtoId(12) @JvmField val checkUserReq: CheckUserReq? = null,
        @ProtoId(13) @JvmField val checkMiniAppReq: CheckMiniAppReq? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckAppSignReq(
        @ProtoId(1) @JvmField val clientInfo: Qqconnect.ConnectClientInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val wording: String = "",
        @ProtoId(11) @JvmField val checkAppSignRsp: CheckAppSignRsp? = null,
        @ProtoId(12) @JvmField val checkUserRsp: CheckUserRsp? = null,
        @ProtoId(13) @JvmField val checkMiniAppRsp: CheckMiniAppRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class CheckUserReq(
        @ProtoId(1) @JvmField val openid: String = "",
        @ProtoId(2) @JvmField val needCheckSameUser: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CheckMiniAppReq(
        @ProtoId(1) @JvmField val miniAppAppid: Long = 0L,
        @ProtoId(2) @JvmField val needCheckBind: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class CheckAppSignRsp(
        @ProtoId(1) @JvmField val iosAppToken: String = "",
        @ProtoId(2) @JvmField val iosUniversalLink: String = "",
        @ProtoId(11) @JvmField val optimizeSwitch: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x8b4 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val gc: Long = 0L,
        @ProtoId(2) @JvmField val guin: Long = 0L,
        @ProtoId(3) @JvmField val flag: Int = 0,
        @ProtoId(21) @JvmField val dstUin: Long = 0L,
        @ProtoId(22) @JvmField val start: Int = 0,
        @ProtoId(23) @JvmField val cnt: Int = 0,
        @ProtoId(24) @JvmField val tag: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) @JvmField val gc: Long = 0L,
        @ProtoId(2) @JvmField val groupName: String = "",
        @ProtoId(3) @JvmField val faceUrl: String = "",
        @ProtoId(4) @JvmField val setDisplayTime: Int = 0,
        // @SerialId(5) @JvmField val groupLabel: List<GroupLabel.Label>? = null,
        @ProtoId(6) @JvmField val textIntro: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val richIntro: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class TagInfo(
        @ProtoId(1) @JvmField val dstUin: Long = 0L,
        @ProtoId(2) @JvmField val start: Int = 0,
        @ProtoId(3) @JvmField val cnt: Int = 0,
        @ProtoId(4) @JvmField val timestamp: Int = 0,
        @ProtoId(5) @JvmField val _0x7ddSeq: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val flag: Int = 0,
        @ProtoId(21) @JvmField val tag: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(22) @JvmField val groupInfo: List<GroupInfo>? = null,
        @ProtoId(23) @JvmField val textLabel: List<ByteArray>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x682 : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgChatinfo: List<ChatInfo>? = null
    ) : ProtoBuf

    @Serializable
    internal class ChatInfo(
        @ProtoId(1) @JvmField val touin: Long = 0L,
        @ProtoId(2) @JvmField val chatflag: Int = 0,
        @ProtoId(3) @JvmField val goldflag: Int = 0,
        @ProtoId(4) @JvmField val totalexpcount: Int = 0,
        @ProtoId(5) @JvmField val curexpcount: Int = 0,
        @ProtoId(6) @JvmField val totalFlag: Int = 0,
        @ProtoId(7) @JvmField val curdayFlag: Int = 0,
        @ProtoId(8) @JvmField val expressTipsMsg: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(9) @JvmField val expressMsg: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val uint64Touinlist: List<Long>? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x6f5 : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val qqVersion: String = "",
        @ProtoId(2) @JvmField val qqPlatform: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class TaskInfo(
        @ProtoId(1) @JvmField val taskId: Int = 0,
        @ProtoId(2) @JvmField val appid: Int = 0,
        @ProtoId(3) @JvmField val passthroughLevel: Int = 0,
        @ProtoId(4) @JvmField val showLevel: Int = 0,
        @ProtoId(5) @JvmField val extra: Int = 0,
        @ProtoId(6) @JvmField val priority: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val configVersion: String = "",
        @ProtoId(2) @JvmField val taskInfo: List<TaskInfo>? = null
    ) : ProtoBuf
}

@Serializable
internal class Oidb0xb7e : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val topItem: List<DiandianTopConfig>? = null
    ) : ProtoBuf

    @Serializable
    internal class DiandianTopConfig(
        @ProtoId(1) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val title: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val subTitle: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val subTitleColor: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val picUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val type: Int = 0,
        @ProtoId(7) @JvmField val topicId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody : ProtoBuf
}

@Serializable
internal class Oidb0xc2f : ProtoBuf {
    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val msgGetFollowUserRecommendListRsp: GetFollowUserRecommendListRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class GetFollowUserRecommendListReq(
        @ProtoId(1) @JvmField val followedUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class RecommendAccountInfo(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val accountType: Int = 0,
        @ProtoId(3) @JvmField val nickName: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) @JvmField val headImgUrl: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val isVip: Int = 0,
        @ProtoId(6) @JvmField val isStar: Int = 0,
        @ProtoId(7) @JvmField val recommendReason: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class GetFollowUserRecommendListRsp(
        @ProtoId(1) @JvmField val msgRecommendList: List<RecommendAccountInfo>? = null,
        @ProtoId(2) @JvmField val jumpUrl: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgGetFollowUserRecommendListReq: GetFollowUserRecommendListReq? = null
    ) : ProtoBuf
}

@Serializable
internal class Cmd0x7ca : ProtoBuf {
    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgAppointId: AppointDefine.AppointID? = null,
        @ProtoId(2) @JvmField val tinyid: Long = 0L,
        @ProtoId(3) @JvmField val opType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoId(1) @JvmField val sigC2C: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val peerUin: Long = 0L,
        @ProtoId(3) @JvmField val errorWording: String = "",
        @ProtoId(4) @JvmField val opType: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class Cmd0xd40 : ProtoBuf {
    @Serializable
    internal class DeviceInfo(
        @ProtoId(1) @JvmField val os: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val dev: DeviceInfo? = null,
        @ProtoId(2) @JvmField val src: Int = 0,
        @ProtoId(3) @JvmField val event: Int = 0,
        @ProtoId(4) @JvmField val redtype: Int = 0
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
        @ProtoId(1) @JvmField val taskid: Int = 0,
        @ProtoId(2) @JvmField val readSeq: Long = 0L,
        @ProtoId(3) @JvmField val appid: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoId(1) @JvmField val msgReadReq: List<ReadRedpointReq>? = null
    ) : ProtoBuf
}

