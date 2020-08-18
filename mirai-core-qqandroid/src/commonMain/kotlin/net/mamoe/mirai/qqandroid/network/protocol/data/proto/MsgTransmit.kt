package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class MsgTransmit : ProtoBuf {
    @Serializable
internal class PbMultiMsgItem(
        @ProtoNumber(1) @JvmField val fileName: String = "",
        @ProtoNumber(2) @JvmField val buffer: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
internal class PbMultiMsgNew(
        @ProtoNumber(1) @JvmField val msg: List<MsgComm.Msg>? = null
    ) : ProtoBuf

    @Serializable
internal class PbMultiMsgTransmit(
        @ProtoNumber(1) @JvmField val msg: List<MsgComm.Msg>? = null,
        @ProtoNumber(2) @JvmField val pbItemList: List<MsgTransmit.PbMultiMsgItem>? = null
    ) : ProtoBuf
}