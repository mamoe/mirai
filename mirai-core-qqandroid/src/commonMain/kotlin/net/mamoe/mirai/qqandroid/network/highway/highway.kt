/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.qqandroid.network.highway

import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.io.InputStream
import kotlinx.io.core.*
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.*

@UseExperimental(MiraiInternalAPI::class)
internal fun createImageDataPacketSequence( // RequestDataTrans
    client: QQAndroidClient,
    command: String,
    appId: Int = 537062845,
    dataFlag: Int = 4096,
    commandId: Int,
    localId: Int = 2052,
    uKey: ByteArray,

    data: Any,
    dataSize: Int,
    fileMd5: ByteArray,
    sizePerPacket: Int = 8000
): Flow<ByteReadPacket> {
    ByteArrayPool.checkBufferSize(sizePerPacket)
    require(data is Input || data is InputStream || data is ByteReadChannel) { "unsupported data: ${data::class.simpleName}" }
    require(uKey.size == 128) { "bad uKey. Required size=128, got ${uKey.size}" }
    require(data !is ByteReadPacket || data.remaining.toInt() == dataSize) { "bad input. given dataSize=$dataSize, but actual readRemaining=${(data as ByteReadPacket).remaining}" }
    require(data !is IoBuffer || data.readRemaining == dataSize) { "bad input. given dataSize=$dataSize, but actual readRemaining=${(data as IoBuffer).readRemaining}" }

    val flow = when (data) {
        is ByteReadPacket -> data.chunkedFlow(sizePerPacket)
        is Input -> data.chunkedFlow(sizePerPacket)
        is ByteReadChannel -> data.chunkedFlow(sizePerPacket)
        is InputStream -> data.chunkedFlow(sizePerPacket)
        else -> error("unreachable code")
    }

    return flow.map { chunkedInput ->
        buildPacket {
            val head = CSDataHighwayHead.ReqDataHighwayHead(
                msgBasehead = CSDataHighwayHead.DataHighwayHead(
                    version = 1,
                    uin = client.uin.toString(),
                    command = command,
                    seq = if (commandId == 2) client.nextHighwayDataTransSequenceIdForGroup()
                    else client.nextHighwayDataTransSequenceIdForFriend(),
                    retryTimes = 0,
                    appid = appId,
                    dataflag = dataFlag,
                    commandId = commandId,
                    localeId = localId
                ),
                msgSeghead = CSDataHighwayHead.SegHead(
                    datalength = chunkedInput.bufferSize,
                    filesize = dataSize.toLong(),
                    serviceticket = uKey,
                    md5 = net.mamoe.mirai.utils.md5(chunkedInput.buffer, 0, chunkedInput.bufferSize),
                    fileMd5 = fileMd5,
                    flag = 0,
                    rtcode = 0
                ),
                reqExtendinfo = EMPTY_BYTE_ARRAY,
                msgLoginSigHead = null
            ).toByteArray(CSDataHighwayHead.ReqDataHighwayHead.serializer())

            writeByte(40)
            writeInt(head.size)
            writeInt(chunkedInput.bufferSize)
            writeFully(head)
            println(chunkedInput.bufferSize)
            writeFully(chunkedInput.buffer, 0, chunkedInput.bufferSize)
            writeByte(41)
        }
    }
}