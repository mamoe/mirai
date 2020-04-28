@file:Suppress("unused", "unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.qqandroid.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.util.KtorExperimentalAPI
import kotlinx.io.pool.useInstance
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.Inet4Address
import java.security.MessageDigest
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater

internal actual object MiraiPlatformUtils {
    actual fun unzip(data: ByteArray, offset: Int, length: Int): ByteArray {
        data.checkOffsetAndLength(offset, length)
        if (length == 0) return ByteArray(0)

        val inflater = Inflater()
        inflater.reset()
        ByteArrayOutputStream().use { output ->
            inflater.setInput(data, offset, length)
            ByteArrayPool.useInstance {
                while (!inflater.finished()) {
                    output.write(it, 0, inflater.inflate(it))
                }
            }

            inflater.end()
            return output.toByteArray()
        }
    }

    actual fun zip(data: ByteArray, offset: Int, length: Int): ByteArray {
        data.checkOffsetAndLength(offset, length)
        if (length == 0) return ByteArray(0)

        val deflater = Deflater()
        deflater.setInput(data, offset, length)
        deflater.finish()

        ByteArrayPool.useInstance {
            return it.take(deflater.deflate(it)).toByteArray().also { deflater.end() }
        }
    }


    actual fun md5(data: ByteArray, offset: Int, length: Int): ByteArray {
        data.checkOffsetAndLength(offset, length)
        return MessageDigest.getInstance("MD5").apply { update(data, offset, length) }.digest()
    }

    actual inline fun md5(str: String): ByteArray = md5(str.toByteArray())

    /**
     * Ktor HttpClient. 不同平台使用不同引擎.
     */
    @OptIn(KtorExperimentalAPI::class)
    actual val Http: HttpClient = HttpClient(CIO)

    /**
     * Localhost 解析
     */
    actual fun localIpAddress(): String = runCatching {
        Inet4Address.getLocalHost().hostAddress
    }.getOrElse { "192.168.1.123" }

    fun md5(stream: InputStream): ByteArray {
        val digest = MessageDigest.getInstance("md5")
        digest.reset()
        stream.readInSequence {
            digest.update(it.toByte())
        }
        return digest.digest()
    }


    private inline fun InputStream.readInSequence(block: (Int) -> Unit) {
        var read: Int
        while (this.read().also { read = it } != -1) {
            block(read)
        }
    }

    actual fun gzip(data: ByteArray, offset: Int, length: Int): ByteArray {
        ByteArrayOutputStream().use { buf ->
            GZIPOutputStream(buf).use { gzip ->
                data.inputStream(offset, length).use { t -> t.copyTo(gzip) }
            }
            buf.flush()
            return buf.toByteArray()
        }
    }

    actual fun ungzip(data: ByteArray, offset: Int, length: Int): ByteArray {
        return GZIPInputStream(data.inputStream(offset, length)).use { it.readBytes() }
    }
}