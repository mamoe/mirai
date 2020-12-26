/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import kotlinx.io.pool.DefaultPool

/**
 * 缓存 [ByteArray] 实例的 [ObjectPool]
 */
public object ByteArrayPool : DefaultPool<ByteArray>(256) {
    /**
     * 每一个 [ByteArray] 的大小
     */
    public const val BUFFER_SIZE: Int = 8192 * 8

    override fun produceInstance(): ByteArray = ByteArray(BUFFER_SIZE)

    override fun clearInstance(instance: ByteArray): ByteArray = instance

    public fun checkBufferSize(size: Int) {
        require(size <= BUFFER_SIZE) { "sizePerPacket is too large. Maximum buffer size=$BUFFER_SIZE" }
    }

    public fun checkBufferSize(size: Long) {
        require(size <= BUFFER_SIZE) { "sizePerPacket is too large. Maximum buffer size=$BUFFER_SIZE" }
    }

    /**
     * 请求一个大小至少为 [requestedSize] 的 [ByteArray] 实例.
     */ // 不要写为扩展函数. 它需要优先于 kotlinx.io 的扩展函数 resolve
    public inline fun <R> useInstance(requestedSize: Int = 0, block: (ByteArray) -> R): R {
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