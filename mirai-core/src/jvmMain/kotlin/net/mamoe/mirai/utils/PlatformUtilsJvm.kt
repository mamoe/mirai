@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.Output
import kotlinx.io.core.copyTo
import kotlinx.io.core.readBytes
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import java.io.*
import java.net.InetAddress
import java.security.MessageDigest
import java.util.zip.CRC32
import java.util.zip.Inflater

actual val deviceName: String = InetAddress.getLocalHost().hostName

actual fun crc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }

actual fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

fun InputStream.md5(): ByteArray {
    val digest = MessageDigest.getInstance("md5")
    digest.reset()
    this.asInput().copyTo(object : OutputStream() {
        override fun write(b: Int) {
            b.toByte().let {
                digest.update(it)
            }
        }
    }.asOutput())
    return digest.digest()
}

fun DataInput.md5(): ByteArray {
    val digest = MessageDigest.getInstance("md5")
    digest.reset()
    val buffer = byteArrayOf(1)
    while (true) {
        try {
            this.readFully(buffer)
        } catch (e: EOFException) {
            break
        }
        digest.update(buffer[0])
    }
    return digest.digest()
}

actual fun solveIpAddress(hostname: String): String = InetAddress.getByName(hostname).hostAddress

actual fun localIpAddress(): String = InetAddress.getLocalHost().hostAddress

actual val Http: HttpClient get() = HttpClient(CIO)

actual fun ByteArray.unzip(): ByteArray {
    val inflater = Inflater()
    inflater.reset()
    val input = this
    val output = ByteArrayOutputStream()
    inflater.setInput(input)
    val buffer = ByteArray(128)
    while (!inflater.finished()) {
        output.write(buffer, 0, inflater.inflate(buffer))
    }
    inflater.end()
    return output.toByteArray()
}