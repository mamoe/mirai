package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class RequestMSFForceOffline(
    @JceId(0) @JvmField val uin: Long = 0L,
    @JceId(1) @JvmField val iSeqno: Long = 0L,
    @JceId(2) @JvmField val kickType: Byte = 0,
    @JceId(3) @JvmField val info: String = "",
    @JceId(4) @JvmField val title: String? = "",
    @JceId(5) @JvmField val sigKick: Byte? = 0,
    @JceId(6) @JvmField val vecSigKickData: ByteArray? = null,
    @JceId(7) @JvmField val sameDevice: Byte? = 0
) : JceStruct


@Serializable
internal class RspMSFForceOffline(
    @JceId(0) @JvmField val uin: Long,
    @JceId(1) @JvmField val seq: Long,
    @JceId(2) @JvmField val const: Byte = 0
) : JceStruct