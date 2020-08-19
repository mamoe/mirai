package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class MsgRevokeUserDef : ProtoBuf {
    @Serializable
    internal class MsgInfoUserDef(
        @ProtoNumber(1) @JvmField val longMessageFlag: Int = 0,
        @ProtoNumber(2) @JvmField val longMsgInfo: List<MsgInfoDef>? = null,
        @ProtoNumber(3) @JvmField val fileUuid: List<String> = listOf()
    ) : ProtoBuf {
        @Serializable
        internal class MsgInfoDef(
            @ProtoNumber(1) @JvmField val msgSeq: Int = 0,
            @ProtoNumber(2) @JvmField val longMsgId: Int = 0,
            @ProtoNumber(3) @JvmField val longMsgNum: Int = 0,
            @ProtoNumber(4) @JvmField val longMsgIndex: Int = 0
        ) : ProtoBuf
    }

    @Serializable
internal class UinTypeUserDef(
        @ProtoNumber(1) @JvmField val fromUinType: Int = 0,
        @ProtoNumber(2) @JvmField val fromGroupCode: Long = 0L,
        @ProtoNumber(3) @JvmField val fileUuid: List<String> = listOf()
    ) : ProtoBuf
}