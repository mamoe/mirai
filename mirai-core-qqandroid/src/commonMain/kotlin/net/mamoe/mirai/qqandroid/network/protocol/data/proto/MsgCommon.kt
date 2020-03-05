/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

/**
 * msf.msgcomm.msg_comm
 */
@Serializable
internal class MsgComm : ProtoBuf {
    @Serializable
    internal class AppShareInfo(
        @SerialId(1) val appshareId: Int = 0,
        @SerialId(2) val appshareCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val appshareResource: PluginInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class C2CTmpMsgHead(
        @SerialId(1) val c2cType: Int = 0,
        @SerialId(2) val serviceType: Int = 0,
        @SerialId(3) val groupUin: Long = 0L,
        @SerialId(4) val groupCode: Long = 0L,
        @SerialId(5) val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val sigType: Int = 0,
        @SerialId(7) val fromPhone: String = "",
        @SerialId(8) val toPhone: String = "",
        @SerialId(9) val lockDisplay: Int = 0,
        @SerialId(10) val directionFlag: Int = 0,
        @SerialId(11) val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ContentHead(
        @SerialId(1) val pkgNum: Int = 0,
        @SerialId(2) val pkgIndex: Int = 0,
        @SerialId(3) val divSeq: Int = 0,
        @SerialId(4) val autoReply: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DiscussInfo(
        @SerialId(1) val discussUin: Long = 0L,
        @SerialId(2) val discussType: Int = 0,
        @SerialId(3) val discussInfoSeq: Long = 0L,
        @SerialId(4) val discussRemark: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(5) val discussName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ExtGroupKeyInfo(
        @SerialId(1) val curMaxSeq: Int = 0,
        @SerialId(2) val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @SerialId(1) val groupCode: Long = 0L,
        @SerialId(2) val groupType: Int = 0,
        @SerialId(3) val groupInfoSeq: Long = 0L,
        @SerialId(4) val groupCard: String = "",
        @SerialId(5) val groupRank: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(6) val groupLevel: Int = 0,
        @SerialId(7) val groupCardType: Int = 0,
        @SerialId(8) val groupName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Msg(
        @SerialId(1) val msgHead: MsgHead,
        @SerialId(2) val contentHead: ContentHead? = null,
        @SerialId(3) val msgBody: ImMsgBody.MsgBody,
        @SerialId(4) val appshareInfo: AppShareInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgHead(
        @SerialId(1) val fromUin: Long = 0L,
        @SerialId(2) val toUin: Long = 0L,
        @SerialId(3) val msgType: Int = 0,
        @SerialId(4) val c2cCmd: Int = 0,
        @SerialId(5) val msgSeq: Int = 0,
        @SerialId(6) val msgTime: Int = 0,
        @SerialId(7) var msgUid: Long = 0L,
        @SerialId(8) val c2cTmpMsgHead: C2CTmpMsgHead? = null,
        @SerialId(9) val groupInfo: GroupInfo? = null,
        @SerialId(10) val fromAppid: Int = 0,
        @SerialId(11) val fromInstid: Int = 0,
        @SerialId(12) val userActive: Int = 0,
        @SerialId(13) val discussInfo: DiscussInfo? = null,
        @SerialId(14) val fromNick: String = "",
        @SerialId(15) val authUin: Long = 0L,
        @SerialId(16) val authNick: String = "",
        @SerialId(17) val msgFlag: Int = 0,
        @SerialId(18) val authRemark: String = "",
        @SerialId(19) val groupName: String = "",
        @SerialId(20) val mutiltransHead: MutilTransHead? = null,
        @SerialId(21) val msgInstCtrl: ImMsgHead.InstCtrl? = null,
        @SerialId(22) val publicAccountGroupSendFlag: Int = 0,
        @SerialId(23) val wseqInC2cMsghead: Int = 0,
        @SerialId(24) val cpid: Long = 0L,
        @SerialId(25) val extGroupKeyInfo: ExtGroupKeyInfo? = null,
        @SerialId(26) val multiCompatibleText: String = "",
        @SerialId(27) val authSex: Int = 0,
        @SerialId(28) val isSrcMsg: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class MsgType0x210(
        @SerialId(1) val subMsgType: Int = 0,
        @SerialId(2) val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MutilTransHead(
        @SerialId(1) val status: Int = 0,
        @SerialId(2) val msgId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PluginInfo(
        @SerialId(1) val resId: Int = 0,
        @SerialId(2) val pkgName: String = "",
        @SerialId(3) val newVer: Int = 0,
        @SerialId(4) val resType: Int = 0,
        @SerialId(5) val lanType: Int = 0,
        @SerialId(6) val priority: Int = 0,
        @SerialId(7) val resName: String = "",
        @SerialId(8) val resDesc: String = "",
        @SerialId(9) val resUrlBig: String = "",
        @SerialId(10) val resUrlSmall: String = "",
        @SerialId(11) val resConf: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Uin2Nick(
        @SerialId(1) val uin: Long = 0L,
        @SerialId(2) val nick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UinPairMsg(
        @SerialId(1) val lastReadTime: Int = 0,
        @SerialId(2) val peerUin: Long = 0L,
        @SerialId(3) val msgCompleted: Int = 0,
        @SerialId(4) val msg: List<Msg>? = null,
        @SerialId(5) val unreadMsgNum: Int = 0,
        @SerialId(8) val c2cType: Int = 0,
        @SerialId(9) val serviceType: Int = 0,
        @SerialId(10) val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}