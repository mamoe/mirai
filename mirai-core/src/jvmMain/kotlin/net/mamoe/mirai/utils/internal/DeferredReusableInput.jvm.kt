/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal

import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.Input
import net.mamoe.mirai.utils.FileCacheStrategy
import java.awt.image.BufferedImage
import java.io.InputStream
import java.net.URL

internal actual class DeferredReusableInput actual constructor(
    val input: Any,
    val extraArg: Any?
) : ReusableInput {



    actual suspend fun init(strategy: FileCacheStrategy) = withContext(Dispatchers.IO) {
        if (delegate != null) {
            return@withContext
        }
        delegate = when (input) {
            is InputStream -> strategy.newImageCache(input)
            is ByteArray -> strategy.newImageCache(input)
            is Input -> strategy.newImageCache(input)
            is URL -> strategy.newImageCache(input)
            is BufferedImage -> strategy.newImageCache(input, extraArg as String)
            else -> error("Internal error: unsupported DeferredReusableInput.input: ${input::class.qualifiedName}")
        }.input
    }

    private var delegate: ReusableInput? = null

    override val md5: ByteArray
        get() = delegate?.md5 ?: error("DeferredReusableInput not yet initialized")
    override val size: Long
        get() = delegate?.size ?: error("DeferredReusableInput not yet initialized")

    override fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput> {
        return delegate?.chunkedFlow(sizePerPacket) ?: error("DeferredReusableInput not yet initialized")
    }

    override suspend fun writeTo(out: ByteWriteChannel): Long {
        return delegate?.writeTo(out) ?: error("DeferredReusableInput not yet initialized")
    }

    override fun asInput(): Input {
        return delegate?.asInput() ?: error("DeferredReusableInput not yet initialized")
    }

    actual val initialized: Boolean get() = delegate != null
}