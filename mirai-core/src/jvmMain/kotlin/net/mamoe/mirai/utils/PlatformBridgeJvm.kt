package net.mamoe.mirai.utils

import java.net.InetAddress
import java.security.MessageDigest
import java.util.zip.CRC32

actual val currentTime: Long = System.currentTimeMillis()

actual val deviceName: String = InetAddress.getLocalHost().hostName


actual fun crc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }

actual fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

actual fun solveIpAddress(hostname: String): String = InetAddress.getByName(hostname).hostAddress

actual fun localIpAddress(): String = InetAddress.getLocalHost().hostAddress