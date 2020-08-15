/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal

import io.ktor.utils.io.*
import kotlinx.io.core.Input

internal interface ReusableInput {
    val md5: ByteArray
    val size: Long

    fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput>
    suspend fun writeTo(out: ByteWriteChannel): Long

    /**
     * Remember to close.
     */
    fun asInput(): Input
}