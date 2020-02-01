package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf

@Serializable
class SyncCookie(
    @SerialId(1) val time1: Long? = null, // 1580277992
    @SerialId(2) val time: Long, // 1580277992
    @SerialId(3) val unknown1: Long = 678328038,// 678328038
    @SerialId(4) val unknown2: Long = 1687142153, // 1687142153
    @SerialId(5) val const1: Long = 1458467940, // 1458467940
    @SerialId(11) val const2: Long = 2683038258, // 2683038258
    @SerialId(12) val unknown3: Long = 0x1d,
    @SerialId(13) val lastSyncTime: Long? = null,
    @SerialId(14) val unknown4: Long = 0
) : ProtoBuf