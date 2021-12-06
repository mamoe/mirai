/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.util

import java.security.MessageDigest


@Suppress("DuplicatedCode") // false positive. `this` is not the same for `List<Byte>` and `ByteArray`
internal fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}

internal fun String.autoHexToBytes(): ByteArray =
    this.trim(Char::isWhitespace).asSequence().chunked(2).map {
        (it[0].toString() + it[1]).toUByte(16).toByte()
    }.toList().toByteArray()

internal fun ByteArray.md5(offset: Int = 0, length: Int = this.size - offset): ByteArray {
    this.checkOffsetAndLength(offset, length)
    return MessageDigest.getInstance("MD5").apply { update(this@md5, offset, length) }.digest()
}

@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
internal fun ByteArray.toUHexString(
    separator: String = " ",
    offset: Int = 0,
    length: Int = this.size - offset
): String {
    this.checkOffsetAndLength(offset, length)
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toUByte().toString(16).uppercase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}
