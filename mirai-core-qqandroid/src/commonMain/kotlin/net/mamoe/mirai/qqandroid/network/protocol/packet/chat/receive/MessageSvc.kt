package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.Jce
import net.mamoe.mirai.qqandroid.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.network.protocol.jce.RequestDataVersion2
import net.mamoe.mirai.qqandroid.network.protocol.jce.RequestDataVersion3
import net.mamoe.mirai.qqandroid.network.protocol.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.RequestPushNotify
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.readRemainingBytes

class MessageSvc {
    internal object PushNotify : PacketFactory<RequestPushNotify>("MessageSvc.PushNotify") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RequestPushNotify {
            val req = Jce.UTF8.load(
                RequestPacket.serializer(),
                this.apply { discardExact(8) }.readRemainingBytes()
            )
            val messageNotification = Jce.UTF8.load(
                RequestPushNotify.serializer(),
                req.sBuffer.loadAs(RequestDataVersion2.serializer()).map.entries.first().value.entries.first().value
            )
            println(messageNotification.contentToString())
            ProtoBuf
            TODO()
        }
    }
}

