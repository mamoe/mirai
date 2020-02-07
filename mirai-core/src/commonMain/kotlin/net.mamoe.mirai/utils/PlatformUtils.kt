@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import io.ktor.util.date.GMTDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.io.core.toByteArray

/**
 * 时间戳
 */
inline val currentTimeMillis: Long get() = GMTDate().timestamp

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