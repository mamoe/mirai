@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import com.soywiz.klock.DateTime

/**
 * 时间戳
 */
val currentTime: Long = DateTime.nowUnixLong()

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
 * Hostname 解析 IP 地址
 */
expect fun solveIpAddress(hostname: String): String

/**
 * Localhost 解析
 */
expect fun localIpAddress(): String

/**
 * 上传群图片
 */
expect suspend fun httpPostFriendImage(
        uKeyHex: String,
        fileSize: Int,
        botNumber: UInt,
        qq: UInt,
        imageData: ByteArray
): Boolean

fun main() {
    "46 52 25 46 60 30 59 4F 4A 5A 51".printStringFromHex()
}