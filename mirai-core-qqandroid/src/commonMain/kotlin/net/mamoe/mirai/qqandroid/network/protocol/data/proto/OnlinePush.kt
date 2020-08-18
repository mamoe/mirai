package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class MsgOnlinePush {
    @Serializable
    internal class PbPushMsg(
        @ProtoNumber(1) @JvmField val msg: MsgComm.Msg,
        @ProtoNumber(2) @JvmField val svrip: Int = 0,
        @ProtoNumber(3) @JvmField val pushToken: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val pingFlag: Int = 0,
        @ProtoNumber(9) @JvmField val generalFlag: Int = 0
    ) : ProtoBuf
}

@Serializable
internal class OnlinePushTrans : ProtoBuf {
    @Serializable
    internal class ExtGroupKeyInfo(
        @ProtoNumber(1) @JvmField val curMaxSeq: Int = 0,
        @ProtoNumber(2) @JvmField val curTime: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class PbMsgInfo(
        @ProtoNumber(1) @JvmField val fromUin: Long = 0L,
        @ProtoNumber(2) @JvmField val toUin: Long = 0L,
        @ProtoNumber(3) @JvmField val msgType: Int = 0,
        @ProtoNumber(4) @JvmField val msgSubtype: Int = 0,
        @ProtoNumber(5) @JvmField val msgSeq: Int = 0,
        @ProtoNumber(6) @JvmField val msgUid: Long = 0L,
        @ProtoNumber(7) @JvmField val msgTime: Int = 0,
        @ProtoNumber(8) @JvmField val realMsgTime: Int = 0,
        @ProtoNumber(9) @JvmField val nickName: String = "",
        @ProtoNumber(10) @JvmField val msgData: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val svrIp: Int = 0,
        @ProtoNumber(12) @JvmField val extGroupKeyInfo: ExtGroupKeyInfo? = null,
        @ProtoNumber(17) @JvmField val generalFlag: Int = 0
    ) : ProtoBuf
}