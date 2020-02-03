package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import io.ktor.client.HttpClient
import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.io.serialization.writeProtoBuf
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgoingUniPacket

internal class ImgStore {
    object GroupPicUp : OutgoingPacketFactory<GroupPicUp.Response>("ImgStore.GroupPicUp") {

        operator fun invoke(
            client: QQAndroidClient,
            uin: Long,
            groupCode: Long,
            md5: ByteArray,
            size: Long,
            picWidth: Int,
            picHeight: Int,
            picType: Int = 1000,
            fileId: Long = 0,
            filename: String,
            srcTerm: Int = 5,
            platformType: Int = 9,
            buType: Int = 1,
            appPicType: Int = 1006,
            originalPic: Int = 0
        ): OutgoingPacket = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Cmd0x388.ReqBody.serializer(),
                Cmd0x388.ReqBody(
                    netType = 3, // wifi
                    subcmd = 1,
                    msgTryupImgReq = listOf(
                        Cmd0x388.TryUpImgReq(
                            groupCode = groupCode,
                            srcUin = uin,
                            fileMd5 = md5,
                            fileSize = size,
                            fileId = fileId,
                            fileName = filename,
                            picWidth = picWidth,
                            picHeight = picHeight,
                            picType = picType,
                            appPicType = appPicType,
                            buildVer = client.buildVer,
                            srcTerm = srcTerm,
                            platformType = platformType,
                            originalPic = originalPic,
                            buType = buType
                        )
                    )
                )
            )
        }

        sealed class Response : Packet {
            class FileExists(
                val fileId: Long,
                val fileInfo: Cmd0x388.ImgInfo
            ) : Response() {
                override fun toString(): String {
                    return "FileExists(fileId=$fileId, fileInfo=$fileInfo)"
                }
            }

            class RequireUpload(
                val fileId: Long,
                val uKey: ByteArray,
                val uploadIpList: List<Int>,
                val uploadPortList: List<Int>
            ) : Response() {
                override fun toString(): String {
                    return "RequireUpload(fileId=$fileId, uKey=${uKey.contentToString()})"
                }
            }

            data class Failed(
                val resultCode: Int,
                val message: String
            ) : Response()
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp0 = readProtoBuf(Cmd0x388.RspBody.serializer())
            resp0.msgTryupImgRsp ?: error("cannot find `msgTryupImgRsp` from `Cmd0x388.RspBody`")
            val resp = resp0.msgTryupImgRsp.first()
            return when {
                resp.result != 0 -> Response.Failed(resultCode = resp.result, message = resp.failMsg)
                resp.boolFileExit -> Response.FileExists(fileId = resp.fileid, fileInfo = resp.msgImgInfo!!)
                else -> Response.RequireUpload(fileId = resp.fileid, uKey = resp.upUkey, uploadIpList = resp.uint32UpIp!!, uploadPortList = resp.uint32UpPort!!)
            }
        }
    }
}