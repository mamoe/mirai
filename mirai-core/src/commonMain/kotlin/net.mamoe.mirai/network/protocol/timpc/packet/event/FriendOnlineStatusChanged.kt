@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "JoinDeclarationAndAssignment")

package net.mamoe.mirai.network.protocol.timpc.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.timpc.packet.KnownPacketId
import net.mamoe.mirai.network.protocol.timpc.packet.PacketId
import net.mamoe.mirai.network.protocol.timpc.packet.SessionPacketFactory
import net.mamoe.mirai.utils.OnlineStatus

data class FriendStatusChanged(
    val qq: QQ,
    val status: OnlineStatus
) : EventPacket

/**
 * 好友在线状态改变
 */
internal object FriendOnlineStatusChangedPacket : SessionPacketFactory<FriendStatusChanged>() {

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): FriendStatusChanged {
        val qq = readUInt()
        discardExact(8)
        val statusId = readUByte()
        val status = OnlineStatus(statusId)
        return FriendStatusChanged(handler.bot.getQQ(qq), status)
    }

    //在线     XX XX XX XX 01 00 00 00 00 00 00 00 0A 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
    //忙碌     XX XX XX XX 01 00 00 00 00 00 00 00 32 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
}