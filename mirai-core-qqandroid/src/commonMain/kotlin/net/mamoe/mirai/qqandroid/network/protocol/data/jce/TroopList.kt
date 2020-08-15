package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class GetTroopListReqV2Simplify(
    @JceId(0) @JvmField val uin: Long,
    @JceId(1) @JvmField val getMSFMsgFlag: Byte? = null,
    @JceId(2) @JvmField val vecCookies: ByteArray? = null,
    @JceId(3) @JvmField val vecGroupInfo: List<StTroopNumSimplify>? = null,
    @JceId(4) @JvmField val groupFlagExt: Byte? = null,
    @JceId(5) @JvmField val shVersion: Int? = null,
    @JceId(6) @JvmField val dwCompanyId: Long? = null,
    @JceId(7) @JvmField val versionNum: Long? = null,
    @JceId(8) @JvmField val getLongGroupName: Byte? = null
) : JceStruct

@Serializable
internal class StTroopNumSimplify(
    @JceId(0) @JvmField val groupCode: Long,
    @JceId(1) @JvmField val dwGroupInfoSeq: Long? = null,
    @JceId(2) @JvmField val dwGroupFlagExt: Long? = null,
    @JceId(3) @JvmField val dwGroupRankSeq: Long? = null
) : JceStruct


@Serializable
internal class GetTroopListRespV2(
    @JceId(0) @JvmField val uin: Long,
    @JceId(1) @JvmField val troopCount: Short,
    @JceId(2) @JvmField val result: Int,
    @JceId(3) @JvmField val errorCode: Short? = null,
    @JceId(4) @JvmField val vecCookies: ByteArray? = null,
    @JceId(5) @JvmField val vecTroopList: List<StTroopNum>? = null,
    @JceId(6) @JvmField val vecTroopListDel: List<StTroopNum>? = null,
    @JceId(7) @JvmField val vecTroopRank: List<StGroupRankInfo>? = null,
    @JceId(8) @JvmField val vecFavGroup: List<StFavoriteGroup>? = null,
    @JceId(9) @JvmField val vecTroopListExt: List<StTroopNum>? = null
) : JceStruct


@Serializable
internal class StTroopNum(
    @JceId(0) @JvmField val groupUin: Long,
    @JceId(1) @JvmField val groupCode: Long,
    @JceId(2) @JvmField val flag: Byte? = null,
    @JceId(3) @JvmField val dwGroupInfoSeq: Long? = null,
    @JceId(4) @JvmField val groupName: String = "",
    @JceId(5) @JvmField val groupMemo: String = "",
    @JceId(6) @JvmField val dwGroupFlagExt: Long? = null,
    @JceId(7) @JvmField val dwGroupRankSeq: Long? = null,
    @JceId(8) @JvmField val dwCertificationType: Long? = null,
    @JceId(9) @JvmField val dwShutUpTimestamp: Long? = null,
    @JceId(10) @JvmField val dwMyShutUpTimestamp: Long? = null,
    @JceId(11) @JvmField val dwCmdUinUinFlag: Long? = null,
    @JceId(12) @JvmField val dwAdditionalFlag: Long? = null,
    @JceId(13) @JvmField val dwGroupTypeFlag: Long? = null,
    @JceId(14) @JvmField val dwGroupSecType: Long? = null,
    @JceId(15) @JvmField val dwGroupSecTypeInfo: Long? = null,
    @JceId(16) @JvmField val dwGroupClassExt: Long? = null,
    @JceId(17) @JvmField val dwAppPrivilegeFlag: Long? = null,
    @JceId(18) @JvmField val dwSubscriptionUin: Long? = null,
    @JceId(19) @JvmField val dwMemberNum: Long? = null,
    @JceId(20) @JvmField val dwMemberNumSeq: Long? = null,
    @JceId(21) @JvmField val dwMemberCardSeq: Long? = null,
    @JceId(22) @JvmField val dwGroupFlagExt3: Long? = null,
    @JceId(23) @JvmField val dwGroupOwnerUin: Long,
    @JceId(24) @JvmField val isConfGroup: Byte? = null,
    @JceId(25) @JvmField val isModifyConfGroupFace: Byte? = null,
    @JceId(26) @JvmField val isModifyConfGroupName: Byte? = null,
    @JceId(27) @JvmField val dwCmduinJoinTime: Long? = null,
    @JceId(28) @JvmField val ulCompanyId: Long? = null,
    @JceId(29) @JvmField val dwMaxGroupMemberNum: Long? = null,
    @JceId(30) @JvmField val dwCmdUinGroupMask: Long? = null,
    @JceId(31) @JvmField val udwHLGuildAppid: Long? = null,
    @JceId(32) @JvmField val udwHLGuildSubType: Long? = null,
    @JceId(33) @JvmField val udwCmdUinRingtoneID: Long? = null,
    @JceId(34) @JvmField val udwCmdUinFlagEx2: Long? = null
) : JceStruct

@Serializable
internal class StGroupRankInfo(
    @JceId(0) @JvmField val dwGroupCode: Long,
    @JceId(1) @JvmField val groupRankSysFlag: Byte? = null,
    @JceId(2) @JvmField val groupRankUserFlag: Byte? = null,
    @JceId(3) @JvmField val vecRankMap: List<StLevelRankPair>? = null,
    @JceId(4) @JvmField val dwGroupRankSeq: Long? = null,
    @JceId(5) @JvmField val ownerName: String? = "",
    @JceId(6) @JvmField val adminName: String? = "",
    @JceId(7) @JvmField val dwOfficeMode: Long? = null,
    @JceId(9) @JvmField val fuckIssue405: List<FuckIssue405?>? = null // fake
) : JceStruct

@Serializable
internal class FuckIssue405

@Serializable
internal class StFavoriteGroup(
    @JceId(0) @JvmField val dwGroupCode: Long,
    @JceId(1) @JvmField val dwTimestamp: Long? = null,
    @JceId(2) @JvmField val dwSnsFlag: Long? = 1L,
    @JceId(3) @JvmField val dwOpenTimestamp: Long? = null
) : JceStruct

@Serializable
internal class StLevelRankPair(
    @JceId(0) @JvmField val dwLevel: Long? = null,
    @JceId(1) @JvmField val rank: String? = ""
) : JceStruct

@Serializable
internal class GetTroopMemberListReq(
    @JceId(0) @JvmField val uin: Long,
    @JceId(1) @JvmField val groupCode: Long,
    @JceId(2) @JvmField val nextUin: Long,
    @JceId(3) @JvmField val groupUin: Long,
    @JceId(4) @JvmField val version: Long? = null,
    @JceId(5) @JvmField val reqType: Long? = null,
    @JceId(6) @JvmField val getListAppointTime: Long? = null,
    @JceId(7) @JvmField val richCardNameVer: Byte? = null
) : JceStruct


@Serializable
internal class GetTroopMemberListResp(
    @JceId(0) @JvmField val uin: Long,
    @JceId(1) @JvmField val groupCode: Long,
    @JceId(2) @JvmField val groupUin: Long,
    @JceId(3) @JvmField val vecTroopMember: List<StTroopMemberInfo>,
    @JceId(4) @JvmField val nextUin: Long,
    @JceId(5) @JvmField val result: Int,
    @JceId(6) @JvmField val errorCode: Short? = null,
    @JceId(7) @JvmField val officeMode: Long? = null,
    @JceId(8) @JvmField val nextGetTime: Long? = null
) : JceStruct

@Serializable
internal class StTroopMemberInfo(
    @JceId(0) @JvmField val memberUin: Long,
    @JceId(1) @JvmField val faceId: Short,
    @JceId(2) @JvmField val age: Byte,
    @JceId(3) @JvmField val gender: Byte,
    @JceId(4) @JvmField val nick: String = "",
    @JceId(5) @JvmField val status: Byte = 20,
    @JceId(6) @JvmField val sShowName: String? = null,
    @JceId(8) @JvmField val sName: String? = null,
    @JceId(9) @JvmField val cGender: Byte? = null,
    @JceId(10) @JvmField val sPhone: String? = "",
    @JceId(11) @JvmField val sEmail: String? = "",
    @JceId(12) @JvmField val sMemo: String? = "",
    @JceId(13) @JvmField val autoRemark: String? = "",
    @JceId(14) @JvmField val dwMemberLevel: Long? = null,
    @JceId(15) @JvmField val dwJoinTime: Long? = null,
    @JceId(16) @JvmField val dwLastSpeakTime: Long? = null,
    @JceId(17) @JvmField val dwCreditLevel: Long? = null,
    @JceId(18) @JvmField val dwFlag: Long? = null,
    @JceId(19) @JvmField val dwFlagExt: Long? = null,
    @JceId(20) @JvmField val dwPoint: Long? = null,
    @JceId(21) @JvmField val concerned: Byte? = null,
    @JceId(22) @JvmField val shielded: Byte? = null,
    @JceId(23) @JvmField val sSpecialTitle: String? = "",
    @JceId(24) @JvmField val dwSpecialTitleExpireTime: Long? = null,
    @JceId(25) @JvmField val job: String? = "",
    @JceId(26) @JvmField val apolloFlag: Byte? = null,
    @JceId(27) @JvmField val dwApolloTimestamp: Long? = null,
    @JceId(28) @JvmField val dwGlobalGroupLevel: Long? = null,
    @JceId(29) @JvmField val dwTitleId: Long? = null,
    @JceId(30) @JvmField val dwShutupTimestap: Long? = null,
    @JceId(31) @JvmField val dwGlobalGroupPoint: Long? = null,
    @JceId(32) @JvmField val qzusrinfo: QzoneUserInfo? = null,
    @JceId(33) @JvmField val richCardNameVer: Byte? = null,
    @JceId(34) @JvmField val dwVipType: Long? = null,
    @JceId(35) @JvmField val dwVipLevel: Long? = null,
    @JceId(36) @JvmField val dwBigClubLevel: Long? = null,
    @JceId(37) @JvmField val dwBigClubFlag: Long? = null,
    @JceId(38) @JvmField val dwNameplate: Long? = null,
    @JceId(39) @JvmField val vecGroupHonor: ByteArray? = null
) : JceStruct

@Serializable
internal class QzoneUserInfo(
    @JceId(0) @JvmField val eStarState: Int? = null,
    @JceId(1) @JvmField val extendInfo: Map<String, String>? = null
) : JceStruct