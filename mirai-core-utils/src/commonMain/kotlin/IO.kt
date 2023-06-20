/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public val EMPTY_BYTE_ARRAY: ByteArray = ByteArray(0)

public val DECRYPTER_16_ZERO: ByteArray = ByteArray(16)
public val KEY_16_ZEROS: ByteArray = ByteArray(16)

/**
 * It's caller's responsibility to close the input
 */
public inline fun <R> ByteReadPacket.useBytes(
    n: Int = remaining.toIntOrFail(),
    block: (data: ByteArray, length: Int) -> R
): R = ByteArrayPool.useInstance(n) {
    this.readFully(it, 0, n)
    block(it, n)
}


/**
 * It's caller's responsibility to close the input
 */
public inline fun <R> Input.useBytes(
    n: Int? = null,
    block: (data: ByteArray, length: Int) -> R
): R {
    return when {
        n != null -> {
            this.readBytes(n).let { block(it, it.size) }
        }

        this is ByteReadPacket -> {
            val count = this.remaining.toIntOrFail()
            ByteArrayPool.useInstance(count) {
                this.readFully(it, 0, count)
                block(it, count)
            }
        }

        else -> {
            this.readBytes().let { block(it, it.size) }
        }
    }
}

public fun Long.toIntOrFail(): Int {
    if (this >= Int.MAX_VALUE || this <= Int.MIN_VALUE) {
        throw IllegalArgumentException("$this does not fit in Int range")
    }
    return this.toInt()
}

public fun ByteReadPacket.readPacketExact(
    n: Int = remaining.toIntOrFail()
): ByteReadPacket = this.readBytes(n).toReadPacket()


public fun Input.readAllText(): String = Charsets.UTF_8.newDecoder().decode(this)

public fun Input.readString(length: Int, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length), charset = charset) // stdlib

public fun Input.readString(length: Long, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

public fun Input.readString(length: Short, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

@JvmSynthetic
public fun Input.readString(length: UShort, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

public fun Input.readString(length: Byte, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

public fun Input.readUShortLVString(): String = String(this.readUShortLVByteArray())
public fun Input.readUShortLVByteArray(): ByteArray = this.readBytes(this.readShort().toUShort().toInt())

public suspend fun Input.copyTo(output: ByteWriteChannel): Long {
    val buffer = ChunkBuffer.Pool.borrow()
    var copied = 0L

    try {
        do {
            buffer.resetForWrite()
            val rc = readAvailable(buffer)
            if (rc == -1) break
            copied += rc
            output.writeFully(buffer)
        } while (true)

        return copied
    } finally {
        buffer.release(ChunkBuffer.Pool)
    }
}
