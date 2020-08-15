/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.Closeable
import kotlinx.io.core.Input
import kotlinx.serialization.InternalSerializationApi
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import java.io.InputStream
import kotlin.jvm.JvmField


@MiraiExperimentalAPI
public interface ChunkedFlowSession<T> : Closeable {
    public val flow: Flow<T>
    override fun close()
}

internal inline fun <T, R> ChunkedFlowSession<T>.map(crossinline mapper: suspend ChunkedFlowSession<T>.(T) -> R): ChunkedFlowSession<R> {
    return object : ChunkedFlowSession<R> {
        override val flow: Flow<R> = this@map.flow.map { this@map.mapper(it) }
        override fun close() = this@map.close()
    }
}


/**
 * 由 [chunkedFlow] 分割得到的区块
 */
@MiraiExperimentalAPI
public class ChunkedInput(
    /**
     * 区块的数据.
     * 由 [ByteArrayPool] 缓存并管理, 只可在 [Flow.collect] 中访问.
     * 它的大小由 [ByteArrayPool.BUFFER_SIZE] 决定, 而有效（有数据）的大小由 [bufferSize] 决定.
     *
     * **注意**: 不要将他带出 [Flow.collect] 作用域, 否则将造成内存泄露
     */
    @JvmField public val buffer: ByteArray,
    @JvmField internal var size: Int
) {
    /**
     * [buffer] 的有效大小
     */
    public val bufferSize: Int get() = size
}

/**
 * 创建将 [ByteReadPacket] 以固定大小分割的 [Sequence].
 *
 * 对于一个 1000 长度的 [ByteReadPacket] 和参数 [sizePerPacket] = 300, 将会产生含四个元素的 [Sequence],
 * 其长度分别为: 300, 300, 300, 100.
 *
 * 若 [ByteReadPacket.remaining] 小于 [sizePerPacket], 将会返回唯一元素 [this] 的 [Sequence]
 */
internal fun ByteReadPacket.chunkedFlow(sizePerPacket: Int, buffer: ByteArray): Flow<ChunkedInput> {
    ByteArrayPool.checkBufferSize(sizePerPacket)
    if (this.remaining <= sizePerPacket.toLong()) {
        return flowOf(
            ChunkedInput(
                buffer,
                this.readAvailable(buffer, 0, sizePerPacket)
            )
        )
    }
    return flow {
        val chunkedInput = ChunkedInput(buffer, 0)
        do {
            chunkedInput.size = this@chunkedFlow.readAvailable(buffer, 0, sizePerPacket)
            emit(chunkedInput)
        } while (this@chunkedFlow.isNotEmpty)
    }
}

/**
 * 创建将 [ByteReadChannel] 以固定大小分割的 [Sequence].
 *
 * 对于一个 1000 长度的 [ByteReadChannel] 和参数 [sizePerPacket] = 300, 将会产生含四个元素的 [Sequence],
 * 其长度分别为: 300, 300, 300, 100.
 */
internal fun ByteReadChannel.chunkedFlow(sizePerPacket: Int, buffer: ByteArray): Flow<ChunkedInput> {
    ByteArrayPool.checkBufferSize(sizePerPacket)
    if (this.isClosedForRead) {
        return flowOf()
    }
    return flow {
        val chunkedInput = ChunkedInput(buffer, 0)
        do {
            chunkedInput.size = this@chunkedFlow.readAvailable(buffer, 0, sizePerPacket)
            emit(chunkedInput)
        } while (!this@chunkedFlow.isClosedForRead)
    }
}


/**
 * 创建将 [Input] 以固定大小分割的 [Sequence].
 *
 * 对于一个 1000 长度的 [Input] 和参数 [sizePerPacket] = 300, 将会产生含四个元素的 [Sequence],
 * 其长度分别为: 300, 300, 300, 100.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal fun Input.chunkedFlow(sizePerPacket: Int, buffer: ByteArray): Flow<ChunkedInput> {
    ByteArrayPool.checkBufferSize(sizePerPacket)

    if (this.endOfInput) {
        return flowOf()
    }

    return flow {
        val chunkedInput = ChunkedInput(buffer, 0)
        while (!this@chunkedFlow.endOfInput) {
            chunkedInput.size = this@chunkedFlow.readAvailable(buffer, 0, sizePerPacket)
            emit(chunkedInput)
        }
    }
}

/**
 * 创建将 [ByteReadPacket] 以固定大小分割的 [Sequence].
 *
 * 对于一个 1000 长度的 [ByteReadPacket] 和参数 [sizePerPacket] = 300, 将会产生含四个元素的 [Sequence],
 * 其长度分别为: 300, 300, 300, 100.
 *
 * 若 [ByteReadPacket.remaining] 小于 [sizePerPacket], 将会返回唯一元素 [this] 的 [Sequence]
 */
@OptIn(ExperimentalCoroutinesApi::class, InternalSerializationApi::class)
internal fun InputStream.chunkedFlow(sizePerPacket: Int, buffer: ByteArray): Flow<ChunkedInput> {
    require(sizePerPacket <= buffer.size) { "sizePerPacket is too large. Maximum buffer size=buffer.size=${buffer.size}" }

    return flow {
        val chunkedInput = ChunkedInput(buffer, 0)
        while (this@chunkedFlow.available() != 0) {
            chunkedInput.size = this@chunkedFlow.read(buffer, 0, sizePerPacket)
            emit(chunkedInput)
        }
    }
}