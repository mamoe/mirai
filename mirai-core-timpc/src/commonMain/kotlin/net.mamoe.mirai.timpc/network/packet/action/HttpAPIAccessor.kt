@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.action

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.content.OutgoingContent
import io.ktor.http.userAgent
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.io.core.Input
import kotlinx.io.core.readAvailable
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.debugPrint


@Suppress("SpellCheckingInspection")
internal suspend inline fun HttpClient.postImage(
    htcmd: String,
    uin: Long,
    groupId: GroupId?,
    imageInput: Input,
    inputSize: Long,
    uKeyHex: String
): Boolean = try {
    post<HttpStatusCode> {
        url {
            protocol = URLProtocol.HTTP
            host = "htdata2.qq.com"
            path("cgi-bin/httpconn")

            parameters["htcmd"] = htcmd
            parameters["uin"] = uin.toString()

            if (groupId != null) parameters["groupcode"] = groupId.value.toString()

            parameters["term"] = "pc"
            parameters["ver"] = "5603"
            parameters["filesize"] = inputSize.toString()
            parameters["range"] = 0.toString()
            parameters["ukey"] = uKeyHex

            userAgent("QQClient")

            buildString().debugPrint("URL")
        }

        body = object : OutgoingContent.WriteChannelContent() {
            override val contentType: ContentType = ContentType.Image.Any
            override val contentLength: Long = inputSize

            override suspend fun writeTo(channel: ByteWriteChannel) {
                ByteArrayPool.useInstance { buffer: ByteArray ->
                    var size: Int
                    while (imageInput.readAvailable(buffer).also { size = it } != 0) {
                        channel.writeFully(buffer, 0, size)
                    }
                }
            }
        }
    } == HttpStatusCode.OK
} finally {
    imageInput.close()
}
