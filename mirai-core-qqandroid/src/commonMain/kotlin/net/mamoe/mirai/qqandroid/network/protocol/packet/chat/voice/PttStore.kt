package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.voice

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.getRandomString
import net.mamoe.mirai.qqandroid.utils._miraiContentToString
import net.mamoe.mirai.qqandroid.utils.encodeToString
import net.mamoe.mirai.qqandroid.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf
import net.mamoe.mirai.qqandroid.utils.toUHexString

internal class PttStore {
    object GroupPttUp : OutgoingPacketFactory<GroupPttUp.Response>("PttStore.GroupPttUp") {

        sealed class Response : Packet {

            class RequireUpload(
                val fileId: Long,
                val uKey: ByteArray,
                val uploadIpList: List<Int>,
                val uploadPortList: List<Int>,
                val fileKey: ByteArray
            ) : Response() {
                override fun toString(): String {
                    return "RequireUpload(fileId=$fileId, uKey=${uKey.contentToString()})"
                }
            }
        }


        @ExperimentalStdlibApi
        operator fun invoke(
            client: QQAndroidClient,
            uin: Long,
            groupCode: Long,
            md5: ByteArray,
            size: Long,
            voiceLength: Int,
            fileId: Long = 0
        ): OutgoingPacket {
            val pack = Cmd0x388.ReqBody(
                netType = 3, // wifi
                subcmd = 3,
                msgTryupPttReq = listOf(
                    Cmd0x388.TryUpPttReq(
                        srcUin = uin,
                        groupCode = groupCode,
                        fileId = fileId,
                        fileSize = size,
                        fileMd5 = md5,
                        fileName = md5,
                        srcTerm = 5,
                        platformType = 9,
                        buType = 4,
                        innerIp = 0,
                        buildVer = "6.5.5.663".encodeToByteArray(),
                        voiceLength = voiceLength,
                        codec = 0,
                        voiceType = 1,
                        boolNewUpChan = true
                    )
                )
            )
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(Cmd0x388.ReqBody.serializer(), pack)
            }
        }


        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp0 = readProtoBuf(Cmd0x388.RspBody.serializer())
            resp0.msgTryupPttRsp ?: error("cannot find `msgTryupPttRsp` from `Cmd0x388.RspBody`")
            val resp = resp0.msgTryupPttRsp.first()
            if (resp.failMsg != null) {
                throw IllegalStateException(resp.failMsg.encodeToString())
            }
            return Response.RequireUpload(
                fileId = resp.fileid,
                uKey = resp.upUkey,
                uploadIpList = resp.uint32UpIp!!,
                uploadPortList = resp.uint32UpPort!!,
                fileKey = resp.fileKey
            )

        }

    }

}
