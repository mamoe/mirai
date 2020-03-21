package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.serialization.jce.JceId

@Serializable
internal class RequestMSFForceOffline(
    @JceId(0) val uin: Long = 0L,
    @JceId(1) val iSeqno: Long = 0L,
    @JceId(2) val kickType: Byte = 0,
    @JceId(3) val info: String = "",
    @JceId(4) val title: String? = "",
    @JceId(5) val sigKick: Byte? = 0,
    @JceId(6) val vecSigKickData: ByteArray? = null,
    @JceId(7) val sameDevice: Byte? = 0
) : JceStruct


@Serializable
internal class RspMSFForceOffline(
    @JceId(0) val uin: Long,
    @JceId(1) val seq: Long,
    @JceId(2) val const: Byte = 0
) : JceStruct