package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.io.readJceRequestBufferMapVersion2ToJceStruct
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.RequestPushNotify
import net.mamoe.mirai.utils.cryptor.contentToString

class MessageSvc {
    internal object PushNotify : PacketFactory<RequestPushNotify>("MessageSvc.PushNotify") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RequestPushNotify {
            val messageNotification = readJceRequestBufferMapVersion2ToJceStruct(RequestPushNotify)
            println(messageNotification.contentToString())
            TODO()
        }

    }
}