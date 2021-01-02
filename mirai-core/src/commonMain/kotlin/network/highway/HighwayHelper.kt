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
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.network.protocol.packet.chat.voice.voiceCodec
import net.mamoe.mirai.internal.utils.PlatformSocket
import net.mamoe.mirai.internal.utils.SocketException
import net.mamoe.mirai.internal.utils.addSuppressedMirai
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.toIpV4AddressString
import net.mamoe.mirai.utils.*
import java.io.InputStream
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime
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


internal object HighwayHelper {
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    @OptIn(ExperimentalTime::class)
    suspend fun uploadImageToServers(
        bot: QQAndroidBot,
        servers: List<Pair<Int, Int>>,
        uKey: ByteArray,
        resource: ExternalResource,
        kind: String,
        commandId: Int
    ) = servers.retryWithServers(
        (resource.size * 1000 / 1024 / 10).coerceAtLeast(5000),
        onFail = {
            throw IllegalStateException("cannot upload $kind, failed on all servers.", it)
        }
    ) { ip, port ->
        bot.network.logger.verbose {
            "[Highway] Uploading $kind to ${ip}:$port, size=${resource.size.sizeToString()}"
        }

        val time = measureTime {
            uploadImage(
                client = bot.client,
                serverIp = ip,
                serverPort = port,
                resource = resource,
                fileMd5 = resource.md5,
                ticket = uKey,
                commandId = commandId
            )
        }

        bot.network.logger.verbose {
            "[Highway] Uploading $kind: succeed at ${(resource.size.toDouble() / 1024 / time.inSeconds).roundToInt()} KiB/s"
        }
    }

    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    @OptIn(InternalCoroutinesApi::class)
    internal suspend fun uploadImage(
        client: QQAndroidClient,
        serverIp: String,
        serverPort: Int,
        ticket: ByteArray,
        resource: ExternalResource,
        fileMd5: ByteArray,
        commandId: Int  // group=2, friend=1
    ) {
        require(fileMd5.size == 16) { "bad md5. Required size=16, got ${fileMd5.size}" }
        //  require(ticket.size == 128) { "bad uKey. Required size=128, got ${ticket.size}" }
        // require(commandId == 2 || commandId == 1) { "bad commandId. Must be 1 or 2" }

        val socket = PlatformSocket()
        while (client.bot.network.areYouOk() && client.bot.isActive) {
            try {
                socket.connect(serverIp, serverPort)
                break
            } catch (e: SocketException) {
                delay(3000)
            }
        }
        socket.use {
            createImageDataPacketSequence(
                client = client,
                appId = client.subAppId.toInt(),
                command = "PicUp.DataUp",
                commandId = commandId,
                ticket = ticket,
                data = resource,
                fileMd5 = fileMd5
            ).useAll {
                socket.send(it)
                //0A 3C 08 01 12 0A 31 39 39 34 37 30 31 30 32 31 1A 0C 50 69 63 55 70 2E 44 61 74 61 55 70 20 E9 A7 05 28 00 30 BD DB 8B 80 02 38 80 20 40 02 4A 0A 38 2E 32 2E 30 2E 31 32 39 36 50 84 10 12 3D 08 00 10 FD 08 18 00 20 FD 08 28 C6 01 38 00 42 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 4A 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 50 89 92 A2 FB 06 58 00 60 00 18 53 20 01 28 00 30 04 3A 00 40 E6 B7 F7 D9 80 2E 48 00 50 00

                socket.read().withUse {
                    discardExact(1)
                    val headLength = readInt()
                    discardExact(4)
                    val proto = readProtoBuf(CSDataHighwayHead.RspDataHighwayHead.serializer(), length = headLength)
                    check(proto.errorCode == 0) { "highway transfer failed, error ${proto.errorCode}" }
                }
            }
        }
    }

    suspend fun uploadPttToServers(
        bot: QQAndroidBot,
        servers: List<Pair<Int, Int>>,
        resource: ExternalResource,
        uKey: ByteArray,
        fileKey: ByteArray,
    ) {
        servers.retryWithServers(10 * 1000, {
            throw IllegalStateException("cannot upload ptt, failed on all servers.", it)
        }, { s: String, i: Int ->
            bot.network.logger.verbose {
                "[Highway] Uploading ptt to ${s}:$i, size=${resource.size.sizeToString()}"
            }
            val time = measureTime {
                uploadPttToServer(s, i, resource, uKey, fileKey)
            }
            bot.network.logger.verbose {
                "[Highway] Uploading ptt: succeed at ${(resource.size.toDouble() / 1024 / time.inSeconds).roundToInt()} KiB/s"
            }

        })

    }

    private suspend fun uploadPttToServer(
        serverIp: String,
        serverPort: Int,
        resource: ExternalResource,
        uKey: ByteArray,
        fileKey: ByteArray,
    ) {
        MiraiPlatformUtils.Http.post<String> {
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

internal class ChunkedFlowSession<T>(
    private val input: InputStream,
    private val buffer: ByteArray,
    private val mapper: (buffer: ByteArray, size: Int, offset: Long) -> T
) : Closeable {
    override fun close() {
        input.close()
    }

    private var offset = 0L

    @Suppress("BlockingMethodInNonBlockingContext")
    internal suspend inline fun useAll(crossinline block: suspend (T) -> Unit) = withUse {
        runBIO {
            while (true) {
                val size = input.read(buffer)
                if (size == -1) return@runBIO
                block(mapper(buffer, size, offset))
                offset += size
            }
        }
    }
}


internal fun createImageDataPacketSequence(
    // RequestDataTrans
    client: QQAndroidClient,
    command: String,
    appId: Int,
    dataFlag: Int = 4096,
    commandId: Int,
    localId: Int = 2052,
    ticket: ByteArray,
    data: ExternalResource,
    fileMd5: ByteArray,
    sizePerPacket: Int = ByteArrayPool.BUFFER_SIZE
): ChunkedFlowSession<ByteReadPacket> {
    ByteArrayPool.checkBufferSize(sizePerPacket)
    //   require(ticket.size == 128) { "bad uKey. Required size=128, got ${ticket.size}" }

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
                serviceticket = ticket,
                md5 = buffer.md5(0, size),
                fileMd5 = fileMd5,
                flag = 0,
                rtcode = 0
            ),
            reqExtendinfo = EMPTY_BYTE_ARRAY,
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

internal suspend inline fun List<Pair<Int, Int>>.retryWithServers(
    timeoutMillis: Long,
    onFail: (exception: Throwable?) -> Unit,
    crossinline block: suspend (ip: String, port: Int) -> Unit
) {
    require(this.isNotEmpty()) { "receiver of retryWithServers must not be empty" }

    var exception: Throwable? = null
    for (pair in this) {
        return kotlin.runCatching {
            withTimeoutOrNull(timeoutMillis) {
                block(pair.first.toIpV4AddressString(), pair.second)
            }
        }.recover {
            if (exception != null) {
                exception!!.addSuppressedMirai(it)
            }
            exception = it
            null
        }.getOrNull() ?: continue
    }

    onFail(exception)
}

internal fun Int.sizeToString() = this.toLong().sizeToString()
internal fun Long.sizeToString(): String {
    return if (this < 1024) {
        "$this B"
    } else ((this * 100.0 / 1024).roundToInt() / 100.0).toString() + " KiB"
}