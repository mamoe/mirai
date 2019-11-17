@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.userAgent
import kotlinx.io.core.Input
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.utils.configureBody


@Suppress("SpellCheckingInspection")
internal suspend inline fun HttpClient.postImage(
    htcmd: String,
    uin: UInt,
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
            parameters["uin"] = uin.toLong().toString()

            if (groupId != null) parameters["groupcode"] = groupId.value.toLong().toString()

            parameters["term"] = "pc"
            parameters["ver"] = "5603"
            parameters["filesize"] = inputSize.toString()
            parameters["range"] = 0.toString()
            parameters["ukey"] = uKeyHex

            userAgent("QQClient")
        }

        configureBody(inputSize, imageInput)
    } == HttpStatusCode.OK
} finally {
    imageInput.close()
}
