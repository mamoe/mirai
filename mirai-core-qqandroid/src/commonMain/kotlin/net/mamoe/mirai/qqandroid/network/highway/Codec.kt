/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.highway

import io.ktor.utils.io.ByteReadChannel
import kotlinx.io.InputStream
import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.io.ByteArrayPool

object Highway {
    suspend fun RequestDataTrans(
        uin: Long,
        command: String,
        sequenceId: Int,
        appId: Int = 537062845,
        dataFlag: Int = 4096,
        commandId: Int,
        localId: Int = 2052,
        uKey: ByteArray,

        data: Any,
        dataSize: Int,
        md5: ByteArray
    ): ByteReadPacket {
        require(data is Input || data is InputStream || data is ByteReadChannel) { "unsupported data: ${data::class.simpleName}" }
        require(uKey.size == 128) { "bad uKey. Required size=128, got ${uKey.size}" }
        require(data !is ByteReadPacket || data.remaining.toInt() == dataSize) { "bad input. given dataSize=$dataSize, but actual readRemaining=${(data as ByteReadPacket).remaining}" }
        require(data !is IoBuffer || data.readRemaining == dataSize) { "bad input. given dataSize=$dataSize, but actual readRemaining=${(data as IoBuffer).readRemaining}" }

        val dataHighwayHead = CSDataHighwayHead.DataHighwayHead(
            version = 1,
            uin = uin.toString(),
            command = command,
            seq = sequenceId,
            retryTimes = 0,
            appid = appId,
            dataflag = dataFlag,
            commandId = commandId,
            localeId = localId
        )
        val segHead = CSDataHighwayHead.SegHead(
            datalength = dataSize,
            filesize = dataSize.toLong(),
            serviceticket = uKey,
            md5 = md5,
            fileMd5 = md5,
            flag = 0,
            rtcode = 0
        )
        //println(data.readBytes().toUHexString())
        return Codec.buildC2SData(dataHighwayHead, segHead, EMPTY_BYTE_ARRAY, null, data, dataSize)
    }

    private object Codec {
        suspend fun buildC2SData(
            dataHighwayHead: CSDataHighwayHead.DataHighwayHead,
            segHead: CSDataHighwayHead.SegHead,
            extendInfo: ByteArray,
            loginSigHead: CSDataHighwayHead.LoginSigHead?,
            body: Any,
            bodySize: Int
        ): ByteReadPacket {
            require(body is Input || body is InputStream || body is ByteReadChannel) { "unsupported body: ${body::class.simpleName}" }
            val head = CSDataHighwayHead.ReqDataHighwayHead(
                msgBasehead = dataHighwayHead,
                msgSeghead = segHead,
                reqExtendinfo = extendInfo,
                msgLoginSigHead = loginSigHead
            ).toByteArray(CSDataHighwayHead.ReqDataHighwayHead.serializer())

            return buildPacket {
                writeByte(40)
                writeInt(head.size)
                writeInt(bodySize)
                writeFully(head)
                when (body) {
                    is ByteReadPacket -> writePacket(body)
                    is Input -> ByteArrayPool.useInstance { buffer ->
                        var size: Int
                        while (body.readAvailable(buffer).also { size = it } != 0) {
                            this@buildPacket.writeFully(buffer, 0, size)
                        }
                    }
                    is ByteReadChannel -> ByteArrayPool.useInstance { buffer ->
                        var size: Int
                        while (body.readAvailable(buffer, 0, buffer.size).also { size = it } != 0) {
                            this@buildPacket.writeFully(buffer, 0, size)
                        }
                    }
                    is InputStream -> ByteArrayPool.useInstance { buffer ->
                        var size: Int
                        while (body.read(buffer).also { size = it } != 0) {
                            this@buildPacket.writeFully(buffer, 0, size)
                        }
                    }
                }
                writeByte(41)
            }
        }
    }
}