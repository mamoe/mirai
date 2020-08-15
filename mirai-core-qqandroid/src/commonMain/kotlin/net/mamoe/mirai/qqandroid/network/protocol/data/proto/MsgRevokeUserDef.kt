package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

class MsgRevokeUserDef : ProtoBuf {
    @Serializable
internal class MsgInfoUserDef(
        @ProtoId(1) @JvmField val longMessageFlag: Int = 0,
        @ProtoId(2) @JvmField val longMsgInfo: List<MsgInfoDef>? = null,
        @ProtoId(3) @JvmField val fileUuid: List<String> = listOf()
    ) : ProtoBuf {
        @Serializable
internal class MsgInfoDef(
            @ProtoId(1) @JvmField val msgSeq: Int = 0,
            @ProtoId(2) @JvmField val longMsgId: Int = 0,
            @ProtoId(3) @JvmField val longMsgNum: Int = 0,
            @ProtoId(4) @JvmField val longMsgIndex: Int = 0
        ) : ProtoBuf
    }

    @Serializable
internal class UinTypeUserDef(
        @ProtoId(1) @JvmField val fromUinType: Int = 0,
        @ProtoId(2) @JvmField val fromGroupCode: Long = 0L,
        @ProtoId(3) @JvmField val fileUuid: List<String> = listOf()
    ) : ProtoBuf
}