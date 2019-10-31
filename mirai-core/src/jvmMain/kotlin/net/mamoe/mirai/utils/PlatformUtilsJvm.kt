@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.io.core.Input
import java.io.DataInput
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.security.MessageDigest
import java.util.zip.CRC32

actual val deviceName: String = InetAddress.getLocalHost().hostName

actual fun crc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }

actual fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

fun InputStream.md5(): ByteArray {
    val digest = MessageDigest.getInstance("md5")
    digest.reset()
    this.transferTo(object : OutputStream() {
        override fun write(b: Int) {
            b.toByte().let {
                digest.update(it)
            }
        }
    })
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

//actual fun solveIpAddress(hostname: String): String = InetAddress.getByName(hostname).hostAddress

actual fun localIpAddress(): String = InetAddress.getLocalHost().hostAddress

internal actual val httpClient: HttpClient = HttpClient(CIO)

internal actual fun HttpRequestBuilder.configureBody(
    inputSize: Long,
    input: Input
) {
    //body = ByteArrayContent(input.readBytes(), ContentType.Image.PNG)

    body = object : OutgoingContent.WriteChannelContent() {
        override val contentType: ContentType = ContentType.Image.PNG
        override val contentLength: Long = inputSize

        override suspend fun writeTo(channel: ByteWriteChannel) {//不知道为什么这个 channel 在 common 找不到...
            val buffer = byteArrayOf(1)
            repeat(contentLength.toInt()) {
                input.readFully(buffer, 0, 1)
                channel.writeFully(buffer, 0, 1)
            }
        }
    }
}