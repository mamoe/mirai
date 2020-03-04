/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

/**
 * msf.msgcomm.msg_comm
 */
@Serializable
internal class MsgComm : ProtoBuf {
    @Serializable
    internal class AppShareInfo(
        @ProtoId(1) val appshareId: Int = 0,
        @ProtoId(2) val appshareCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val appshareResource: PluginInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class C2CTmpMsgHead(
        @ProtoId(1) val c2cType: Int = 0,
        @ProtoId(2) val serviceType: Int = 0,
        @ProtoId(3) val groupUin: Long = 0L,
        @ProtoId(4) val groupCode: Long = 0L,
        @ProtoId(5) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val sigType: Int = 0,
        @ProtoId(7) val fromPhone: String = "",
        @ProtoId(8) val toPhone: String = "",
        @ProtoId(9) val lockDisplay: Int = 0,
        @ProtoId(10) val directionFlag: Int = 0,
        @ProtoId(11) val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ContentHead(
        @ProtoId(1) val pkgNum: Int = 0,
        @ProtoId(2) val pkgIndex: Int = 0,
        @ProtoId(3) val divSeq: Int = 0,
        @ProtoId(4) val autoReply: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DiscussInfo(
        @ProtoId(1) val discussUin: Long = 0L,
        @ProtoId(2) val discussType: Int = 0,
        @ProtoId(3) val discussInfoSeq: Long = 0L,
        @ProtoId(4) val discussRemark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val discussName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ExtGroupKeyInfo(
        @ProtoId(1) val curMaxSeq: Int = 0,
        @ProtoId(2) val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) val groupCode: Long = 0L,
        @ProtoId(2) val groupType: Int = 0,
        @ProtoId(3) val groupInfoSeq: Long = 0L,
        @ProtoId(4) val groupCard: String = "",
        @ProtoId(5) val groupRank: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) val groupLevel: Int = 0,
        @ProtoId(7) val groupCardType: Int = 0,
        @ProtoId(8) val groupName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Msg(
        @ProtoId(1) val msgHead: MsgHead,
        @ProtoId(2) val contentHead: ContentHead? = null,
        @ProtoId(3) val msgBody: ImMsgBody.MsgBody,
        @ProtoId(4) val appshareInfo: AppShareInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgHead(
        @ProtoId(1) val fromUin: Long = 0L,
        @ProtoId(2) val toUin: Long = 0L,
        @ProtoId(3) val msgType: Int = 0,
        @ProtoId(4) val c2cCmd: Int = 0,
        @ProtoId(5) val msgSeq: Int = 0,
        @ProtoId(6) val msgTime: Int = 0,
        @ProtoId(7) var msgUid: Long = 0L,
        @ProtoId(8) val c2cTmpMsgHead: C2CTmpMsgHead? = null,
        @ProtoId(9) val groupInfo: GroupInfo? = null,
        @ProtoId(10) val fromAppid: Int = 0,
        @ProtoId(11) val fromInstid: Int = 0,
        @ProtoId(12) val userActive: Int = 0,
        @ProtoId(13) val discussInfo: DiscussInfo? = null,
        @ProtoId(14) val fromNick: String = "",
        @ProtoId(15) val authUin: Long = 0L,
        @ProtoId(16) val authNick: String = "",
        @ProtoId(17) val msgFlag: Int = 0,
        @ProtoId(18) val authRemark: String = "",
        @ProtoId(19) val groupName: String = "",
        @ProtoId(20) val mutiltransHead: MutilTransHead? = null,
        @ProtoId(21) val msgInstCtrl: ImMsgHead.InstCtrl? = null,
        @ProtoId(22) val publicAccountGroupSendFlag: Int = 0,
        @ProtoId(23) val wseqInC2cMsghead: Int = 0,
        @ProtoId(24) val cpid: Long = 0L,
        @ProtoId(25) val extGroupKeyInfo: ExtGroupKeyInfo? = null,
        @ProtoId(26) val multiCompatibleText: String = "",
        @ProtoId(27) val authSex: Int = 0,
        @ProtoId(28) val isSrcMsg: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class MsgType0x210(
        @ProtoId(1) val subMsgType: Int = 0,
        @ProtoId(2) val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MutilTransHead(
        @ProtoId(1) val status: Int = 0,
        @ProtoId(2) val msgId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PluginInfo(
        @ProtoId(1) val resId: Int = 0,
        @ProtoId(2) val pkgName: String = "",
        @ProtoId(3) val newVer: Int = 0,
        @ProtoId(4) val resType: Int = 0,
        @ProtoId(5) val lanType: Int = 0,
        @ProtoId(6) val priority: Int = 0,
        @ProtoId(7) val resName: String = "",
        @ProtoId(8) val resDesc: String = "",
        @ProtoId(9) val resUrlBig: String = "",
        @ProtoId(10) val resUrlSmall: String = "",
        @ProtoId(11) val resConf: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Uin2Nick(
        @ProtoId(1) val uin: Long = 0L,
        @ProtoId(2) val nick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UinPairMsg(
        @ProtoId(1) val lastReadTime: Int = 0,
        @ProtoId(2) val peerUin: Long = 0L,
        @ProtoId(3) val msgCompleted: Int = 0,
        @ProtoId(4) val msg: List<Msg>? = null,
        @ProtoId(5) val unreadMsgNum: Int = 0,
        @ProtoId(8) val c2cType: Int = 0,
        @ProtoId(9) val serviceType: Int = 0,
        @ProtoId(10) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}