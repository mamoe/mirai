@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import com.soywiz.klock.DateTime
import io.ktor.client.HttpClient

/**
 * 时间戳
 */
inline val currentTime: Long get() = DateTime.nowUnixLong()

/**
 * 设备名
 */
expect val deviceName: String


/**
 * CRC32 算法
 */
expect fun crc32(key: ByteArray): Int

/**
 * MD5 算法
 *
 * @return 16 bytes
 */
expect fun md5(byteArray: ByteArray): ByteArray

/**
 * hostname 解析 ipv4
 */
expect fun solveIpAddress(hostname: String): String

/**
 * Localhost 解析
 */
expect fun localIpAddress(): String

/**
 * Ktor HttpClient. 不同平台使用不同引擎.
 */
@PublishedApi
internal expect val Http: HttpClient
