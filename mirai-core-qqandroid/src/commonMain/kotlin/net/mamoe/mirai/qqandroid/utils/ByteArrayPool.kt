package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool

/**
 * 缓存 [ByteArray] 实例的 [ObjectPool]
 */
internal object ByteArrayPool : DefaultPool<ByteArray>(256) {
    /**
     * 每一个 [ByteArray] 的大小
     */
    const val BUFFER_SIZE: Int = 81920 / 2

    override fun produceInstance(): ByteArray = ByteArray(BUFFER_SIZE)

    override fun clearInstance(instance: ByteArray): ByteArray = instance

    fun checkBufferSize(size: Int) {
        require(size <= BUFFER_SIZE) { "sizePerPacket is too large. Maximum buffer size=$BUFFER_SIZE" }
    }

    fun checkBufferSize(size: Long) {
        require(size <= BUFFER_SIZE) { "sizePerPacket is too large. Maximum buffer size=$BUFFER_SIZE" }
    }

    /**
     * 请求一个大小至少为 [requestedSize] 的 [ByteArray] 实例.
     */ // 不要写为扩展函数. 它需要优先于 kotlinx.io 的扩展函数 resolve
    inline fun <R> useInstance(requestedSize: Int = 0, block: (ByteArray) -> R): R {
        if (requestedSize > BUFFER_SIZE) {
            return ByteArray(requestedSize).run(block)
        }
        val instance = borrow()
        try {
            return block(instance)
        } finally {
            recycle(instance)
        }
    }
}