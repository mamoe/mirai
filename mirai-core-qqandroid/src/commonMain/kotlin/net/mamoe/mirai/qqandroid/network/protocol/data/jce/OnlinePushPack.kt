/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

class OnlinePushPack {
    @Serializable
    internal class DelMsgInfo(
        @SerialId(0) val fromUin: Long,
        @SerialId(1) val uMsgTime: Long,
        @SerialId(2) val shMsgSeq: Short,
        @SerialId(3) val vMsgCookies: ByteArray? = null,
        @SerialId(4) val wCmd: Short? = null,
        @SerialId(5) val uMsgType: Long? = null,
        @SerialId(6) val uAppId: Long? = null,
        @SerialId(7) val sendTime: Long? = null,
        @SerialId(8) val ssoSeq: Int? = null,
        @SerialId(9) val ssoIp: Int? = null,
        @SerialId(10) val clientIp: Int? = null
    ) : JceStruct

    @Serializable
    internal class DeviceInfo(
        @SerialId(0) val netType: Byte? = null,
        @SerialId(1) val devType: String? = "",
        @SerialId(2) val oSVer: String? = "",
        @SerialId(3) val vendorName: String? = "",
        @SerialId(4) val vendorOSName: String? = "",
        @SerialId(5) val iOSIdfa: String? = ""
    ) : JceStruct

    @Serializable
    internal class Name(
        @SerialId(0) val fromUin: Long,
        @SerialId(1) val uMsgTime: Long,
        @SerialId(2) val shMsgType: Short,
        @SerialId(3) val shMsgSeq: Short,
        @SerialId(4) val msg: String = "",
        @SerialId(5) val uRealMsgTime: Int? = null,
        @SerialId(6) val vMsg: ByteArray? = null,
        @SerialId(7) val uAppShareID: Long? = null,
        @SerialId(8) val vMsgCookies: ByteArray? = null,
        @SerialId(9) val vAppShareCookie: ByteArray? = null,
        @SerialId(10) val msgUid: Long? = null,
        @SerialId(11) val lastChangeTime: Long? = 1L,
        @SerialId(12) val vCPicInfo: List<CPicInfo>? = null,
        @SerialId(13) val stShareData: ShareData? = null,
        @SerialId(14) val fromInstId: Long? = null,
        @SerialId(15) val vRemarkOfSender: ByteArray? = null,
        @SerialId(16) val fromMobile: String? = "",
        @SerialId(17) val fromName: String? = "",
        @SerialId(18) val vNickName: List<String>? = null,
        @SerialId(19) val stC2CTmpMsgHead: TempMsgHead? = null
    ) : JceStruct

    @Serializable
    internal class SvcReqPushMsg(
        @SerialId(0) val uin: Long,
        @SerialId(1) val uMsgTime: Long,
        @SerialId(2) val vMsgInfos: List<MsgInfo>,
        @SerialId(3) val svrip: Int? = 0,
        @SerialId(4) val vSyncCookie: ByteArray? = null,
        @SerialId(5) val vUinPairMsg: List<UinPairMsg>? = null,
        @SerialId(6) val mPreviews: Map<String, ByteArray>? = null
        // @SerialId(7) val wUserActive: Int? = null,
        //@SerialId(12) val wGeneralFlag: Int? = null
    ) : JceStruct

    @Serializable
    internal class SvcRespPushMsg(
        @SerialId(0) val uin: Long,
        @SerialId(1) val vDelInfos: List<DelMsgInfo>,
        @SerialId(2) val svrip: Int,
        @SerialId(3) val pushToken: ByteArray? = null,
        @SerialId(4) val serviceType: Int? = null,
        @SerialId(5) val deviceInfo: DeviceInfo? = null
    ) : JceStruct

    @Serializable
    internal class UinPairMsg(
        @SerialId(1) val uLastReadTime: Long? = null,
        @SerialId(2) val peerUin: Long? = null,
        @SerialId(3) val uMsgCompleted: Long? = null,
        @SerialId(4) val vMsgInfos: List<MsgInfo>? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210(
        @SerialId(0) val uSubMsgType: Long,
        @SerialId(1) val stMsgInfo0x2: MsgType0x210SubMsgType0x2? = null,
        @SerialId(3) val stMsgInfo0xa: MsgType0x210SubMsgType0xa? = null,
        @SerialId(4) val stMsgInfo0xe: MsgType0x210SubMsgType0xe? = null,
        @SerialId(5) val stMsgInfo0x13: MsgType0x210SubMsgType0x13? = null,
        @SerialId(6) val stMsgInfo0x17: MsgType0x210SubMsgType0x17? = null,
        @SerialId(7) val stMsgInfo0x20: MsgType0x210SubMsgType0x20? = null,
        @SerialId(8) val stMsgInfo0x1d: MsgType0x210SubMsgType0x1d? = null,
        @SerialId(9) val stMsgInfo0x24: MsgType0x210SubMsgType0x24? = null,
        @SerialId(10) val vProtobuf: ByteArray? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x13(
        @SerialId(0) val uint32SrcAppId: Long? = null,
        @SerialId(1) val uint32SrcInstId: Long? = null,
        @SerialId(2) val uint32DstAppId: Long? = null,
        @SerialId(3) val uint32DstInstId: Long? = null,
        @SerialId(4) val uint64DstUin: Long? = null,
        @SerialId(5) val uint64Sessionid: Long? = null,
        @SerialId(6) val uint32Size: Long? = null,
        @SerialId(7) val uint32Index: Long? = null,
        @SerialId(8) val uint32Type: Long? = null,
        @SerialId(9) val buf: ByteArray? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x17(
        @SerialId(0) val dwOpType: Long? = null,
        @SerialId(1) val stAddGroup: AddGroup? = null,
        @SerialId(2) val stDelGroup: DelGroup? = null,
        @SerialId(3) val stModGroupName: ModGroupName? = null,
        @SerialId(4) val stModGroupSort: ModGroupSort? = null,
        @SerialId(5) val stModFriendGroup: ModFriendGroup? = null
    ) : JceStruct

    @Serializable
    internal class AddGroup(
        @SerialId(0) val dwGroupID: Long? = null,
        @SerialId(1) val dwSortID: Long? = null,
        @SerialId(2) val groupName: String? = ""
    ) : JceStruct

    @Serializable
    internal class DelGroup(
        @SerialId(0) val dwGroupID: Long? = null
    ) : JceStruct

    @Serializable
    internal class ModFriendGroup(
        @SerialId(0) val vMsgFrdGroup: List<FriendGroup>? = null
    ) : JceStruct

    @Serializable
    internal class FriendGroup(
        @SerialId(0) val dwFuin: Long? = null,
        @SerialId(1) val vOldGroupID: List<Long>? = null,
        @SerialId(2) val vNewGroupID: List<Long>? = null
    ) : JceStruct

    @Serializable
    internal class ModGroupName(
        @SerialId(0) val dwGroupID: Long? = null,
        @SerialId(1) val groupName: String? = ""
    ) : JceStruct

    @Serializable
    internal class ModGroupSort(
        @SerialId(0) val vMsgGroupSort: List<GroupSort>? = null
    ) : JceStruct

    @Serializable
    internal class GroupSort(
        @SerialId(0) val dwGroupID: Long? = null,
        @SerialId(1) val dwSortID: Long? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x1d(
        @SerialId(0) val dwOpType: Long? = null,
        @SerialId(1) val dwUin: Long? = null,
        @SerialId(2) val dwID: Long? = null,
        @SerialId(3) val value: String? = ""
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x2(
        @SerialId(0) val uSrcAppId: Long? = null,
        @SerialId(1) val uSrcInstId: Long? = null,
        @SerialId(2) val uDstAppId: Long? = null,
        @SerialId(3) val uDstInstId: Long? = null,
        @SerialId(4) val uDstUin: Long? = null,
        @SerialId(5) val fileName: ByteArray? = null,
        @SerialId(6) val fileIndex: ByteArray? = null,
        @SerialId(7) val fileMd5: ByteArray? = null,
        @SerialId(8) val fileKey: ByteArray? = null,
        @SerialId(9) val uServerIp: Long? = null,
        @SerialId(10) val uServerPort: Long? = null,
        @SerialId(11) val fileLen: Long? = null,
        @SerialId(12) val sessionId: Long? = null,
        @SerialId(13) val originfileMd5: ByteArray? = null,
        @SerialId(14) val uOriginfiletype: Long? = null,
        @SerialId(15) val uSeq: Long? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x20(
        @SerialId(0) val dwOpType: Long? = null,
        @SerialId(1) val dwType: Long? = null,
        @SerialId(2) val dwUin: Long? = null,
        @SerialId(3) val remaek: String? = ""
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0x24(
        @SerialId(0) val vPluginNumList: List<PluginNum>? = null
    ) : JceStruct

    @Serializable
    internal class PluginNum(
        @SerialId(0) val dwID: Long? = null,
        @SerialId(1) val dwNUm: Long? = null,
        @SerialId(2) val flag: Byte? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0xa(
        @SerialId(0) val uSrcAppId: Long? = null,
        @SerialId(1) val uSrcInstId: Long? = null,
        @SerialId(2) val uDstAppId: Long? = null,
        @SerialId(3) val uDstInstId: Long? = null,
        @SerialId(4) val uDstUin: Long? = null,
        @SerialId(5) val uType: Long? = null,
        @SerialId(6) val uServerIp: Long? = null,
        @SerialId(7) val uServerPort: Long? = null,
        @SerialId(8) val vUrlNotify: ByteArray? = null,
        @SerialId(9) val vTokenKey: ByteArray? = null,
        @SerialId(10) val uFileLen: Long? = null,
        @SerialId(11) val fileName: ByteArray? = null,
        @SerialId(12) val vMd5: ByteArray? = null,
        @SerialId(13) val sessionId: Long? = null,
        @SerialId(14) val originfileMd5: ByteArray? = null,
        @SerialId(15) val uOriginfiletype: Long? = null,
        @SerialId(16) val uSeq: Long? = null
    ) : JceStruct

    @Serializable
    internal class MsgType0x210SubMsgType0xe(
        @SerialId(0) val uint32SrcAppId: Long? = null,
        @SerialId(1) val uint32SrcInstId: Long? = null,
        @SerialId(2) val uint32DstAppId: Long? = null,
        @SerialId(3) val uint32DstInstId: Long? = null,
        @SerialId(4) val uint64DstUin: Long? = null,
        @SerialId(5) val uint64Sessionid: Long? = null,
        @SerialId(6) val uint32Operate: Long? = null,
        @SerialId(7) val uint32Seq: Long? = null,
        @SerialId(8) val uint32Code: Long? = null,
        @SerialId(9) val msg: String? = ""
    ) : JceStruct
}