@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.io.readBoolean


@PacketVersion(date = "2019.11.2", timVersion = "2.3.2 (21173)")
data class AndroidDeviceStatusChangePacket(val kind: Kind) : Packet {
    enum class Kind {
        ONLINE,
        OFFLINE
    }
}

/**
 * Android 客户端在线状态改变
 */
@PacketVersion(date = "2019.10.31", timVersion = "2.3.2 (21173)")
internal object AndroidDeviceOnlineStatusChangedEventFactory : KnownEventParserAndHandler<AndroidDeviceStatusChangePacket>(0x00C4u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): AndroidDeviceStatusChangePacket {
        discardExact(13)
        return AndroidDeviceStatusChangePacket(
            if (readBoolean()) AndroidDeviceStatusChangePacket.Kind.OFFLINE else AndroidDeviceStatusChangePacket.Kind.ONLINE
        )
    }
}

