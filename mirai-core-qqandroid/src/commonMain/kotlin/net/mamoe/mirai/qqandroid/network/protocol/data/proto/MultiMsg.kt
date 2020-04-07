package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf

@Serializable
internal class MultiMsg : ProtoBuf {
    @Serializable
    class ExternMsg(
        @ProtoId(1) val channelType: Int = 0
    ) : ProtoBuf

    @Serializable
    class MultiMsgApplyDownReq(
        @ProtoId(1) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(2) val msgType: Int = 0,
        @ProtoId(3) val srcUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    class MultiMsgApplyDownRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(3) val msgKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val uint32DownIp: List<Int>? = null,
        @ProtoId(5) val uint32DownPort: List<Int>? = null,
        @ProtoId(6) val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(7) val msgExternInfo: MultiMsg.ExternMsg? = null,
        @ProtoId(8) val bytesDownIpV6: List<ByteArray>? = null,
        @ProtoId(9) val uint32DownV6Port: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    class MultiMsgApplyUpReq(
        @ProtoId(1) val dstUin: Long = 0L,
        @ProtoId(2) val msgSize: Long = 0L,
        @ProtoId(3) val msgMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val msgType: Int = 0,
        @ProtoId(5) val applyId: Int = 0
    ) : ProtoBuf

    @Serializable
    class MultiMsgApplyUpRsp(
        @ProtoId(1) val result: Int = 0,
        @ProtoId(2) val msgResid: String = "",
        @ProtoId(3) val msgUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(4) val uint32UpIp: List<Int>,
        @ProtoId(5) val uint32UpPort: List<Int>,
        @ProtoId(6) val blockSize: Long = 0L,
        @ProtoId(7) val upOffset: Long = 0L,
        @ProtoId(8) val applyId: Int = 0,
        @ProtoId(9) val msgKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(10) val msgSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoId(11) val msgExternInfo: MultiMsg.ExternMsg? = null,
        @ProtoId(12) val bytesUpIpV6: List<ByteArray>? = null,
        @ProtoId(13) val uint32UpV6Port: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    class ReqBody(
        @ProtoId(1) val subcmd: Int = 0,
        @ProtoId(2) val termType: Int = 0,
        @ProtoId(3) val platformType: Int = 0,
        @ProtoId(4) val netType: Int = 0,
        @ProtoId(5) val buildVer: String = "",
        @ProtoId(6) val multimsgApplyupReq: List<MultiMsg.MultiMsgApplyUpReq>? = null,
        @ProtoId(7) val multimsgApplydownReq: List<MultiMsg.MultiMsgApplyDownReq>? = null,
        @ProtoId(8) val buType: Int = 0,
        @ProtoId(9) val reqChannelType: Int = 0
    ) : ProtoBuf

    @Serializable
    class RspBody(
        @ProtoId(1) val subcmd: Int = 0,
        @ProtoId(2) val multimsgApplyupRsp: List<MultiMsg.MultiMsgApplyUpRsp>? = null,
        @ProtoId(3) val multimsgApplydownRsp: List<MultiMsg.MultiMsgApplyDownRsp>? = null
    ) : ProtoBuf
}
