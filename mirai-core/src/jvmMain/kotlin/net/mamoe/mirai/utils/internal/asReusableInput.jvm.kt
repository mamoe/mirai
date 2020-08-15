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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Input
import kotlinx.io.streams.asInput
import net.mamoe.mirai.message.data.toLongUnsigned
import java.io.File
import java.io.InputStream

internal const val DEFAULT_REUSABLE_INPUT_BUFFER_SIZE = 8192

internal actual fun ByteArray.asReusableInput(): ReusableInput {
    return object : ReusableInput {
        override val md5: ByteArray = md5()
        override val size: Long get() = this@asReusableInput.size.toLongUnsigned()

        override fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput> {
            return object : ChunkedFlowSession<ChunkedInput> {
                private val stream = inputStream()
                override val flow: Flow<ChunkedInput> = stream.chunkedFlow(sizePerPacket, ByteArray(DEFAULT_REUSABLE_INPUT_BUFFER_SIZE.coerceAtLeast(sizePerPacket)))

                override fun close() {
                    stream.close()
                    // nothing to do
                }
            }
        }

        override suspend fun writeTo(out: ByteWriteChannel): Long {
            out.writeFully(this@asReusableInput, 0, this@asReusableInput.size)
            out.flush()
            return this@asReusableInput.size.toLongUnsigned()
        }

        override fun asInput(): Input {
            return ByteReadPacket(this@asReusableInput)
        }
    }
}

internal fun File.asReusableInput(deleteOnClose: Boolean): ReusableInput {
    return object : ReusableInput {
        override val md5: ByteArray = inputStream().use { it.md5() }
        override val size: Long get() = length()

        override fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput> {
            val stream = inputStream()
            return object : ChunkedFlowSession<ChunkedInput> {
                override val flow: Flow<ChunkedInput> = stream.chunkedFlow(sizePerPacket, ByteArray(DEFAULT_REUSABLE_INPUT_BUFFER_SIZE.coerceAtLeast(sizePerPacket)))
                override fun close() {
                    stream.close()
                    if (deleteOnClose) this@asReusableInput.delete()
                }
            }
        }

        override suspend fun writeTo(out: ByteWriteChannel): Long {
            return inputStream().use { it.copyTo(out) }
        }

        override fun asInput(): Input {
            return inputStream().asInput()
        }
    }
}

internal fun File.asReusableInput(deleteOnClose: Boolean, md5: ByteArray): ReusableInput {
    return object : ReusableInput {
        override val md5: ByteArray get() = md5
        override val size: Long get() = length()

        override fun chunkedFlow(sizePerPacket: Int): ChunkedFlowSession<ChunkedInput> {
            val stream = inputStream()
            return object : ChunkedFlowSession<ChunkedInput> {
                override val flow: Flow<ChunkedInput> = stream.chunkedFlow(sizePerPacket, ByteArray(DEFAULT_REUSABLE_INPUT_BUFFER_SIZE.coerceAtLeast(sizePerPacket)))
                override fun close() {
                    stream.close()
                    if (deleteOnClose) this@asReusableInput.delete()
                }
            }
        }

        override suspend fun writeTo(out: ByteWriteChannel): Long {
            return inputStream().use { it.copyTo(out) }
        }

        override fun asInput(): Input {
            return inputStream().asInput()
        }
    }
}

private suspend fun InputStream.copyTo(out: ByteWriteChannel): Long = withContext(Dispatchers.IO) {
    var bytesCopied: Long = 0

    ByteArrayPool.useInstance { buffer ->
        var bytes = read(buffer)
        while (bytes >= 0) {
            out.writeFully(buffer, 0, bytes)
            bytesCopied += bytes
            bytes = read(buffer)
        }
    }

    out.flush()

    return@withContext bytesCopied
}