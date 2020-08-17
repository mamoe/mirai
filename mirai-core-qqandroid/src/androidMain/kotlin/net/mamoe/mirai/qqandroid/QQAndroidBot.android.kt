/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.consumeEachBufferRange
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.io.*
import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.qqandroid.utils.ByteArrayPool
import net.mamoe.mirai.qqandroid.utils.toReadPacket
import java.nio.ByteBuffer

@Suppress("DEPRECATION")
internal actual fun ByteReadChannel.toKotlinByteReadChannel(): kotlinx.coroutines.io.ByteReadChannel {
    return object : kotlinx.coroutines.io.ByteReadChannel {
        override val availableForRead: Int
            get() = this@toKotlinByteReadChannel.availableForRead
        override val isClosedForRead: Boolean
            get() = this@toKotlinByteReadChannel.isClosedForRead
        override val isClosedForWrite: Boolean
            get() = this@toKotlinByteReadChannel.isClosedForWrite

        @Suppress("DEPRECATION_ERROR", "OverridingDeprecatedMember")
        override var readByteOrder: ByteOrder
            get() = when (this@toKotlinByteReadChannel.readByteOrder) {
                io.ktor.utils.io.core.ByteOrder.BIG_ENDIAN -> ByteOrder.BIG_ENDIAN
                io.ktor.utils.io.core.ByteOrder.LITTLE_ENDIAN -> ByteOrder.LITTLE_ENDIAN
            }
            set(value) {
                this@toKotlinByteReadChannel.readByteOrder = when (value) {
                    ByteOrder.BIG_ENDIAN -> io.ktor.utils.io.core.ByteOrder.BIG_ENDIAN
                    ByteOrder.LITTLE_ENDIAN -> io.ktor.utils.io.core.ByteOrder.LITTLE_ENDIAN
                }
            }

        @Suppress("DEPRECATION_ERROR", "DEPRECATION", "OverridingDeprecatedMember")
        override val totalBytesRead: Long
            get() = this@toKotlinByteReadChannel.totalBytesRead

        override fun cancel(cause: Throwable?): Boolean = this@toKotlinByteReadChannel.cancel(cause)
        override suspend fun consumeEachBufferRange(visitor: ConsumeEachBufferVisitor) =
            this@toKotlinByteReadChannel.consumeEachBufferRange(visitor)

        override suspend fun discard(max: Long): Long = this@toKotlinByteReadChannel.discard(max)

        @Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
        @ExperimentalIoApi
        override fun <R> lookAhead(visitor: LookAheadSession.() -> R): R {
            return this@toKotlinByteReadChannel.lookAhead l@{
                visitor(object : LookAheadSession {
                    override fun consumed(n: Int) {
                        return this@l.consumed(n)
                    }

                    override fun request(skip: Int, atLeast: Int): ByteBuffer? {
                        return this@l.request(skip, atLeast)
                    }
                })
            }
        }

        @Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
        @ExperimentalIoApi
        override suspend fun <R> lookAheadSuspend(visitor: suspend LookAheadSuspendSession.() -> R): R =
            this@toKotlinByteReadChannel.lookAheadSuspend l@{
                visitor(object : LookAheadSuspendSession {
                    override suspend fun awaitAtLeast(n: Int): Boolean {
                        return this@l.awaitAtLeast(n)
                    }

                    override fun consumed(n: Int) {
                        return this@l.consumed(n)
                    }

                    override fun request(skip: Int, atLeast: Int): ByteBuffer? {
                        return this@l.request(skip, atLeast)
                    }

                })
            }

        override suspend fun read(min: Int, consumer: (ByteBuffer) -> Unit) =
            this@toKotlinByteReadChannel.read(min, consumer)

        override suspend fun readAvailable(dst: ByteBuffer): Int = this@toKotlinByteReadChannel.readAvailable(dst)
        override suspend fun readAvailable(dst: ByteArray, offset: Int, length: Int): Int =
            this@toKotlinByteReadChannel.readAvailable(dst, offset, length)

        override suspend fun readAvailable(dst: IoBuffer): Int {
            ByteArrayPool.useInstance {
                val read = this@toKotlinByteReadChannel.readAvailable(it, 0, it.size)
                dst.writeFully(it, 0, read)
                return read
            }
        }

        override suspend fun readBoolean(): Boolean = this@toKotlinByteReadChannel.readBoolean()
        override suspend fun readByte(): Byte = this@toKotlinByteReadChannel.readByte()
        override suspend fun readDouble(): Double = this@toKotlinByteReadChannel.readDouble()
        override suspend fun readFloat(): Float = this@toKotlinByteReadChannel.readFloat()
        override suspend fun readFully(dst: ByteBuffer): Int {
            TODO("not implemented")
        }

        override suspend fun readFully(dst: ByteArray, offset: Int, length: Int) =
            this@toKotlinByteReadChannel.readFully(dst, offset, length)

        override suspend fun readFully(dst: IoBuffer, n: Int) {
            ByteArrayPool.useInstance {
                dst.writeFully(it, 0, this.readAvailable(it, 0, it.size))
            }
        }

        override suspend fun readInt(): Int = this@toKotlinByteReadChannel.readInt()
        override suspend fun readLong(): Long = this@toKotlinByteReadChannel.readLong()
        override suspend fun readPacket(size: Int, headerSizeHint: Int): ByteReadPacket {
            return this@toKotlinByteReadChannel.readPacket(size, headerSizeHint).readBytes().toReadPacket()
        }

        override suspend fun readRemaining(limit: Long, headerSizeHint: Int): ByteReadPacket {
            return this@toKotlinByteReadChannel.readRemaining(limit, headerSizeHint).readBytes().toReadPacket()
        }

        @OptIn(ExperimentalIoApi::class)
        @ExperimentalIoApi
        override fun readSession(consumer: ReadSession.() -> Unit) {
            @Suppress("DEPRECATION")
            this@toKotlinByteReadChannel.readSession lambda@{
                consumer(object : ReadSession {
                    override val availableForRead: Int
                        get() = this@lambda.availableForRead

                    override fun discard(n: Int): Int = this@lambda.discard(n)

                    override fun request(atLeast: Int): IoBuffer? {
                        val ioBuffer: io.ktor.utils.io.core.IoBuffer = this@lambda.request(atLeast) ?: return null
                        val buffer = IoBuffer.Pool.borrow()
                        val bytes = (ioBuffer as Input).readBytes()
                        buffer.writeFully(bytes)
                        return buffer
                    }
                })
            }
        }

        override suspend fun readShort(): Short = this@toKotlinByteReadChannel.readShort()

        @Suppress("EXPERIMENTAL_OVERRIDE", "EXPERIMENTAL_API_USAGE")
        @ExperimentalIoApi
        override suspend fun readSuspendableSession(consumer: suspend SuspendableReadSession.() -> Unit) =
            this@toKotlinByteReadChannel.readSuspendableSession l@{
                consumer(object : SuspendableReadSession {
                    override val availableForRead: Int
                        get() = this@l.availableForRead

                    override suspend fun await(atLeast: Int): Boolean = this@l.await(atLeast)
                    override fun discard(n: Int): Int = this@l.discard(n)
                    override fun request(atLeast: Int): IoBuffer? {
                        @Suppress("DuplicatedCode") val ioBuffer: io.ktor.utils.io.core.IoBuffer =
                            this@l.request(atLeast) ?: return null
                        val buffer = IoBuffer.Pool.borrow()
                        val bytes = (ioBuffer as Input).readBytes()
                        buffer.writeFully(bytes)
                        return buffer
                    }
                })
            }

        override suspend fun readUTF8Line(limit: Int): String? = this@toKotlinByteReadChannel.readUTF8Line(limit)
        override suspend fun <A : Appendable> readUTF8LineTo(out: A, limit: Int): Boolean =
            this@toKotlinByteReadChannel.readUTF8LineTo(out, limit)
    }
}