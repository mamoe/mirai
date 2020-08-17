package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class ModifyGroupCardReq(
    @JceId(0) @JvmField val dwZero: Long,
    @JceId(1) @JvmField val dwGroupCode: Long,
    @JceId(2) @JvmField val dwNewSeq: Long,
    @JceId(3) @JvmField val vecUinInfo: List<stUinInfo>
) : JceStruct

@Serializable
internal class stUinInfo(
    @JceId(0) @JvmField val dwuin: Long,
    @JceId(1) @JvmField val dwFlag: Long,
    @JceId(2) @JvmField val sName: String = "",
    @JceId(3) @JvmField val gender: Byte,
    @JceId(4) @JvmField val sPhone: String = "",
    @JceId(5) @JvmField val sEmail: String = "",
    @JceId(6) @JvmField val sRemark: String = ""
) : JceStruct

@Serializable
internal class GetFriendListReq(
    @JceId(0) @JvmField val reqtype: Int? = null,
    @JceId(1) @JvmField val ifReflush: Byte? = null,
    @JceId(2) @JvmField val uin: Long? = null,
    @JceId(3) @JvmField val startIndex: Short? = null,
    @JceId(4) @JvmField val getfriendCount: Short? = null,
    @JceId(5) @JvmField val groupid: Byte? = null,
    @JceId(6) @JvmField val ifGetGroupInfo: Byte? = null,
    @JceId(7) @JvmField val groupstartIndex: Byte? = null,
    @JceId(8) @JvmField val getgroupCount: Byte? = null,
    @JceId(9) @JvmField val ifGetMSFGroup: Byte? = null,
    @JceId(10) @JvmField val ifShowTermType: Byte? = null,
    @JceId(11) @JvmField val version: Long? = null,
    @JceId(12) @JvmField val uinList: List<Long>? = null,
    @JceId(13) @JvmField val eAppType: Int = 0,
    @JceId(14) @JvmField val ifGetDOVId: Byte? = null,
    @JceId(15) @JvmField val ifGetBothFlag: Byte? = null,
    @JceId(16) @JvmField val vec0xd50Req: ByteArray? = null,
    @JceId(17) @JvmField val vec0xd6bReq: ByteArray? = null,
    @JceId(18) @JvmField val vecSnsTypelist: List<Long>? = null
) : JceStruct


@Serializable
internal class GetFriendListResp(
    @JceId(0) @JvmField val reqtype: Int,
    @JceId(1) @JvmField val ifReflush: Byte,
    @JceId(2) @JvmField val uin: Long,
    @JceId(3) @JvmField val startIndex: Short,
    @JceId(4) @JvmField val getfriendCount: Short,
    @JceId(5) @JvmField val totoalFriendCount: Short,
    @JceId(6) @JvmField val friendCount: Short,
    @JceId(7) @JvmField val vecFriendInfo: List<FriendInfo>? = null,
    @JceId(8) @JvmField val groupid: Byte? = null,
    @JceId(9) @JvmField val ifGetGroupInfo: Byte,
    @JceId(10) @JvmField val groupstartIndex: Byte? = null,
    @JceId(11) @JvmField val getgroupCount: Byte? = null,
    @JceId(12) @JvmField val totoalGroupCount: Short? = null,
    @JceId(13) @JvmField val groupCount: Byte? = null,
    @JceId(14) @JvmField val vecGroupInfo: List<GroupInfo>? = null,
    @JceId(15) @JvmField val result: Int,
    @JceId(16) @JvmField val errorCode: Short? = null,
    @JceId(17) @JvmField val onlineFriendCount: Short? = null,
    @JceId(18) @JvmField val serverTime: Long? = null,
    @JceId(19) @JvmField val sqqOnLineCount: Short? = null,
    @JceId(20) @JvmField val vecMSFGroupInfo: List<GroupInfo>? = null,
    @JceId(21) @JvmField val respType: Byte? = null,
    @JceId(22) @JvmField val hasOtherRespFlag: Byte? = null,
    @JceId(23) @JvmField val stSelfInfo: FriendInfo? = null,
    @JceId(24) @JvmField val showPcIcon: Byte? = null,
    @JceId(25) @JvmField val wGetExtSnsRspCode: Short? = null,
    @JceId(26) @JvmField val stSubSrvRspCode: FriendListSubSrvRspCode? = null
) : JceStruct

@Serializable
internal class FriendListSubSrvRspCode(
    @JceId(0) @JvmField val wGetMutualMarkRspCode: Short? = null,
    @JceId(1) @JvmField val wGetIntimateInfoRspCode: Short? = null
) : JceStruct

@Serializable
internal class FriendInfo(
    @JceId(0) @JvmField val friendUin: Long,
    @JceId(1) @JvmField val groupId: Byte,
    @JceId(2) @JvmField val faceId: Short,
    @JceId(3) @JvmField val remark: String = "",
    @JceId(4) @JvmField val sqqtype: Byte,
    @JceId(5) @JvmField val status: Byte = 20,
    @JceId(6) @JvmField val memberLevel: Byte? = null,
    @JceId(7) @JvmField val isMqqOnLine: Byte? = null,
    @JceId(8) @JvmField val sqqOnLineState: Byte? = null,
    @JceId(9) @JvmField val isIphoneOnline: Byte? = null,
    @JceId(10) @JvmField val detalStatusFlag: Byte? = null,
    @JceId(11) @JvmField val sqqOnLineStateV2: Byte? = null,
    @JceId(12) @JvmField val sShowName: String? = "",
    @JceId(13) @JvmField val isRemark: Byte? = null,
    @JceId(14) @JvmField val nick: String = "",
    @JceId(15) @JvmField val specialFlag: Byte? = null,
    @JceId(16) @JvmField val vecIMGroupID: ByteArray? = null,
    @JceId(17) @JvmField val vecMSFGroupID: ByteArray? = null,
    @JceId(18) @JvmField val iTermType: Int? = null,
    @JceId(19) @JvmField val oVipInfo: VipBaseInfo? = null, //? bad
    @JceId(20) @JvmField val network: Byte? = null,
    @JceId(21) @JvmField val vecRing: ByteArray? = null,
    @JceId(22) @JvmField val uAbiFlag: Long? = null,
    @JceId(23) @JvmField val ulFaceAddonId: Long? = null,
    @JceId(24) @JvmField val eNetworkType: Int? = 0,
    @JceId(25) @JvmField val uVipFont: Long? = null,
    @JceId(26) @JvmField val eIconType: Int? = 0,
    @JceId(27) @JvmField val termDesc: String? = "",
    @JceId(28) @JvmField val uColorRing: Long? = null,
    @JceId(29) @JvmField val apolloFlag: Byte? = null,
    @JceId(30) @JvmField val uApolloTimestamp: Long? = null,
    @JceId(31) @JvmField val sex: Byte? = null,
    @JceId(32) @JvmField val uFounderFont: Long? = null,
    @JceId(33) @JvmField val eimId: String? = "",
    @JceId(34) @JvmField val eimMobile: String? = "",
    @JceId(35) @JvmField val olympicTorch: Byte? = null,
    @JceId(36) @JvmField val uApolloSignTime: Long? = null,
    @JceId(37) @JvmField val uLaviUin: Long? = null,
    @JceId(38) @JvmField val uTagUpdateTime: Long? = null,
    @JceId(39) @JvmField val uGameLastLoginTime: Long? = null,
    @JceId(40) @JvmField val uGameAppid: Long? = null,
    @JceId(41) @JvmField val vecCardID: ByteArray? = null,
    @JceId(42) @JvmField val ulBitSet: Long? = null,
    @JceId(43) @JvmField val kingOfGloryFlag: Byte? = null,
    @JceId(44) @JvmField val ulKingOfGloryRank: Long? = null,
    @JceId(45) @JvmField val masterUin: String? = "",
    @JceId(46) @JvmField val uLastMedalUpdateTime: Long? = null,
    @JceId(47) @JvmField val uFaceStoreId: Long? = null,
    @JceId(48) @JvmField val uFontEffect: Long? = null,
    @JceId(49) @JvmField val sDOVId: String? = "",
    @JceId(50) @JvmField val uBothFlag: Long? = null,
    @JceId(51) @JvmField val centiShow3DFlag: Byte? = null,
    @JceId(52) @JvmField val vecIntimateInfo: ByteArray? = null,
    @JceId(53) @JvmField val showNameplate: Byte? = null,
    @JceId(54) @JvmField val newLoverDiamondFlag: Byte? = null,
    @JceId(55) @JvmField val vecExtSnsFrdData: ByteArray? = null,
    @JceId(56) @JvmField val vecMutualMarkData: ByteArray? = null
) : JceStruct

@Serializable
internal class VipBaseInfo(
    @JceId(0) @JvmField val mOpenInfo: Map<Int, VipOpenInfo>? = null,
    // 1, 2 are since 8.2.7
    @JceId(1) @JvmField val iNameplateVipType: Int? = 0,
    @JceId(2) @JvmField val iGrayNameplateFlag: Int? = 0
) : JceStruct

@Serializable
internal class VipOpenInfo(
    @JceId(0) @JvmField val open: Boolean? = false,
    @JceId(1) @JvmField val iVipType: Int = -1,
    @JceId(2) @JvmField val iVipLevel: Int = -1,
    @JceId(3) @JvmField val iVipFlag: Int? = null,
    @JceId(4) @JvmField val nameplateId: Long? = null
) : JceStruct

@Serializable
internal class GroupInfo(
    @JceId(0) @JvmField val groupId: Byte,
    @JceId(1) @JvmField val groupname: String = "",
    @JceId(2) @JvmField val friendCount: Int,
    @JceId(3) @JvmField val onlineFriendCount: Int,
    @JceId(4) @JvmField val seqid: Byte? = null,
    @JceId(5) @JvmField val sqqOnLineCount: Int? = null
) : JceStruct

