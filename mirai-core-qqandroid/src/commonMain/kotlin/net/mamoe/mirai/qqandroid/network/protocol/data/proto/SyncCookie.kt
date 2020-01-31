package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf

@Serializable
class SyncCookie(
    @SerialId(2) val time: Long,
    @SerialId(3) val unknown1: Long = 2994099792,
    @SerialId(4) val unknown2: Long = 3497826378,
    @SerialId(5) val const1: Long = 1680172298,
    @SerialId(6) val const2: Long = 2424173273,
    @SerialId(7) val unknown3: Long = 0,
    @SerialId(8) val unknown4: Long = 0
) : ProtoBuf