/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId
import kotlin.jvm.JvmField

@Serializable
internal class SvcRspGetDevLoginInfo(
    @JvmField @TarsId(0) val iResult: Int,
    @JvmField @TarsId(1) val result: String? = "",
    @JvmField @TarsId(2) val iNextItemIndex: Long,
    @JvmField @TarsId(3) val iTotalItemCount: Long,
    @JvmField @TarsId(4) val vecCurrentLoginDevInfo: List<SvcDevLoginInfo>? = null,
    @JvmField @TarsId(5) val vecHistoryLoginDevInfo: List<SvcDevLoginInfo>? = null,
    @JvmField @TarsId(6) val vecAuthLoginDevInfo: List<SvcDevLoginInfo>? = null,
) : JceStruct

