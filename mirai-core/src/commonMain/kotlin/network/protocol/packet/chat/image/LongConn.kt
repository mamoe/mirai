/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.image

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.internal.network.protocol.data.proto.GetImgUrlReq
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf

internal class LongConn {

    internal object OffPicUp : OutgoingPacketFactory<OffPicUp.Response>("LongConn.OffPicUp") {

        operator fun invoke(client: QQAndroidClient, req: Cmd0x352.TryUpImgReq): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                writeProtoBuf(
                    Cmd0x352.ReqBody.serializer(),
                    Cmd0x352.ReqBody(1, msgTryupImgReq = listOf(req))
                )
            }
        }


        //08 01 12 7D 08 00 10 AB E1 9D DF 07 18 00 28 01 32 1C 0A 10 8E C4 9D 72 26 AE 20 C0 5D A2 B6 78 4D 12 B7 3A 10 E9 07 18 86 1F 20 30 28 30 52 25 2F 61 30 30 39 32 64 61 39 2D 64 39 31 38 2D 34 38 31 62 2D 38 34 30 63 2D 33 32 33 64 64 33 39 33 34 35 37 63 5A 25 2F 61 30 30 39 32 64 61 39 2D 64 39 31 38 2D 34 38 31 62 2D 38 34 30 63 2D 33 32 33 64 64 33 39 33 34 35 37 63 60 00 68 80 40 20 01

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp = readProtoBuf(Cmd0x352.RspBody.serializer())
            if (resp.failMsg?.isNotEmpty() == true) {
                return Response.Failed(resp.failMsg)
            }
            check(resp.subcmd == 1)
            val imgRsp = resp.msgTryupImgRsp.first()
            if (imgRsp.result != 0) {
                return Response.Failed(imgRsp.failMsg ?: "")
            }

            return if (imgRsp.boolFileExit) {
                Response.FileExists(imgRsp.upResid, imgRsp.msgImgInfo!!)
            } else {
                Response.RequireUpload(imgRsp.upResid, imgRsp.uint32UpIp, imgRsp.uint32UpPort, imgRsp.upUkey)
            }
        }


        sealed class Response : Packet {
            data class FileExists(val resourceId: String, val imageInfo: Cmd0x352.ImgInfo) : Response()
            @Suppress("ArrayInDataClass")
            data class RequireUpload(val resourceId: String, val serverIp: List<Int>, val serverPort: List<Int>, val uKey: ByteArray) : Response()

            data class Failed(val message: String) : Response()
        }

    }

    object OffPicDown : OutgoingPacketFactory<OffPicDown.ImageDownPacketResponse>("LongConn.OffPicDown") {
        operator fun invoke(client: QQAndroidClient, @Suppress("UNUSED_PARAMETER") req: GetImgUrlReq): OutgoingPacket {
            return buildOutgoingUniPacket(client) {
                TODO()
            }
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): ImageDownPacketResponse {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


        sealed class ImageDownPacketResponse : Packet {
            object Success : ImageDownPacketResponse()
        }
    }
}