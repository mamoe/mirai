package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class LongMsg : ProtoBuf {
    @Serializable
internal class MsgDeleteReq(
        @ProtoId(1) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) @JvmField val msgType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgDeleteRsp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgDownReq(
        @ProtoId(1) @JvmField val srcUin: Int = 0,
        @ProtoId(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val msgType: Int = 0,
        @ProtoId(4) @JvmField val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgDownRsp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class MsgUpReq(
        @ProtoId(1) @JvmField val msgType: Int = 0,
        @ProtoId(2) @JvmField val dstUin: Long = 0L,
        @ProtoId(3) @JvmField val msgId: Int = 0,
        @ProtoId(4) @JvmField val msgContent: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(5) @JvmField val storeType: Int = 0,
        @ProtoId(6) @JvmField val msgUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) @JvmField val needCache: Int = 0
    ) : ProtoBuf

    @Serializable
internal class MsgUpRsp(
        @ProtoId(1) @JvmField val result: Int = 0,
        @ProtoId(2) @JvmField val msgId: Int = 0,
        @ProtoId(3) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class ReqBody(
        @ProtoId(1) @JvmField val subcmd: Int = 0,
        @ProtoId(2) @JvmField val termType: Int = 0,
        @ProtoId(3) @JvmField val platformType: Int = 0,
        @ProtoId(4) @JvmField val msgUpReq: List<LongMsg.MsgUpReq>? = null,
        @ProtoId(5) @JvmField val msgDownReq: List<LongMsg.MsgDownReq>? = null,
        @ProtoId(6) @JvmField val msgDelReq: List<LongMsg.MsgDeleteReq>? = null,
        @ProtoId(10) @JvmField val agentType: Int = 0
    ) : ProtoBuf

    @Serializable
internal class RspBody(
        @ProtoId(1) @JvmField val subcmd: Int = 0,
        @ProtoId(2) @JvmField val msgUpRsp: List<LongMsg.MsgUpRsp>? = null,
        @ProtoId(3) @JvmField val msgDownRsp: List<LongMsg.MsgDownRsp>? = null,
        @ProtoId(4) @JvmField val msgDelRsp: List<LongMsg.MsgDeleteRsp>? = null
    ) : ProtoBuf
}