/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.highway

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.content.OutgoingContent
import io.ktor.http.userAgent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.io.InputStream
import kotlinx.io.core.Input
import kotlinx.io.core.readAvailable
import kotlinx.io.core.use
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.qqandroid.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.withUse
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.PlatformSocket
import net.mamoe.mirai.utils.io.discardExact


@Suppress("SpellCheckingInspection")
internal suspend inline fun HttpClient.postImage(
    htcmd: String,
    uin: Long,
    groupcode: Long?,
    imageInput: Any, // Input from kotlinx.io, InputStream from kotlinx.io MPP, ByteReadChannel from ktor
    inputSize: Long,
    uKeyHex: String
): Boolean = post<HttpStatusCode> {
    url {
        protocol = URLProtocol.HTTP
        host = "htdata2.qq.com"
        path("cgi-bin/httpconn")

        parameters["htcmd"] = htcmd
        parameters["uin"] = uin.toString()

        if (groupcode != null) parameters["groupcode"] = groupcode.toString()

        parameters["term"] = "pc"
        parameters["ver"] = "5603"
        parameters["filesize"] = inputSize.toString()
        parameters["range"] = 0.toString()
        parameters["ukey"] = uKeyHex

        userAgent("QQClient")
    }

    body = object : OutgoingContent.WriteChannelContent() {
        override val contentType: ContentType = ContentType.Image.Any
        override val contentLength: Long = inputSize

        override suspend fun writeTo(channel: io.ktor.utils.io.ByteWriteChannel) {
            ByteArrayPool.useInstance { buffer: ByteArray ->
                when (imageInput) {
                    is Input -> {
                        var size: Int
                        while (imageInput.readAvailable(buffer).also { size = it } != 0) {
                            channel.writeFully(buffer, 0, size)
                        }
                    }
                    is ByteReadChannel -> imageInput.copyAndClose(channel)
                    is InputStream -> {
                        var size: Int
                        while (imageInput.read(buffer).also { size = it } != 0) {
                            channel.writeFully(buffer, 0, size)
                        }
                    }
                    else -> error("unsupported imageInput: ${imageInput::class.simpleName}")
                }
            }
        }
    }
} == HttpStatusCode.OK

@UseExperimental(MiraiInternalAPI::class)
internal object HighwayHelper {
    suspend fun uploadImage(
        client: QQAndroidClient,
        serverIp: String,
        serverPort: Int,
        uKey: ByteArray,
        imageInput: Any,
        inputSize: Int,
        md5: ByteArray,
        commandId: Int  // group=2, friend=1
    ) {
        require(imageInput is Input || imageInput is InputStream || imageInput is ByteReadChannel) { "unsupported imageInput: ${imageInput::class.simpleName}" }
        require(md5.size == 16) { "bad md5. Required size=16, got ${md5.size}" }
        require(uKey.size == 128) { "bad uKey. Required size=128, got ${uKey.size}" }
        require(commandId == 2 || commandId == 1) { "bad commandId. Must be 1 or 2" }

        val socket = PlatformSocket()
        socket.connect(serverIp, serverPort)
        socket.use {

            // TODO: 2020/2/23 使用缓存, 或使用 HTTP 发送更好 (因为无需读取到内存)
            socket.send(
                Highway.RequestDataTrans(
                    uin = client.uin,
                    command = "PicUp.DataUp",
                    sequenceId =
                    if (commandId == 2) client.nextHighwayDataTransSequenceIdForGroup()
                    else client.nextHighwayDataTransSequenceIdForFriend(),
                    uKey = uKey,
                    data = imageInput,
                    dataSize = inputSize,
                    md5 = md5,
                    commandId = commandId
                )
            )

            //0A 3C 08 01 12 0A 31 39 39 34 37 30 31 30 32 31 1A 0C 50 69 63 55 70 2E 44 61 74 61 55 70 20 E9 A7 05 28 00 30 BD DB 8B 80 02 38 80 20 40 02 4A 0A 38 2E 32 2E 30 2E 31 32 39 36 50 84 10 12 3D 08 00 10 FD 08 18 00 20 FD 08 28 C6 01 38 00 42 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 4A 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 50 89 92 A2 FB 06 58 00 60 00 18 53 20 01 28 00 30 04 3A 00 40 E6 B7 F7 D9 80 2E 48 00 50 00
            socket.read().withUse {
                discardExact(1)
                val headLength = readInt()
                discardExact(4)
                val proto = readProtoBuf(CSDataHighwayHead.RspDataHighwayHead.serializer(), length = headLength)
                check(proto.errorCode == 0) { "image upload failed: Transfer errno=${proto.errorCode}" }
            }
        }
    }
}