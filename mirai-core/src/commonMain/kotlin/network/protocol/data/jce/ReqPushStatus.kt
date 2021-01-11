/*
 * Copyright 2020-2021 Mamoe Technologies and contributors.
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
internal class RequestPushStatus(
    @JvmField @TarsId(0) val uin: Long,
    @JvmField @TarsId(1) val status: Byte,
    @JvmField @TarsId(2) val dataLine: Byte? = null,
    @JvmField @TarsId(3) val printable: Byte? = null,
    @JvmField @TarsId(4) val viewFile: Byte? = null,
    @JvmField @TarsId(5) val nPCVer: Long? = null,
    @JvmField @TarsId(6) val nClientType: Long? = null,
    @JvmField @TarsId(7) val nInstanceId: Long? = null,
    @JvmField @TarsId(8) val vecInstanceList: List<InstanceInfo>? = null
) : JceStruct


