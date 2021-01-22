/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.highway

import kotlinx.io.core.Closeable
import net.mamoe.mirai.utils.runBIO
import net.mamoe.mirai.utils.withUse
import java.io.InputStream

internal class ChunkedFlowSession<T>(
    private val input: InputStream,
    private val buffer: ByteArray,
    private val mapper: (buffer: ByteArray, size: Int, offset: Long) -> T
) : Closeable {
    override fun close() {
        input.close()
    }

    private var offset = 0L

    @Suppress("BlockingMethodInNonBlockingContext")
    internal suspend inline fun useAll(crossinline block: suspend (T) -> Unit) = withUse {
        runBIO {
            while (true) {
                val size = input.read(buffer)
                if (size == -1) return@runBIO
                block(mapper(buffer, size, offset))
                offset += size
            }
        }
    }
}