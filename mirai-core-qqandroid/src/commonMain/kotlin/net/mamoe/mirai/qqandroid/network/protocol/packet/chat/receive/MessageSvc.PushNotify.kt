package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.io.serialization.readRemainingAsJceStruct
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.data.RequestDataVersion2
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.data.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.RequestPushNotify
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.firstValue
import net.mamoe.mirai.utils.io.toReadPacket

class MessageSvc {
    internal object PushNotify : PacketFactory<RequestPushNotify>("MessageSvc.PushNotify") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RequestPushNotify {
            discardExact(8)

            val requestPushNotify = readRemainingAsJceStruct(RequestPacket.serializer()).sBuffer
                .loadAs(RequestDataVersion2.serializer()).map.firstValue().firstValue()
                .toReadPacket().apply { discardExact(1) }
                .readRemainingAsJceStruct(RequestPushNotify.serializer())

            println(requestPushNotify.contentToString())


            return requestPushNotify
        }
    }
}

