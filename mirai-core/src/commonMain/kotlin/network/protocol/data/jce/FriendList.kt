/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId


@Serializable
internal class DelFriendReq(
    @JvmField @TarsId(0) val uin: Long,
    @JvmField @TarsId(1) val delUin: Long,
    @JvmField @TarsId(2) val delType: Byte,
    @JvmField @TarsId(3) val version: Int? = null
) : JceStruct

@Serializable
internal class DelFriendResp(
    @JvmField @TarsId(0) val uin: Long,
    @JvmField @TarsId(1) val delUin: Long,
    @JvmField @TarsId(2) val result: Int,
    @JvmField @TarsId(3) val errorCode: Short? = null
) : JceStruct

@Serializable
internal class ModifyGroupCardReq(
    @TarsId(0) @JvmField val dwZero: Long,
    @TarsId(1) @JvmField val dwGroupCode: Long,
    @TarsId(2) @JvmField val dwNewSeq: Long,
    @TarsId(3) @JvmField val vecUinInfo: List<stUinInfo>
) : JceStruct

@Serializable
internal class stUinInfo(
    @TarsId(0) @JvmField val dwuin: Long,
    @TarsId(1) @JvmField val dwFlag: Long,
    @TarsId(2) @JvmField val sName: String = "",
    @TarsId(3) @JvmField val gender: Byte,
    @TarsId(4) @JvmField val sPhone: String = "",
    @TarsId(5) @JvmField val sEmail: String = "",
    @TarsId(6) @JvmField val sRemark: String = ""
) : JceStruct

@Serializable
internal class GetFriendListReq(
    @TarsId(0) @JvmField val reqtype: Int? = null,
    @TarsId(1) @JvmField val ifReflush: Byte? = null,
    @TarsId(2) @JvmField val uin: Long? = null,
    @TarsId(3) @JvmField val startIndex: Short? = null,
    @TarsId(4) @JvmField val getfriendCount: Short? = null,
    @TarsId(5) @JvmField val groupid: Byte? = null,
    @TarsId(6) @JvmField val ifGetGroupInfo: Byte? = null,
    @TarsId(7) @JvmField val groupstartIndex: Byte? = null,
    @TarsId(8) @JvmField val getgroupCount: Byte? = null,
    @TarsId(9) @JvmField val ifGetMSFGroup: Byte? = null,
    @TarsId(10) @JvmField val ifShowTermType: Byte? = null,
    @TarsId(11) @JvmField val version: Long? = null,
    @TarsId(12) @JvmField val uinList: List<Long>? = null,
    @TarsId(13) @JvmField val eAppType: Int = 0,
    @TarsId(14) @JvmField val ifGetDOVId: Byte? = null,
    @TarsId(15) @JvmField val ifGetBothFlag: Byte? = null,
    @TarsId(16) @JvmField val vec0xd50Req: ByteArray? = null,
    @TarsId(17) @JvmField val vec0xd6bReq: ByteArray? = null,
    @TarsId(18) @JvmField val vecSnsTypelist: List<Long>? = null
) : JceStruct


@Serializable
internal class GetFriendListResp(
    @TarsId(0) @JvmField val reqtype: Int,
    @TarsId(1) @JvmField val ifReflush: Byte,
    @TarsId(2) @JvmField val uin: Long,
    @TarsId(3) @JvmField val startIndex: Short,
    @TarsId(4) @JvmField val getfriendCount: Short,
    @TarsId(5) @JvmField val totoalFriendCount: Short,
    @TarsId(6) @JvmField val friendCount: Short,
    @TarsId(7) @JvmField val vecFriendInfo: List<FriendInfo>? = null,
    @TarsId(8) @JvmField val groupid: Byte? = null,
    @TarsId(9) @JvmField val ifGetGroupInfo: Byte,
    @TarsId(10) @JvmField val groupstartIndex: Byte? = null,
    @TarsId(11) @JvmField val getgroupCount: Byte? = null,
    @TarsId(12) @JvmField val totoalGroupCount: Short? = null,
    @TarsId(13) @JvmField val groupCount: Byte? = null,
    @TarsId(14) @JvmField val vecGroupInfo: List<GroupInfo>? = null,
    @TarsId(15) @JvmField val result: Int,
    @TarsId(16) @JvmField val errorCode: Short? = null,
    @TarsId(17) @JvmField val onlineFriendCount: Short? = null,
    @TarsId(18) @JvmField val serverTime: Long? = null,
    @TarsId(19) @JvmField val sqqOnLineCount: Short? = null,
    @TarsId(20) @JvmField val vecMSFGroupInfo: List<GroupInfo>? = null,
    @TarsId(21) @JvmField val respType: Byte? = null,
    @TarsId(22) @JvmField val hasOtherRespFlag: Byte? = null,
    @TarsId(23) @JvmField val stSelfInfo: FriendInfo? = null,
    @TarsId(24) @JvmField val showPcIcon: Byte? = null,
    @TarsId(25) @JvmField val wGetExtSnsRspCode: Short? = null,
    @TarsId(26) @JvmField val stSubSrvRspCode: FriendListSubSrvRspCode? = null
) : JceStruct

@Serializable
internal class FriendListSubSrvRspCode(
    @TarsId(0) @JvmField val wGetMutualMarkRspCode: Short? = null,
    @TarsId(1) @JvmField val wGetIntimateInfoRspCode: Short? = null
) : JceStruct

@Serializable
internal class FriendInfo(
    @TarsId(0) @JvmField val friendUin: Long,
    @TarsId(1) @JvmField val groupId: Byte,
    @TarsId(2) @JvmField val faceId: Short,
    @TarsId(3) @JvmField val remark: String = "",
    @TarsId(4) @JvmField val sqqtype: Byte,
    @TarsId(5) @JvmField val status: Byte = 20,
    @TarsId(6) @JvmField val memberLevel: Byte? = null,
    @TarsId(7) @JvmField val isMqqOnLine: Byte? = null,
    @TarsId(8) @JvmField val sqqOnLineState: Byte? = null,
    @TarsId(9) @JvmField val isIphoneOnline: Byte? = null,
    @TarsId(10) @JvmField val detalStatusFlag: Byte? = null,
    @TarsId(11) @JvmField val sqqOnLineStateV2: Byte? = null,
    @TarsId(12) @JvmField val sShowName: String? = "",
    @TarsId(13) @JvmField val isRemark: Byte? = null,
    @TarsId(14) @JvmField val nick: String = "",
    @TarsId(15) @JvmField val specialFlag: Byte? = null,
    @TarsId(16) @JvmField val vecIMGroupID: ByteArray? = null,
    @TarsId(17) @JvmField val vecMSFGroupID: ByteArray? = null,
    @TarsId(18) @JvmField val iTermType: Int? = null,
    @TarsId(19) @JvmField val oVipInfo: VipBaseInfo? = null, //? bad
    @TarsId(20) @JvmField val network: Byte? = null,
    @TarsId(21) @JvmField val vecRing: ByteArray? = null,
    @TarsId(22) @JvmField val uAbiFlag: Long? = null,
    @TarsId(23) @JvmField val ulFaceAddonId: Long? = null,
    @TarsId(24) @JvmField val eNetworkType: Int? = 0,
    @TarsId(25) @JvmField val uVipFont: Long? = null,
    @TarsId(26) @JvmField val eIconType: Int? = 0,
    @TarsId(27) @JvmField val termDesc: String? = "",
    @TarsId(28) @JvmField val uColorRing: Long? = null,
    @TarsId(29) @JvmField val apolloFlag: Byte? = null,
    @TarsId(30) @JvmField val uApolloTimestamp: Long? = null,
    @TarsId(31) @JvmField val sex: Byte? = null,
    @TarsId(32) @JvmField val uFounderFont: Long? = null,
    @TarsId(33) @JvmField val eimId: String? = "",
    @TarsId(34) @JvmField val eimMobile: String? = "",
    @TarsId(35) @JvmField val olympicTorch: Byte? = null,
    @TarsId(36) @JvmField val uApolloSignTime: Long? = null,
    @TarsId(37) @JvmField val uLaviUin: Long? = null,
    @TarsId(38) @JvmField val uTagUpdateTime: Long? = null,
    @TarsId(39) @JvmField val uGameLastLoginTime: Long? = null,
    @TarsId(40) @JvmField val uGameAppid: Long? = null,
    @TarsId(41) @JvmField val vecCardID: ByteArray? = null,
    @TarsId(42) @JvmField val ulBitSet: Long? = null,
    @TarsId(43) @JvmField val kingOfGloryFlag: Byte? = null,
    @TarsId(44) @JvmField val ulKingOfGloryRank: Long? = null,
    @TarsId(45) @JvmField val masterUin: String? = "",
    @TarsId(46) @JvmField val uLastMedalUpdateTime: Long? = null,
    @TarsId(47) @JvmField val uFaceStoreId: Long? = null,
    @TarsId(48) @JvmField val uFontEffect: Long? = null,
    @TarsId(49) @JvmField val sDOVId: String? = "",
    @TarsId(50) @JvmField val uBothFlag: Long? = null,
    @TarsId(51) @JvmField val centiShow3DFlag: Byte? = null,
    @TarsId(52) @JvmField val vecIntimateInfo: ByteArray? = null,
    @TarsId(53) @JvmField val showNameplate: Byte? = null,
    @TarsId(54) @JvmField val newLoverDiamondFlag: Byte? = null,
    @TarsId(55) @JvmField val vecExtSnsFrdData: ByteArray? = null,
    @TarsId(56) @JvmField val vecMutualMarkData: ByteArray? = null
) : JceStruct

@Serializable
internal class VipBaseInfo(
    @TarsId(0) @JvmField val mOpenInfo: Map<Int, VipOpenInfo>? = null,
    // 1, 2 are since 8.2.7
    @TarsId(1) @JvmField val iNameplateVipType: Int? = 0,
    @TarsId(2) @JvmField val iGrayNameplateFlag: Int? = 0
) : JceStruct

@Serializable
internal class VipOpenInfo(
    @TarsId(0) @JvmField val open: Boolean? = false,
    @TarsId(1) @JvmField val iVipType: Int = -1,
    @TarsId(2) @JvmField val iVipLevel: Int = -1,
    @TarsId(3) @JvmField val iVipFlag: Int? = null,
    @TarsId(4) @JvmField val nameplateId: Long? = null
) : JceStruct

@Serializable
internal class GroupInfo(
    @TarsId(0) @JvmField val groupId: Byte,
    @TarsId(1) @JvmField val groupname: String = "",
    @TarsId(2) @JvmField val friendCount: Int,
    @TarsId(3) @JvmField val onlineFriendCount: Int,
    @TarsId(4) @JvmField val seqid: Byte? = null,
    @TarsId(5) @JvmField val sqqOnLineCount: Int? = null
) : JceStruct

