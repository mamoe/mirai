package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

/**
 * msf.msgcomm.msg_comm
 */
@Serializable
internal class MsgComm : ProtoBuf {
    @Serializable
    internal class AppShareInfo(
        @ProtoId(1) @JvmField val appshareId: Int = 0,
        @ProtoId(2) @JvmField val appshareCookie: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val appshareResource: PluginInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class C2CTmpMsgHead(
        @ProtoId(1) @JvmField val c2cType: Int = 0,
        @ProtoId(2) @JvmField val serviceType: Int = 0,
        @ProtoId(3) @JvmField val groupUin: Long = 0L,
        @ProtoId(4) @JvmField val groupCode: Long = 0L,
        @ProtoId(5) @JvmField val sig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val sigType: Int = 0,
        @ProtoId(7) @JvmField val fromPhone: String = "",
        @ProtoId(8) @JvmField val toPhone: String = "",
        @ProtoId(9) @JvmField val lockDisplay: Int = 0,
        @ProtoId(10) @JvmField val directionFlag: Int = 0,
        @ProtoId(11) @JvmField val reserved: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ContentHead(
        @ProtoId(1) @JvmField val pkgNum: Int = 0,
        @ProtoId(2) @JvmField val pkgIndex: Int = 0,
        @ProtoId(3) @JvmField val divSeq: Int = 0,
        @ProtoId(4) @JvmField val autoReply: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DiscussInfo(
        @ProtoId(1) @JvmField val discussUin: Long = 0L,
        @ProtoId(2) @JvmField val discussType: Int = 0,
        @ProtoId(3) @JvmField val discussInfoSeq: Long = 0L,
        @ProtoId(4) @JvmField val discussRemark: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val discussName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class ExtGroupKeyInfo(
        @ProtoId(1) @JvmField val curMaxSeq: Int = 0,
        @ProtoId(2) @JvmField val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class GroupInfo(
        @ProtoId(1) @JvmField val groupCode: Long = 0L,
        @ProtoId(2) @JvmField val groupType: Int = 0,
        @ProtoId(3) @JvmField val groupInfoSeq: Long = 0L,
        @ProtoId(4) @JvmField val groupCard: String = "",
        @ProtoId(5) @JvmField val groupRank: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(6) @JvmField val groupLevel: Int = 0,
        @ProtoId(7) @JvmField val groupCardType: Int = 0,
        @ProtoId(8) @JvmField val groupName: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class Msg(
        @ProtoId(1) @JvmField val msgHead: MsgHead,
        @ProtoId(2) @JvmField val contentHead: ContentHead? = null,
        @ProtoId(3) @JvmField val msgBody: ImMsgBody.MsgBody,
        @ProtoId(4) @JvmField val appshareInfo: AppShareInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class MsgHead(
        @ProtoId(1) @JvmField val fromUin: Long = 0L,
        @ProtoId(2) @JvmField val toUin: Long = 0L,
        @ProtoId(3) @JvmField val msgType: Int = 0,
        @ProtoId(4) @JvmField val c2cCmd: Int = 0,
        @ProtoId(5) @JvmField val msgSeq: Int = 0,
        @ProtoId(6) @JvmField val msgTime: Int = 0,
        @ProtoId(7) var msgUid: Long = 0L,
        @ProtoId(8) @JvmField val c2cTmpMsgHead: C2CTmpMsgHead? = null,
        @ProtoId(9) @JvmField val groupInfo: GroupInfo? = null,
        @ProtoId(10) @JvmField val fromAppid: Int = 0,
        @ProtoId(11) @JvmField val fromInstid: Int = 0,
        @ProtoId(12) @JvmField val userActive: Int = 0,
        @ProtoId(13) @JvmField val discussInfo: DiscussInfo? = null,
        @ProtoId(14) @JvmField val fromNick: String = "",
        @ProtoId(15) @JvmField val authUin: Long = 0L,
        @ProtoId(16) @JvmField val authNick: String = "",
        @ProtoId(17) @JvmField val msgFlag: Int = 0,
        @ProtoId(18) @JvmField val authRemark: String = "",
        @ProtoId(19) @JvmField val groupName: String = "",
        @ProtoId(20) @JvmField val mutiltransHead: MutilTransHead? = null,
        @ProtoId(21) @JvmField val msgInstCtrl: ImMsgHead.InstCtrl? = null,
        @ProtoId(22) @JvmField val publicAccountGroupSendFlag: Int = 0,
        @ProtoId(23) @JvmField val wseqInC2cMsghead: Int = 0,
        @ProtoId(24) @JvmField val cpid: Long = 0L,
        @ProtoId(25) @JvmField val extGroupKeyInfo: ExtGroupKeyInfo? = null,
        @ProtoId(26) @JvmField val multiCompatibleText: String = "",
        @ProtoId(27) @JvmField val authSex: Int = 0,
        @ProtoId(28) @JvmField val isSrcMsg: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class MsgType0x210(
        @ProtoId(1) @JvmField val subMsgType: Int = 0,
        @ProtoId(2) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class MutilTransHead(
        @ProtoId(1) @JvmField val status: Int = 0,
        @ProtoId(2) @JvmField val msgId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class PluginInfo(
        @ProtoId(1) @JvmField val resId: Int = 0,
        @ProtoId(2) @JvmField val pkgName: String = "",
        @ProtoId(3) @JvmField val newVer: Int = 0,
        @ProtoId(4) @JvmField val resType: Int = 0,
        @ProtoId(5) @JvmField val lanType: Int = 0,
        @ProtoId(6) @JvmField val priority: Int = 0,
        @ProtoId(7) @JvmField val resName: String = "",
        @ProtoId(8) @JvmField val resDesc: String = "",
        @ProtoId(9) @JvmField val resUrlBig: String = "",
        @ProtoId(10) @JvmField val resUrlSmall: String = "",
        @ProtoId(11) @JvmField val resConf: String = ""
    ) : ProtoBuf

    @Serializable
    internal class Uin2Nick(
        @ProtoId(1) @JvmField val uin: Long = 0L,
        @ProtoId(2) @JvmField val nick: String = ""
    ) : ProtoBuf

    @Serializable
    internal class UinPairMsg(
        @ProtoId(1) @JvmField val lastReadTime: Int = 0,
        @ProtoId(2) @JvmField val peerUin: Long = 0L,
        @ProtoId(3) @JvmField val msgCompleted: Int = 0,
        @ProtoId(4) @JvmField val msg: List<Msg>? = null,
        @ProtoId(5) @JvmField val unreadMsgNum: Int = 0,
        @ProtoId(8) @JvmField val c2cType: Int = 0,
        @ProtoId(9) @JvmField val serviceType: Int = 0,
        @ProtoId(10) @JvmField val pbReserve: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf
}