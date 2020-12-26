/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.network.highway

import kotlinx.coroutines.flow.Flow
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.writeFully
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.*


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

    val session: ChunkedFlowSession<ChunkedInput> = object : ChunkedFlowSession<ChunkedInput> {
        val input = data.inputStream()
        override val flow: Flow<ChunkedInput> = input.chunkedFlow(
            sizePerPacket, ByteArray(sizePerPacket)
        )

        override fun close() = input.close()
    }

    var offset = 0L
    return session.map { chunkedInput ->
        buildPacket {
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
                    datalength = chunkedInput.bufferSize,
                    dataoffset = offset,
                    filesize = data.size.toLong(),
                    serviceticket = ticket,
                    md5 = chunkedInput.buffer.md5(0, chunkedInput.bufferSize),
                    fileMd5 = fileMd5,
                    flag = 0,
                    rtcode = 0
                ),
                reqExtendinfo = EMPTY_BYTE_ARRAY,
                msgLoginSigHead = null
            ).toByteArray(CSDataHighwayHead.ReqDataHighwayHead.serializer())

            offset += chunkedInput.bufferSize

            writeByte(40)
            writeInt(head.size)
            writeInt(chunkedInput.bufferSize)
            writeFully(head)
            writeFully(chunkedInput.buffer, 0, chunkedInput.bufferSize)
            writeByte(41)
        }
    }
}