package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory

internal object ImageUpPacket : PacketFactory<ImageUpPacket.ImageUpPacketResponse>() {

    init {
        this._commandName = "LongConn.OffPicUp"
    }


    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): ImageUpPacketResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    sealed class ImageUpPacketResponse : Packet {
        object Success : ImageUpPacketResponse()
    }


}