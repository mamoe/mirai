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

internal class OnlinePushPack {
    @Serializable
    internal class DelMsgInfo(
        @ProtoId(0) val fromUin: Long,
        @ProtoId(1) val uMsgTime: Long,
        @ProtoId(2) val shMsgSeq: Short,
        @ProtoId(3) val vMsgCookies: ByteArray? = null,
        @ProtoId(4) val wCmd: Short? = null,
        @ProtoId(5) val uMsgType: Long? = null,
        @ProtoId(6) val uAppId: Long? = null,
        @ProtoId(7) val sendTime: Long? = null,
        @ProtoId(8) val ssoSeq: Int? = null,
        @ProtoId(9) val ssoIp: Int? = null,
        @ProtoId(10) val clientIp: Int? = null
    ) : JceStruct

    @Serializable
    internal class DeviceInfo(
        @ProtoId(0) val netType: Byte? = null,
        @ProtoId(1) val devType: String? = "",
        @ProtoId(2) val oSVer: String? = "",
        @ProtoId(3) val vendorName: String? = "",
        @ProtoId(4) val vendorOSName: String? = "",
        @ProtoId(5) val iOSIdfa: String? = ""
    ) : JceStruct

    @Serializable
    internal class Name(
        @ProtoId(0) val fromUin: Long,
        @ProtoId(1) val uMsgTime: Long,
        @ProtoId(2) val shMsgType: Short,
        @ProtoId(3) val shMsgSeq: Short,
        @ProtoId(4) val msg: String = "",
        @ProtoId(5) val uRealMsgTime: Int? = null,
        @ProtoId(6) val vMsg: ByteArray? = null,
        @ProtoId(7) val uAppShareID: Long? = null,
        @ProtoId(8) val vMsgCookies: ByteArray? = null,
        @ProtoId(9) val vAppShareCookie: ByteArray? = null,
        @ProtoId(10) val msgUid: Long? = null,
        @ProtoId(11) val lastChangeTime: Long? = 1L,
        @ProtoId(12) val vCPicInfo: List<CPicInfo>? = null,
        @ProtoId(13) val stShareData: ShareData? = null,
        @ProtoId(14) val fromInstId: Long? = null,
        @ProtoId(15) val vRemarkOfSender: ByteArray? = null,
        @ProtoId(16) val fromMobile: String? = "",
        @ProtoId(17) val fromName: String? = "",
        @ProtoId(18) val vNickName: List<String>? = null,
        @ProtoId(19) val stC2CTmpMsgHead: TempMsgHead? = null
    ) : JceStruct

    @Serializable
    internal class SvcReqPushMsg(
        @ProtoId(0) val uin: Long,
        @ProtoId(1) val uMsgTime: Long,
        @ProtoId(2) val vMsgInfos: List<MsgInfo>,
        @ProtoId(3) val svrip: Int? = 0,
        @ProtoId(4) val vSyncCookie: ByteArray? = null,
        @ProtoId(5) val vUinPairMsg: List<UinPairMsg>? = null,
        @ProtoId(6) val mPreviews: Map<String, ByteArray>? = null
        // @SerialId(7) val wUserActive: Int? = null,
        //@SerialId(12) val wGeneralFlag: Int? = null
    ) : JceStruct

    @Serializable
    internal class SvcRespPushMsg(
        @ProtoId(0) val uin: Long,
        @ProtoId(1) val vDelInfos: List<DelMsgInfo>,
        @ProtoId(2) val svrip: Int,
        @ProtoId(3) val pushToken: ByteArray? = null,
        @ProtoId(4) val serviceType: Int? = null,
        @ProtoId(5) val deviceInfo: DeviceInfo? = null
    ) : JceStruct

    @Serializable
    internal class UinPairMsg(
        @ProtoId(1) val uLastReadTime: Long? = null,
        @ProtoId(2) val peerUin: Long? = null,
        @ProtoId(3) val uMsgCompleted: Long? = null,
        @ProtoId(4) val vMsgInfos: List<MsgInfo>? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210(
        @ProtoId(0) val uSubMsgType: Long,
        @ProtoId(1) val stMsgInfo0x2: MsgType0x210SubMsgType0x2? = null,
        @ProtoId(3) val stMsgInfo0xa: MsgType0x210SubMsgType0xa? = null,
        @ProtoId(4) val stMsgInfo0xe: MsgType0x210SubMsgType0xe? = null,
        @ProtoId(5) val stMsgInfo0x13: MsgType0x210SubMsgType0x13? = null,
        @ProtoId(6) val stMsgInfo0x17: MsgType0x210SubMsgType0x17? = null,
        @ProtoId(7) val stMsgInfo0x20: MsgType0x210SubMsgType0x20? = null,
        @ProtoId(8) val stMsgInfo0x1d: MsgType0x210SubMsgType0x1d? = null,
        @ProtoId(9) val stMsgInfo0x24: MsgType0x210SubMsgType0x24? = null,
        @ProtoId(10) val vProtobuf: ByteArray? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x13(
        @ProtoId(0) val uint32SrcAppId: Long? = null,
        @ProtoId(1) val uint32SrcInstId: Long? = null,
        @ProtoId(2) val uint32DstAppId: Long? = null,
        @ProtoId(3) val uint32DstInstId: Long? = null,
        @ProtoId(4) val uint64DstUin: Long? = null,
        @ProtoId(5) val uint64Sessionid: Long? = null,
        @ProtoId(6) val uint32Size: Long? = null,
        @ProtoId(7) val uint32Index: Long? = null,
        @ProtoId(8) val uint32Type: Long? = null,
        @ProtoId(9) val buf: ByteArray? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x17(
        @ProtoId(0) val dwOpType: Long? = null,
        @ProtoId(1) val stAddGroup: AddGroup? = null,
        @ProtoId(2) val stDelGroup: DelGroup? = null,
        @ProtoId(3) val stModGroupName: ModGroupName? = null,
        @ProtoId(4) val stModGroupSort: ModGroupSort? = null,
        @ProtoId(5) val stModFriendGroup: ModFriendGroup? = null
    ) : JceStruct

    @Serializable
    internal class AddGroup(
        @ProtoId(0) val dwGroupID: Long? = null,
        @ProtoId(1) val dwSortID: Long? = null,
        @ProtoId(2) val groupName: String? = ""
    ) : JceStruct

    @Serializable
    internal class DelGroup(
        @ProtoId(0) val dwGroupID: Long? = null
    ) : JceStruct

    @Serializable
    internal class ModFriendGroup(
        @ProtoId(0) val vMsgFrdGroup: List<FriendGroup>? = null
    ) : JceStruct

    @Serializable
    internal class FriendGroup(
        @ProtoId(0) val dwFuin: Long? = null,
        @ProtoId(1) val vOldGroupID: List<Long>? = null,
        @ProtoId(2) val vNewGroupID: List<Long>? = null
    ) : JceStruct

    @Serializable
    internal class ModGroupName(
        @ProtoId(0) val dwGroupID: Long? = null,
        @ProtoId(1) val groupName: String? = ""
    ) : JceStruct

    @Serializable
    internal class ModGroupSort(
        @ProtoId(0) val vMsgGroupSort: List<GroupSort>? = null
    ) : JceStruct

    @Serializable
    internal class GroupSort(
        @ProtoId(0) val dwGroupID: Long? = null,
        @ProtoId(1) val dwSortID: Long? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x1d(
        @ProtoId(0) val dwOpType: Long? = null,
        @ProtoId(1) val dwUin: Long? = null,
        @ProtoId(2) val dwID: Long? = null,
        @ProtoId(3) val value: String? = ""
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x2(
        @ProtoId(0) val uSrcAppId: Long? = null,
        @ProtoId(1) val uSrcInstId: Long? = null,
        @ProtoId(2) val uDstAppId: Long? = null,
        @ProtoId(3) val uDstInstId: Long? = null,
        @ProtoId(4) val uDstUin: Long? = null,
        @ProtoId(5) val fileName: ByteArray? = null,
        @ProtoId(6) val fileIndex: ByteArray? = null,
        @ProtoId(7) val fileMd5: ByteArray? = null,
        @ProtoId(8) val fileKey: ByteArray? = null,
        @ProtoId(9) val uServerIp: Long? = null,
        @ProtoId(10) val uServerPort: Long? = null,
        @ProtoId(11) val fileLen: Long? = null,
        @ProtoId(12) val sessionId: Long? = null,
        @ProtoId(13) val originfileMd5: ByteArray? = null,
        @ProtoId(14) val uOriginfiletype: Long? = null,
        @ProtoId(15) val uSeq: Long? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x20(
        @ProtoId(0) val dwOpType: Long? = null,
        @ProtoId(1) val dwType: Long? = null,
        @ProtoId(2) val dwUin: Long? = null,
        @ProtoId(3) val remaek: String? = ""
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x24(
        @ProtoId(0) val vPluginNumList: List<PluginNum>? = null
    ) : JceStruct

    @Serializable
    internal class PluginNum(
        @ProtoId(0) val dwID: Long? = null,
        @ProtoId(1) val dwNUm: Long? = null,
        @ProtoId(2) val flag: Byte? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0xa(
        @ProtoId(0) val uSrcAppId: Long? = null,
        @ProtoId(1) val uSrcInstId: Long? = null,
        @ProtoId(2) val uDstAppId: Long? = null,
        @ProtoId(3) val uDstInstId: Long? = null,
        @ProtoId(4) val uDstUin: Long? = null,
        @ProtoId(5) val uType: Long? = null,
        @ProtoId(6) val uServerIp: Long? = null,
        @ProtoId(7) val uServerPort: Long? = null,
        @ProtoId(8) val vUrlNotify: ByteArray? = null,
        @ProtoId(9) val vTokenKey: ByteArray? = null,
        @ProtoId(10) val uFileLen: Long? = null,
        @ProtoId(11) val fileName: ByteArray? = null,
        @ProtoId(12) val vMd5: ByteArray? = null,
        @ProtoId(13) val sessionId: Long? = null,
        @ProtoId(14) val originfileMd5: ByteArray? = null,
        @ProtoId(15) val uOriginfiletype: Long? = null,
        @ProtoId(16) val uSeq: Long? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0xe(
        @ProtoId(0) val uint32SrcAppId: Long? = null,
        @ProtoId(1) val uint32SrcInstId: Long? = null,
        @ProtoId(2) val uint32DstAppId: Long? = null,
        @ProtoId(3) val uint32DstInstId: Long? = null,
        @ProtoId(4) val uint64DstUin: Long? = null,
        @ProtoId(5) val uint64Sessionid: Long? = null,
        @ProtoId(6) val uint32Operate: Long? = null,
        @ProtoId(7) val uint32Seq: Long? = null,
        @ProtoId(8) val uint32Code: Long? = null,
        @ProtoId(9) val msg: String? = ""
    ) : JceStruct
}