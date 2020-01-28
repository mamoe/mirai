package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.io.JceStruct
import net.mamoe.mirai.qqandroid.io.serialization.loadAs
import net.mamoe.mirai.qqandroid.io.serialization.readRemainingAsJceStruct
import net.mamoe.mirai.qqandroid.network.protocol.jce.RequestDataVersion2
import net.mamoe.mirai.qqandroid.network.protocol.jce.RequestPacket
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.data.RequestPushNotify
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.firstValue
import net.mamoe.mirai.utils.io.debugPrint
import net.mamoe.mirai.utils.io.toReadPacket
import net.mamoe.mirai.utils.io.toUHexString

class MessageSvc {
    internal object PushNotify : PacketFactory<RequestPushNotify>("MessageSvc.PushNotify") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): RequestPushNotify {
            discardExact(8)

            @Serializable
            class ResponseDataRequestPushNotify(
                @SerialId(0) val notify: RequestPushNotify
            ) : JceStruct

            val requestPushNotify = readRemainingAsJceStruct(RequestPacket.serializer()).sBuffer
                .loadAs(RequestDataVersion2.serializer()).map.firstValue().firstValue()
                .toReadPacket().apply { discardExact(1) }
                .debugPrint()
                .readRemainingAsJceStruct(RequestPushNotify.serializer())

            println(requestPushNotify.contentToString())
            return requestPushNotify
        }
    }
}

