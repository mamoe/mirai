/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.highway

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.voiceCodec
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.copyTo
import net.mamoe.mirai.utils.toUHexString
import net.mamoe.mirai.utils.withUse


/**
 * 在发送完成后将会 [InputStream.close]
 */
internal fun ExternalResource.consumeAsWriteChannelContent(contentType: ContentType?): OutgoingContent.WriteChannelContent {
    return object : OutgoingContent.WriteChannelContent() {
        override val contentType: ContentType? = contentType
        override val contentLength: Long = size

        override suspend fun writeTo(channel: ByteWriteChannel) {
            input().withUse { copyTo(channel) }
        }
    }
}

internal val FALLBACK_HTTP_SERVER = "htdata2.qq.com" to 0

@OptIn(InternalAPI::class) // ktor bug
@Suppress("SpellCheckingInspection")
internal suspend fun HttpClient.postImage(
    serverIp: String,
    serverPort: Int = DEFAULT_PORT,
    htcmd: String,
    uin: Long,
    groupcode: Long?,
    imageInput: ExternalResource,
    uKeyHex: String,
): Boolean = post {
    url {
        protocol = URLProtocol.HTTP
        host = serverIp // "htdata2.qq.com"
        port = serverPort
        path("cgi-bin/httpconn")

        parameters["htcmd"] = htcmd
        parameters["uin"] = uin.toString()

        if (groupcode != null) parameters["groupcode"] = groupcode.toString()

        parameters["term"] = "pc"
        parameters["ver"] = "5603"
        parameters["filesize"] = imageInput.size.toString()
        parameters["range"] = 0.toString()
        parameters["ukey"] = uKeyHex

        userAgent("QQClient")
    }

    body = imageInput.consumeAsWriteChannelContent(ContentType.Image.Any)
}.status == HttpStatusCode.OK

internal suspend fun HttpClient.postPtt(
    serverIp: String,
    serverPort: Int,
    resource: ExternalResource,
    uKey: ByteArray,
    fileKey: ByteArray,
) {
    post {
        url("http://$serverIp:$serverPort")
        parameter("ver", 4679)
        parameter("ukey", uKey.toUHexString(""))
        parameter("filekey", fileKey.toUHexString(""))
        parameter("filesize", resource.size)
        parameter("bmd5", resource.md5.toUHexString(""))
        parameter("mType", "pttDu")
        parameter("voice_encodec", resource.voiceCodec)
        setBody(resource.consumeAsWriteChannelContent(null))
    }
}
