package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class GetTroopListReqV2Simplify(
    @SerialId(0) val uin: Long,
    @SerialId(1) val getMSFMsgFlag: Byte? = null,
    @SerialId(2) val vecCookies: ByteArray? = null,
    @SerialId(3) val vecGroupInfo: List<stTroopNumSimplify>? = null,
    @SerialId(4) val groupFlagExt: Byte? = null,
    @SerialId(5) val shVersion: Int? = null,
    @SerialId(6) val dwCompanyId: Long? = null,
    @SerialId(7) val versionNum: Long? = null,
    @SerialId(8) val getLongGroupName: Byte? = null
) : JceStruct

@Serializable
internal class stTroopNumSimplify(
    @SerialId(0) val groupCode: Long,
    @SerialId(1) val dwGroupInfoSeq: Long? = null,
    @SerialId(2) val dwGroupFlagExt: Long? = null,
    @SerialId(3) val dwGroupRankSeq: Long? = null
) : JceStruct


@Serializable
internal class GetTroopListRespV2(
    @SerialId(0) val uin: Long,
    @SerialId(1) val troopcount: Short,
    @SerialId(2) val result: Int,
    @SerialId(3) val errorCode: Short? = null,
    @SerialId(4) val vecCookies: ByteArray? = null,
    @SerialId(5) val vecTroopList: List<stTroopNum>? = null,
    @SerialId(6) val vecTroopListDel: List<stTroopNum>? = null,
    @SerialId(7) val vecTroopRank: List<stGroupRankInfo>? = null,
    @SerialId(8) val vecFavGroup: List<stFavoriteGroup>? = null,
    @SerialId(9) val vecTroopListExt: List<stTroopNum>? = null
) : JceStruct


@Serializable
internal class stTroopNum(
    @SerialId(0) val groupUin: Long,
    @SerialId(1) val groupCode: Long,
    @SerialId(2) val flag: Byte? = null,
    @SerialId(3) val dwGroupInfoSeq: Long? = null,
    @SerialId(4) val groupName: String = "",
    @SerialId(5) val groupMemo: String = "",
    @SerialId(6) val dwGroupFlagExt: Long? = null,
    @SerialId(7) val dwGroupRankSeq: Long? = null,
    @SerialId(8) val dwCertificationType: Long? = null,
    @SerialId(9) val dwShutupTimestamp: Long? = null,
    @SerialId(10) val dwMyShutupTimestamp: Long? = null,
    @SerialId(11) val dwCmdUinUinFlag: Long? = null,
    @SerialId(12) val dwAdditionalFlag: Long? = null,
    @SerialId(13) val dwGroupTypeFlag: Long? = null,
    @SerialId(14) val dwGroupSecType: Long? = null,
    @SerialId(15) val dwGroupSecTypeInfo: Long? = null,
    @SerialId(16) val dwGroupClassExt: Long? = null,
    @SerialId(17) val dwAppPrivilegeFlag: Long? = null,
    @SerialId(18) val dwSubscriptionUin: Long? = null,
    @SerialId(19) val dwMemberNum: Long? = null,
    @SerialId(20) val dwMemberNumSeq: Long? = null,
    @SerialId(21) val dwMemberCardSeq: Long? = null,
    @SerialId(22) val dwGroupFlagExt3: Long? = null,
    @SerialId(23) val dwGroupOwnerUin: Long,
    @SerialId(24) val isConfGroup: Byte? = null,
    @SerialId(25) val isModifyConfGroupFace: Byte? = null,
    @SerialId(26) val isModifyConfGroupName: Byte? = null,
    @SerialId(27) val dwCmduinJoinTime: Long? = null,
    @SerialId(28) val ulCompanyId: Long? = null,
    @SerialId(29) val dwMaxGroupMemberNum: Long? = null,
    @SerialId(30) val dwCmdUinGroupMask: Long? = null,
    @SerialId(31) val udwHLGuildAppid: Long? = null,
    @SerialId(32) val udwHLGuildSubType: Long? = null,
    @SerialId(33) val udwCmdUinRingtoneID: Long? = null,
    @SerialId(34) val udwCmdUinFlagEx2: Long? = null
) : JceStruct

@Serializable
internal class stGroupRankInfo(
    @SerialId(0) val dwGroupCode: Long,
    @SerialId(1) val groupRankSysFlag: Byte? = null,
    @SerialId(2) val groupRankUserFlag: Byte? = null,
    @SerialId(3) val vecRankMap: List<stLevelRankPair>? = null,
    @SerialId(4) val dwGroupRankSeq: Long? = null,
    @SerialId(5) val ownerName: String? = "",
    @SerialId(6) val adminName: String? = "",
    @SerialId(7) val dwOfficeMode: Long? = null
) : JceStruct

@Serializable
internal class stFavoriteGroup(
    @SerialId(0) val dwGroupCode: Long,
    @SerialId(1) val dwTimestamp: Long? = null,
    @SerialId(2) val dwSnsFlag: Long? = 1L,
    @SerialId(3) val dwOpenTimestamp: Long? = null
) : JceStruct

@Serializable
internal class stLevelRankPair(
    @SerialId(0) val dwLevel: Long? = null,
    @SerialId(1) val rank: String? = ""
) : JceStruct

@Serializable
internal class GetTroopMemberListReq(
    @SerialId(0) val uin: Long,
    @SerialId(1) val groupCode: Long,
    @SerialId(2) val nextUin: Long,
    @SerialId(3) val groupUin: Long,
    @SerialId(4) val version: Long? = null,
    @SerialId(5) val reqType: Long? = null,
    @SerialId(6) val getListAppointTime: Long? = null,
    @SerialId(7) val richCardNameVer: Byte? = null
) : JceStruct


@Serializable
internal class GetTroopMemberListResp(
    @SerialId(0) val uin: Long,
    @SerialId(1) val groupCode: Long,
    @SerialId(2) val groupUin: Long,
    @SerialId(3) val vecTroopMember: List<stTroopMemberInfo>,
    @SerialId(4) val nextUin: Long,
    @SerialId(5) val result: Int,
    @SerialId(6) val errorCode: Short? = null,
    @SerialId(7) val officeMode: Long? = null,
    @SerialId(8) val nextGetTime: Long? = null
) : JceStruct

@Serializable
internal class stTroopMemberInfo(
    @SerialId(0) val memberUin: Long,
    @SerialId(1) val faceId: Short,
    @SerialId(2) val age: Byte,
    @SerialId(3) val gender: Byte,
    @SerialId(4) val nick: String = "",
    @SerialId(5) val status: Byte = 20,
    @SerialId(6) val sShowName: String? = "",
    @SerialId(8) val sName: String? = "",
    @SerialId(9) val cGender: Byte? = null,
    @SerialId(10) val sPhone: String? = "",
    @SerialId(11) val sEmail: String? = "",
    @SerialId(12) val sMemo: String? = "",
    @SerialId(13) val autoRemark: String? = "",
    @SerialId(14) val dwMemberLevel: Long? = null,
    @SerialId(15) val dwJoinTime: Long? = null,
    @SerialId(16) val dwLastSpeakTime: Long? = null,
    @SerialId(17) val dwCreditLevel: Long? = null,
    @SerialId(18) val dwFlag: Long? = null,
    @SerialId(19) val dwFlagExt: Long? = null,
    @SerialId(20) val dwPoint: Long? = null,
    @SerialId(21) val concerned: Byte? = null,
    @SerialId(22) val shielded: Byte? = null,
    @SerialId(23) val sSpecialTitle: String? = "",
    @SerialId(24) val dwSpecialTitleExpireTime: Long? = null,
    @SerialId(25) val job: String? = "",
    @SerialId(26) val apolloFlag: Byte? = null,
    @SerialId(27) val dwApolloTimestamp: Long? = null,
    @SerialId(28) val dwGlobalGroupLevel: Long? = null,
    @SerialId(29) val dwTitleId: Long? = null,
    @SerialId(30) val dwShutupTimestap: Long? = null,
    @SerialId(31) val dwGlobalGroupPoint: Long? = null,
    @SerialId(32) val qzusrinfo: QzoneUserInfo? = null,
    @SerialId(33) val richCardNameVer: Byte? = null,
    @SerialId(34) val dwVipType: Long? = null,
    @SerialId(35) val dwVipLevel: Long? = null,
    @SerialId(36) val dwBigClubLevel: Long? = null,
    @SerialId(37) val dwBigClubFlag: Long? = null,
    @SerialId(38) val dwNameplate: Long? = null,
    @SerialId(39) val vecGroupHonor: ByteArray? = null
) : JceStruct

@Serializable
internal class QzoneUserInfo(
    @SerialId(0) val eStarState: Int? = null,
    @SerialId(1) val extendInfo: Map<String, String>? = null
) : JceStruct