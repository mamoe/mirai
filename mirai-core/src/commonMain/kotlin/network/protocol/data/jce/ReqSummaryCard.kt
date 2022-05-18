/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField


@Serializable
internal class ReqHead(
    @JvmField @TarsId(0) val iVersion: Int = 1,
) : JceStruct

@Serializable
internal class ReqSummaryCard(
    @JvmField @TarsId(0) val uin: Long,
    @JvmField @TarsId(1) val eComeFrom: Int = 65535,
    @JvmField @TarsId(2) val uQzoneFeedTimestamp: Long? = null,
    @JvmField @TarsId(3) val isFriend: Byte? = null,
    @JvmField @TarsId(4) val groupCode: Long? = null,
    @JvmField @TarsId(5) val groupUin: Long? = null,
    //@JvmField @TarsId(6) val vSeed: ByteArray? = null,
    //@JvmField @TarsId(7) val searchName: String? = "",
    @JvmField @TarsId(8) val getControl: Long? = null,
    @JvmField @TarsId(9) val eAddFriendSource: Int? = null,
    @JvmField @TarsId(10) val vSecureSig: ByteArray? = null,
    //@JvmField @TarsId(11) val vReqLastGameInfo: ByteArray? = null,
    //@JvmField @TarsId(12) val vReqTemplateInfo: ByteArray? = null,
    //@JvmField @TarsId(13) val vReqStarInfo: ByteArray? = null,
    //@JvmField @TarsId(14) val vvReqServices: List<ByteArray>? = null,
    @JvmField @TarsId(15) val tinyId: Long? = null,
    @JvmField @TarsId(16) val uLikeSource: Long? = null,
    //@JvmField @TarsId(17) val stLocaleInfo: UserLocaleInfo? = null,
    @JvmField @TarsId(18) val reqMedalWallInfo: Byte? = null,
    @JvmField @TarsId(19) val vReq0x5ebFieldId: List<Int>? = null,
    @JvmField @TarsId(20) val reqNearbyGodInfo: Byte? = null,
    //@JvmField @TarsId(21) val reqCommLabel: Byte? = null,
    @JvmField @TarsId(22) val reqExtendCard: Byte? = null,
    //@JvmField @TarsId(23) val vReqKandianInfo: ByteArray? = null,
    //@JvmField @TarsId(24) val uRichCardNameVer: Long? = null
) : JceStruct

@Serializable
internal class RespHead(
    @JvmField @TarsId(0) val iVersion: Int,
    @JvmField @TarsId(1) val iResult: Int,
    @JvmField @TarsId(2) val errorMsg: String = "",
    @JvmField @TarsId(3) val vCookies: ByteArray? = null,
) : JceStruct

@Serializable
internal class RespSearch(
    @JvmField @TarsId(0) val vRecords: List<SearchInfo>,
    @JvmField @TarsId(1) val vSecureSig: ByteArray? = null,
    @JvmField @TarsId(2) val vvRespServices: List<ByteArray>? = null,
) : JceStruct

@Serializable
internal class RespSummaryCard(
    //    @JvmField @TarsId(0) val iFace: Int? = null,
    @JvmField @TarsId(1) val sex: Byte? = null,
    @JvmField @TarsId(2) val age: Byte? = null,
    @JvmField @TarsId(3) val nick: String? = "",
    @JvmField @TarsId(4) val remark: String? = "",
    @JvmField @TarsId(5) val iLevel: Int? = null,
    @JvmField @TarsId(6) val province: String? = "",
    @JvmField @TarsId(7) val city: String? = "",
    @JvmField @TarsId(8) val sign: String? = "",
    @JvmField @TarsId(9) val groupName: String? = "",
    @JvmField @TarsId(10) val groupNick: String? = "",
    @JvmField @TarsId(11) val mobile: String? = "",
    @JvmField @TarsId(12) val contactName: String? = "",
    @JvmField @TarsId(13) val ulShowControl: Long? = null,
    @JvmField @TarsId(14) val qzoneFeedsDesc: String? = "",
    //    @JvmField @TarsId(15)  val oLatestPhotos:AlbumInfo? = null,
    @JvmField @TarsId(16) val iVoteCount: Int? = null,
    @JvmField @TarsId(17) val iLastestVoteCount: Int? = null,
    @JvmField @TarsId(18) val valid4Vote: Byte? = null,
    @JvmField @TarsId(19) val country: String? = "",
    @JvmField @TarsId(20) val status: String? = "",
    @JvmField @TarsId(21) val autoRemark: String? = "",
    @JvmField @TarsId(22) val cacheControl: Long? = null,
    @JvmField @TarsId(23) val uin: Long? = null,
    @JvmField @TarsId(24) val iPhotoCount: Int? = null,
    @JvmField @TarsId(25) val eAddOption: Int? = 101,
    @JvmField @TarsId(26) val vAddQuestion: List<String>? = null,
    @JvmField @TarsId(27) val vSeed: ByteArray? = null,
    @JvmField @TarsId(28) val discussName: String? = "",
    @JvmField @TarsId(29) val stVipInfo: VipBaseInfo? = null,
    @JvmField @TarsId(30) val showName: String? = "",
    @JvmField @TarsId(31) val stVoiceInfo: VoiceInfo? = null,
    @JvmField @TarsId(32) val vRichSign: ByteArray? = null,
    @JvmField @TarsId(33) val uSignModifyTime: Long? = null,
    @JvmField @TarsId(34) val vRespLastGameInfo: ByteArray? = null,
    @JvmField @TarsId(35) val userFlag: Long? = null,
    @JvmField @TarsId(36) val uLoginDays: Long? = null,
    @JvmField @TarsId(37) val loginDesc: String? = "",
    @JvmField @TarsId(38) val uTemplateId: Long? = null,
    @JvmField @TarsId(39) val uQQMasterLoginDays: Long? = 20L,
    @JvmField @TarsId(40) val ulFaceAddonId: Long? = null,
    @JvmField @TarsId(41) val vRespTemplateInfo: ByteArray? = null,
    @JvmField @TarsId(42) val respMusicInfo: String? = "",
    @JvmField @TarsId(43) val vRespStarInfo: ByteArray? = null,
    @JvmField @TarsId(44) val stDiamonds: VipBaseInfo? = null,
    @JvmField @TarsId(45) val uAccelerateMultiple: Long? = null,
    @JvmField @TarsId(46) val vvRespServices: List<ByteArray>? = null,
    @JvmField @TarsId(47) val spaceName: String? = "",
    //    @JvmField @TarsId(48)  val stDateCard:DateCard? = null,
    @JvmField @TarsId(49) val iBirthday: Int? = null,
    //    @JvmField @TarsId(50)  val stQCallInfo:QCallInfo? = null,
    //    @JvmField @TarsId(51)  val stGiftInfo:GiftInfo? = null,
    //    @JvmField @TarsId(52)  val stPanSocialInfo:PanSocialInfo? = null,
    //    @JvmField @TarsId(53)  val stVideoInfo:QQVideoInfo? = null,
    @JvmField @TarsId(54) val vTempChatSig: ByteArray? = null,
    //    @JvmField @TarsId(55)  val stInterestTag:InterestTagInfo? = null,
    //    @JvmField @TarsId(56) val stUserFeed: UserFeed? = null,
    //    @JvmField @TarsId(57)  val stQiqiVideoInfo:QiqiVideoInfo? = null,
    //    @JvmField @TarsId(58)  val stPrivInfo:PrivilegeBaseInfo? = null,
    //    @JvmField @TarsId(59)  val stApollo:QQApolloInfo? = null,
    //    @JvmField @TarsId(60)  val stAddFrdSrcInfo:AddFrdSrcInfo? = null,
    //    @JvmField @TarsId(61)  val stBindPhoneInfo:BindPhoneInfo? = null,
    @JvmField @TarsId(62) val vVisitingCardInfo: ByteArray? = null,
    @JvmField @TarsId(63) val voteLimitedNotice: String? = "",
    @JvmField @TarsId(64) val haveVotedCnt: Short? = null,
    @JvmField @TarsId(65) val availVoteCnt: Short? = null,
    @JvmField @TarsId(66) val eIMBindPhoneNum: String? = "",
    @JvmField @TarsId(67) val eIMId: String? = "",
    @JvmField @TarsId(68) val email: String? = "",
    @JvmField @TarsId(69) val uCareer: Long? = null,
    @JvmField @TarsId(70) val personal: String? = "",
    @JvmField @TarsId(71) val vHotChatInfo: ByteArray? = null,
    //    @JvmField @TarsId(72)  val stOlympicInfo:OlympicInfo? = null,
    @JvmField @TarsId(73) val stCoverInfo: TCoverInfo? = null,
    @JvmField @TarsId(74) val stNowBroadcastInfo: TNowBroadcastInfo? = null,
    //    @JvmField @TarsId(75)  val stEimInfo:TEIMInfo? = null,
    @JvmField @TarsId(78) val stVideoHeadInfo: TVideoHeadInfo? = null,
    @JvmField @TarsId(79) val iContactNotBindQQ: Int? = null,
    //    @JvmField @TarsId(80)  val stMedalWallInfo:TMedalWallInfo? = null,
    @JvmField @TarsId(81) val vvRespServicesBigOrder: List<ByteArray>? = null,
    @JvmField @TarsId(82) val vResp0x5ebInfo: ByteArray? = null,
    @JvmField @TarsId(83) val stNearbyGodInfo: TNearbyGodInfo? = null,
    @JvmField @TarsId(84) val vRespQQStoryInfo: ByteArray? = null,
    @JvmField @TarsId(85) val vRespCustomLabelInfo: ByteArray? = null,
    @JvmField @TarsId(86) val vPraiseList: List<TPraiseInfo>? = null,
    @JvmField @TarsId(87) val stCampusCircleInfo: TCampusCircleInfo? = null,
    @JvmField @TarsId(88) val stTimInfo: TTimInfo? = null,
    @JvmField @TarsId(89) val stQimInfo: TQimInfo? = null,
    //    @JvmField @TarsId(90)  val stHeartInfo:HeartInfo? = null,
    @JvmField @TarsId(91) val vQzoneCoverInfo: ByteArray? = null,
    @JvmField @TarsId(92) val vNearbyTaskInfo: ByteArray? = null,
    @JvmField @TarsId(93) val vNowInfo: ByteArray? = null,
    @JvmField @TarsId(94) val uFriendGroupId: Long? = null,
    @JvmField @TarsId(95) val vCommLabel: ByteArray? = null,
    @JvmField @TarsId(96) val vExtendCard: ByteArray? = null,
    @JvmField @TarsId(97) val qzoneHeader: String? = "",
    @JvmField @TarsId(98) val mapQzoneEx: Map<String, String>? = null,
    @JvmField @TarsId(99) val vRespKandianInfo: ByteArray? = null,
    //    @JvmField @TarsId(100) val stWeishiInfo:WeishiInfo? = null,
    @JvmField @TarsId(101) val uRichCardNameVer: Long? = null,
    @JvmField @TarsId(102) val uCurMulType: Long? = null,
    @JvmField @TarsId(103) val vLongNickTopicInfo: ByteArray? = null,
) : JceStruct

@Serializable
internal class RespVoiceManage(
    @JvmField @TarsId(0) val eOpType: Int,
) : JceStruct

@Serializable
internal class SearchInfo(
    @JvmField @TarsId(0) val uIN: Long,
    @JvmField @TarsId(1) val eSource: Int,
    @JvmField @TarsId(2) val nick: String? = "",
    @JvmField @TarsId(3) val mobile: String? = "",
    @JvmField @TarsId(4) val isFriend: Byte? = null,
    @JvmField @TarsId(5) val inContact: Byte? = null,
    @JvmField @TarsId(6) val isEnterpriseQQ: Byte? = null,
) : JceStruct

@Serializable
internal class TCampusCircleInfo(
    @JvmField @TarsId(0) val iIsSigned: Int? = null,
    @JvmField @TarsId(1) val name: String? = "",
    @JvmField @TarsId(2) val academy: String? = "",
    @JvmField @TarsId(3) val eStatus: Int? = null,
    @JvmField @TarsId(4) val stSchoolInfo: TCampusSchoolInfo? = null,
) : JceStruct

@Serializable
internal class TCampusSchoolInfo(
    @JvmField @TarsId(0) val uTimestamp: Long? = null,
    @JvmField @TarsId(1) val uSchoolId: Long? = null,
    @JvmField @TarsId(2) val iIsValidForCertified: Int? = null,
) : JceStruct

@Serializable
internal class TCoverInfo(
    @JvmField @TarsId(0) val vTagInfo: ByteArray? = null,
) : JceStruct


@Serializable
internal class TNearbyGodInfo(
    @JvmField @TarsId(0) val iIsGodFlag: Int? = null,
    @JvmField @TarsId(1) val jumpUrl: String? = "",
) : JceStruct

@Serializable
internal class TNowBroadcastInfo(
    @JvmField @TarsId(0) val iFlag: Int? = null,
    @JvmField @TarsId(1) val iconURL: String? = "",
    @JvmField @TarsId(2) val hrefURL: String? = "",
    @JvmField @TarsId(3) val vAnchorDataRsp: ByteArray? = null,
) : JceStruct

@Serializable
internal class TPraiseInfo(
    @JvmField @TarsId(0) val uCustomId: Long? = null,
    @JvmField @TarsId(1) val iIsPayed: Int? = null,
) : JceStruct

@Serializable
internal class TQimInfo(
    @JvmField @TarsId(0) val iIsOnline: Int? = null,
) : JceStruct

@Serializable
internal class TTimInfo(
    @JvmField @TarsId(0) val iIsOnline: Int? = null,
) : JceStruct

@Serializable
internal class TVideoHeadInfo(
    @JvmField @TarsId(0) val iNearbyFlag: Int? = null,
    @JvmField @TarsId(1) val iBasicFlag: Int? = null,
    @JvmField @TarsId(2) val vMsg: ByteArray? = null,
) : JceStruct

@Serializable
internal class UserFeed(
    @JvmField @TarsId(0) val uFlag: Long? = null,
    @JvmField @TarsId(1) val vFeedInfo: ByteArray? = null,
) : JceStruct

@Serializable
internal class UserLocaleInfo(
    @JvmField @TarsId(1) val longitude: Long? = null,
    @JvmField @TarsId(2) val latitude: Long? = null,
) : JceStruct

@Serializable
internal class VoiceInfo(
    @JvmField @TarsId(0) val vVoiceId: ByteArray? = null,
    @JvmField @TarsId(1) val shDuration: Short? = null,
    @JvmField @TarsId(2) val read: Byte? = 2,
    @JvmField @TarsId(3) val url: String? = "",
) : JceStruct
