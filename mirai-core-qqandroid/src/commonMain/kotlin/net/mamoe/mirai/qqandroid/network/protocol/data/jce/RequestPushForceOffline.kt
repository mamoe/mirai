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
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.io.JceStruct

@Serializable
internal class RequestPushForceOffline(
    @ProtoId(0) val uin: Long,
    @ProtoId(1) val title: String? = "",
    @ProtoId(2) val tips: String? = "",
    @ProtoId(3) val sameDevice: Byte? = null
) : JceStruct