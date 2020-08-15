package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField
import kotlin.math.absoluteValue
import kotlin.random.Random

@Serializable
internal class SyncCookie(
    @ProtoId(1) @JvmField val time1: Long? = null, // 1580277992
    @ProtoId(2) @JvmField val time: Long, // 1580277992
    @ProtoId(3) @JvmField val unknown1: Long = Random.nextLong().absoluteValue,// 678328038
    @ProtoId(4) @JvmField val unknown2: Long = Random.nextLong().absoluteValue, // 1687142153
    @ProtoId(5) @JvmField val const1: Long = const1_, // 1458467940
    @ProtoId(11) @JvmField val const2: Long = const2_, // 2683038258
    @ProtoId(12) @JvmField val unknown3: Long = 0x1d,
    @ProtoId(13) @JvmField val lastSyncTime: Long? = null,
    @ProtoId(14) @JvmField val unknown4: Long = 0
) : ProtoBuf

private val const1_: Long = Random.nextLong().absoluteValue
private val const2_: Long = Random.nextLong().absoluteValue
/*

@Serializable
internal class SyncCookie(
    @SerialId(1) @JvmField val time1: Long? = null, // 1580277992
    @SerialId(2) @JvmField val time: Long, // 1580277992
    @SerialId(3) @JvmField val unknown1: Long = 678328038,// 678328038
    @SerialId(4) @JvmField val unknown2: Long = 1687142153, // 1687142153
    @SerialId(5) @JvmField val const1: Long = 1458467940, // 1458467940
    @SerialId(11) @JvmField val const2: Long = 2683038258, // 2683038258
    @SerialId(12) @JvmField val unknown3: Long = 0x1d,
    @SerialId(13) @JvmField val lastSyncTime: Long? = null,
    @SerialId(14) @JvmField val unknown4: Long = 0
) : ProtoBuf
 */