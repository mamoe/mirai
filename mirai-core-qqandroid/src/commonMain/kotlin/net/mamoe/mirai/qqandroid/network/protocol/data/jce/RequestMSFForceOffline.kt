package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class RequestMSFForceOffline(
    @TarsId(0) @JvmField val uin: Long = 0L,
    @TarsId(1) @JvmField val iSeqno: Long = 0L,
    @TarsId(2) @JvmField val kickType: Byte = 0,
    @TarsId(3) @JvmField val info: String = "",
    @TarsId(4) @JvmField val title: String? = "",
    @TarsId(5) @JvmField val sigKick: Byte? = 0,
    @TarsId(6) @JvmField val vecSigKickData: ByteArray? = null,
    @TarsId(7) @JvmField val sameDevice: Byte? = 0
) : JceStruct


@Serializable
internal class RspMSFForceOffline(
    @TarsId(0) @JvmField val uin: Long,
    @TarsId(1) @JvmField val seq: Long,
    @TarsId(2) @JvmField val const: Byte = 0
) : JceStruct