/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.highway

import kotlinx.io.core.*
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY

object Highway {
    fun RequestDataTrans(
        uin: Long,
        command: String,
        sequenceId: Int,
        appId: Int = 537062845,
        dataFlag: Int = 4096,
        commandId: Int,
        localId: Int = 2052,
        uKey: ByteArray,

        data: Input,
        dataSize: Int,
        md5: ByteArray
    ): ByteReadPacket {
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
        fun buildC2SData(
            dataHighwayHead: CSDataHighwayHead.DataHighwayHead,
            segHead: CSDataHighwayHead.SegHead,
            extendInfo: ByteArray,
            loginSigHead: CSDataHighwayHead.LoginSigHead?,
            body: Input,
            bodySize: Int
        ): ByteReadPacket {
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
                check(body.copyTo(this).toInt() == bodySize) { "bad body size" }
                writeByte(41)
            }
        }
    }
}