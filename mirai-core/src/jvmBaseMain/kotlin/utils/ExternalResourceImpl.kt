/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.sha1
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.Collections

@Suppress("FunctionName")
internal actual fun CombinedExternalResource(vararg resources: ExternalResource): ExternalResource {
    return CombinedExternalResource(resources.toList())
}

/**
 * it is caller's responsibility to guarantee the immutability of the stream.
 */
internal class CombinedExternalResource(
    private val inputs: Collection<ExternalResource>
) : ExternalResource {
    override val isAutoClose: Boolean = true

    override val size: Long = inputs.sumOf { it.size }
    override val md5: ByteArray by lazy { combine().md5() }
    override val sha1: ByteArray by lazy { combine().sha1() }

    override val formatName: String = ""

    private val _closed = CompletableDeferred<Unit>()
    override val closed: Deferred<Unit>
        get() = _closed

    override fun close() {
        _closed.complete(Unit)
    }

    override fun inputStream(): InputStream = combine()

    @MiraiInternalApi
    override fun input(): Input = inputStream().asInput()

    private fun combine(): InputStream {
        return SequenceInputStream(Collections.enumeration(inputs.map { it.inputStream() }))
    }
}