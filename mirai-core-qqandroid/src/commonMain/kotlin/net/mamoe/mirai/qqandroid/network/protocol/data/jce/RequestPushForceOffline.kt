package net.mamoe.mirai.qqandroid.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.utils.io.JceStruct
import net.mamoe.mirai.qqandroid.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class RequestPushForceOffline(
    @TarsId(0) @JvmField val uin: Long,
    @TarsId(1) @JvmField val title: String? = "",
    @TarsId(2) @JvmField val tips: String? = "",
    @TarsId(3) @JvmField val sameDevice: Byte? = null
) : JceStruct