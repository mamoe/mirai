package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class GetFriendListReq(
    @SerialId(0) val reqtype: Int? = null,
    @SerialId(1) val ifReflush: Byte? = null,
    @SerialId(2) val uin: Long? = null,
    @SerialId(3) val startIndex: Short? = null,
    @SerialId(4) val getfriendCount: Short? = null,
    @SerialId(5) val groupid: Byte? = null,
    @SerialId(6) val ifGetGroupInfo: Byte? = null,
    @SerialId(7) val groupstartIndex: Byte? = null,
    @SerialId(8) val getgroupCount: Byte? = null,
    @SerialId(9) val ifGetMSFGroup: Byte? = null,
    @SerialId(10) val ifShowTermType: Byte? = null,
    @SerialId(11) val version: Long? = null,
    @SerialId(12) val uinList: List<Long>? = null,
    @SerialId(13) val eAppType: Int = 0,
    @SerialId(14) val ifGetDOVId: Byte? = null,
    @SerialId(15) val ifGetBothFlag: Byte? = null,
    @SerialId(16) val vec0xd50Req: ByteArray? = null,
    @SerialId(17) val vec0xd6bReq: ByteArray? = null,
    @SerialId(18) val vecSnsTypelist: List<Long>? = null
) : JceStruct


@Serializable
internal class GetFriendListResp(
    @SerialId(0) val reqtype: Int,
    @SerialId(1) val ifReflush: Byte,
    @SerialId(2) val uin: Long,
    @SerialId(3) val startIndex: Short,
    @SerialId(4) val getfriendCount: Short,
    @SerialId(5) val totoalFriendCount: Short,
    @SerialId(6) val friendCount: Short,
    @SerialId(7) val vecFriendInfo: List<FriendInfo>,
    @SerialId(8) val groupid: Byte? = null,
    @SerialId(9) val ifGetGroupInfo: Byte,
    @SerialId(10) val groupstartIndex: Byte? = null,
    @SerialId(11) val getgroupCount: Byte? = null,
    @SerialId(12) val totoalGroupCount: Short? = null,
    @SerialId(13) val groupCount: Byte? = null,
    @SerialId(14) val vecGroupInfo: List<GroupInfo>? = null,
    @SerialId(15) val result: Int,
    @SerialId(16) val errorCode: Short? = null,
    @SerialId(17) val onlineFriendCount: Short? = null,
    @SerialId(18) val serverTime: Long? = null,
    @SerialId(19) val sqqOnLineCount: Short? = null,
    @SerialId(20) val vecMSFGroupInfo: List<GroupInfo>? = null,
    @SerialId(21) val respType: Byte? = null,
    @SerialId(22) val hasOtherRespFlag: Byte? = null,
    @SerialId(23) val stSelfInfo: FriendInfo? = null,
    @SerialId(24) val showPcIcon: Byte? = null,
    @SerialId(25) val wGetExtSnsRspCode: Short? = null,
    @SerialId(26) val stSubSrvRspCode: FriendListSubSrvRspCode? = null
) : JceStruct

@Serializable
internal class FriendListSubSrvRspCode(
    @SerialId(0) val wGetMutualMarkRspCode: Short? = null,
    @SerialId(1) val wGetIntimateInfoRspCode: Short? = null
) : JceStruct

@Serializable
internal class FriendInfo(
    @SerialId(0) val friendUin: Long,
    @SerialId(1) val groupId: Byte,
    @SerialId(2) val faceId: Short,
    @SerialId(3) val remark: String = "",
    @SerialId(4) val sqqtype: Byte,
    @SerialId(5) val status: Byte = 20,
    @SerialId(6) val memberLevel: Byte? = null,
    @SerialId(7) val isMqqOnLine: Byte? = null,
    @SerialId(8) val sqqOnLineState: Byte? = null,
    @SerialId(9) val isIphoneOnline: Byte? = null,
    @SerialId(10) val detalStatusFlag: Byte? = null,
    @SerialId(11) val sqqOnLineStateV2: Byte? = null,
    @SerialId(12) val sShowName: String? = "",
    @SerialId(13) val isRemark: Byte? = null,
    @SerialId(14) val nick: String? = "",
    @SerialId(15) val specialFlag: Byte? = null,
    @SerialId(16) val vecIMGroupID: ByteArray? = null,
    @SerialId(17) val vecMSFGroupID: ByteArray? = null,
    @SerialId(18) val iTermType: Int? = null,
    @SerialId(19) val oVipInfo: VipBaseInfo? = null,
    @SerialId(20) val network: Byte? = null,
    @SerialId(21) val vecRing: ByteArray? = null,
    @SerialId(22) val uAbiFlag: Long? = null,
    @SerialId(23) val ulFaceAddonId: Long? = null,
    @SerialId(24) val eNetworkType: Int? = 0,
    @SerialId(25) val uVipFont: Long? = null,
    @SerialId(26) val eIconType: Int? = 0,
    @SerialId(27) val termDesc: String? = "",
    @SerialId(28) val uColorRing: Long? = null,
    @SerialId(29) val apolloFlag: Byte? = null,
    @SerialId(30) val uApolloTimestamp: Long? = null,
    @SerialId(31) val sex: Byte? = null,
    @SerialId(32) val uFounderFont: Long? = null,
    @SerialId(33) val eimId: String? = "",
    @SerialId(34) val eimMobile: String? = "",
    @SerialId(35) val olympicTorch: Byte? = null,
    @SerialId(36) val uApolloSignTime: Long? = null,
    @SerialId(37) val uLaviUin: Long? = null,
    @SerialId(38) val uTagUpdateTime: Long? = null,
    @SerialId(39) val uGameLastLoginTime: Long? = null,
    @SerialId(40) val uGameAppid: Long? = null,
    @SerialId(41) val vecCardID: ByteArray? = null,
    @SerialId(42) val ulBitSet: Long? = null,
    @SerialId(43) val kingOfGloryFlag: Byte? = null,
    @SerialId(44) val ulKingOfGloryRank: Long? = null,
    @SerialId(45) val masterUin: String? = "",
    @SerialId(46) val uLastMedalUpdateTime: Long? = null,
    @SerialId(47) val uFaceStoreId: Long? = null,
    @SerialId(48) val uFontEffect: Long? = null,
    @SerialId(49) val sDOVId: String? = "",
    @SerialId(50) val uBothFlag: Long? = null,
    @SerialId(51) val centiShow3DFlag: Byte? = null,
    @SerialId(52) val vecIntimateInfo: ByteArray? = null,
    @SerialId(53) val showNameplate: Byte? = null,
    @SerialId(54) val newLoverDiamondFlag: Byte? = null,
    @SerialId(55) val vecExtSnsFrdData: ByteArray? = null,
    @SerialId(56) val vecMutualMarkData: ByteArray? = null
) : JceStruct

@Serializable
internal class VipBaseInfo(
    @SerialId(0) val mOpenInfo: Map<Int, VipOpenInfo>
) : JceStruct

@Serializable
internal class VipOpenInfo(
    @SerialId(0) val open: Boolean,
    @SerialId(1) val iVipType: Int = -1,
    @SerialId(2) val iVipLevel: Int = -1,
    @SerialId(3) val iVipFlag: Int? = null,
    @SerialId(4) val nameplateId: Long? = null
) : JceStruct

@Serializable
internal class GroupInfo(
    @SerialId(0) val groupId: Byte,
    @SerialId(1) val groupname: String = "",
    @SerialId(2) val friendCount: Int,
    @SerialId(3) val onlineFriendCount: Int,
    @SerialId(4) val seqid: Byte? = null,
    @SerialId(5) val sqqOnLineCount: Int? = null
) : JceStruct

