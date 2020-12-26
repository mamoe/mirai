/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

/**
 * msf.msgcomm.msg_comm
 */
@Serializable
internal class MsgComm : ProtoBuf {
    @Serializable
    internal class AppShareInfo(
        @ProtoNumber(1) @JvmField val appshareId: Int = 0,
        @ProtoNumber(2) @JvmField val appshareCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val appshareResource: PluginInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class C2CTmpMsgHead(
        @ProtoNumber(1) @JvmField val c2cType: Int = 0,
        @ProtoNumber(2) @JvmField val serviceType: Int = 0,
        @ProtoNumber(3) @JvmField val groupUin: Long = 0L,
        @ProtoNumber(4) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(5) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val sigType: Int = 0,
        @ProtoNumber(7) @JvmField val fromPhone: String = "",
        @ProtoNumber(8) @JvmField val toPhone: String = "",
        @ProtoNumber(9) @JvmField val lockDisplay: Int = 0,
        @ProtoNumber(10) @JvmField val directionFlag: Int = 0,
        @ProtoNumber(11) @JvmField val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ContentHead(
        @ProtoNumber(1) @JvmField val pkgNum: Int = 0,
        @ProtoNumber(2) @JvmField val pkgIndex: Int = 0,
        @ProtoNumber(3) @JvmField val divSeq: Int = 0,
        @ProtoNumber(4) @JvmField val autoReply: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DiscussInfo(
        @ProtoNumber(1) @JvmField val discussUin: Long = 0L,
        @ProtoNumber(2) @JvmField val discussType: Int = 0,
        @ProtoNumber(3) @JvmField val discussInfoSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val discussRemark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val discussName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ExtGroupKeyInfo(
        @ProtoNumber(1) @JvmField val curMaxSeq: Int = 0,
        @ProtoNumber(2) @JvmField val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoNumber(1) @JvmField val groupCode: Long = 0L,
        @ProtoNumber(2) @JvmField val groupType: Int = 0,
        @ProtoNumber(3) @JvmField val groupInfoSeq: Long = 0L,
        @ProtoNumber(4) @JvmField val groupCard: String = "",
        @ProtoNumber(5) @JvmField val groupRank: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(6) @JvmField val groupLevel: Int = 0,
        @ProtoNumber(7) @JvmField val groupCardType: Int = 0,
        @ProtoNumber(8) @JvmField val groupName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Msg(
        @ProtoNumber(1) @JvmField val msgHead: MsgHead,
        @ProtoNumber(2) @JvmField val contentHead: ContentHead? = null,
        @ProtoNumber(3) @JvmField val msgBody: ImMsgBody.MsgBody,
        @ProtoNumber(4) @JvmField val appshareInfo: AppShareInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgHead(
        @ProtoNumber(1) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(2) @JvmField val toUin: Long = 0L,
        @ProtoNumber(3) @JvmField val msgType: Int = 0,
        @ProtoNumber(4) @JvmField val c2cCmd: Int = 0,
        @ProtoNumber(5) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(6) @JvmField val msgTime: Int = 0,
        @ProtoNumber(7) var msgUid: Long = 0L,
        @ProtoNumber(8) @JvmField val c2cTmpMsgHead: C2CTmpMsgHead? = null,
        @ProtoNumber(9) @JvmField val groupInfo: GroupInfo? = null,
        @ProtoNumber(10) @JvmField val fromAppid: Int = 0,
        @ProtoNumber(11) @JvmField val fromInstid: Int = 0,
        @ProtoNumber(12) @JvmField val userActive: Int = 0,
        @ProtoNumber(13) @JvmField val discussInfo: DiscussInfo? = null,
        @ProtoNumber(14) @JvmField val fromNick: String = "",
        @ProtoNumber(15) @JvmField val authUin: Long = 0L,
        @ProtoNumber(16) @JvmField val authNick: String = "",
        @ProtoNumber(17) @JvmField val msgFlag: Int = 0,
        @ProtoNumber(18) @JvmField val authRemark: String = "",
        @ProtoNumber(19) @JvmField val groupName: String = "",
        @ProtoNumber(20) @JvmField val mutiltransHead: MutilTransHead? = null,
        @ProtoNumber(21) @JvmField val msgInstCtrl: ImMsgHead.InstCtrl? = null,
        @ProtoNumber(22) @JvmField val publicAccountGroupSendFlag: Int = 0,
        @ProtoNumber(23) @JvmField val wseqInC2cMsghead: Int = 0,
        @ProtoNumber(24) @JvmField val cpid: Long = 0L,
        @ProtoNumber(25) @JvmField val extGroupKeyInfo: ExtGroupKeyInfo? = null,
        @ProtoNumber(26) @JvmField val multiCompatibleText: String = "",
        @ProtoNumber(27) @JvmField val authSex: Int = 0,
        @ProtoNumber(28) @JvmField val isSrcMsg: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class MsgType0x210(
        @ProtoNumber(1) @JvmField val subMsgType: Int = 0,
        @ProtoNumber(2) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MutilTransHead(
        @ProtoNumber(1) @JvmField val status: Int = 0,
        @ProtoNumber(2) @JvmField val msgId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PluginInfo(
        @ProtoNumber(1) @JvmField val resId: Int = 0,
        @ProtoNumber(2) @JvmField val pkgName: String = "",
        @ProtoNumber(3) @JvmField val newVer: Int = 0,
        @ProtoNumber(4) @JvmField val resType: Int = 0,
        @ProtoNumber(5) @JvmField val lanType: Int = 0,
        @ProtoNumber(6) @JvmField val priority: Int = 0,
        @ProtoNumber(7) @JvmField val resName: String = "",
        @ProtoNumber(8) @JvmField val resDesc: String = "",
        @ProtoNumber(9) @JvmField val resUrlBig: String = "",
        @ProtoNumber(10) @JvmField val resUrlSmall: String = "",
        @ProtoNumber(11) @JvmField val resConf: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Uin2Nick(
        @ProtoNumber(1) @JvmField val uin: Long = 0L,
        @ProtoNumber(2) @JvmField val nick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UinPairMsg(
        @ProtoNumber(1) @JvmField val lastReadTime: Int = 0,
        @ProtoNumber(2) @JvmField val peerUin: Long = 0L,
        @ProtoNumber(3) @JvmField val msgCompleted: Int = 0,
        @ProtoNumber(4) @JvmField val msg: List<Msg> = emptyList(),
        @ProtoNumber(5) @JvmField val unreadMsgNum: Int = 0,
        @ProtoNumber(8) @JvmField val c2cType: Int = 0,
        @ProtoNumber(9) @JvmField val serviceType: Int = 0,
        @ProtoNumber(10) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}