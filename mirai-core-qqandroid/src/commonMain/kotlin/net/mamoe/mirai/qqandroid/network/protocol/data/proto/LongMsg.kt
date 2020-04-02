package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

internal class LongMsg : ProtoBuf {
    @Serializable
    class MsgDeleteReq(
        @ProtoId(1) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
    class MsgDeleteRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MsgDownReq(
        @ProtoId(1) val srcUin: Int = 0,
        @ProtoId(2) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgType: Int = 0,
        @ProtoId(4) val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
    class MsgDownRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class MsgUpReq(
        @ProtoId(1) val msgType: Int = 0,
        @ProtoId(2) val dstUin: Long = 0L,
        @ProtoId(3) val msgId: Int = 0,
        @ProtoId(4) val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) val storeType: Int = 0,
        @ProtoId(6) val msgUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
    class MsgUpRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val msgId: Int = 0,
        @ProtoId(3) val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    class ReqBody(
        @ProtoId(1) val subcmd: Int = 0,
        @ProtoId(2) val termType: Int = 0,
        @ProtoId(3) val platformType: Int = 0,
        @ProtoId(4) val msgUpReq: List<LongMsg.MsgUpReq>? = null,
        @ProtoId(5) val msgDownReq: List<LongMsg.MsgDownReq>? = null,
        @ProtoId(6) val msgDelReq: List<LongMsg.MsgDeleteReq>? = null,
        @ProtoId(10) val agentType: Int = 0
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @ProtoId(1) val subcmd: Int = 0,
        @ProtoId(2) val msgUpRsp: List<LongMsg.MsgUpRsp>? = null,
        @ProtoId(3) val msgDownRsp: List<LongMsg.MsgDownRsp>? = null,
        @ProtoId(4) val msgDelRsp: List<LongMsg.MsgDeleteRsp>? = null
    ) : ProtoBuf
}