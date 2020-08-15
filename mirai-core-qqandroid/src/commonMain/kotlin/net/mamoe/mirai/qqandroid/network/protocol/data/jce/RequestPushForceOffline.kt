package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import moe.him188.jcekt.JceId
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import kotlin.jvm.JvmField

@Serializable
internal class RequestPushForceOffline(
    @JceId(0) @JvmField val uin: Long,
    @JceId(1) @JvmField val title: String? = "",
    @JceId(2) @JvmField val tips: String? = "",
    @JceId(3) @JvmField val sameDevice: Byte? = null
) : JceStruct