package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class LongMsg : ProtoBuf {
    @Serializable
internal class MsgDeleteReq(
        @ProtoNumber(1) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgDeleteRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgDownReq(
        @ProtoNumber(1) @JvmField val srcUin: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgType: Int = 0,
        @ProtoNumber(4) @JvmField val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgDownRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgUpReq(
        @ProtoNumber(1) @JvmField val msgType: Int = 0,
        @ProtoNumber(2) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(3) @JvmField val msgId: Int = 0,
        @ProtoNumber(4) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(5) @JvmField val storeType: Int = 0,
        @ProtoNumber(6) @JvmField val msgUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgUpRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgId: Int = 0,
        @ProtoNumber(3) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ReqBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val termType: Int = 0,
        @ProtoNumber(3) @JvmField val platformType: Int = 0,
        @ProtoNumber(4) @JvmField val msgUpReq: List<LongMsg.MsgUpReq>? = null,
        @ProtoNumber(5) @JvmField val msgDownReq: List<LongMsg.MsgDownReq>? = null,
        @ProtoNumber(6) @JvmField val msgDelReq: List<LongMsg.MsgDeleteReq>? = null,
        @ProtoNumber(10) @JvmField val agentType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class RspBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val msgUpRsp: List<LongMsg.MsgUpRsp>? = null,
        @ProtoNumber(3) @JvmField val msgDownRsp: List<LongMsg.MsgDownRsp>? = null,
        @ProtoNumber(4) @JvmField val msgDelRsp: List<LongMsg.MsgDeleteRsp>? = null
    ) : ProtoBuf
}