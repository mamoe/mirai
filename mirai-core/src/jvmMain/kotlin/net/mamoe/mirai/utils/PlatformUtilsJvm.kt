@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.content.ByteArrayContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.InetAddress
import java.security.MessageDigest
import java.util.zip.CRC32

actual val deviceName: String = InetAddress.getLocalHost().hostName

/*
 * TODO we may use libraries that provide these functions
 */

actual fun crc32(key: ByteArray): Int = CRC32().let { it.update(key); it.value.toInt() }

actual fun md5(byteArray: ByteArray): ByteArray = MessageDigest.getInstance("MD5").digest(byteArray)

actual fun solveIpAddress(hostname: String): String = InetAddress.getByName(hostname).hostAddress

actual fun localIpAddress(): String = InetAddress.getLocalHost().hostAddress

/**
 * Provided by Ktor Http
 */
private val httpClient: HttpClient = HttpClient {
    engine { CIO }
}

actual suspend fun httpPostFriendImage(
        uKeyHex: String,
        fileSize: Long,
        botNumber: UInt,
        qq: UInt,
        imageData: ByteReadPacket
): Boolean = (httpClient.post {
    url {
        protocol = URLProtocol.HTTP
        host = "htdata2.qq.com"
        path("cgi-bin/httpconn")
        parameters["htcmd"] = "0x6ff0070"
        parameters["ver"] = "5603"
        parameters["ukey"] = uKeyHex
        parameters["filezise"] = imageData.remaining.toString()
        parameters["range"] = 0.toString()
        parameters["uin"] = qq.toString()
    }

    body = ByteArrayContent(imageData.readBytes(), ContentType.Image.Any)
} as HttpStatusCode).value == 200


//.postImage(imageData)

/**
 * 上传群图片
 */
actual suspend fun httpPostGroupImage(
        bot: UInt,
        groupNumber: UInt,
        uKeyHex: String,
        fileSize: Long,
        imageData: ByteReadPacket
): Boolean = Jsoup.connect("http://htdata2.qq.com/cgi-bin/httpconn" +
        "?htcmd=0x6ff0071" +
        "&term=pc" +
        "&ver=5603" +
        "&filesize=${imageData.remaining}" +
        "&uin=$bot" +
        "&groupcode=$groupNumber" +
        "&range=0" +
        "&ukey=" + uKeyHex)
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