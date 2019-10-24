@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import com.soywiz.klock.DateTime
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.userAgent
import kotlinx.io.core.Input

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
 * Provided by Ktor Http
 */
internal expect val httpClient: HttpClient

/**
 * 上传好友图片
 */
@Suppress("DuplicatedCode")
suspend fun httpPostFriendImage(
    botAccount: UInt,
    uKeyHex: String,
    imageInput: Input,
    inputSize: Long
): Boolean = (httpClient.postImage(imageInput, inputSize, uKeyHex) {
    url {
        parameters["htcmd"] = "0x6ff0070"
        parameters["uin"] = botAccount.toLong().toString()
    }

} as HttpStatusCode).value.also { println(it) } == 200

/**
 * 上传群图片
 */
@Suppress("DuplicatedCode")
suspend fun httpPostGroupImage(
    botAccount: UInt,
    groupNumber: UInt,
    uKeyHex: String,
    imageInput: Input,
    inputSize: Long
): Boolean = (httpClient.postImage(imageInput, inputSize, uKeyHex) {
    url {
        parameters["htcmd"] = "0x6ff0071"
        parameters["uin"] = botAccount.toLong().toString()
        parameters["groupcode"] = groupNumber.toLong().toString()
    }
} as HttpStatusCode).value.also { println(it) } == 200


private suspend inline fun <reified T> HttpClient.postImage(
    imageInput: Input,
    inputSize: Long,
    uKeyHex: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = post {
    url {
        protocol = URLProtocol.HTTP
        host = "htdata2.qq.com"
        path("cgi-bin/httpconn")

        parameters["ver"] = "5603"
        parameters["filezise"] = inputSize.toString()
        parameters["range"] = 0.toString()
        parameters["ukey"] = uKeyHex

        userAgent("QQClient")
    }
    block()
    configureBody(inputSize, imageInput)
}

internal expect fun HttpRequestBuilder.configureBody(inputSize: Long, input: Input)