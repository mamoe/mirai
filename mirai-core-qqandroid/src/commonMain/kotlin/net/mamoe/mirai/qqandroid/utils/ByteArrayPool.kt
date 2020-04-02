package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.pool.DefaultPool

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
}