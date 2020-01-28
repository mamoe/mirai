@file:Suppress("ArrayInDataClass")

package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.network.protocol.protobuf.ProtoBuf

class MessageCommon {

    /**
     * 1 -> varint
     * 2 -> delimi
     * 3 -> varint
     * 4 -> varint
     * 5 -> varint
     * 6 -> varint
     * 7 -> delimi
     * 8 -> delimi
     * 9 -> delimi
     * 10 -> delimi
     * 11 -> delimi
     */
    @Serializable
    data class PluginInfo(
        @SerialId(1) val resId: Int = 0,
        @SerialId(2) val packageName: String = "",
        @SerialId(3) val newVer: Int = 0,
        @SerialId(4) val resType: Int = 0,
        @SerialId(5) val lanType: Int = 0,
        @SerialId(6) val priority: Int = 0,
        @SerialId(7) val resName: String = "",
        @SerialId(8) val resDesc: String = "",
        @SerialId(9) val resUrlBig: String = "",
        @SerialId(10) val resUrlSmall: String = "",
        @SerialId(11) val resConf: String = ""
    ) : ProtoBuf

    @Serializable
    data class AppShareInfo(
        @ProtoType(ProtoNumberType.FIXED) @SerialId(1) val id: Int = 0,
        @SerialId(2) val cookie: ByteArray = EMPTY_BYTE_ARRAY,
        @SerialId(3) val resource: PluginInfo = PluginInfo()
    ) : ProtoBuf

    @Serializable
    data class ContentHead(
        @SerialId(1) val pkgNum: Int = 0,
        @SerialId(2) val pkgIndex: Int = 0,
        @SerialId(3) val divSeq: Int = 0,
        @SerialId(4) val autoReply: Int = 0
    ) : ProtoBuf

    @Serializable
    data class Msg(
        val s: String
    ) : ProtoBuf
}