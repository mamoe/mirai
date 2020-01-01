@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "JoinDeclarationAndAssignment")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import net.mamoe.mirai.data.OnlineStatus
import net.mamoe.mirai.event.events.FriendStatusChanged
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.timpc.network.packet.PacketId

import net.mamoe.mirai.timpc.network.packet.SessionPacketFactory
import net.mamoe.mirai.utils.io.readQQ

/**
 * 好友在线状态改变
 */
internal object FriendOnlineStatusChangedPacket : SessionPacketFactory<FriendStatusChanged>() {

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): FriendStatusChanged {
        val qq = readQQ()
        discardExact(8)
        val statusId = readUByte()

        val status = OnlineStatus.ofIdOrNull(statusId.toInt()) ?: OnlineStatus.UNKNOWN
        return FriendStatusChanged(handler.bot.getQQ(qq), status)
    }

    //在线     XX XX XX XX 01 00 00 00 00 00 00 00 0A 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
    //忙碌     XX XX XX XX 01 00 00 00 00 00 00 00 32 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
}