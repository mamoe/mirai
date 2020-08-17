/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils.internal

import kotlinx.io.pool.DefaultPool
import kotlinx.io.pool.ObjectPool
import java.io.InputStream

internal expect fun InputStream.md5(): ByteArray
internal expect fun ByteArray.md5(offset: Int = 0, length: Int = this.size - offset): ByteArray

@Suppress("DuplicatedCode") // false positive. `this` is not the same for `List<Byte>` and `ByteArray`
internal fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}


internal inline fun InputStream.readInSequence(block: (ByteArray, len: Int) -> Unit) {
    var read: Int
    ByteArrayPool.useInstance { buf ->
        while (this.read(buf).also { read = it } != -1) {
            block(buf, read)
        }
    }
}


/**
 * 缓存 [ByteArray] 实例的 [ObjectPool]
 */
internal object ByteArrayPool : DefaultPool<ByteArray>(256) {
    /**
     * 每一个 [ByteArray] 的大小
     */
    const val BUFFER_SIZE: Int = 8192 * 8

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