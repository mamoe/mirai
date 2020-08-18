package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class GetTroopListReqV2Simplify(
    @TarsId(0) @JvmField val uin: Long,
    @TarsId(1) @JvmField val getMSFMsgFlag: Byte? = null,
    @TarsId(2) @JvmField val vecCookies: ByteArray? = null,
    @TarsId(3) @JvmField val vecGroupInfo: List<StTroopNumSimplify>? = null,
    @TarsId(4) @JvmField val groupFlagExt: Byte? = null,
    @TarsId(5) @JvmField val shVersion: Int? = null,
    @TarsId(6) @JvmField val dwCompanyId: Long? = null,
    @TarsId(7) @JvmField val versionNum: Long? = null,
    @TarsId(8) @JvmField val getLongGroupName: Byte? = null
) : JceStruct

@Serializable
internal class StTroopNumSimplify(
    @TarsId(0) @JvmField val groupCode: Long,
    @TarsId(1) @JvmField val dwGroupInfoSeq: Long? = null,
    @TarsId(2) @JvmField val dwGroupFlagExt: Long? = null,
    @TarsId(3) @JvmField val dwGroupRankSeq: Long? = null
) : JceStruct


@Serializable
internal class GetTroopListRespV2(
    @TarsId(0) @JvmField val uin: Long,
    @TarsId(1) @JvmField val troopCount: Short,
    @TarsId(2) @JvmField val result: Int,
    @TarsId(3) @JvmField val errorCode: Short? = null,
    @TarsId(4) @JvmField val vecCookies: ByteArray? = null,
    @TarsId(5) @JvmField val vecTroopList: List<StTroopNum>? = null,
    @TarsId(6) @JvmField val vecTroopListDel: List<StTroopNum>? = null,
    @TarsId(7) @JvmField val vecTroopRank: List<StGroupRankInfo>? = null,
    @TarsId(8) @JvmField val vecFavGroup: List<StFavoriteGroup>? = null,
    @TarsId(9) @JvmField val vecTroopListExt: List<StTroopNum>? = null
) : JceStruct


@Serializable
internal class StTroopNum(
    @TarsId(0) @JvmField val groupUin: Long,
    @TarsId(1) @JvmField val groupCode: Long,
    @TarsId(2) @JvmField val flag: Byte? = null,
    @TarsId(3) @JvmField val dwGroupInfoSeq: Long? = null,
    @TarsId(4) @JvmField val groupName: String = "",
    @TarsId(5) @JvmField val groupMemo: String = "",
    @TarsId(6) @JvmField val dwGroupFlagExt: Long? = null,
    @TarsId(7) @JvmField val dwGroupRankSeq: Long? = null,
    @TarsId(8) @JvmField val dwCertificationType: Long? = null,
    @TarsId(9) @JvmField val dwShutUpTimestamp: Long? = null,
    @TarsId(10) @JvmField val dwMyShutUpTimestamp: Long? = null,
    @TarsId(11) @JvmField val dwCmdUinUinFlag: Long? = null,
    @TarsId(12) @JvmField val dwAdditionalFlag: Long? = null,
    @TarsId(13) @JvmField val dwGroupTypeFlag: Long? = null,
    @TarsId(14) @JvmField val dwGroupSecType: Long? = null,
    @TarsId(15) @JvmField val dwGroupSecTypeInfo: Long? = null,
    @TarsId(16) @JvmField val dwGroupClassExt: Long? = null,
    @TarsId(17) @JvmField val dwAppPrivilegeFlag: Long? = null,
    @TarsId(18) @JvmField val dwSubscriptionUin: Long? = null,
    @TarsId(19) @JvmField val dwMemberNum: Long? = null,
    @TarsId(20) @JvmField val dwMemberNumSeq: Long? = null,
    @TarsId(21) @JvmField val dwMemberCardSeq: Long? = null,
    @TarsId(22) @JvmField val dwGroupFlagExt3: Long? = null,
    @TarsId(23) @JvmField val dwGroupOwnerUin: Long,
    @TarsId(24) @JvmField val isConfGroup: Byte? = null,
    @TarsId(25) @JvmField val isModifyConfGroupFace: Byte? = null,
    @TarsId(26) @JvmField val isModifyConfGroupName: Byte? = null,
    @TarsId(27) @JvmField val dwCmduinJoinTime: Long? = null,
    @TarsId(28) @JvmField val ulCompanyId: Long? = null,
    @TarsId(29) @JvmField val dwMaxGroupMemberNum: Long? = null,
    @TarsId(30) @JvmField val dwCmdUinGroupMask: Long? = null,
    @TarsId(31) @JvmField val udwHLGuildAppid: Long? = null,
    @TarsId(32) @JvmField val udwHLGuildSubType: Long? = null,
    @TarsId(33) @JvmField val udwCmdUinRingtoneID: Long? = null,
    @TarsId(34) @JvmField val udwCmdUinFlagEx2: Long? = null
) : JceStruct

@Serializable
internal class StGroupRankInfo(
    @TarsId(0) @JvmField val dwGroupCode: Long,
    @TarsId(1) @JvmField val groupRankSysFlag: Byte? = null,
    @TarsId(2) @JvmField val groupRankUserFlag: Byte? = null,
    @TarsId(3) @JvmField val vecRankMap: List<StLevelRankPair>? = null,
    @TarsId(4) @JvmField val dwGroupRankSeq: Long? = null,
    @TarsId(5) @JvmField val ownerName: String? = "",
    @TarsId(6) @JvmField val adminName: String? = "",
    @TarsId(7) @JvmField val dwOfficeMode: Long? = null,
    @TarsId(9) @JvmField val fuckIssue405: List<FuckIssue405?>? = null // fake
) : JceStruct

@Serializable
internal class FuckIssue405

@Serializable
internal class StFavoriteGroup(
    @TarsId(0) @JvmField val dwGroupCode: Long,
    @TarsId(1) @JvmField val dwTimestamp: Long? = null,
    @TarsId(2) @JvmField val dwSnsFlag: Long? = 1L,
    @TarsId(3) @JvmField val dwOpenTimestamp: Long? = null
) : JceStruct

@Serializable
internal class StLevelRankPair(
    @TarsId(0) @JvmField val dwLevel: Long? = null,
    @TarsId(1) @JvmField val rank: String? = ""
) : JceStruct

@Serializable
internal class GetTroopMemberListReq(
    @TarsId(0) @JvmField val uin: Long,
    @TarsId(1) @JvmField val groupCode: Long,
    @TarsId(2) @JvmField val nextUin: Long,
    @TarsId(3) @JvmField val groupUin: Long,
    @TarsId(4) @JvmField val version: Long? = null,
    @TarsId(5) @JvmField val reqType: Long? = null,
    @TarsId(6) @JvmField val getListAppointTime: Long? = null,
    @TarsId(7) @JvmField val richCardNameVer: Byte? = null
) : JceStruct


@Serializable
internal class GetTroopMemberListResp(
    @TarsId(0) @JvmField val uin: Long,
    @TarsId(1) @JvmField val groupCode: Long,
    @TarsId(2) @JvmField val groupUin: Long,
    @TarsId(3) @JvmField val vecTroopMember: List<StTroopMemberInfo>,
    @TarsId(4) @JvmField val nextUin: Long,
    @TarsId(5) @JvmField val result: Int,
    @TarsId(6) @JvmField val errorCode: Short? = null,
    @TarsId(7) @JvmField val officeMode: Long? = null,
    @TarsId(8) @JvmField val nextGetTime: Long? = null
) : JceStruct

@Serializable
internal class StTroopMemberInfo(
    @TarsId(0) @JvmField val memberUin: Long,
    @TarsId(1) @JvmField val faceId: Short,
    @TarsId(2) @JvmField val age: Byte,
    @TarsId(3) @JvmField val gender: Byte,
    @TarsId(4) @JvmField val nick: String = "",
    @TarsId(5) @JvmField val status: Byte = 20,
    @TarsId(6) @JvmField val sShowName: String? = null,
    @TarsId(8) @JvmField val sName: String? = null,
    @TarsId(9) @JvmField val cGender: Byte? = null,
    @TarsId(10) @JvmField val sPhone: String? = "",
    @TarsId(11) @JvmField val sEmail: String? = "",
    @TarsId(12) @JvmField val sMemo: String? = "",
    @TarsId(13) @JvmField val autoRemark: String? = "",
    @TarsId(14) @JvmField val dwMemberLevel: Long? = null,
    @TarsId(15) @JvmField val dwJoinTime: Long? = null,
    @TarsId(16) @JvmField val dwLastSpeakTime: Long? = null,
    @TarsId(17) @JvmField val dwCreditLevel: Long? = null,
    @TarsId(18) @JvmField val dwFlag: Long? = null,
    @TarsId(19) @JvmField val dwFlagExt: Long? = null,
    @TarsId(20) @JvmField val dwPoint: Long? = null,
    @TarsId(21) @JvmField val concerned: Byte? = null,
    @TarsId(22) @JvmField val shielded: Byte? = null,
    @TarsId(23) @JvmField val sSpecialTitle: String? = "",
    @TarsId(24) @JvmField val dwSpecialTitleExpireTime: Long? = null,
    @TarsId(25) @JvmField val job: String? = "",
    @TarsId(26) @JvmField val apolloFlag: Byte? = null,
    @TarsId(27) @JvmField val dwApolloTimestamp: Long? = null,
    @TarsId(28) @JvmField val dwGlobalGroupLevel: Long? = null,
    @TarsId(29) @JvmField val dwTitleId: Long? = null,
    @TarsId(30) @JvmField val dwShutupTimestap: Long? = null,
    @TarsId(31) @JvmField val dwGlobalGroupPoint: Long? = null,
    @TarsId(32) @JvmField val qzusrinfo: QzoneUserInfo? = null,
    @TarsId(33) @JvmField val richCardNameVer: Byte? = null,
    @TarsId(34) @JvmField val dwVipType: Long? = null,
    @TarsId(35) @JvmField val dwVipLevel: Long? = null,
    @TarsId(36) @JvmField val dwBigClubLevel: Long? = null,
    @TarsId(37) @JvmField val dwBigClubFlag: Long? = null,
    @TarsId(38) @JvmField val dwNameplate: Long? = null,
    @TarsId(39) @JvmField val vecGroupHonor: ByteArray? = null
) : JceStruct

@Serializable
internal class QzoneUserInfo(
    @TarsId(0) @JvmField val eStarState: Int? = null,
    @TarsId(1) @JvmField val extendInfo: Map<String, String>? = null
) : JceStruct