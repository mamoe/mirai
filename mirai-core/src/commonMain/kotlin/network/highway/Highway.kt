/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.highway

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.produceIn
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.writeFully
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.BdhSession
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.PlatformSocket
import net.mamoe.mirai.internal.utils.crypto.TEA
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.internal.utils.retryWithServers
import net.mamoe.mirai.internal.utils.sizeToString
import net.mamoe.mirai.utils.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.roundToInt
import kotlin.time.measureTime

internal object Highway {


    @Suppress("ArrayInDataClass")
    data class BdhUploadResponse(
        var extendInfo: ByteArray? = null,
    )

    suspend fun uploadResourceBdh(
        bot: QQAndroidBot,
        resource: ExternalResource,
        kind: ResourceKind,
        commandId: Int,  // group image=2, friend image=1, groupPtt=29
        extendInfo: ByteArray = EMPTY_BYTE_ARRAY,
        encrypt: Boolean = false,
        initialTicket: ByteArray? = null,
        tryOnce: Boolean = false,
        noBdhAwait: Boolean = false,
        fallbackSession: (Throwable) -> BdhSession = { throw IllegalStateException("Failed to get bdh session", it) }
    ): BdhUploadResponse {
        val bdhSession = kotlin.runCatching {
            val deferred = bot.client.bdhSession
            // no need to care about timeout. proceed by bot init
            @OptIn(ExperimentalCoroutinesApi::class)
            if (noBdhAwait) deferred.getCompleted() else deferred.await()
        }.getOrElse(fallbackSession)

        return tryServersUpload(
            bot = bot,
            servers = if (tryOnce) listOf(bdhSession.ssoAddresses.random()) else bdhSession.ssoAddresses,
            resourceSize = resource.size,
            resourceKind = kind,
            channelKind = ChannelKind.HIGHWAY
        ) { ip, port ->
            val md5 = resource.md5
            require(md5.size == 16) { "bad md5. Required size=16, got ${md5.size}" }

            val resp = BdhUploadResponse()
            highwayPacketSession(
                client = bot.client,
                appId = bot.client.subAppId.toInt(),
                command = "PicUp.DataUp",
                commandId = commandId,
                initialTicket = initialTicket ?: bdhSession.sigSession,
                data = resource,
                fileMd5 = md5,
                extendInfo = if (encrypt) TEA.encrypt(extendInfo, bdhSession.sessionKey) else extendInfo
            ).sendConcurrently(
                createConnection = { PlatformSocket.connect(ip, port) },
                coroutines = bot.configuration.highwayUploadCoroutineCount
            ) { head ->
                if (head.rspExtendinfo.isNotEmpty()) {
                    resp.extendInfo = head.rspExtendinfo
                }
            }
            resp
        }
    }
}

internal enum class ResourceKind(
    private val display: String
) {
    PRIVATE_IMAGE("private image"),
    GROUP_IMAGE("group image"),
    PRIVATE_VOICE("private voice"),
    GROUP_VOICE("group voice"),

    LONG_MESSAGE("long message"),
    FORWARD_MESSAGE("forward message"),
    ;

    override fun toString(): String = display
}

internal enum class ChannelKind(
    private val display: String
) {
    HIGHWAY("Highway"),
    HTTP("Http")
    ;

    override fun toString(): String = display
}

internal suspend inline fun <reified R> tryServersUpload(
    bot: QQAndroidBot,
    servers: Collection<Pair<Int, Int>>,
    resourceSize: Long,
    resourceKind: ResourceKind,
    channelKind: ChannelKind,
    crossinline implOnEachServer: suspend (ip: String, port: Int) -> R
) = servers.retryWithServers(
    (resourceSize * 1000 / 1024 / 10).coerceAtLeast(5000),
    onFail = { throw IllegalStateException("cannot upload $resourceKind, failed on all servers.", it) }
) { ip, port ->
    bot.network.logger.verbose {
        "[${channelKind}] Uploading $resourceKind to ${ip}:$port, size=${resourceSize.sizeToString()}"
    }

    var resp: R? = null
    val time = measureTime {
        runCatching {
            resp = implOnEachServer(ip, port)
        }.onFailure {
            bot.network.logger.verbose {
                "[${channelKind}] Uploading $resourceKind to ${ip}:$port, size=${resourceSize.sizeToString()} failed: $it"
            }
            throw it
        }
    }

    bot.network.logger.verbose {
        "[${channelKind}] Uploading $resourceKind: succeed at ${(resourceSize.toDouble() / 1024 / time.inSeconds).roundToInt()} KiB/s"
    }

    resp as R
}

internal suspend inline fun <reified R> tryServersDownload(
    bot: QQAndroidBot,
    servers: Collection<Pair<Int, Int>>,
    resourceKind: ResourceKind,
    channelKind: ChannelKind,
    crossinline implOnEachServer: suspend (ip: String, port: Int) -> R
) = servers.retryWithServers(
    5000,
    onFail = { throw IllegalStateException("cannot download $resourceKind, failed on all servers.", it) }
) { ip, port ->
    tryDownloadImplEach(bot, channelKind, resourceKind, ip, port, implOnEachServer)
}

internal suspend inline fun <reified R> tryDownload(
    bot: QQAndroidBot,
    host: String,
    port: Int,
    times: Int = 1,
    resourceKind: ResourceKind,
    channelKind: ChannelKind,
    crossinline implOnEachServer: suspend (ip: String, port: Int) -> R
) = retryCatching(times) {
    tryDownloadImplEach(bot, channelKind, resourceKind, host, port, implOnEachServer)
}.getOrElse { throw IllegalStateException("Cannot download $resourceKind", it) }


private suspend inline fun <reified R> tryDownloadImplEach(
    bot: QQAndroidBot,
    channelKind: ChannelKind,
    resourceKind: ResourceKind,
    host: String,
    port: Int,
    crossinline implOnEachServer: suspend (ip: String, port: Int) -> R
): R {
    bot.network.logger.verbose {
        "[${channelKind}] Downloading $resourceKind from ${host}:$port"
    }

    var resp: R? = null
    runCatching {
        resp = implOnEachServer(host, port)
    }.onFailure {
        bot.network.logger.verbose {
            "[${channelKind}] Downloading $resourceKind from ${host}:$port failed: $it"
        }
        throw it
    }

    bot.network.logger.verbose {
        "[${channelKind}] Downloading $resourceKind: succeed"
    }

    return resp as R
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

private fun <T> Flow<T>.produceIn0(coroutineScope: CoroutineScope): ReceiveChannel<T> {
    return kotlin.runCatching {
        @OptIn(FlowPreview::class)
        produceIn(coroutineScope) // this is experimental api
    }.getOrElse {
        // fallback strategy in case binary changes.

        val channel = Channel<T>()
        coroutineScope.launch(CoroutineName("Flow collector")) {
            collect {
                channel.send(it)
            }
            channel.close()
        }
        channel
    }
}

internal suspend fun ChunkedFlowSession<ByteReadPacket>.sendConcurrently(
    createConnection: suspend () -> PlatformSocket,
    coroutines: Int = 5,
    respCallback: (resp: CSDataHighwayHead.RspDataHighwayHead) -> Unit = {}
) = coroutineScope {
    val channel = asFlow().produceIn0(this)
    // 'single thread' producer emits chunks to channel

    repeat(coroutines) {
        launch(CoroutineName("Worker $it")) {
            val socket = createConnection()
            while (isActive) {
                val next = channel.tryReceive() ?: break // concurrent-safe receive
                val result = next.withUse {
                    socket.sendReceiveHighway(next)
                }
                respCallback(result)
            }
        }
    }
}

private suspend fun <E : Any> ReceiveChannel<E>.tryReceive(): E? {
    return kotlin.runCatching {
        @OptIn(ExperimentalCoroutinesApi::class)
        receiveOrNull() // this is experimental api
    }.recoverCatching {
        // in case binary changes
        receive()
    }.getOrNull()
}

private suspend fun PlatformSocket.sendReceiveHighway(
    it: ByteReadPacket,
): CSDataHighwayHead.RspDataHighwayHead {
    send(it)
    //0A 3C 08 01 12 0A 31 39 39 34 37 30 31 30 32 31 1A 0C 50 69 63 55 70 2E 44 61 74 61 55 70 20 E9 A7 05 28 00 30 BD DB 8B 80 02 38 80 20 40 02 4A 0A 38 2E 32 2E 30 2E 31 32 39 36 50 84 10 12 3D 08 00 10 FD 08 18 00 20 FD 08 28 C6 01 38 00 42 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 4A 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 50 89 92 A2 FB 06 58 00 60 00 18 53 20 01 28 00 30 04 3A 00 40 E6 B7 F7 D9 80 2E 48 00 50 00

    read().withUse {
        discardExact(1)
        val headLength = readInt()
        discardExact(4)
        val proto = readProtoBuf(CSDataHighwayHead.RspDataHighwayHead.serializer(), length = headLength)
        check(proto.errorCode == 0) { "highway transfer failed, error ${proto.errorCode}" }
        return proto
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
