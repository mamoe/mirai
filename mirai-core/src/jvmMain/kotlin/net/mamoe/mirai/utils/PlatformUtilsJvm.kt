@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.io.core.Input
import kotlinx.io.core.readFully
import java.io.DataInput
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.security.MessageDigest
import java.util.zip.CRC32

actual val deviceName: String = InetAddress.getLocalHost().hostName

/*
 * TODO: we may use libraries that provide these functions
 */

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

actual fun solveIpAddress(hostname: String): String = InetAddress.getByName(hostname).hostAddress

actual fun localIpAddress(): String = InetAddress.getLocalHost().hostAddress

/*
actual suspend fun httpPostFriendImageOld(
    uKeyHex: String,
    botNumber: UInt,
    imageData: ByteReadPacket
): Boolean = Jsoup.connect(
    "http://htdata2.qq.com/cgi-bin/httpconn" +
            "?htcmd=0x6ff0070" +
            "&ver=5603" +
            "&ukey=$uKeyHex" +
            "&filezise=${imageData.remaining}" +
            "&range=0" +
            "&uin=$botNumber"
)
    .postImage(imageData)


private suspend fun Connection.postImage(image: ByteReadPacket): Boolean = this
    .userAgent("QQClient")
    .header("Content-Length", image.remaining.toString())
    .requestBody(String(image.readBytes(), Charsets.ISO_8859_1))
    .method(Connection.Method.POST)
    .postDataCharset("ISO_8859_1")
    .header("Content-type", "image/gif")
    .ignoreContentType(true)
    .suspendExecute()
    .statusCode() == 200

private suspend fun Connection.suspendExecute(): Connection.Response = withContext(Dispatchers.IO) {
    execute()
}
*/

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
                input.readFully(buffer)
                channel.writeFully(buffer, 0, buffer.size)
            }
        }
    }
}