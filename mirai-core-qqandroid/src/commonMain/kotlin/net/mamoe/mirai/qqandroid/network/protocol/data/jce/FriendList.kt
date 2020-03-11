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
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.serialization.jce.JceId

@Serializable
internal class ModifyGroupCardReq(
    @JceId(0) val dwZero: Long,
    @JceId(1) val dwGroupCode: Long,
    @JceId(2) val dwNewSeq: Long,
    @JceId(3) val vecUinInfo: List<stUinInfo>
) : JceStruct

@Serializable
internal class stUinInfo(
    @JceId(0) val dwuin: Long,
    @JceId(1) val dwFlag: Long,
    @JceId(2) val sName: String = "",
    @JceId(3) val gender: Byte,
    @JceId(4) val sPhone: String = "",
    @JceId(5) val sEmail: String = "",
    @JceId(6) val sRemark: String = ""
) : JceStruct

@Serializable
internal class GetFriendListReq(
    @JceId(0) val reqtype: Int? = null,
    @JceId(1) val ifReflush: Byte? = null,
    @JceId(2) val uin: Long? = null,
    @JceId(3) val startIndex: Short? = null,
    @JceId(4) val getfriendCount: Short? = null,
    @JceId(5) val groupid: Byte? = null,
    @JceId(6) val ifGetGroupInfo: Byte? = null,
    @JceId(7) val groupstartIndex: Byte? = null,
    @JceId(8) val getgroupCount: Byte? = null,
    @JceId(9) val ifGetMSFGroup: Byte? = null,
    @JceId(10) val ifShowTermType: Byte? = null,
    @JceId(11) val version: Long? = null,
    @JceId(12) val uinList: List<Long>? = null,
    @JceId(13) val eAppType: Int = 0,
    @JceId(14) val ifGetDOVId: Byte? = null,
    @JceId(15) val ifGetBothFlag: Byte? = null,
    @JceId(16) val vec0xd50Req: ByteArray? = null,
    @JceId(17) val vec0xd6bReq: ByteArray? = null,
    @JceId(18) val vecSnsTypelist: List<Long>? = null
) : JceStruct


@Serializable
internal class GetFriendListResp(
    @JceId(0) val reqtype: Int,
    @JceId(1) val ifReflush: Byte,
    @JceId(2) val uin: Long,
    @JceId(3) val startIndex: Short,
    @JceId(4) val getfriendCount: Short,
    @JceId(5) val totoalFriendCount: Short,
    @JceId(6) val friendCount: Short,
    @JceId(7) val vecFriendInfo: List<FriendInfo>? = null,
    @JceId(8) val groupid: Byte? = null,
    @JceId(9) val ifGetGroupInfo: Byte,
    @JceId(10) val groupstartIndex: Byte? = null,
    @JceId(11) val getgroupCount: Byte? = null,
    @JceId(12) val totoalGroupCount: Short? = null,
    @JceId(13) val groupCount: Byte? = null,
    @JceId(14) val vecGroupInfo: List<GroupInfo>? = null,
    @JceId(15) val result: Int,
    @JceId(16) val errorCode: Short? = null,
    @JceId(17) val onlineFriendCount: Short? = null,
    @JceId(18) val serverTime: Long? = null,
    @JceId(19) val sqqOnLineCount: Short? = null,
    @JceId(20) val vecMSFGroupInfo: List<GroupInfo>? = null,
    @JceId(21) val respType: Byte? = null,
    @JceId(22) val hasOtherRespFlag: Byte? = null,
    @JceId(23) val stSelfInfo: FriendInfo? = null,
    @JceId(24) val showPcIcon: Byte? = null,
    @JceId(25) val wGetExtSnsRspCode: Short? = null,
    @JceId(26) val stSubSrvRspCode: FriendListSubSrvRspCode? = null
) : JceStruct

@Serializable
internal class FriendListSubSrvRspCode(
    @JceId(0) val wGetMutualMarkRspCode: Short? = null,
    @JceId(1) val wGetIntimateInfoRspCode: Short? = null
) : JceStruct

@Serializable
internal class FriendInfo(
    @JceId(0) val friendUin: Long,
    @JceId(1) val groupId: Byte,
    @JceId(2) val faceId: Short,
    @JceId(3) val remark: String = "",
    @JceId(4) val sqqtype: Byte,
    @JceId(5) val status: Byte = 20,
    @JceId(6) val memberLevel: Byte? = null,
    @JceId(7) val isMqqOnLine: Byte? = null,
    @JceId(8) val sqqOnLineState: Byte? = null,
    @JceId(9) val isIphoneOnline: Byte? = null,
    @JceId(10) val detalStatusFlag: Byte? = null,
    @JceId(11) val sqqOnLineStateV2: Byte? = null,
    @JceId(12) val sShowName: String? = "",
    @JceId(13) val isRemark: Byte? = null,
    @JceId(14) val nick: String? = "",
    @JceId(15) val specialFlag: Byte? = null,
    @JceId(16) val vecIMGroupID: ByteArray? = null,
    @JceId(17) val vecMSFGroupID: ByteArray? = null,
    @JceId(18) val iTermType: Int? = null,
    @JceId(19) val oVipInfo: VipBaseInfo? = null,
    @JceId(20) val network: Byte? = null,
    @JceId(21) val vecRing: ByteArray? = null,
    @JceId(22) val uAbiFlag: Long? = null,
    @JceId(23) val ulFaceAddonId: Long? = null,
    @JceId(24) val eNetworkType: Int? = 0,
    @JceId(25) val uVipFont: Long? = null,
    @JceId(26) val eIconType: Int? = 0,
    @JceId(27) val termDesc: String? = "",
    @JceId(28) val uColorRing: Long? = null,
    @JceId(29) val apolloFlag: Byte? = null,
    @JceId(30) val uApolloTimestamp: Long? = null,
    @JceId(31) val sex: Byte? = null,
    @JceId(32) val uFounderFont: Long? = null,
    @JceId(33) val eimId: String? = "",
    @JceId(34) val eimMobile: String? = "",
    @JceId(35) val olympicTorch: Byte? = null,
    @JceId(36) val uApolloSignTime: Long? = null,
    @JceId(37) val uLaviUin: Long? = null,
    @JceId(38) val uTagUpdateTime: Long? = null,
    @JceId(39) val uGameLastLoginTime: Long? = null,
    @JceId(40) val uGameAppid: Long? = null,
    @JceId(41) val vecCardID: ByteArray? = null,
    @JceId(42) val ulBitSet: Long? = null,
    @JceId(43) val kingOfGloryFlag: Byte? = null,
    @JceId(44) val ulKingOfGloryRank: Long? = null,
    @JceId(45) val masterUin: String? = "",
    @JceId(46) val uLastMedalUpdateTime: Long? = null,
    @JceId(47) val uFaceStoreId: Long? = null,
    @JceId(48) val uFontEffect: Long? = null,
    @JceId(49) val sDOVId: String? = "",
    @JceId(50) val uBothFlag: Long? = null,
    @JceId(51) val centiShow3DFlag: Byte? = null,
    @JceId(52) val vecIntimateInfo: ByteArray? = null,
    @JceId(53) val showNameplate: Byte? = null,
    @JceId(54) val newLoverDiamondFlag: Byte? = null,
    @JceId(55) val vecExtSnsFrdData: ByteArray? = null,
    @JceId(56) val vecMutualMarkData: ByteArray? = null
) : JceStruct

@Serializable
internal class VipBaseInfo(
    @JceId(0) val mOpenInfo: Map<Int, VipOpenInfo>
) : JceStruct

@Serializable
internal class VipOpenInfo(
    @JceId(0) val open: Boolean,
    @JceId(1) val iVipType: Int = -1,
    @JceId(2) val iVipLevel: Int = -1,
    @JceId(3) val iVipFlag: Int? = null,
    @JceId(4) val nameplateId: Long? = null
) : JceStruct

@Serializable
internal class GroupInfo(
    @JceId(0) val groupId: Byte,
    @JceId(1) val groupname: String = "",
    @JceId(2) val friendCount: Int,
    @JceId(3) val onlineFriendCount: Int,
    @JceId(4) val seqid: Byte? = null,
    @JceId(5) val sqqOnLineCount: Int? = null
) : JceStruct

