/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public val EMPTY_BYTE_ARRAY: ByteArray = ByteArray(0)

public val DECRYPTER_16_ZERO: ByteArray = ByteArray(16)
public val KEY_16_ZEROS: ByteArray = ByteArray(16)

public inline fun <C : Closeable, R> C.withUse(block: C.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use(block)
}

public inline fun <I : Closeable, O : Closeable, R> I.withOut(output: O, block: I.(output: O) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use { output.use { block(this, output) } }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public inline fun <R> ByteReadPacket.useBytes(
    n: Int = remaining.toInt(),//not that safe but adequate
    block: (data: ByteArray, length: Int) -> R
): R = ByteArrayPool.useInstance(n) {
    this.readFully(it, 0, n)
    block(it, n)
}

public inline fun ByteReadPacket.readPacketExact(
    n: Int = remaining.toInt()//not that safe but adequate
): ByteReadPacket = this.readBytes(n).toReadPacket()


public typealias TlvMap = MutableMap<Int, ByteArray>

public inline fun TlvMap.getOrFail(tag: Int): ByteArray {
    return this[tag] ?: error("cannot find tlv 0x${tag.toUHexString("")}($tag)")
}

public inline fun TlvMap.getOrFail(tag: Int, lazyMessage: (tag: Int) -> String): ByteArray {
    return this[tag] ?: error(lazyMessage(tag))
}

@Suppress("FunctionName")
public inline fun Input._readTLVMap(tagSize: Int = 2, suppressDuplication: Boolean = true): TlvMap =
    _readTLVMap(true, tagSize, suppressDuplication)

@Suppress("DuplicatedCode", "FunctionName")
public fun Input._readTLVMap(
    expectingEOF: Boolean = true,
    tagSize: Int,
    suppressDuplication: Boolean = true
): TlvMap {
    val map = mutableMapOf<Int, ByteArray>()
    var key = 0

    while (kotlin.run {
            try {
                key = when (tagSize) {
                    1 -> readUByte().toInt()
                    2 -> readUShort().toInt()
                    4 -> readUInt().toInt()
                    else -> error("Unsupported tag size: $tagSize")
                }
            } catch (e: Exception) { // java.nio.BufferUnderflowException is not a EOFException...
                if (expectingEOF) {
                    return map
                }
                throw e
            }
            key
        }.toUByte() != UByte.MAX_VALUE) {

        if (map.containsKey(key)) {
            @Suppress("ControlFlowWithEmptyBody")
            if (!suppressDuplication) {
                /*
                @Suppress("DEPRECATION")
                MiraiLogger.error(
                    @Suppress("IMPLICIT_CAST_TO_ANY")
                    """
                Error readTLVMap:
                duplicated key ${when (tagSize) {
                        1 -> key.toByte()
                        2 -> key.toShort()
                        4 -> key
                        else -> error("unreachable")
                    }.contentToString()}
                map=${map.contentToString()}
                duplicating value=${this.readUShortLVByteArray().toUHexString()}
                """.trimIndent()
                )*/
            } else {
                this.discardExact(this.readShort().toInt() and 0xffff)
            }
        } else {
            try {
                map[key] = this.readBytes(readUShort().toInt())
            } catch (e: Exception) { // BufferUnderflowException, java.io.EOFException
                // if (expectingEOF) {
                //     return map
                // }
                throw e
            }
        }
    }
    return map
}

public fun Input.readAllText(): String = Charsets.UTF_8.newDecoder().decode(this)

public inline fun Input.readString(length: Int, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length), charset = charset) // stdlib

public inline fun Input.readString(length: Long, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

public inline fun Input.readString(length: Short, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

@JvmSynthetic
public inline fun Input.readString(length: UShort, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

public inline fun Input.readString(length: Byte, charset: Charset = Charsets.UTF_8): String =
    String(this.readBytes(length.toInt()), charset = charset)

public fun Input.readUShortLVString(): String = String(this.readUShortLVByteArray())
public fun Input.readUShortLVByteArray(): ByteArray = this.readBytes(this.readUShort().toInt())

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
