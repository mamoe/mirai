package net.mamoe.mirai.utils

import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool

internal const val DEFAULT_BUFFER_SIZE = 4098
internal const val DEFAULT_BYTE_ARRAY_POOL_SIZE = 2048

/**
 * The default ktor byte buffer pool
 */
val ByteArrayPool: ObjectPool<ByteArray> = ByteBufferPool()

class ByteBufferPool : DefaultPool<ByteArray>(DEFAULT_BYTE_ARRAY_POOL_SIZE) {
    override fun produceInstance(): ByteArray = ByteArray(DEFAULT_BUFFER_SIZE)

    override fun clearInstance(instance: ByteArray): ByteArray = instance.apply { map { 0 } }
}
