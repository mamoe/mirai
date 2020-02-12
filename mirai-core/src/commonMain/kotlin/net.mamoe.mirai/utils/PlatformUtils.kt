/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import kotlinx.io.core.toByteArray

/**
 * 时间戳
 */
expect val currentTimeMillis: Long

inline val currentTimeSeconds: Long get() = currentTimeMillis / 1000


/**
 * 解 zip 压缩
 */
expect fun ByteArray.unzip(offset: Int = 0, length: Int = this.size - offset): ByteArray

/**
 * MD5 算法
 *
 * @return 16 bytes
 */
expect fun md5(byteArray: ByteArray): ByteArray

inline fun md5(str: String): ByteArray = md5(str.toByteArray())

/**
 * Localhost 解析
 */
expect fun localIpAddress(): String

/**
 * Ktor HttpClient. 不同平台使用不同引擎.
 */
expect val Http: HttpClient

internal fun ByteArray.checkOffsetAndLength(offset: Int, length: Int) {
    require(offset >= 0) { "offset shouldn't be negative: $offset" }
    require(length >= 0) { "length shouldn't be negative: $length" }
    require(offset + length <= this.size) { "offset ($offset) + length ($length) > array.size (${this.size})" }
}