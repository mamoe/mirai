/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.JceId

@Serializable
internal class GetTroopListReqV2Simplify(
    @JceId(0) val uin: Long,
    @JceId(1) val getMSFMsgFlag: Byte? = null,
    @JceId(2) val vecCookies: ByteArray? = null,
    @JceId(3) val vecGroupInfo: List<StTroopNumSimplify>? = null,
    @JceId(4) val groupFlagExt: Byte? = null,
    @JceId(5) val shVersion: Int? = null,
    @JceId(6) val dwCompanyId: Long? = null,
    @JceId(7) val versionNum: Long? = null,
    @JceId(8) val getLongGroupName: Byte? = null
) : JceStruct

@Serializable
internal class StTroopNumSimplify(
    @JceId(0) val groupCode: Long,
    @JceId(1) val dwGroupInfoSeq: Long? = null,
    @JceId(2) val dwGroupFlagExt: Long? = null,
    @JceId(3) val dwGroupRankSeq: Long? = null
) : JceStruct


@Serializable
internal class GetTroopListRespV2(
    @JceId(0) val uin: Long,
    @JceId(1) val troopCount: Short,
    @JceId(2) val result: Int,
    @JceId(3) val errorCode: Short? = null,
    @JceId(4) val vecCookies: ByteArray? = null,
    @JceId(5) val vecTroopList: List<StTroopNum>? = null,
    @JceId(6) val vecTroopListDel: List<StTroopNum>? = null,
    @JceId(7) val vecTroopRank: List<StGroupRankInfo>? = null,
    @JceId(8) val vecFavGroup: List<StFavoriteGroup>? = null,
    @JceId(9) val vecTroopListExt: List<StTroopNum>? = null
) : JceStruct


@Serializable
internal class StTroopNum(
    @JceId(0) val groupUin: Long,
    @JceId(1) val groupCode: Long,
    @JceId(2) val flag: Byte? = null,
    @JceId(3) val dwGroupInfoSeq: Long? = null,
    @JceId(4) val groupName: String = "",
    @JceId(5) val groupMemo: String = "",
    @JceId(6) val dwGroupFlagExt: Long? = null,
    @JceId(7) val dwGroupRankSeq: Long? = null,
    @JceId(8) val dwCertificationType: Long? = null,
    @JceId(9) val dwShutUpTimestamp: Long? = null,
    @JceId(10) val dwMyShutUpTimestamp: Long? = null,
    @JceId(11) val dwCmdUinUinFlag: Long? = null,
    @JceId(12) val dwAdditionalFlag: Long? = null,
    @JceId(13) val dwGroupTypeFlag: Long? = null,
    @JceId(14) val dwGroupSecType: Long? = null,
    @JceId(15) val dwGroupSecTypeInfo: Long? = null,
    @JceId(16) val dwGroupClassExt: Long? = null,
    @JceId(17) val dwAppPrivilegeFlag: Long? = null,
    @JceId(18) val dwSubscriptionUin: Long? = null,
    @JceId(19) val dwMemberNum: Long? = null,
    @JceId(20) val dwMemberNumSeq: Long? = null,
    @JceId(21) val dwMemberCardSeq: Long? = null,
    @JceId(22) val dwGroupFlagExt3: Long? = null,
    @JceId(23) val dwGroupOwnerUin: Long,
    @JceId(24) val isConfGroup: Byte? = null,
    @JceId(25) val isModifyConfGroupFace: Byte? = null,
    @JceId(26) val isModifyConfGroupName: Byte? = null,
    @JceId(27) val dwCmduinJoinTime: Long? = null,
    @JceId(28) val ulCompanyId: Long? = null,
    @JceId(29) val dwMaxGroupMemberNum: Long? = null,
    @JceId(30) val dwCmdUinGroupMask: Long? = null,
    @JceId(31) val udwHLGuildAppid: Long? = null,
    @JceId(32) val udwHLGuildSubType: Long? = null,
    @JceId(33) val udwCmdUinRingtoneID: Long? = null,
    @JceId(34) val udwCmdUinFlagEx2: Long? = null
) : JceStruct

@Serializable
internal class StGroupRankInfo(
    @JceId(0) val dwGroupCode: Long,
    @JceId(1) val groupRankSysFlag: Byte? = null,
    @JceId(2) val groupRankUserFlag: Byte? = null,
    @JceId(3) val vecRankMap: List<StLevelRankPair>? = null,
    @JceId(4) val dwGroupRankSeq: Long? = null,
    @JceId(5) val ownerName: String? = "",
    @JceId(6) val adminName: String? = "",
    @JceId(7) val dwOfficeMode: Long? = null
) : JceStruct

@Serializable
internal class StFavoriteGroup(
    @JceId(0) val dwGroupCode: Long,
    @JceId(1) val dwTimestamp: Long? = null,
    @JceId(2) val dwSnsFlag: Long? = 1L,
    @JceId(3) val dwOpenTimestamp: Long? = null
) : JceStruct

@Serializable
internal class StLevelRankPair(
    @JceId(0) val dwLevel: Long? = null,
    @JceId(1) val rank: String? = ""
) : JceStruct

@Serializable
internal class GetTroopMemberListReq(
    @JceId(0) val uin: Long,
    @JceId(1) val groupCode: Long,
    @JceId(2) val nextUin: Long,
    @JceId(3) val groupUin: Long,
    @JceId(4) val version: Long? = null,
    @JceId(5) val reqType: Long? = null,
    @JceId(6) val getListAppointTime: Long? = null,
    @JceId(7) val richCardNameVer: Byte? = null
) : JceStruct


@Serializable
internal class GetTroopMemberListResp(
    @JceId(0) val uin: Long,
    @JceId(1) val groupCode: Long,
    @JceId(2) val groupUin: Long,
    @JceId(3) val vecTroopMember: List<StTroopMemberInfo>,
    @JceId(4) val nextUin: Long,
    @JceId(5) val result: Int,
    @JceId(6) val errorCode: Short? = null,
    @JceId(7) val officeMode: Long? = null,
    @JceId(8) val nextGetTime: Long? = null
) : JceStruct

@Serializable
internal class StTroopMemberInfo(
    @JceId(0) val memberUin: Long,
    @JceId(1) val faceId: Short,
    @JceId(2) val age: Byte,
    @JceId(3) val gender: Byte,
    @JceId(4) val nick: String = "",
    @JceId(5) val status: Byte = 20,
    @JceId(6) val sShowName: String? = null,
    @JceId(8) val sName: String? = null,
    @JceId(9) val cGender: Byte? = null,
    @JceId(10) val sPhone: String? = "",
    @JceId(11) val sEmail: String? = "",
    @JceId(12) val sMemo: String? = "",
    @JceId(13) val autoRemark: String? = "",
    @JceId(14) val dwMemberLevel: Long? = null,
    @JceId(15) val dwJoinTime: Long? = null,
    @JceId(16) val dwLastSpeakTime: Long? = null,
    @JceId(17) val dwCreditLevel: Long? = null,
    @JceId(18) val dwFlag: Long? = null,
    @JceId(19) val dwFlagExt: Long? = null,
    @JceId(20) val dwPoint: Long? = null,
    @JceId(21) val concerned: Byte? = null,
    @JceId(22) val shielded: Byte? = null,
    @JceId(23) val sSpecialTitle: String? = "",
    @JceId(24) val dwSpecialTitleExpireTime: Long? = null,
    @JceId(25) val job: String? = "",
    @JceId(26) val apolloFlag: Byte? = null,
    @JceId(27) val dwApolloTimestamp: Long? = null,
    @JceId(28) val dwGlobalGroupLevel: Long? = null,
    @JceId(29) val dwTitleId: Long? = null,
    @JceId(30) val dwShutupTimestap: Long? = null,
    @JceId(31) val dwGlobalGroupPoint: Long? = null,
    @JceId(32) val qzusrinfo: QzoneUserInfo? = null,
    @JceId(33) val richCardNameVer: Byte? = null,
    @JceId(34) val dwVipType: Long? = null,
    @JceId(35) val dwVipLevel: Long? = null,
    @JceId(36) val dwBigClubLevel: Long? = null,
    @JceId(37) val dwBigClubFlag: Long? = null,
    @JceId(38) val dwNameplate: Long? = null,
    @JceId(39) val vecGroupHonor: ByteArray? = null
) : JceStruct

@Serializable
internal class QzoneUserInfo(
    @JceId(0) val eStarState: Int? = null,
    @JceId(1) val extendInfo: Map<String, String>? = null
) : JceStruct