package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgingPacket

internal object ImageUpPacket : PacketFactory<ImageUpPacket.ImageUpPacketResponse>("LongConn.OffPicUp") {

    operator fun invoke(client: QQAndroidClient, req: UploadImgReq): OutgoingPacket {
        return buildOutgingPacket(client, key = client.wLoginSigInfo.d2Key) {
            ProtoBuf.dump(
                Cmd0x352Packet.serializer(),
                Cmd0x352Packet.createByImageRequest(req)
            )
        }
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): ImageUpPacketResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    sealed class ImageUpPacketResponse : Packet {
        object Success : ImageUpPacketResponse()
    }

}