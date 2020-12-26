/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId

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