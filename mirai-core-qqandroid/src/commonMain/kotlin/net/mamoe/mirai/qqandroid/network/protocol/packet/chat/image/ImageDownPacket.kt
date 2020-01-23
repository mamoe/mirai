package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory

internal object ImageDownPacket : PacketFactory<ImageDownPacket.ImageDownPacketResponse>() {

    init {
        this._commandName = "LongConn.OffPicDown"
    }


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): ImageDownPacketResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    sealed class ImageDownPacketResponse : Packet {
        object Success : ImageDownPacketResponse()
    }


}