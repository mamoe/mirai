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

internal class OnlinePushPack {
    @Serializable
    internal class DelMsgInfo(
        @JceId(0) val fromUin: Long,
        @JceId(1) val uMsgTime: Long,
        @JceId(2) val shMsgSeq: Short,
        @JceId(3) val vMsgCookies: ByteArray? = null,
        @JceId(4) val wCmd: Short? = null,
        @JceId(5) val uMsgType: Long? = null,
        @JceId(6) val uAppId: Long? = null,
        @JceId(7) val sendTime: Long? = null,
        @JceId(8) val ssoSeq: Int? = null,
        @JceId(9) val ssoIp: Int? = null,
        @JceId(10) val clientIp: Int? = null
    ) : JceStruct

    @Serializable
    internal class DeviceInfo(
        @JceId(0) val netType: Byte? = null,
        @JceId(1) val devType: String? = "",
        @JceId(2) val oSVer: String? = "",
        @JceId(3) val vendorName: String? = "",
        @JceId(4) val vendorOSName: String? = "",
        @JceId(5) val iOSIdfa: String? = ""
    ) : JceStruct

    @Serializable
    internal class Name(
        @JceId(0) val fromUin: Long,
        @JceId(1) val uMsgTime: Long,
        @JceId(2) val shMsgType: Short,
        @JceId(3) val shMsgSeq: Short,
        @JceId(4) val msg: String = "",
        @JceId(5) val uRealMsgTime: Int? = null,
        @JceId(6) val vMsg: ByteArray? = null,
        @JceId(7) val uAppShareID: Long? = null,
        @JceId(8) val vMsgCookies: ByteArray? = null,
        @JceId(9) val vAppShareCookie: ByteArray? = null,
        @JceId(10) val msgUid: Long? = null,
        @JceId(11) val lastChangeTime: Long? = 1L,
        @JceId(12) val vCPicInfo: List<CPicInfo>? = null,
        @JceId(13) val stShareData: ShareData? = null,
        @JceId(14) val fromInstId: Long? = null,
        @JceId(15) val vRemarkOfSender: ByteArray? = null,
        @JceId(16) val fromMobile: String? = "",
        @JceId(17) val fromName: String? = "",
        @JceId(18) val vNickName: List<String>? = null,
        @JceId(19) val stC2CTmpMsgHead: TempMsgHead? = null
    ) : JceStruct

    @Serializable
    internal class SvcReqPushMsg(
        @JceId(0) val uin: Long,
        @JceId(1) val uMsgTime: Long,
        @JceId(2) val vMsgInfos: List<MsgInfo>,
        @JceId(3) val svrip: Int? = 0,
        @JceId(4) val vSyncCookie: ByteArray? = null,
        @JceId(5) val vUinPairMsg: List<UinPairMsg>? = null,
        @JceId(6) val mPreviews: Map<String, ByteArray>? = null
        // @SerialId(7) val wUserActive: Int? = null,
        //@SerialId(12) val wGeneralFlag: Int? = null
    ) : JceStruct

    @Serializable
    internal class SvcRespPushMsg(
        @JceId(0) val uin: Long,
        @JceId(1) val vDelInfos: List<DelMsgInfo>,
        @JceId(2) val svrip: Int,
        @JceId(3) val pushToken: ByteArray? = null,
        @JceId(4) val serviceType: Int? = null,
        @JceId(5) val deviceInfo: DeviceInfo? = null
    ) : JceStruct

    @Serializable
    internal class UinPairMsg(
        @JceId(1) val uLastReadTime: Long? = null,
        @JceId(2) val peerUin: Long? = null,
        @JceId(3) val uMsgCompleted: Long? = null,
        @JceId(4) val vMsgInfos: List<MsgInfo>? = null
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
        @JceId(10) val vProtobuf: ByteArray? = null
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
    internal class MsgType0x210SubMsgType0x17(
        @JceId(0) val dwOpType: Long? = null,
        @JceId(1) val stAddGroup: AddGroup? = null,
        @JceId(2) val stDelGroup: DelGroup? = null,
        @JceId(3) val stModGroupName: ModGroupName? = null,
        @JceId(4) val stModGroupSort: ModGroupSort? = null,
        @JceId(5) val stModFriendGroup: ModFriendGroup? = null
    ) : JceStruct

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
    internal class ModFriendGroup(
        @JceId(0) val vMsgFrdGroup: List<FriendGroup>? = null
    ) : JceStruct

    @Serializable
    internal class FriendGroup(
        @JceId(0) val dwFuin: Long? = null,
        @JceId(1) val vOldGroupID: List<Long>? = null,
        @JceId(2) val vNewGroupID: List<Long>? = null
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
    internal class GroupSort(
        @JceId(0) val dwGroupID: Long? = null,
        @JceId(1) val dwSortID: Long? = null
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
    internal class PluginNum(
        @JceId(0) val dwID: Long? = null,
        @JceId(1) val dwNUm: Long? = null,
        @JceId(2) val flag: Byte? = null
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
        @JceId(8) val vUrlNotify: ByteArray? = null,
        @JceId(9) val vTokenKey: ByteArray? = null,
        @JceId(10) val uFileLen: Long? = null,
        @JceId(11) val fileName: ByteArray? = null,
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
}