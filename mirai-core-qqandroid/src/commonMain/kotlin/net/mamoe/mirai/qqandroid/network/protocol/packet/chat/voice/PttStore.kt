package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.voice

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.getRandomString
import net.mamoe.mirai.qqandroid.utils._miraiContentToString
import net.mamoe.mirai.qqandroid.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf

internal class PttStore {
    object GroupPttUp : OutgoingPacketFactory<GroupPttUp.Response>("PttStore.GroupPttUp") {

        sealed class Response : Packet {

            class Resp(
                val resp: Cmd0x388.RspBody
            ) : Response() {
                override fun toString(): String {
                    return resp._miraiContentToString()
                }
            }
        }

        /**
         * 发语音
         * 收到请求后可以通过下面的代码来上传到服务器
         *
        val up_rsp = response.resp.msgTryupPttRsp!![0]
        if (!up_rsp.boolFileExit) {
        val server = up_rsp.uint32UpIp!![0].toIpV4AddressString()
        val port = up_rsp.uint32UpPort?.get(0)
        val id = up_rsp.fileid

        HttpClient().post<String> {
        url("http://$server:$port")
        parameter("ver", 4679)
        parameter("ukey", up_rsp.upUkey.toUHexString(""))
        parameter("filekey", up_rsp.fileKey.toUHexString(""))
        parameter("filesize", size)
        parameter("bmd5", md5)
        parameter("mType", "pttDu")
        parameter("voice_encodec", 0)
        body = file.readBytes()
        }

         * */

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
                        fileName = getRandomString(16).encodeToByteArray(),
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
            return Response.Resp(resp0)
        }

    }

}
