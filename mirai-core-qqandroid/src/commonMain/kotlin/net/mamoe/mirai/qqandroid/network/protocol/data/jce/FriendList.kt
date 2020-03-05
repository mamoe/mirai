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
internal class ModifyGroupCardReq(
    @ProtoId(0) val dwZero: Long,
    @ProtoId(1) val dwGroupCode: Long,
    @ProtoId(2) val dwNewSeq: Long,
    @ProtoId(3) val vecUinInfo: List<stUinInfo>
) : JceStruct

@Serializable
internal class stUinInfo(
    @ProtoId(0) val dwuin: Long,
    @ProtoId(1) val dwFlag: Long,
    @ProtoId(2) val sName: String = "",
    @ProtoId(3) val gender: Byte,
    @ProtoId(4) val sPhone: String = "",
    @ProtoId(5) val sEmail: String = "",
    @ProtoId(6) val sRemark: String = ""
) : JceStruct

@Serializable
internal class GetFriendListReq(
    @ProtoId(0) val reqtype: Int? = null,
    @ProtoId(1) val ifReflush: Byte? = null,
    @ProtoId(2) val uin: Long? = null,
    @ProtoId(3) val startIndex: Short? = null,
    @ProtoId(4) val getfriendCount: Short? = null,
    @ProtoId(5) val groupid: Byte? = null,
    @ProtoId(6) val ifGetGroupInfo: Byte? = null,
    @ProtoId(7) val groupstartIndex: Byte? = null,
    @ProtoId(8) val getgroupCount: Byte? = null,
    @ProtoId(9) val ifGetMSFGroup: Byte? = null,
    @ProtoId(10) val ifShowTermType: Byte? = null,
    @ProtoId(11) val version: Long? = null,
    @ProtoId(12) val uinList: List<Long>? = null,
    @ProtoId(13) val eAppType: Int = 0,
    @ProtoId(14) val ifGetDOVId: Byte? = null,
    @ProtoId(15) val ifGetBothFlag: Byte? = null,
    @ProtoId(16) val vec0xd50Req: ByteArray? = null,
    @ProtoId(17) val vec0xd6bReq: ByteArray? = null,
    @ProtoId(18) val vecSnsTypelist: List<Long>? = null
) : JceStruct


@Serializable
internal class GetFriendListResp(
    @ProtoId(0) val reqtype: Int,
    @ProtoId(1) val ifReflush: Byte,
    @ProtoId(2) val uin: Long,
    @ProtoId(3) val startIndex: Short,
    @ProtoId(4) val getfriendCount: Short,
    @ProtoId(5) val totoalFriendCount: Short,
    @ProtoId(6) val friendCount: Short,
    @ProtoId(7) val vecFriendInfo: List<FriendInfo>? = null,
    @ProtoId(8) val groupid: Byte? = null,
    @ProtoId(9) val ifGetGroupInfo: Byte,
    @ProtoId(10) val groupstartIndex: Byte? = null,
    @ProtoId(11) val getgroupCount: Byte? = null,
    @ProtoId(12) val totoalGroupCount: Short? = null,
    @ProtoId(13) val groupCount: Byte? = null,
    @ProtoId(14) val vecGroupInfo: List<GroupInfo>? = null,
    @ProtoId(15) val result: Int,
    @ProtoId(16) val errorCode: Short? = null,
    @ProtoId(17) val onlineFriendCount: Short? = null,
    @ProtoId(18) val serverTime: Long? = null,
    @ProtoId(19) val sqqOnLineCount: Short? = null,
    @ProtoId(20) val vecMSFGroupInfo: List<GroupInfo>? = null,
    @ProtoId(21) val respType: Byte? = null,
    @ProtoId(22) val hasOtherRespFlag: Byte? = null,
    @ProtoId(23) val stSelfInfo: FriendInfo? = null,
    @ProtoId(24) val showPcIcon: Byte? = null,
    @ProtoId(25) val wGetExtSnsRspCode: Short? = null,
    @ProtoId(26) val stSubSrvRspCode: FriendListSubSrvRspCode? = null
) : JceStruct

@Serializable
internal class FriendListSubSrvRspCode(
    @ProtoId(0) val wGetMutualMarkRspCode: Short? = null,
    @ProtoId(1) val wGetIntimateInfoRspCode: Short? = null
) : JceStruct

@Serializable
internal class FriendInfo(
    @ProtoId(0) val friendUin: Long,
    @ProtoId(1) val groupId: Byte,
    @ProtoId(2) val faceId: Short,
    @ProtoId(3) val remark: String = "",
    @ProtoId(4) val sqqtype: Byte,
    @ProtoId(5) val status: Byte = 20,
    @ProtoId(6) val memberLevel: Byte? = null,
    @ProtoId(7) val isMqqOnLine: Byte? = null,
    @ProtoId(8) val sqqOnLineState: Byte? = null,
    @ProtoId(9) val isIphoneOnline: Byte? = null,
    @ProtoId(10) val detalStatusFlag: Byte? = null,
    @ProtoId(11) val sqqOnLineStateV2: Byte? = null,
    @ProtoId(12) val sShowName: String? = "",
    @ProtoId(13) val isRemark: Byte? = null,
    @ProtoId(14) val nick: String? = "",
    @ProtoId(15) val specialFlag: Byte? = null,
    @ProtoId(16) val vecIMGroupID: ByteArray? = null,
    @ProtoId(17) val vecMSFGroupID: ByteArray? = null,
    @ProtoId(18) val iTermType: Int? = null,
    @ProtoId(19) val oVipInfo: VipBaseInfo? = null,
    @ProtoId(20) val network: Byte? = null,
    @ProtoId(21) val vecRing: ByteArray? = null,
    @ProtoId(22) val uAbiFlag: Long? = null,
    @ProtoId(23) val ulFaceAddonId: Long? = null,
    @ProtoId(24) val eNetworkType: Int? = 0,
    @ProtoId(25) val uVipFont: Long? = null,
    @ProtoId(26) val eIconType: Int? = 0,
    @ProtoId(27) val termDesc: String? = "",
    @ProtoId(28) val uColorRing: Long? = null,
    @ProtoId(29) val apolloFlag: Byte? = null,
    @ProtoId(30) val uApolloTimestamp: Long? = null,
    @ProtoId(31) val sex: Byte? = null,
    @ProtoId(32) val uFounderFont: Long? = null,
    @ProtoId(33) val eimId: String? = "",
    @ProtoId(34) val eimMobile: String? = "",
    @ProtoId(35) val olympicTorch: Byte? = null,
    @ProtoId(36) val uApolloSignTime: Long? = null,
    @ProtoId(37) val uLaviUin: Long? = null,
    @ProtoId(38) val uTagUpdateTime: Long? = null,
    @ProtoId(39) val uGameLastLoginTime: Long? = null,
    @ProtoId(40) val uGameAppid: Long? = null,
    @ProtoId(41) val vecCardID: ByteArray? = null,
    @ProtoId(42) val ulBitSet: Long? = null,
    @ProtoId(43) val kingOfGloryFlag: Byte? = null,
    @ProtoId(44) val ulKingOfGloryRank: Long? = null,
    @ProtoId(45) val masterUin: String? = "",
    @ProtoId(46) val uLastMedalUpdateTime: Long? = null,
    @ProtoId(47) val uFaceStoreId: Long? = null,
    @ProtoId(48) val uFontEffect: Long? = null,
    @ProtoId(49) val sDOVId: String? = "",
    @ProtoId(50) val uBothFlag: Long? = null,
    @ProtoId(51) val centiShow3DFlag: Byte? = null,
    @ProtoId(52) val vecIntimateInfo: ByteArray? = null,
    @ProtoId(53) val showNameplate: Byte? = null,
    @ProtoId(54) val newLoverDiamondFlag: Byte? = null,
    @ProtoId(55) val vecExtSnsFrdData: ByteArray? = null,
    @ProtoId(56) val vecMutualMarkData: ByteArray? = null
) : JceStruct

@Serializable
internal class VipBaseInfo(
    @ProtoId(0) val mOpenInfo: Map<Int, VipOpenInfo>
) : JceStruct

@Serializable
internal class VipOpenInfo(
    @ProtoId(0) val open: Boolean,
    @ProtoId(1) val iVipType: Int = -1,
    @ProtoId(2) val iVipLevel: Int = -1,
    @ProtoId(3) val iVipFlag: Int? = null,
    @ProtoId(4) val nameplateId: Long? = null
) : JceStruct

@Serializable
internal class GroupInfo(
    @ProtoId(0) val groupId: Byte,
    @ProtoId(1) val groupname: String = "",
    @ProtoId(2) val friendCount: Int,
    @ProtoId(3) val onlineFriendCount: Int,
    @ProtoId(4) val seqid: Byte? = null,
    @ProtoId(5) val sqqOnLineCount: Int? = null
) : JceStruct

