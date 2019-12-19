@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import io.ktor.util.date.GMTDate

/**
 * 时间戳
 */
inline val currentTime: Long get() = GMTDate().timestamp

/**
 * 设备名
 */
expect val deviceName: String


/**
 * CRC32 算法
 */
expect fun crc32(key: ByteArray): Int

expect fun ByteArray.unzip(): ByteArray

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
expect val Http: HttpClient
