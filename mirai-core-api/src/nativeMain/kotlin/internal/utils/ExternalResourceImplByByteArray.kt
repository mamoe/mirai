/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.utils.*

internal class ExternalResourceImplByByteArray(
    private val data: ByteArray,
    formatName: String?
) : ExternalResource {
    override val size: Long = data.size.toLong()
    override val md5: ByteArray by lazy { data.md5() }
    override val sha1: ByteArray by lazy { data.sha1() }
    override val formatName: String by lazy {
        formatName ?: getFileType(data.copyOf(COUNT_BYTES_USED_FOR_DETECTING_FILE_TYPE))
        ?: ExternalResource.DEFAULT_FORMAT_NAME
    }
    override val closed: CompletableDeferred<Unit> = CompletableDeferred()
    override val origin: Any
        get() = data//.clone()

    override fun input(): Input = ByteReadPacket(data)

    override fun close() {
        kotlin.runCatching { closed.complete(Unit) }
    }
}
