package net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketFactory

internal class OnlinePush {
    internal object PbPushGroupMsg : PacketFactory<GroupMessage>("OnlinePush.PbPushGroupMsg") {
        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): GroupMessage {
            TODO()
        }
    }
}