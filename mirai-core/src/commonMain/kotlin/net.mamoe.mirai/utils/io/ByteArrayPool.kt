package net.mamoe.mirai.utils.io

import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool

internal const val DEFAULT_BYTE_ARRAY_POOL_SIZE = 256
internal const val DEFAULT_BYTE_ARRAY_SIZE = 81920

val ByteArrayPool: ObjectPool<ByteArray> = ByteArrayPoolImpl

private object ByteArrayPoolImpl : DefaultPool<ByteArray>(DEFAULT_BYTE_ARRAY_POOL_SIZE) {
    override fun produceInstance(): ByteArray = ByteArray(DEFAULT_BYTE_ARRAY_SIZE)

    override fun clearInstance(instance: ByteArray): ByteArray = instance
}

