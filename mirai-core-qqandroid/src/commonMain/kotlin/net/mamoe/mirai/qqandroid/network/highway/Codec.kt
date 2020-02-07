package net.mamoe.mirai.qqandroid.network.highway

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.content.OutgoingContent
import io.ktor.http.userAgent
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.qqandroid.io.serialization.toByteArray
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.io.ByteArrayPool

@Suppress("SpellCheckingInspection")
internal suspend inline fun HttpClient.postImage(
    htcmd: String,
    uin: Long,
    groupcode: Long?,
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

            override suspend fun writeTo(channel: ByteWriteChannel) {
                ByteArrayPool.useInstance { buffer: ByteArray ->
                    var size: Int
                    while (imageInput.readAvailable(buffer).also { size = it } != 0) {
                        channel.writeFully(buffer, 0, size)
                    }
                }
            }
        }
    } == HttpStatusCode.OK
} finally {
    imageInput.close()
}

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
            }.also { println(it.remaining) }
        }
    }
}