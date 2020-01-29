package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class RequestPushForceOffline(
    @SerialId(0) val uin: Long,
    @SerialId(1) val title: String? = "",
    @SerialId(2) val tips: String? = "",
    @SerialId(3) val sameDevice: Byte? = null
) : JceStruct