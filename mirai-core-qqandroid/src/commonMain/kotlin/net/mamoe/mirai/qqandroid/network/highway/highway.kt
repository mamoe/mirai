/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.qqandroid.network.highway

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.writeFully
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.utils.ByteArrayPool
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.qqandroid.utils.io.serialization.toByteArray
import net.mamoe.mirai.utils.internal.ChunkedFlowSession
import net.mamoe.mirai.utils.internal.ChunkedInput
import net.mamoe.mirai.utils.internal.ReusableInput
import net.mamoe.mirai.utils.internal.map


internal fun createImageDataPacketSequence(
    // RequestDataTrans
    client: QQAndroidClient,
    command: String,
    appId: Int,
    dataFlag: Int = 4096,
    commandId: Int,
    localId: Int = 2052,
    ticket: ByteArray,
    data: ReusableInput,
    fileMd5: ByteArray,
    sizePerPacket: Int = ByteArrayPool.BUFFER_SIZE
): ChunkedFlowSession<ByteReadPacket> {
    ByteArrayPool.checkBufferSize(sizePerPacket)
    //   require(ticket.size == 128) { "bad uKey. Required size=128, got ${ticket.size}" }

    val session: ChunkedFlowSession<ChunkedInput> = data.chunkedFlow(sizePerPacket)

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
                    filesize = data.size,
                    serviceticket = ticket,
                    md5 = MiraiPlatformUtils.md5(chunkedInput.buffer, 0, chunkedInput.bufferSize),
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