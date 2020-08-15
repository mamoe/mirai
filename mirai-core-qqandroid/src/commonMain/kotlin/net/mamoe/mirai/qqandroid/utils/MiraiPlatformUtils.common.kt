/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import io.ktor.client.*

internal expect object MiraiPlatformUtils {
    fun unzip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    fun zip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    fun gzip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    fun ungzip(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray


    fun md5(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): ByteArray

    inline fun md5(str: String): ByteArray

    fun localIpAddress(): String

    /**
     * Ktor HttpClient. 不同平台使用不同引擎.
     */
    val Http: HttpClient
}

@Suppress("DuplicatedCode") // false positive. `this` is not the same for `List<Byte>` and `ByteArray`
internal fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}