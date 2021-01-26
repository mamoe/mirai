/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.highway

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.writeFully
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.voiceCodec
import net.mamoe.mirai.internal.utils.PlatformSocket
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.retryWithServers
import net.mamoe.mirai.internal.utils.sizeToString
import net.mamoe.mirai.utils.*
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReference
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.roundToInt
import kotlin.time.measureTime


/**
 * 在发送完成后将会 [InputStream.close]
 */
internal fun ExternalResource.consumeAsWriteChannelContent(contentType: ContentType?): OutgoingContent.WriteChannelContent {
    return object : OutgoingContent.WriteChannelContent() {
        override val contentType: ContentType? = contentType
        override val contentLength: Long = size

        override suspend fun writeTo(channel: ByteWriteChannel) {
            inputStream().withUse { copyTo(channel) }
        }
    }
}

@Suppress("SpellCheckingInspection")
internal suspend fun HttpClient.postImage(
    htcmd: String,
    uin: Long,
    groupcode: Long?,
    imageInput: ExternalResource,
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
        parameters["filesize"] = imageInput.size.toString()
        parameters["range"] = 0.toString()
        parameters["ukey"] = uKeyHex

        userAgent("QQClient")
    }

    body = imageInput.consumeAsWriteChannelContent(ContentType.Image.Any)
} == HttpStatusCode.OK


internal object Highway {

    suspend fun uploadResourceHighway(
        bot: QQAndroidBot,
        servers: List<Pair<Int, Int>>,
        uKey: ByteArray,
        resource: ExternalResource,
        kind: String,
        commandId: Int,  // group=2, friend=1, groupPtt=29
    ) {
        runWithLogs(bot, servers, resource.size, kind) { ip, port ->
            val md5 = resource.md5
            require(md5.size == 16) { "bad md5. Required size=16, got ${md5.size}" }

            PlatformSocket.withConnection(ip, port) {
                highwayPacketSession(
                    client = bot.client,
                    appId = bot.client.subAppId.toInt(),
                    command = "PicUp.DataUp",
                    commandId = commandId,
                    initialTicket = uKey,
                    data = resource,
                    fileMd5 = md5,
                ).sendSequentially(this)
            }
        }
    }

    @Suppress("ArrayInDataClass")
    data class BdhUploadResponse(
        var extendInfo: ByteArray? = null,
    )

    suspend fun uploadResourceBdh(
        bot: QQAndroidBot,
        resource: ExternalResource,
        kind: String,
        commandId: Int,  // group=2, friend=1, groupPtt=29
        extendInfo: ByteArray,
        encrypt: Boolean,
    ): BdhUploadResponse {
        val bdhSession = bot.client.bdhSession.await() // no need to care about timeout. proceed by bot init

        return runWithLogs(bot, bdhSession.ssoAddresses, resource.size, kind) { ip, port ->
            val md5 = resource.md5
            require(md5.size == 16) { "bad md5. Required size=16, got ${md5.size}" }

            PlatformSocket.withConnection(ip, port) {
                val resp = BdhUploadResponse()
                highwayPacketSession(
                    client = bot.client,
                    appId = bot.client.subAppId.toInt(),
                    command = "PicUp.DataUp",
                    commandId = commandId,
                    initialTicket = bdhSession.sigSession,
                    data = resource,
                    fileMd5 = md5,
                    extendInfo = if (encrypt) TEA.encrypt(extendInfo, bdhSession.sessionKey) else extendInfo
                ).sendSequentially(this) { head ->
                    if (head.rspExtendinfo.isNotEmpty()) {
                        resp.extendInfo = head.rspExtendinfo
                    }
                }
                resp
            }
        }
    }

    suspend fun uploadPttHttp(
        bot: QQAndroidBot,
        servers: List<Pair<Int, Int>>,
        resource: ExternalResource,
        uKey: ByteArray,
        fileKey: ByteArray,
    ) {
        servers.retryWithServers(
            10 * 1000,
            onFail = { throw IllegalStateException("cannot upload ptt, failed on all servers.", it) }
        ) { s: String, i: Int ->
            bot.network.logger.verbose {
                "[Highway] Uploading ptt to ${s}:$i, size=${resource.size.sizeToString()}"
            }
            val time = measureTime {
                uploadPttToServer(s, i, resource, uKey, fileKey)
            }
            bot.network.logger.verbose {
                "[Highway] Uploading ptt: succeed at ${(resource.size.toDouble() / 1024 / time.inSeconds).roundToInt()} KiB/s"
            }

        }

    }

    private suspend fun uploadPttToServer(
        serverIp: String,
        serverPort: Int,
        resource: ExternalResource,
        uKey: ByteArray,
        fileKey: ByteArray,
    ) {
        Mirai.Http.post<String> {
            url("http://$serverIp:$serverPort")
            parameter("ver", 4679)
            parameter("ukey", uKey.toUHexString(""))
            parameter("filekey", fileKey.toUHexString(""))
            parameter("filesize", resource.size)
            parameter("bmd5", resource.md5.toUHexString(""))
            parameter("mType", "pttDu")
            parameter("voice_encodec", resource.voiceCodec)
            body = resource.consumeAsWriteChannelContent(null)
        }
    }
}

internal suspend inline fun <reified R> runWithLogs(
    bot: QQAndroidBot,
    servers: Collection<Pair<Int, Int>>,
    resourceSize: Long,
    kind: String,
    crossinline implOnEachServer: suspend (ip: String, port: Int) -> R
) = servers.retryWithServers(
    (resourceSize * 1000 / 1024 / 10).coerceAtLeast(5000),
    onFail = { throw IllegalStateException("cannot upload $kind, failed on all servers.", it) }
) { ip, port ->
    bot.network.logger.verbose {
        "[Highway] Uploading $kind to ${ip}:$port, size=${resourceSize.sizeToString()}"
    }

    var resp: R? = null
    val time = measureTime {
        runCatching {
            resp = implOnEachServer(ip, port)
        }.onFailure {
            bot.network.logger.verbose {
                "[Highway] Uploading $kind to ${ip}:$port, size=${resourceSize.sizeToString()} failed: $it"
            }
            throw it
        }
    }

    bot.network.logger.verbose {
        "[Highway] Uploading $kind: succeed at ${(resourceSize.toDouble() / 1024 / time.inSeconds).roundToInt()} KiB/s"
    }

    resp as R
}

internal suspend fun ChunkedFlowSession<ByteReadPacket>.sendSequentially(
    socket: PlatformSocket,
    respCallback: (resp: CSDataHighwayHead.RspDataHighwayHead) -> Unit = {}
) {
    contract { callsInPlace(respCallback, InvocationKind.UNKNOWN) }
    useAll {
        socket.send(it)
        //0A 3C 08 01 12 0A 31 39 39 34 37 30 31 30 32 31 1A 0C 50 69 63 55 70 2E 44 61 74 61 55 70 20 E9 A7 05 28 00 30 BD DB 8B 80 02 38 80 20 40 02 4A 0A 38 2E 32 2E 30 2E 31 32 39 36 50 84 10 12 3D 08 00 10 FD 08 18 00 20 FD 08 28 C6 01 38 00 42 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 4A 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 50 89 92 A2 FB 06 58 00 60 00 18 53 20 01 28 00 30 04 3A 00 40 E6 B7 F7 D9 80 2E 48 00 50 00

        socket.read().withUse {
            discardExact(1)
            val headLength = readInt()
            discardExact(4)
            val proto = readProtoBuf(CSDataHighwayHead.RspDataHighwayHead.serializer(), length = headLength)
            check(proto.errorCode == 0) { "highway transfer failed, error ${proto.errorCode}" }
            respCallback(proto)
        }
    }
}

internal fun highwayPacketSession(
    // RequestDataTrans
    client: QQAndroidClient,
    command: String,
    appId: Int,
    dataFlag: Int = 4096,
    commandId: Int,
    localId: Int = 2052,
    initialTicket: ByteArray,
    data: ExternalResource,
    fileMd5: ByteArray,
    sizePerPacket: Int = ByteArrayPool.BUFFER_SIZE,
    extendInfo: ByteArray = EMPTY_BYTE_ARRAY,
): ChunkedFlowSession<ByteReadPacket> {
    ByteArrayPool.checkBufferSize(sizePerPacket)
    //   require(ticket.size == 128) { "bad uKey. Required size=128, got ${ticket.size}" }

    val ticket = AtomicReference(initialTicket)

    return ChunkedFlowSession(data.inputStream(), ByteArray(sizePerPacket)) { buffer, size, offset ->
        val head = CSDataHighwayHead.ReqDataHighwayHead(
            msgBasehead = CSDataHighwayHead.DataHighwayHead(
                version = 1,
                uin = client.uin.toString(),
                command = command,
                seq = when (commandId) {
                    2 -> client.nextHighwayDataTransSequenceIdForGroup()
                    1 -> client.nextHighwayDataTransSequenceIdForFriend()
                    27 -> client.nextHighwayDataTransSequenceIdForApplyUp()
                    29 -> client.nextHighwayDataTransSequenceIdForGroup()
                    else -> error("illegal commandId: $commandId")
                },
                retryTimes = 0,
                appid = appId,
                dataflag = dataFlag,
                commandId = commandId,
                localeId = localId
            ),
            msgSeghead = CSDataHighwayHead.SegHead(
                //   cacheAddr = 812157193,
                datalength = size,
                dataoffset = offset,
                filesize = data.size,
                serviceticket = ticket.get(),
                md5 = buffer.md5(0, size),
                fileMd5 = fileMd5,
                flag = 0,
                rtcode = 0
            ),
            reqExtendinfo = extendInfo,
            msgLoginSigHead = null
        ).toByteArray(CSDataHighwayHead.ReqDataHighwayHead.serializer())

        buildPacket {
            writeByte(40)
            writeInt(head.size)
            writeInt(size)
            writeFully(head)
            writeFully(buffer, 0, size)
            writeByte(41)
        }
    }
}
