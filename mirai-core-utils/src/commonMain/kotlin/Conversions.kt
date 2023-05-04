/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "unused")

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/*
 * 类型转换 Utils.
 * 这些函数为内部函数, 可能会改变
 */

/**
 * Converts a Short to its hex representation in network order (big-endian).
 */
public fun Short.toByteArray(): ByteArray = with(toInt()) {
    byteArrayOf(
        (shr(8) and 0xFF).toByte(),
        (shr(0) and 0xFF).toByte()
    )
}

/**
 * Converts an Int to its hex representation in network order (big-endian).
 */
public fun Int.toByteArray(): ByteArray = byteArrayOf(
    ushr(24).toByte(),
    ushr(16).toByte(),
    ushr(8).toByte(),
    ushr(0).toByte()
)

/**
 * Converts a Long to its hex representation in network order (big-endian).
 */
public fun Long.toByteArray(): ByteArray = byteArrayOf(
    (ushr(56) and 0xFF).toByte(),
    (ushr(48) and 0xFF).toByte(),
    (ushr(40) and 0xFF).toByte(),
    (ushr(32) and 0xFF).toByte(),
    (ushr(24) and 0xFF).toByte(),
    (ushr(16) and 0xFF).toByte(),
    (ushr(8) and 0xFF).toByte(),
    (ushr(0) and 0xFF).toByte()
)

/**
 * Converts an Int to its hex representation in network order (big-endian).
 */
public fun Int.toUHexString(separator: String = " "): String = this.toByteArray().toUHexString(separator)

/**
 * Converts an UShort to its hex representation in network order (big-endian).
 */
public fun UShort.toByteArray(): ByteArray = with(toUInt()) {
    byteArrayOf(
        (shr(8) and 255u).toByte(),
        (shr(0) and 255u).toByte()
    )
}

/**
 * Converts a Short to its hex representation in network order (big-endian).
 */
public fun Short.toUHexString(separator: String = " "): String = this.toUShort().toUHexString(separator)

/**
 * Converts an UShort to its hex representation in network order (big-endian).
 */
public fun UShort.toUHexString(separator: String = " "): String =
    this.toInt().shr(8).toUShort().toUByte().toUHexString() + separator + this.toUByte().toUHexString()

/**
 * Converts an ULong to its hex representation in network order (big-endian).
 */
public fun ULong.toUHexString(separator: String = " "): String =
    this.toLong().toUHexString(separator)

/**
 * Converts a Long to its hex representation in network order (big-endian).
 */
public fun Long.toUHexString(separator: String = " "): String =
    this.ushr(32).toUInt().toUHexString(separator) + separator + this.toUInt().toUHexString(separator)


/**
 * Converts an UByte to its hex representation.
 */
public fun UByte.toUHexString(): String = this.toByte().toUHexString()

/**
 * 255u -> 00 00 00 FF
 */
public fun UInt.toByteArray(): ByteArray = byteArrayOf(
    (shr(24) and 255u).toByte(),
    (shr(16) and 255u).toByte(),
    (shr(8) and 255u).toByte(),
    (shr(0) and 255u).toByte()
)

/**
 * Converts an UInt to its hex representation in network order (big-endian).
 */
public fun UInt.toUHexString(separator: String = " "): String = this.toByteArray().toUHexString(separator)

/**
 * 转无符号十六进制表示, 并补充首位 `0`.
 * 转换结果示例: `FF`, `0E`
 */
public fun Byte.toUHexString(): String = this.toUByte().fixToUHex()

/**
 * 转无符号十六进制表示, 并补充首位 `0`.
 */
public fun Byte.fixToUHex(): String = this.toUByte().fixToUHex()

/**
 * 转无符号十六进制表示, 并补充首位 `0`.
 */
public fun UByte.fixToUHex(): String =
    if (this.toInt() in 0..15) "0${this.toString(16).uppercase()}" else this.toString(16).uppercase()

/**
 * Converts 4 bytes to an UInt in network order (big-endian).
 */
public fun ByteArray.toUInt(): UInt =
    (this[0].toUInt().and(255u) shl 24)
        .plus(this[1].toUInt().and(255u) shl 16)
        .plus(this[2].toUInt().and(255u) shl 8)
        .plus(this[3].toUInt().and(255u) shl 0)

/**
 * Converts 2 bytes to an UShort in network order (big-endian).
 */
public fun ByteArray.toUShort(): UShort =
    ((this[0].toUInt().and(255u) shl 8) + (this[1].toUInt().and(255u) shl 0)).toUShort()

/**
 * Converts 4 bytes to an Int in network order (big-endian).
 */
public fun ByteArray.toInt(offset: Int = 0): Int =
    this[offset + 0].toInt().and(255).shl(24)
        .plus(this[offset + 1].toInt().and(255).shl(16))
        .plus(this[offset + 2].toInt().and(255).shl(8))
        .plus(this[offset + 3].toInt().and(255).shl(0))

/**
 * Converts 8 bytes to an Long in network order (big-endian).
 */
public fun ByteArray.toLong(): Long {
    var rsp: Long = 0
    rsp += this[0].toLong().and(255).shl(56)
    rsp += this[1].toLong().and(255).shl(48)
    rsp += this[2].toLong().and(255).shl(40)
    rsp += this[3].toLong().and(255).shl(32)
    rsp += this[4].toLong().and(255).shl(24)
    rsp += this[5].toLong().and(255).shl(16)
    rsp += this[6].toLong().and(255).shl(8)
    rsp += this[7].toLong().and(255).shl(0)
    return rsp
}


///////////////////////////////////////////////////////////////////////////
// hexToBytes
///////////////////////////////////////////////////////////////////////////


private val byteStringCandidates = arrayOf('a'..'f', 'A'..'F', '0'..'9', ' '..' ')
private const val CHUNK_SPACE = -1

public fun String.hexToBytes(): ByteArray {
    val array = ByteArray(countHexBytes())
    forEachHexChunkIndexed { index, char1, char2 ->
        array[index] = Byte.parseFromHexChunk(char1, char2)
    }
    return array
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun String.hexToUBytes(): UByteArray {
    val array = UByteArray(countHexBytes())
    forEachHexChunkIndexed { index, char1, char2 ->
        array[index] = Byte.parseFromHexChunk(char1, char2).toUByte()
    }
    return array
}

public fun Byte.Companion.parseFromHexChunk(char1: Char, char2: Char): Byte {
    return (char1.digitToInt(16).shl(SIZE_BITS / 2) or char2.digitToInt(16)).toByte()
}

private inline fun String.forEachHexChunkIndexed(block: (index: Int, char1: Char, char2: Char) -> Unit) {
    var index = 0
    forEachHexChunk { char1: Char, char2: Char ->
        block(index++, char1, char2)
    }
}

private inline fun String.forEachHexChunk(block: (char1: Char, char2: Char) -> Unit) {
    var chunkSize = 0
    var char1: Char = 0.toChar()
    for ((index, c) in this.withIndex()) { // compiler optimization
        if (c == ' ') {
            if (chunkSize != 0) {
                throw IllegalArgumentException("Invalid size of chunk at index ${index.minus(1)}")
            }
            continue
        }
        if (c in 'a'..'f' || c in 'A'..'F' || c in '0'..'9') { // compiler optimization
            when (chunkSize) {
                0 -> {
                    chunkSize = 1
                    char1 = c
                }
                1 -> {
                    block(char1, c)
                    chunkSize = 0
                }
            }
        } else {
            throw IllegalArgumentException("Invalid char '$c' at index $index")
        }
    }
    if (chunkSize != 0) {
        throw IllegalArgumentException("Invalid size of chunk at end of string")
    }
}

public fun String.countHexBytes(): Int {
    var chunkSize = 0
    var count = 0
    for ((index, c) in this.withIndex()) {
        if (c == ' ') {
            if (chunkSize != 0) {
                throw IllegalArgumentException("Invalid size of chunk at index ${index.minus(1)}")
            }
            continue
        }
        if (c in 'a'..'f' || c in 'A'..'F' || c in '0'..'9') {
            when (chunkSize) {
                0 -> {
                    chunkSize = 1
                }
                1 -> {
                    count++
                    chunkSize = 0
                }
            }
        } else {
            throw IllegalArgumentException("Invalid char '$c' at index $index")
        }
    }
    if (chunkSize != 0) {
        throw IllegalArgumentException("Invalid size of chunk at end of string")
    }
    return count
}
