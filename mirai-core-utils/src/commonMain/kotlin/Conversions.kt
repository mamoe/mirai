/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "unused")

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

/*
 * 类型转换 Utils.
 * 这些函数为内部函数, 可能会改变
 */

/**
 * 255 -> 00 FF
 */
public fun Short.toByteArray(): ByteArray = with(toInt()) {
    byteArrayOf(
        (shr(8) and 0xFF).toByte(),
        (shr(0) and 0xFF).toByte()
    )
}

/**
 * 255 -> 00 00 00 FF
 */
public fun Int.toByteArray(): ByteArray = byteArrayOf(
    ushr(24).toByte(),
    ushr(16).toByte(),
    ushr(8).toByte(),
    ushr(0).toByte()
)

/**
 * 255 -> 00 00 00 FF
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

public fun Int.toUHexString(separator: String = " "): String = this.toByteArray().toUHexString(separator)

/**
 * 255 -> 00 FF
 */
public fun UShort.toByteArray(): ByteArray = with(toUInt()) {
    byteArrayOf(
        (shr(8) and 255u).toByte(),
        (shr(0) and 255u).toByte()
    )
}

public fun Short.toUHexString(separator: String = " "): String = this.toUShort().toUHexString(separator)

public fun UShort.toUHexString(separator: String = " "): String =
    this.toInt().shr(8).toUShort().toUByte().toUHexString() + separator + this.toUByte().toUHexString()

public fun ULong.toUHexString(separator: String = " "): String =
    this.toLong().toUHexString(separator)

public fun Long.toUHexString(separator: String = " "): String =
    this.ushr(32).toUInt().toUHexString(separator) + separator + this.toUInt().toUHexString(separator)

/**
 * 255 -> 00 FF
 */
public fun UByte.toByteArray(): ByteArray = byteArrayOf((this and 255u).toByte())

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
 * 转 [ByteArray] 后再转 hex
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
    if (this.toInt() in 0..15) "0${this.toString(16).toUpperCase()}" else this.toString(16).toUpperCase()

public fun String.hexToBytes(): ByteArray =
    this.split(" ")
        .asSequence()
        .filterNot { it.isEmpty() }
        .map { s -> s.toUByte(16).toByte() }
        .toList()
        .toByteArray()

/**
 * 每 2 char 为一组, 转换 Hex 为 [ByteArray]
 *
 * 这个方法很累, 不建议经常使用.
 */
public fun String.chunkedHexToBytes(): ByteArray =
    this.asSequence().chunked(2).map { (it[0].toString() + it[1]).toUByte(16).toByte() }.toList().toByteArray()

/**
 * 删掉全部空格和换行后每 2 char 为一组, 转换 Hex 为 [ByteArray].
 */
public fun String.autoHexToBytes(): ByteArray =
    this.replace("\n", "").replace(" ", "").asSequence().chunked(2).map {
        (it[0].toString() + it[1]).toUByte(16).toByte()
    }.toList().toByteArray()

/**
 * 将无符号 Hex 转为 [UByteArray], 有根据 hex 的 [hashCode] 建立的缓存.
 */
public fun String.hexToUBytes(): UByteArray =
    this.split(" ")
        .asSequence()
        .filterNot { it.isEmpty() }
        .map { s -> s.toUByte(16) }
        .toList()
        .toUByteArray()

/**
 * 将 [this] 前 4 个 [Byte] 的 bits 合并为一个 [Int]
 *
 * 详细解释:
 * 一个 [Byte] 有 8 bits
 * 一个 [Int] 有 32 bits
 * 本函数将 4 个 [Byte] 的 bits 连接得到 [Int]
 */
public fun ByteArray.toUInt(): UInt =
    (this[0].toUInt().and(255u) shl 24) + (this[1].toUInt().and(255u) shl 16) + (this[2].toUInt()
        .and(255u) shl 8) + (this[3].toUInt().and(
        255u
    ) shl 0)

public fun ByteArray.toUShort(): UShort =
    ((this[0].toUInt().and(255u) shl 8) + (this[1].toUInt().and(255u) shl 0)).toUShort()

public fun ByteArray.toInt(): Int =
    (this[0].toInt().and(255) shl 24) + (this[1].toInt().and(255) shl 16) + (this[2].toInt()
        .and(255) shl 8) + (this[3].toInt().and(
        255
    ) shl 0)