package net.mamoe.mirai.internal.network.protocol.packet.chat.receive

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.internal.network.protocol.packet.IncomingPacketFactory
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf

internal object PbC2CMsgSync : IncomingPacketFactory<Packet?>(
    "OnlinePush.PbC2CMsgSync", ""
) {
    override suspend fun ByteReadPacket.decode(bot: QQAndroidBot, sequenceId: Int): Packet? {
        return readProtoBuf(MsgOnlinePush.PbPushMsg.serializer()).msg.transform(bot, true)
    }
}