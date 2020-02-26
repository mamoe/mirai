/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.io

import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool

internal const val DEFAULT_BYTE_ARRAY_POOL_SIZE = 256
internal const val DEFAULT_BYTE_ARRAY_SIZE = 81920 / 2

val ByteArrayPool: ObjectPool<ByteArray> = ByteArrayPoolImpl

private object ByteArrayPoolImpl : DefaultPool<ByteArray>(DEFAULT_BYTE_ARRAY_POOL_SIZE) {
    override fun produceInstance(): ByteArray = ByteArray(DEFAULT_BYTE_ARRAY_SIZE)

    override fun clearInstance(instance: ByteArray): ByteArray = instance
}

