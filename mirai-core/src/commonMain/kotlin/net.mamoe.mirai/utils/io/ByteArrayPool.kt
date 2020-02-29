/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.utils.io

import io.ktor.utils.io.pool.DefaultPool
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 缓存 [ByteArray] 实例的 [ObjectPool]
 *
 * **注意**: 这是 Mirai 内部 API. 它可能在任何时刻被改动. 不要将它用于生产环境
 */
@MiraiInternalAPI
object ByteArrayPool : DefaultPool<ByteArray>(256) {
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

