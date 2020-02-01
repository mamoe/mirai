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
    @SerialId(4) val groupName: String? = "",
    @SerialId(5) val groupMemo: String? = "",
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
    @SerialId(23) val dwGroupOwnerUin: Long? = null,
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