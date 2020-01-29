@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.io.core.copyTo
import kotlinx.io.pool.useInstance
import kotlinx.io.streams.asInput
import kotlinx.io.streams.asOutput
import net.mamoe.mirai.utils.io.ByteArrayPool
import java.io.*
import java.net.InetAddress
import java.security.MessageDigest
import java.util.concurrent.Executors
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

actual fun ByteArray.unzip(offset: Int, length: Int): ByteArray {
    this.checkOffsetAndLength(offset, length)
    if (length == 0) return ByteArray(0)

    val inflater = Inflater()
    inflater.reset()
    ByteArrayOutputStream().use { output ->
        inflater.setInput(this, offset, length)
        ByteArrayPool.useInstance {
            while (!inflater.finished()) {
                output.write(it, 0, inflater.inflate(it))
            }
        }

        inflater.end()
        return output.toByteArray()
    }
}

actual fun newCoroutineDispatcher(threadCount: Int): CoroutineDispatcher {
    return Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()
}