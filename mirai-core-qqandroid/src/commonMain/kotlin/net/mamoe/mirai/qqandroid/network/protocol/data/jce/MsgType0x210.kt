package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class AddGroup(
    @JceId(0) @JvmField val dwGroupID: Long? = null,
    @JceId(1) @JvmField val dwSortID: Long? = null,
    @JceId(2) @JvmField val groupName: String? = ""
) : JceStruct

@Serializable
internal class DelGroup(
    @JceId(0) @JvmField val dwGroupID: Long? = null
) : JceStruct

@Serializable
internal class FriendGroup(
    @JceId(0) @JvmField val dwFuin: Long? = null,
    @JceId(1) @JvmField val vOldGroupID: List<Long>? = null,
    @JceId(2) @JvmField val vNewGroupID: List<Long>? = null
) : JceStruct

@Serializable
internal class GroupSort(
    @JceId(0) @JvmField val dwGroupID: Long? = null,
    @JceId(1) @JvmField val dwSortID: Long? = null
) : JceStruct

@Serializable
internal class MarketFaceInfo(
    @JceId(0) @JvmField val insertIdx: Long,
    @JceId(1) @JvmField val marketFaceBuff: ByteArray
) : JceStruct

@Serializable
internal class ModFriendGroup(
    @JceId(0) @JvmField val vMsgFrdGroup: List<FriendGroup>? = null
) : JceStruct

@Serializable
internal class ModGroupName(
    @JceId(0) @JvmField val dwGroupID: Long? = null,
    @JceId(1) @JvmField val groupName: String? = ""
) : JceStruct

@Serializable
internal class ModGroupSort(
    @JceId(0) @JvmField val vMsgGroupSort: List<GroupSort>? = null
) : JceStruct

@Serializable
internal class MsgType0x210(
    @JceId(0) @JvmField val uSubMsgType: Long,
    @JceId(1) @JvmField val stMsgInfo0x2: MsgType0x210SubMsgType0x2? = null,
    @JceId(3) @JvmField val stMsgInfo0xa: MsgType0x210SubMsgType0xa? = null,
    @JceId(4) @JvmField val stMsgInfo0xe: MsgType0x210SubMsgType0xe? = null,
    @JceId(5) @JvmField val stMsgInfo0x13: MsgType0x210SubMsgType0x13? = null,
    @JceId(6) @JvmField val stMsgInfo0x17: MsgType0x210SubMsgType0x17? = null,
    @JceId(7) @JvmField val stMsgInfo0x20: MsgType0x210SubMsgType0x20? = null,
    @JceId(8) @JvmField val stMsgInfo0x1d: MsgType0x210SubMsgType0x1d? = null,
    @JceId(9) @JvmField val stMsgInfo0x24: MsgType0x210SubMsgType0x24? = null,
    @JceId(10) @JvmField val vProtobuf: ByteArray = EMPTY_BYTE_ARRAY
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x13(
    @JceId(0) @JvmField val uint32SrcAppId: Long? = null,
    @JceId(1) @JvmField val uint32SrcInstId: Long? = null,
    @JceId(2) @JvmField val uint32DstAppId: Long? = null,
    @JceId(3) @JvmField val uint32DstInstId: Long? = null,
    @JceId(4) @JvmField val uint64DstUin: Long? = null,
    @JceId(5) @JvmField val uint64Sessionid: Long? = null,
    @JceId(6) @JvmField val uint32Size: Long? = null,
    @JceId(7) @JvmField val uint32Index: Long? = null,
    @JceId(8) @JvmField val uint32Type: Long? = null,
    @JceId(9) @JvmField val buf: ByteArray? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x13_MsgItem(
    @JceId(0) @JvmField val uint32Type: Long? = null,
    @JceId(1) @JvmField val text: ByteArray? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x17(
    @JceId(0) @JvmField val dwOpType: Long? = null,
    @JceId(1) @JvmField val stAddGroup: AddGroup? = null,
    @JceId(2) @JvmField val stDelGroup: DelGroup? = null,
    @JceId(3) @JvmField val stModGroupName: ModGroupName? = null,
    @JceId(4) @JvmField val stModGroupSort: ModGroupSort? = null,
    @JceId(5) @JvmField val stModFriendGroup: ModFriendGroup? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x1d(
    @JceId(0) @JvmField val dwOpType: Long? = null,
    @JceId(1) @JvmField val dwUin: Long? = null,
    @JceId(2) @JvmField val dwID: Long? = null,
    @JceId(3) @JvmField val value: String? = ""
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x2(
    @JceId(0) @JvmField val uSrcAppId: Long? = null,
    @JceId(1) @JvmField val uSrcInstId: Long? = null,
    @JceId(2) @JvmField val uDstAppId: Long? = null,
    @JceId(3) @JvmField val uDstInstId: Long? = null,
    @JceId(4) @JvmField val uDstUin: Long? = null,
    @JceId(5) @JvmField val fileName: ByteArray? = null,
    @JceId(6) @JvmField val fileIndex: ByteArray? = null,
    @JceId(7) @JvmField val fileMd5: ByteArray? = null,
    @JceId(8) @JvmField val fileKey: ByteArray? = null,
    @JceId(9) @JvmField val uServerIp: Long? = null,
    @JceId(10) @JvmField val uServerPort: Long? = null,
    @JceId(11) @JvmField val fileLen: Long? = null,
    @JceId(12) @JvmField val sessionId: Long? = null,
    @JceId(13) @JvmField val originfileMd5: ByteArray? = null,
    @JceId(14) @JvmField val uOriginfiletype: Long? = null,
    @JceId(15) @JvmField val uSeq: Long? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x20(
    @JceId(0) @JvmField val dwOpType: Long? = null,
    @JceId(1) @JvmField val dwType: Long? = null,
    @JceId(2) @JvmField val dwUin: Long? = null,
    @JceId(3) @JvmField val remaek: String? = ""
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x24(
    @JceId(0) @JvmField val vPluginNumList: List<PluginNum>? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0xa(
    @JceId(0) @JvmField val uSrcAppId: Long? = null,
    @JceId(1) @JvmField val uSrcInstId: Long? = null,
    @JceId(2) @JvmField val uDstAppId: Long? = null,
    @JceId(3) @JvmField val uDstInstId: Long? = null,
    @JceId(4) @JvmField val uDstUin: Long? = null,
    @JceId(5) @JvmField val uType: Long? = null,
    @JceId(6) @JvmField val uServerIp: Long? = null,
    @JceId(7) @JvmField val uServerPort: Long? = null,
    @JceId(8) @JvmField val vUrlNotify: ByteArray? = null,
    @JceId(9) @JvmField val vTokenKey: ByteArray? = null,
    @JceId(10) @JvmField val uFileLen: Long? = null,
    @JceId(11) @JvmField val fileName: ByteArray? = null,
    @JceId(12) @JvmField val vMd5: ByteArray? = null,
    @JceId(13) @JvmField val sessionId: Long? = null,
    @JceId(14) @JvmField val originfileMd5: ByteArray? = null,
    @JceId(15) @JvmField val uOriginfiletype: Long? = null,
    @JceId(16) @JvmField val uSeq: Long? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0xe(
    @JceId(0) @JvmField val uint32SrcAppId: Long? = null,
    @JceId(1) @JvmField val uint32SrcInstId: Long? = null,
    @JceId(2) @JvmField val uint32DstAppId: Long? = null,
    @JceId(3) @JvmField val uint32DstInstId: Long? = null,
    @JceId(4) @JvmField val uint64DstUin: Long? = null,
    @JceId(5) @JvmField val uint64Sessionid: Long? = null,
    @JceId(6) @JvmField val uint32Operate: Long? = null,
    @JceId(7) @JvmField val uint32Seq: Long? = null,
    @JceId(8) @JvmField val uint32Code: Long? = null,
    @JceId(9) @JvmField val msg: String? = ""
) : JceStruct

@Serializable
internal class PersonInfoChange(
    @JceId(0) @JvmField val type: Byte? = null,
    @JceId(1) @JvmField val vChgField: List<PersonInfoField>? = null
) : JceStruct

@Serializable
internal class PersonInfoField(
    @JceId(0) @JvmField val uField: Long? = null
) : JceStruct

@Serializable
internal class PluginNum(
    @JceId(0) @JvmField val dwID: Long? = null,
    @JceId(1) @JvmField val dwNUm: Long? = null,
    @JceId(2) @JvmField val flag: Byte? = null
) : JceStruct

@Serializable
internal class SlaveMasterMsg(
    @JceId(0) @JvmField val uMsgType: Long? = null,
    @JceId(1) @JvmField val uCmd: Long? = null,
    @JceId(2) @JvmField val uSeq: Long? = null,
    @JceId(3) @JvmField val fromUin: Long? = null,
    @JceId(4) @JvmField val wFromApp: Short? = null,
    @JceId(5) @JvmField val uFromInstId: Long? = null,
    @JceId(6) @JvmField val toUin: Long? = null,
    @JceId(7) @JvmField val wToApp: Short? = null,
    @JceId(8) @JvmField val uToInstId: Long? = null,
    @JceId(9) @JvmField val vOrigMsg: ByteArray? = null,
    @JceId(10) @JvmField val uLastChangeTime: Long? = null,
    @JceId(11) @JvmField val vReserved: ByteArray? = null,
    @JceId(12) @JvmField val vMarketFace: List<MarketFaceInfo>? = null,
    @JceId(13) @JvmField val uSuperQQBubbleId: Long? = null
) : JceStruct

@Serializable
internal class Type_1_QQDataTextMsg(
    @JceId(0) @JvmField val msgItem: List<MsgType0x210SubMsgType0x13_MsgItem>? = null
) : JceStruct

