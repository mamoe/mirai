package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildOutgingPacket

internal object ImageDownPacket : PacketFactory<ImageDownPacket.ImageDownPacketResponse>() {

    init {
        this._commandName = "LongConn.OffPicDown"
    }


    operator fun invoke(client: QQAndroidClient, req: GetImgUrlReq): OutgoingPacket {
        return buildOutgingPacket(client, this._commandName, this._commandName, client.wLoginSigInfo.d2Key) {
            ProtoBuf.dump(
                Cmd0x352Packet.serializer(),
                Cmd0x352Packet.createByImageRequest(req)
            )
        }
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): ImageDownPacketResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    sealed class ImageDownPacketResponse : Packet {
        object Success : ImageDownPacketResponse()
    }


}