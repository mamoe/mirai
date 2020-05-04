/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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