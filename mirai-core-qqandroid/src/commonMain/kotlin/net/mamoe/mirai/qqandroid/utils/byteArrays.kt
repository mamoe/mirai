/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "NOTHING_TO_INLINE", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.charsets.Charset
import kotlinx.io.charsets.Charsets
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.String
import kotlinx.io.core.use
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic


@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
internal fun List<Byte>.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }

    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toUByte().toString(16).toUpperCase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

@kotlin.internal.LowPriorityInOverloadResolution
@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
internal fun ByteArray.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
    this.checkOffsetAndLength(offset, length)
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toUByte().toString(16).toUpperCase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

@JvmSynthetic
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
@ExperimentalUnsignedTypes
internal fun UByteArray.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toByte().toUByte().toString(16).toUpperCase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun ByteArray.encodeToString(offset: Int = 0, charset: Charset = Charsets.UTF_8): String =
    String(this, charset = charset, offset = offset, length = this.size - offset)

@PublishedApi
internal inline fun ByteArray.toReadPacket(offset: Int = 0, length: Int = this.size - offset) =
    ByteReadPacket(this, offset = offset, length = length)

internal inline fun <R> ByteArray.read(t: ByteReadPacket.() -> R): R {
    contract {
        callsInPlace(t, InvocationKind.EXACTLY_ONCE)
    }
    return this.toReadPacket().use(t)
}