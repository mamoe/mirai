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
import net.mamoe.mirai.contact.GroupId

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
): Boolean = (httpClient.postImage(
    htcmd = "0x6ff0070",
    uin = botAccount,
    groupcode = null,
    imageInput = imageInput,
    inputSize = inputSize,
    uKeyHex = uKeyHex
) as HttpStatusCode).value.also { println(it) } == 200

/**
 * 上传群图片
 */
@Suppress("DuplicatedCode")
suspend fun httpPostGroupImage(
    botAccount: UInt,
    groupId: GroupId,
    uKeyHex: String,
    imageInput: Input,
    inputSize: Long
): Boolean = (httpClient.postImage(
    htcmd = "0x6ff0071",
    uin = botAccount,
    groupcode = groupId,
    imageInput = imageInput,
    inputSize = inputSize,
    uKeyHex = uKeyHex
) as HttpStatusCode).value.also { println(it) } == 200

@Suppress("SpellCheckingInspection")
private suspend inline fun <reified T> HttpClient.postImage(
    htcmd: String,
    uin: UInt,
    groupcode: GroupId?,
    imageInput: Input,
    inputSize: Long,
    uKeyHex: String
): T = post {
    url {
        protocol = URLProtocol.HTTP
        host = "htdata2.qq.com"
        path("cgi-bin/httpconn")

        parameters["htcmd"] = htcmd
        parameters["uin"] = uin.toLong().toString()

        if (groupcode != null) parameters["groupcode"] = groupcode.value.toLong().toString()

        parameters["term"] = "pc"
        parameters["ver"] = "5603"
        parameters["filesize"] = inputSize.toString()
        parameters["range"] = 0.toString()
        parameters["ukey"] = uKeyHex

        userAgent("QQClient")
    }

    println(url.buildString())
    configureBody(inputSize, imageInput)
}

internal expect fun HttpRequestBuilder.configureBody(inputSize: Long, input: Input)