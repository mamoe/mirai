package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.voice

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.Packet
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.qqandroid.utils.encodeToString
import net.mamoe.mirai.qqandroid.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.utils.io.serialization.writeProtoBuf

internal class PttStore {
    object GroupPttUp : OutgoingPacketFactory<GroupPttUp.Response>("PttStore.GroupPttUp") {

        sealed class Response : Packet {

            class RequireUpload(
                val fileId: Long,
                val uKey: ByteArray,
                val uploadIpList: List<Int>,
                val uploadPortList: List<Int>,
                val fileKey: ByteArray
            ) : GroupPttUp.Response() {
                override fun toString(): String {
                    return "RequireUpload(fileId=$fileId, uKey=${uKey.contentToString()})"
                }
            }
        }

        @OptIn(ExperimentalStdlibApi::class)
        operator fun invoke(
            client: QQAndroidClient,
            uin: Long,
            groupCode: Long,
            md5: ByteArray,
            size: Long,
            codec: Int = 0
        ): OutgoingPacket {
            val pack = Cmd0x388.ReqBody(
                netType = 3, // wifi
                subcmd = 3,
                msgTryupPttReq = listOf(
                    Cmd0x388.TryUpPttReq(
                        srcUin = uin,
                        groupCode = groupCode,
                        fileId = 0,
                        fileSize = size,
                        fileMd5 = md5,
                        fileName = md5,
                        srcTerm = 5,
                        platformType = 9,
                        buType = 4,
                        innerIp = 0,
                        buildVer = "6.5.5.663".encodeToByteArray(),
                        voiceLength = 1,
                        codec = codec,
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

    object GroupPttDown : OutgoingPacketFactory<GroupPttDown.Response>("PttStore.GroupPttDown") {

        sealed class Response : Packet {
            class DownLoadInfo(
                val downDomain: ByteArray,
                val downPara: ByteArray,
                val strDomain: String,
                val uint32DownIp: List<Int>,
                val uint32DownPort: List<Int>
            ) : GroupPttDown.Response() {
                override fun toString(): String {
                    return "GroupPttDown(downPara=${downPara.encodeToString()},strDomain=$strDomain})"
                }
            }

        }

        @OptIn(ExperimentalStdlibApi::class)
        operator fun invoke(
            client: QQAndroidClient,
            groupCode: Long,
            dstUin: Long,
            md5: ByteArray

        ): OutgoingPacket = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Cmd0x388.ReqBody.serializer(), Cmd0x388.ReqBody(
                    netType = 3, // wifi
                    subcmd = 4,
                    msgGetpttUrlReq = listOf(
                        Cmd0x388.GetPttUrlReq(
                            groupCode = groupCode,
                            fileMd5 = md5,
                            dstUin = dstUin,
                            buType = 4,
                            innerIp = 0,
                            buildVer = "6.5.5.663".encodeToByteArray(),
                            codec = 0,
                            reqTerm = 5,
                            reqPlatformType = 9
                        )
                    )
                )
            )
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp0 = readProtoBuf(Cmd0x388.RspBody.serializer())
            resp0.msgGetpttUrlRsp ?: error("cannot find `msgGetpttUrlRsp` from `Cmd0x388.RspBody`")
            val resp = resp0.msgGetpttUrlRsp.first()
            if (!resp.failMsg.contentEquals(EMPTY_BYTE_ARRAY)) {
                throw IllegalStateException(resp.failMsg.encodeToString())
            }
            return Response.DownLoadInfo(
                downDomain = resp.downDomain,
                downPara = resp.downPara,
                uint32DownIp = resp.uint32DownIp!!,
                uint32DownPort = resp.uint32DownPort!!,
                strDomain = resp.strDomain
            )
        }
    }

}
