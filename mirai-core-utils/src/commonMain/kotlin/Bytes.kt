/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import kotlinx.io.charsets.Charset
import kotlinx.io.core.ByteReadPacket
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@JvmOverloads
public fun generateImageId(md5: ByteArray, format: String = "mirai"): String {
    return """{${generateUUID(md5)}}.$format"""
}

@JvmOverloads
public fun generateImageIdFromResourceId(resourceId: String, format: String = "mirai"): String? {
    //  friend image id:  /f8f1ab55-bf8e-4236-b55e-955848d7069f
    //  friend image id:  /                      f8f1ab55-bf8e-4236-b55e-955848d7069f

    //  friend image id:  /0000000000-3666252994-EFF4427CE3D27DB6B1D9A8AB72E7A29C
    //  friend image id:  /0000000000-3666252994-EFF4427C E3D2 7DB6 B1D9 A8AB72E7A29C
    //   group image id:                        {EF42A82D-8DB6-5D0F-4F11-68961D8DA5CB}.png

    if (resourceId.isNotEmpty()) {
        if (resourceId[0] == '{') {
            // {EF42A82D-8DB6-5D0F-4F11-68961D8DA5CB
            if (resourceId.substringBefore('}', "").length == 37) {
                return resourceId
            }
        }
    }

    val md5String = resourceId.substringAfterLast("-").substringAfter("/").takeIf { it.length == 32 }
        ?: resourceId.replace("-", "").substringAfter('/').takeIf { it.length == 32 }
        ?: return null
    return "{${generateUUID(md5String)}}.$format"
}

public fun generateUUID(md5: ByteArray): String {
    return "${md5[0, 3]}-${md5[4, 5]}-${md5[6, 7]}-${md5[8, 9]}-${md5[10, 15]}"
}

public fun generateUUID(md5String: String): String {
    with(md5String) {
        check(length == 32) { "it should md5String.length == 32" }
        return "${substring(0, 8)}-${substring(8, 12)}-${substring(12, 16)}-${substring(16, 20)}-${substring(20, 32)}"
    }
}

@JvmSynthetic
public operator fun ByteArray.get(rangeStart: Int, rangeEnd: Int): String = buildString {
    for (it in rangeStart..rangeEnd) {
        append(this@get[it].fixToString())
    }
}

private fun Byte.fixToString(): String {
    return when (val b = this.toInt() and 0xff) {
        in 0..15 -> "0${this.toString(16).uppercase()}"
        else -> b.toString(16).uppercase()
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
public fun ByteArray.toUHexString(
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

public fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}


@JvmOverloads
@Suppress("DuplicatedCode") // false positive. foreach is not common to UByteArray and ByteArray
public fun List<Byte>.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
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
                var ret = it.toUByte().toString(16).uppercase()
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
public fun UByteArray.toUHexString(separator: String = " ", offset: Int = 0, length: Int = this.size - offset): String {
    if (length == 0) {
        return ""
    }
    val lastIndex = offset + length
    return buildString(length * 2) {
        this@toUHexString.forEachIndexed { index, it ->
            if (index in offset until lastIndex) {
                var ret = it.toByte().toUByte().toString(16).uppercase()
                if (ret.length == 1) ret = "0$ret"
                append(ret)
                if (index < lastIndex - 1) append(separator)
            }
        }
    }
}

public inline fun ByteArray.encodeToString(offset: Int = 0, charset: Charset = Charsets.UTF_8): String =
    kotlinx.io.core.String(this, charset = charset, offset = offset, length = this.size - offset)

public expect fun ByteArray.encodeBase64(): String
public expect fun String.decodeBase64(): ByteArray

public inline fun ByteArray.toReadPacket(offset: Int = 0, length: Int = this.size - offset): ByteReadPacket =
    ByteReadPacket(this, offset = offset, length = length)

public inline fun <R> ByteArray.read(t: ByteReadPacket.() -> R): R {
    contract {
        callsInPlace(t, InvocationKind.EXACTLY_ONCE)
    }
    return this.toReadPacket().withUse(t)
}