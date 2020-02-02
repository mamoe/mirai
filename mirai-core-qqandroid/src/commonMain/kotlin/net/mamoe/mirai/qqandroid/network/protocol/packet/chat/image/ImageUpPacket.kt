package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.writeFully
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.ProtoBufWithNullableSupport
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x352Packet
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.UploadImgReq
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.buildLoginOutgoingPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.writeSsoPacket

internal object ImageUpPacket : OutgoingPacketFactory<ImageUpPacket.ImageUpPacketResponse>("LongConn.OffPicUp") {

    operator fun invoke(client: QQAndroidClient, req: UploadImgReq): OutgoingPacket {
        // TODO: 2020/1/24 测试: bodyType, subAppId
        return buildLoginOutgoingPacket(client, key = client.wLoginSigInfo.d2Key, bodyType = 1) {
            writeSsoPacket(client, subAppId = 0, commandName = commandName, sequenceId = it) {
                val data = ProtoBufWithNullableSupport.dump(
                    Cmd0x352Packet.serializer(),
                    Cmd0x352Packet.createByImageRequest(req)
                )
                writeInt(data.size + 4)
                writeFully(data)
            }
        }
    }

    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): ImageUpPacketResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    sealed class ImageUpPacketResponse : Packet {
        object Success : ImageUpPacketResponse()
    }


}