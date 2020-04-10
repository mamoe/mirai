package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.jce.JceId

@Serializable
internal class AddGroup(
    @JceId(0) val dwGroupID: Long? = null,
    @JceId(1) val dwSortID: Long? = null,
    @JceId(2) val groupName: String? = ""
) : JceStruct

@Serializable
internal class DelGroup(
    @JceId(0) val dwGroupID: Long? = null
) : JceStruct

@Serializable
internal class FriendGroup(
    @JceId(0) val dwFuin: Long? = null,
    @JceId(1) val vOldGroupID: List<Long>? = null,
    @JceId(2) val vNewGroupID: List<Long>? = null
) : JceStruct

@Serializable
internal class GroupSort(
    @JceId(0) val dwGroupID: Long? = null,
    @JceId(1) val dwSortID: Long? = null
) : JceStruct

@Serializable
internal class MarketFaceInfo(
    @JceId(0) val insertIdx: Long,
    @JceId(1) val marketFaceBuff: ByteArray
) : JceStruct

@Serializable
internal class ModFriendGroup(
    @JceId(0) val vMsgFrdGroup: List<FriendGroup>? = null
) : JceStruct

@Serializable
internal class ModGroupName(
    @JceId(0) val dwGroupID: Long? = null,
    @JceId(1) val groupName: String? = ""
) : JceStruct

@Serializable
internal class ModGroupSort(
    @JceId(0) val vMsgGroupSort: List<GroupSort>? = null
) : JceStruct

@Serializable
internal class MsgType0x210(
    @JceId(0) val uSubMsgType: Long,
    @JceId(1) val stMsgInfo0x2: MsgType0x210SubMsgType0x2? = null,
    @JceId(3) val stMsgInfo0xa: MsgType0x210SubMsgType0xa? = null,
    @JceId(4) val stMsgInfo0xe: MsgType0x210SubMsgType0xe? = null,
    @JceId(5) val stMsgInfo0x13: MsgType0x210SubMsgType0x13? = null,
    @JceId(6) val stMsgInfo0x17: MsgType0x210SubMsgType0x17? = null,
    @JceId(7) val stMsgInfo0x20: MsgType0x210SubMsgType0x20? = null,
    @JceId(8) val stMsgInfo0x1d: MsgType0x210SubMsgType0x1d? = null,
    @JceId(9) val stMsgInfo0x24: MsgType0x210SubMsgType0x24? = null,
    @JceId(10) val vProtobuf: ByteArray = EMPTY_BYTE_ARRAY
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x13(
    @JceId(0) val uint32SrcAppId: Long? = null,
    @JceId(1) val uint32SrcInstId: Long? = null,
    @JceId(2) val uint32DstAppId: Long? = null,
    @JceId(3) val uint32DstInstId: Long? = null,
    @JceId(4) val uint64DstUin: Long? = null,
    @JceId(5) val uint64Sessionid: Long? = null,
    @JceId(6) val uint32Size: Long? = null,
    @JceId(7) val uint32Index: Long? = null,
    @JceId(8) val uint32Type: Long? = null,
    @JceId(9) val buf: ByteArray? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x13_MsgItem(
    @JceId(0) val uint32Type: Long? = null,
    @JceId(1) val text: ByteArray? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x17(
    @JceId(0) val dwOpType: Long? = null,
    @JceId(1) val stAddGroup: AddGroup? = null,
    @JceId(2) val stDelGroup: DelGroup? = null,
    @JceId(3) val stModGroupName: ModGroupName? = null,
    @JceId(4) val stModGroupSort: ModGroupSort? = null,
    @JceId(5) val stModFriendGroup: ModFriendGroup? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x1d(
    @JceId(0) val dwOpType: Long? = null,
    @JceId(1) val dwUin: Long? = null,
    @JceId(2) val dwID: Long? = null,
    @JceId(3) val value: String? = ""
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x2(
    @JceId(0) val uSrcAppId: Long? = null,
    @JceId(1) val uSrcInstId: Long? = null,
    @JceId(2) val uDstAppId: Long? = null,
    @JceId(3) val uDstInstId: Long? = null,
    @JceId(4) val uDstUin: Long? = null,
    @JceId(5) val fileName: ByteArray? = null,
    @JceId(6) val fileIndex: ByteArray? = null,
    @JceId(7) val fileMd5: ByteArray? = null,
    @JceId(8) val fileKey: ByteArray? = null,
    @JceId(9) val uServerIp: Long? = null,
    @JceId(10) val uServerPort: Long? = null,
    @JceId(11) val fileLen: Long? = null,
    @JceId(12) val sessionId: Long? = null,
    @JceId(13) val originfileMd5: ByteArray? = null,
    @JceId(14) val uOriginfiletype: Long? = null,
    @JceId(15) val uSeq: Long? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x20(
    @JceId(0) val dwOpType: Long? = null,
    @JceId(1) val dwType: Long? = null,
    @JceId(2) val dwUin: Long? = null,
    @JceId(3) val remaek: String? = ""
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0x24(
    @JceId(0) val vPluginNumList: List<PluginNum>? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0xa(
    @JceId(0) val uSrcAppId: Long? = null,
    @JceId(1) val uSrcInstId: Long? = null,
    @JceId(2) val uDstAppId: Long? = null,
    @JceId(3) val uDstInstId: Long? = null,
    @JceId(4) val uDstUin: Long? = null,
    @JceId(5) val uType: Long? = null,
    @JceId(6) val uServerIp: Long? = null,
    @JceId(7) val uServerPort: Long? = null,
    @JceId(8) val vUrlNotify: String? = null,
    @JceId(9) val vTokenKey: ByteArray? = null,
    @JceId(10) val uFileLen: Long? = null,
    @JceId(11) val fileName: String? = null,
    @JceId(12) val vMd5: ByteArray? = null,
    @JceId(13) val sessionId: Long? = null,
    @JceId(14) val originfileMd5: ByteArray? = null,
    @JceId(15) val uOriginfiletype: Long? = null,
    @JceId(16) val uSeq: Long? = null
) : JceStruct

@Serializable
internal class MsgType0x210SubMsgType0xe(
    @JceId(0) val uint32SrcAppId: Long? = null,
    @JceId(1) val uint32SrcInstId: Long? = null,
    @JceId(2) val uint32DstAppId: Long? = null,
    @JceId(3) val uint32DstInstId: Long? = null,
    @JceId(4) val uint64DstUin: Long? = null,
    @JceId(5) val uint64Sessionid: Long? = null,
    @JceId(6) val uint32Operate: Long? = null,
    @JceId(7) val uint32Seq: Long? = null,
    @JceId(8) val uint32Code: Long? = null,
    @JceId(9) val msg: String? = ""
) : JceStruct

@Serializable
internal class PersonInfoChange(
    @JceId(0) val type: Byte? = null,
    @JceId(1) val vChgField: List<PersonInfoField>? = null
) : JceStruct

@Serializable
internal class PersonInfoField(
    @JceId(0) val uField: Long? = null
) : JceStruct

@Serializable
internal class PluginNum(
    @JceId(0) val dwID: Long? = null,
    @JceId(1) val dwNUm: Long? = null,
    @JceId(2) val flag: Byte? = null
) : JceStruct

@Serializable
internal class SlaveMasterMsg(
    @JceId(0) val uMsgType: Long? = null,
    @JceId(1) val uCmd: Long? = null,
    @JceId(2) val uSeq: Long? = null,
    @JceId(3) val fromUin: Long? = null,
    @JceId(4) val wFromApp: Short? = null,
    @JceId(5) val uFromInstId: Long? = null,
    @JceId(6) val toUin: Long? = null,
    @JceId(7) val wToApp: Short? = null,
    @JceId(8) val uToInstId: Long? = null,
    @JceId(9) val vOrigMsg: ByteArray? = null,
    @JceId(10) val uLastChangeTime: Long? = null,
    @JceId(11) val vReserved: ByteArray? = null,
    @JceId(12) val vMarketFace: List<MarketFaceInfo>? = null,
    @JceId(13) val uSuperQQBubbleId: Long? = null
) : JceStruct

@Serializable
internal class Type_1_QQDataTextMsg(
    @JceId(0) val msgItem: List<MsgType0x210SubMsgType0x13_MsgItem>? = null
) : JceStruct

