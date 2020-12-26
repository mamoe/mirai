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

internal class OnlinePushPack {
    @Serializable
    internal class DelMsgInfo(
        @TarsId(0) @JvmField val fromUin: Long,
        @TarsId(1) @JvmField val uMsgTime: Long,
        @TarsId(2) @JvmField val shMsgSeq: Short,
        @TarsId(3) @JvmField val vMsgCookies: ByteArray? = null,
        @TarsId(4) @JvmField val wCmd: Short? = null,
        @TarsId(5) @JvmField val uMsgType: Long? = null,
        @TarsId(6) @JvmField val uAppId: Long? = null,
        @TarsId(7) @JvmField val sendTime: Long? = null,
        @TarsId(8) @JvmField val ssoSeq: Int? = null,
        @TarsId(9) @JvmField val ssoIp: Int? = null,
        @TarsId(10) @JvmField val clientIp: Int? = null
    ) : JceStruct

    @Serializable
    internal class DeviceInfo(
        @TarsId(0) @JvmField val netType: Byte? = null,
        @TarsId(1) @JvmField val devType: String? = "",
        @TarsId(2) @JvmField val oSVer: String? = "",
        @TarsId(3) @JvmField val vendorName: String? = "",
        @TarsId(4) @JvmField val vendorOSName: String? = "",
        @TarsId(5) @JvmField val iOSIdfa: String? = ""
    ) : JceStruct

    @Serializable
    internal class Name(
        @TarsId(0) @JvmField val fromUin: Long,
        @TarsId(1) @JvmField val uMsgTime: Long,
        @TarsId(2) @JvmField val shMsgType: Short,
        @TarsId(3) @JvmField val shMsgSeq: Short,
        @TarsId(4) @JvmField val msg: String = "",
        @TarsId(5) @JvmField val uRealMsgTime: Int? = null,
        @TarsId(6) @JvmField val vMsg: ByteArray? = null,
        @TarsId(7) @JvmField val uAppShareID: Long? = null,
        @TarsId(8) @JvmField val vMsgCookies: ByteArray? = null,
        @TarsId(9) @JvmField val vAppShareCookie: ByteArray? = null,
        @TarsId(10) @JvmField val msgUid: Long? = null,
        @TarsId(11) @JvmField val lastChangeTime: Long? = 1L,
        @TarsId(12) @JvmField val vCPicInfo: List<CPicInfo>? = null,
        @TarsId(13) @JvmField val stShareData: ShareData? = null,
        @TarsId(14) @JvmField val fromInstId: Long? = null,
        @TarsId(15) @JvmField val vRemarkOfSender: ByteArray? = null,
        @TarsId(16) @JvmField val fromMobile: String? = "",
        @TarsId(17) @JvmField val fromName: String? = "",
        @TarsId(18) @JvmField val vNickName: List<String>? = null,
        @TarsId(19) @JvmField val stC2CTmpMsgHead: TempMsgHead? = null
    ) : JceStruct

    @Serializable
    internal class SvcReqPushMsg(
        @TarsId(0) @JvmField val uin: Long,
        @TarsId(1) @JvmField val uMsgTime: Long,
        @TarsId(2) @JvmField val vMsgInfos: List<MsgInfo>,
        @TarsId(3) @JvmField val svrip: Int? = 0,
        @TarsId(4) @JvmField val vSyncCookie: ByteArray? = null,
        @TarsId(5) @JvmField val vUinPairMsg: List<UinPairMsg>? = null,
        @TarsId(6) @JvmField val mPreviews: Map<String, ByteArray>? = null
        // @SerialId(7) @JvmField val wUserActive: Int? = null,
        //@SerialId(12) @JvmField val wGeneralFlag: Int? = null
    ) : JceStruct

    @Serializable
    internal class SvcRespPushMsg(
        @TarsId(0) @JvmField val uin: Long,
        @TarsId(1) @JvmField val vDelInfos: List<DelMsgInfo>,
        @TarsId(2) @JvmField val svrip: Int = 0,
        @TarsId(3) @JvmField val pushToken: ByteArray? = null,
        @TarsId(4) @JvmField val serviceType: Int? = null,
        @TarsId(5) @JvmField val deviceInfo: DeviceInfo? = null
    ) : JceStruct

    @Serializable
    internal class UinPairMsg(
        @TarsId(1) @JvmField val uLastReadTime: Long? = null,
        @TarsId(2) @JvmField val peerUin: Long? = null,
        @TarsId(3) @JvmField val uMsgCompleted: Long? = null,
        @TarsId(4) @JvmField val vMsgInfos: List<MsgInfo>? = null
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
        @TarsId(10) @JvmField val vProtobuf: ByteArray? = null
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
    internal class MsgType0x210SubMsgType0x17(
        @TarsId(0) @JvmField val dwOpType: Long? = null,
        @TarsId(1) @JvmField val stAddGroup: AddGroup? = null,
        @TarsId(2) @JvmField val stDelGroup: DelGroup? = null,
        @TarsId(3) @JvmField val stModGroupName: ModGroupName? = null,
        @TarsId(4) @JvmField val stModGroupSort: ModGroupSort? = null,
        @TarsId(5) @JvmField val stModFriendGroup: ModFriendGroup? = null
    ) : JceStruct

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
    internal class ModFriendGroup(
        @TarsId(0) @JvmField val vMsgFrdGroup: List<FriendGroup>? = null
    ) : JceStruct

    @Serializable
    internal class FriendGroup(
        @TarsId(0) @JvmField val dwFuin: Long? = null,
        @TarsId(1) @JvmField val vOldGroupID: List<Long>? = null,
        @TarsId(2) @JvmField val vNewGroupID: List<Long>? = null
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
    internal class GroupSort(
        @TarsId(0) @JvmField val dwGroupID: Long? = null,
        @TarsId(1) @JvmField val dwSortID: Long? = null
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
    internal class PluginNum(
        @TarsId(0) @JvmField val dwID: Long? = null,
        @TarsId(1) @JvmField val dwNUm: Long? = null,
        @TarsId(2) @JvmField val flag: Byte? = null
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
}