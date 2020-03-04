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
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class GetTroopListReqV2Simplify(
    @ProtoId(0) val uin: Long,
    @ProtoId(1) val getMSFMsgFlag: Byte? = null,
    @ProtoId(2) val vecCookies: ByteArray? = null,
    @ProtoId(3) val vecGroupInfo: List<StTroopNumSimplify>? = null,
    @ProtoId(4) val groupFlagExt: Byte? = null,
    @ProtoId(5) val shVersion: Int? = null,
    @ProtoId(6) val dwCompanyId: Long? = null,
    @ProtoId(7) val versionNum: Long? = null,
    @ProtoId(8) val getLongGroupName: Byte? = null
) : JceStruct

@Serializable
internal class StTroopNumSimplify(
    @ProtoId(0) val groupCode: Long,
    @ProtoId(1) val dwGroupInfoSeq: Long? = null,
    @ProtoId(2) val dwGroupFlagExt: Long? = null,
    @ProtoId(3) val dwGroupRankSeq: Long? = null
) : JceStruct


@Serializable
internal class GetTroopListRespV2(
    @ProtoId(0) val uin: Long,
    @ProtoId(1) val troopCount: Short,
    @ProtoId(2) val result: Int,
    @ProtoId(3) val errorCode: Short? = null,
    @ProtoId(4) val vecCookies: ByteArray? = null,
    @ProtoId(5) val vecTroopList: List<StTroopNum>? = null,
    @ProtoId(6) val vecTroopListDel: List<StTroopNum>? = null,
    @ProtoId(7) val vecTroopRank: List<StGroupRankInfo>? = null,
    @ProtoId(8) val vecFavGroup: List<StFavoriteGroup>? = null,
    @ProtoId(9) val vecTroopListExt: List<StTroopNum>? = null
) : JceStruct


@Serializable
internal class StTroopNum(
    @ProtoId(0) val groupUin: Long,
    @ProtoId(1) val groupCode: Long,
    @ProtoId(2) val flag: Byte? = null,
    @ProtoId(3) val dwGroupInfoSeq: Long? = null,
    @ProtoId(4) val groupName: String = "",
    @ProtoId(5) val groupMemo: String = "",
    @ProtoId(6) val dwGroupFlagExt: Long? = null,
    @ProtoId(7) val dwGroupRankSeq: Long? = null,
    @ProtoId(8) val dwCertificationType: Long? = null,
    @ProtoId(9) val dwShutUpTimestamp: Long? = null,
    @ProtoId(10) val dwMyShutUpTimestamp: Long? = null,
    @ProtoId(11) val dwCmdUinUinFlag: Long? = null,
    @ProtoId(12) val dwAdditionalFlag: Long? = null,
    @ProtoId(13) val dwGroupTypeFlag: Long? = null,
    @ProtoId(14) val dwGroupSecType: Long? = null,
    @ProtoId(15) val dwGroupSecTypeInfo: Long? = null,
    @ProtoId(16) val dwGroupClassExt: Long? = null,
    @ProtoId(17) val dwAppPrivilegeFlag: Long? = null,
    @ProtoId(18) val dwSubscriptionUin: Long? = null,
    @ProtoId(19) val dwMemberNum: Long? = null,
    @ProtoId(20) val dwMemberNumSeq: Long? = null,
    @ProtoId(21) val dwMemberCardSeq: Long? = null,
    @ProtoId(22) val dwGroupFlagExt3: Long? = null,
    @ProtoId(23) val dwGroupOwnerUin: Long,
    @ProtoId(24) val isConfGroup: Byte? = null,
    @ProtoId(25) val isModifyConfGroupFace: Byte? = null,
    @ProtoId(26) val isModifyConfGroupName: Byte? = null,
    @ProtoId(27) val dwCmduinJoinTime: Long? = null,
    @ProtoId(28) val ulCompanyId: Long? = null,
    @ProtoId(29) val dwMaxGroupMemberNum: Long? = null,
    @ProtoId(30) val dwCmdUinGroupMask: Long? = null,
    @ProtoId(31) val udwHLGuildAppid: Long? = null,
    @ProtoId(32) val udwHLGuildSubType: Long? = null,
    @ProtoId(33) val udwCmdUinRingtoneID: Long? = null,
    @ProtoId(34) val udwCmdUinFlagEx2: Long? = null
) : JceStruct

@Serializable
internal class StGroupRankInfo(
    @ProtoId(0) val dwGroupCode: Long,
    @ProtoId(1) val groupRankSysFlag: Byte? = null,
    @ProtoId(2) val groupRankUserFlag: Byte? = null,
    @ProtoId(3) val vecRankMap: List<StLevelRankPair>? = null,
    @ProtoId(4) val dwGroupRankSeq: Long? = null,
    @ProtoId(5) val ownerName: String? = "",
    @ProtoId(6) val adminName: String? = "",
    @ProtoId(7) val dwOfficeMode: Long? = null
) : JceStruct

@Serializable
internal class StFavoriteGroup(
    @ProtoId(0) val dwGroupCode: Long,
    @ProtoId(1) val dwTimestamp: Long? = null,
    @ProtoId(2) val dwSnsFlag: Long? = 1L,
    @ProtoId(3) val dwOpenTimestamp: Long? = null
) : JceStruct

@Serializable
internal class StLevelRankPair(
    @ProtoId(0) val dwLevel: Long? = null,
    @ProtoId(1) val rank: String? = ""
) : JceStruct

@Serializable
internal class GetTroopMemberListReq(
    @ProtoId(0) val uin: Long,
    @ProtoId(1) val groupCode: Long,
    @ProtoId(2) val nextUin: Long,
    @ProtoId(3) val groupUin: Long,
    @ProtoId(4) val version: Long? = null,
    @ProtoId(5) val reqType: Long? = null,
    @ProtoId(6) val getListAppointTime: Long? = null,
    @ProtoId(7) val richCardNameVer: Byte? = null
) : JceStruct


@Serializable
internal class GetTroopMemberListResp(
    @ProtoId(0) val uin: Long,
    @ProtoId(1) val groupCode: Long,
    @ProtoId(2) val groupUin: Long,
    @ProtoId(3) val vecTroopMember: List<StTroopMemberInfo>,
    @ProtoId(4) val nextUin: Long,
    @ProtoId(5) val result: Int,
    @ProtoId(6) val errorCode: Short? = null,
    @ProtoId(7) val officeMode: Long? = null,
    @ProtoId(8) val nextGetTime: Long? = null
) : JceStruct

@Serializable
internal class StTroopMemberInfo(
    @ProtoId(0) val memberUin: Long,
    @ProtoId(1) val faceId: Short,
    @ProtoId(2) val age: Byte,
    @ProtoId(3) val gender: Byte,
    @ProtoId(4) val nick: String = "",
    @ProtoId(5) val status: Byte = 20,
    @ProtoId(6) val sShowName: String? = null,
    @ProtoId(8) val sName: String? = null,
    @ProtoId(9) val cGender: Byte? = null,
    @ProtoId(10) val sPhone: String? = "",
    @ProtoId(11) val sEmail: String? = "",
    @ProtoId(12) val sMemo: String? = "",
    @ProtoId(13) val autoRemark: String? = "",
    @ProtoId(14) val dwMemberLevel: Long? = null,
    @ProtoId(15) val dwJoinTime: Long? = null,
    @ProtoId(16) val dwLastSpeakTime: Long? = null,
    @ProtoId(17) val dwCreditLevel: Long? = null,
    @ProtoId(18) val dwFlag: Long? = null,
    @ProtoId(19) val dwFlagExt: Long? = null,
    @ProtoId(20) val dwPoint: Long? = null,
    @ProtoId(21) val concerned: Byte? = null,
    @ProtoId(22) val shielded: Byte? = null,
    @ProtoId(23) val sSpecialTitle: String? = "",
    @ProtoId(24) val dwSpecialTitleExpireTime: Long? = null,
    @ProtoId(25) val job: String? = "",
    @ProtoId(26) val apolloFlag: Byte? = null,
    @ProtoId(27) val dwApolloTimestamp: Long? = null,
    @ProtoId(28) val dwGlobalGroupLevel: Long? = null,
    @ProtoId(29) val dwTitleId: Long? = null,
    @ProtoId(30) val dwShutupTimestap: Long? = null,
    @ProtoId(31) val dwGlobalGroupPoint: Long? = null,
    @ProtoId(32) val qzusrinfo: QzoneUserInfo? = null,
    @ProtoId(33) val richCardNameVer: Byte? = null,
    @ProtoId(34) val dwVipType: Long? = null,
    @ProtoId(35) val dwVipLevel: Long? = null,
    @ProtoId(36) val dwBigClubLevel: Long? = null,
    @ProtoId(37) val dwBigClubFlag: Long? = null,
    @ProtoId(38) val dwNameplate: Long? = null,
    @ProtoId(39) val vecGroupHonor: ByteArray? = null
) : JceStruct

@Serializable
internal class QzoneUserInfo(
    @ProtoId(0) val eStarState: Int? = null,
    @ProtoId(1) val extendInfo: Map<String, String>? = null
) : JceStruct