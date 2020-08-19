package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class AddGroup(
    @TarsId(0) @JvmField val dwGroupID: Long? = null,
    @TarsId(1) @JvmField val dwSortID: Long? = null,
    @TarsId(2) @JvmField val groupName: String? = ""
) : JceStruct

@Serializable
internal class DelGroup(
    @TarsId(0) @JvmField val dwGroupID: Long? = null
) : JceStruct

@Serializable
internal class FriendGroup(
    @TarsId(0) @JvmField val dwFuin: Long? = null,
    @TarsId(1) @JvmField val vOldGroupID: List<Long>? = null,
    @TarsId(2) @JvmField val vNewGroupID: List<Long>? = null
) : JceStruct

@Serializable
internal class GroupSort(
    @TarsId(0) @JvmField val dwGroupID: Long? = null,
    @TarsId(1) @JvmField val dwSortID: Long? = null
) : JceStruct

@Serializable
internal class MarketFaceInfo(
    @TarsId(0) @JvmField val insertIdx: Long,
    @TarsId(1) @JvmField val marketFaceBuff: ByteArray
) : JceStruct

@Serializable
internal class ModFriendGroup(
    @TarsId(0) @JvmField val vMsgFrdGroup: List<FriendGroup>? = null
) : JceStruct

@Serializable
internal class ModGroupName(
    @TarsId(0) @JvmField val dwGroupID: Long? = null,
    @TarsId(1) @JvmField val groupName: String? = ""
) : JceStruct

@Serializable
internal class ModGroupSort(
    @TarsId(0) @JvmField val vMsgGroupSort: List<GroupSort>? = null
) : JceStruct

@Serializable
internal class MsgType0x210(
    @TarsId(0) @JvmField val uSubMsgType: Long,
    @TarsId(1) @JvmField val stMsgInfo0x2: MsgType0x210SubMsgType0x2? = null,
    @TarsId(3) @JvmField val stMsgInfo0xa: MsgType0x210SubMsgType0xa? = null,
    @TarsId(4) @JvmField val stMsgInfo0xe: MsgType0x210SubMsgType0xe? = null,
    @TarsId(5) @JvmField val stMsgInfo0x13: MsgType0x210SubMsgType0x13? = null,
    @TarsId(6) @JvmField val stMsgInfo0x17: MsgType0x210SubMsgType0x17? = null,
    @TarsId(7) @JvmField val stMsgInfo0x20: MsgType0x210SubMsgType0x20? = null,
    @TarsId(8) @JvmField val stMsgInfo0x1d: MsgType0x210SubMsgType0x1d? = null,
    @TarsId(9) @JvmField val stMsgInfo0x24: MsgType0x210SubMsgType0x24? = null,
    @TarsId(10) @JvmField val vProtobuf: ByteArray = EMPTY_BYTE_ARRAY
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x13(
    @TarsId(0) @JvmField val uint32SrcAppId: Long? = null,
    @TarsId(1) @JvmField val uint32SrcInstId: Long? = null,
    @TarsId(2) @JvmField val uint32DstAppId: Long? = null,
    @TarsId(3) @JvmField val uint32DstInstId: Long? = null,
    @TarsId(4) @JvmField val uint64DstUin: Long? = null,
    @TarsId(5) @JvmField val uint64Sessionid: Long? = null,
    @TarsId(6) @JvmField val uint32Size: Long? = null,
    @TarsId(7) @JvmField val uint32Index: Long? = null,
    @TarsId(8) @JvmField val uint32Type: Long? = null,
    @TarsId(9) @JvmField val buf: ByteArray? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x13_MsgItem(
    @TarsId(0) @JvmField val uint32Type: Long? = null,
    @TarsId(1) @JvmField val text: ByteArray? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x17(
    @TarsId(0) @JvmField val dwOpType: Long? = null,
    @TarsId(1) @JvmField val stAddGroup: AddGroup? = null,
    @TarsId(2) @JvmField val stDelGroup: DelGroup? = null,
    @TarsId(3) @JvmField val stModGroupName: ModGroupName? = null,
    @TarsId(4) @JvmField val stModGroupSort: ModGroupSort? = null,
    @TarsId(5) @JvmField val stModFriendGroup: ModFriendGroup? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x1d(
    @TarsId(0) @JvmField val dwOpType: Long? = null,
    @TarsId(1) @JvmField val dwUin: Long? = null,
    @TarsId(2) @JvmField val dwID: Long? = null,
    @TarsId(3) @JvmField val value: String? = ""
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x2(
    @TarsId(0) @JvmField val uSrcAppId: Long? = null,
    @TarsId(1) @JvmField val uSrcInstId: Long? = null,
    @TarsId(2) @JvmField val uDstAppId: Long? = null,
    @TarsId(3) @JvmField val uDstInstId: Long? = null,
    @TarsId(4) @JvmField val uDstUin: Long? = null,
    @TarsId(5) @JvmField val fileName: ByteArray? = null,
    @TarsId(6) @JvmField val fileIndex: ByteArray? = null,
    @TarsId(7) @JvmField val fileMd5: ByteArray? = null,
    @TarsId(8) @JvmField val fileKey: ByteArray? = null,
    @TarsId(9) @JvmField val uServerIp: Long? = null,
    @TarsId(10) @JvmField val uServerPort: Long? = null,
    @TarsId(11) @JvmField val fileLen: Long? = null,
    @TarsId(12) @JvmField val sessionId: Long? = null,
    @TarsId(13) @JvmField val originfileMd5: ByteArray? = null,
    @TarsId(14) @JvmField val uOriginfiletype: Long? = null,
    @TarsId(15) @JvmField val uSeq: Long? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x20(
    @TarsId(0) @JvmField val dwOpType: Long? = null,
    @TarsId(1) @JvmField val dwType: Long? = null,
    @TarsId(2) @JvmField val dwUin: Long? = null,
    @TarsId(3) @JvmField val remaek: String? = ""
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x24(
    @TarsId(0) @JvmField val vPluginNumList: List<PluginNum>? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0xa(
    @TarsId(0) @JvmField val uSrcAppId: Long? = null,
    @TarsId(1) @JvmField val uSrcInstId: Long? = null,
    @TarsId(2) @JvmField val uDstAppId: Long? = null,
    @TarsId(3) @JvmField val uDstInstId: Long? = null,
    @TarsId(4) @JvmField val uDstUin: Long? = null,
    @TarsId(5) @JvmField val uType: Long? = null,
    @TarsId(6) @JvmField val uServerIp: Long? = null,
    @TarsId(7) @JvmField val uServerPort: Long? = null,
    @TarsId(8) @JvmField val vUrlNotify: ByteArray? = null,
    @TarsId(9) @JvmField val vTokenKey: ByteArray? = null,
    @TarsId(10) @JvmField val uFileLen: Long? = null,
    @TarsId(11) @JvmField val fileName: ByteArray? = null,
    @TarsId(12) @JvmField val vMd5: ByteArray? = null,
    @TarsId(13) @JvmField val sessionId: Long? = null,
    @TarsId(14) @JvmField val originfileMd5: ByteArray? = null,
    @TarsId(15) @JvmField val uOriginfiletype: Long? = null,
    @TarsId(16) @JvmField val uSeq: Long? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0xe(
    @TarsId(0) @JvmField val uint32SrcAppId: Long? = null,
    @TarsId(1) @JvmField val uint32SrcInstId: Long? = null,
    @TarsId(2) @JvmField val uint32DstAppId: Long? = null,
    @TarsId(3) @JvmField val uint32DstInstId: Long? = null,
    @TarsId(4) @JvmField val uint64DstUin: Long? = null,
    @TarsId(5) @JvmField val uint64Sessionid: Long? = null,
    @TarsId(6) @JvmField val uint32Operate: Long? = null,
    @TarsId(7) @JvmField val uint32Seq: Long? = null,
    @TarsId(8) @JvmField val uint32Code: Long? = null,
    @TarsId(9) @JvmField val msg: String? = ""
) : JceStruct

@Serializable
internal class PersonInfoChange(
    @TarsId(0) @JvmField val type: Byte? = null,
    @TarsId(1) @JvmField val vChgField: List<PersonInfoField>? = null
) : JceStruct

@Serializable
internal class PersonInfoField(
    @TarsId(0) @JvmField val uField: Long? = null
) : JceStruct

@Serializable
internal class PluginNum(
    @TarsId(0) @JvmField val dwID: Long? = null,
    @TarsId(1) @JvmField val dwNUm: Long? = null,
    @TarsId(2) @JvmField val flag: Byte? = null
) : JceStruct

@Serializable
internal class SlaveMasterMsg(
    @TarsId(0) @JvmField val uMsgType: Long? = null,
    @TarsId(1) @JvmField val uCmd: Long? = null,
    @TarsId(2) @JvmField val uSeq: Long? = null,
    @TarsId(3) @JvmField val fromUin: Long? = null,
    @TarsId(4) @JvmField val wFromApp: Short? = null,
    @TarsId(5) @JvmField val uFromInstId: Long? = null,
    @TarsId(6) @JvmField val toUin: Long? = null,
    @TarsId(7) @JvmField val wToApp: Short? = null,
    @TarsId(8) @JvmField val uToInstId: Long? = null,
    @TarsId(9) @JvmField val vOrigMsg: ByteArray? = null,
    @TarsId(10) @JvmField val uLastChangeTime: Long? = null,
    @TarsId(11) @JvmField val vReserved: ByteArray? = null,
    @TarsId(12) @JvmField val vMarketFace: List<MarketFaceInfo>? = null,
    @TarsId(13) @JvmField val uSuperQQBubbleId: Long? = null
) : JceStruct

@Serializable
internal class Type_1_QQDataTextMsg(
    @TarsId(0) @JvmField val msgItem: List<MsgType0x210SubMsgType0x13_MsgItem>? = null
) : JceStruct

