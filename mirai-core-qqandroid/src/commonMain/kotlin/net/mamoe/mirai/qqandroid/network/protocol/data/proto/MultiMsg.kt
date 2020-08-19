package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class MultiMsg : ProtoBuf {
    @Serializable
    internal class ExternMsg(
        @ProtoNumber(1) @JvmField val channelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyDownReq(
        @ProtoNumber(1) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(2) @JvmField val msgType: Int = 0,
        @ProtoNumber(3) @JvmField val srcUin: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyDownRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val thumbDownPara: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(3) @JvmField val msgKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val uint32DownIp: List<Int>? = null,
        @ProtoNumber(5) @JvmField val uint32DownPort: List<Int>? = null,
        @ProtoNumber(6) @JvmField val msgResid: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(7) @JvmField val msgExternInfo: ExternMsg? = null,
        @ProtoNumber(8) @JvmField val bytesDownIpV6: List<ByteArray>? = null,
        @ProtoNumber(9) @JvmField val uint32DownV6Port: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyUpReq(
        @ProtoNumber(1) @JvmField val dstUin: Long = 0L,
        @ProtoNumber(2) @JvmField val msgSize: Long = 0L,
        @ProtoNumber(3) @JvmField val msgMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val msgType: Int = 0,
        @ProtoNumber(5) @JvmField val applyId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MultiMsgApplyUpRsp(
        @ProtoNumber(1) @JvmField val result: Int = 0,
        @ProtoNumber(2) @JvmField val msgResid: String = "",
        @ProtoNumber(3) @JvmField val msgUkey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(4) @JvmField val uint32UpIp: List<Int> = listOf(),
        @ProtoNumber(5) @JvmField val uint32UpPort: List<Int> = listOf(),
        @ProtoNumber(6) @JvmField val blockSize: Long = 0L,
        @ProtoNumber(7) @JvmField val upOffset: Long = 0L,
        @ProtoNumber(8) @JvmField val applyId: Int = 0,
        @ProtoNumber(9) @JvmField val msgKey: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(10) @JvmField val msgSig: ByteArray = EMPTY_BYTE_ARRAY,
        @ProtoNumber(11) @JvmField val msgExternInfo: ExternMsg? = null,
        @ProtoNumber(12) @JvmField val bytesUpIpV6: List<ByteArray>? = null,
        @ProtoNumber(13) @JvmField val uint32UpV6Port: List<Int>? = null
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val termType: Int = 0,
        @ProtoNumber(3) @JvmField val platformType: Int = 0,
        @ProtoNumber(4) @JvmField val netType: Int = 0,
        @ProtoNumber(5) @JvmField val buildVer: String = "",
        @ProtoNumber(6) @JvmField val multimsgApplyupReq: List<MultiMsg.MultiMsgApplyUpReq>? = null,
        @ProtoNumber(7) @JvmField val multimsgApplydownReq: List<MultiMsg.MultiMsgApplyDownReq>? = null,
        @ProtoNumber(8) @JvmField val buType: Int = 0,
        @ProtoNumber(9) @JvmField val reqChannelType: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @ProtoNumber(1) @JvmField val subcmd: Int = 0,
        @ProtoNumber(2) @JvmField val multimsgApplyupRsp: List<MultiMsg.MultiMsgApplyUpRsp>? = null,
        @ProtoNumber(3) @JvmField val multimsgApplydownRsp: List<MultiMsg.MultiMsgApplyDownRsp>? = null
    ) : ProtoBuf
}
