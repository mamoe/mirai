@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "JoinDeclarationAndAssignment")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import kotlinx.io.core.readUInt
import net.mamoe.mirai.event.events.FriendOnlineStatusChangedEvent
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.OnlineStatus

@CorrespondingEvent(FriendOnlineStatusChangedEvent::class)
abstract class FriendStatusChanged : Packet {
    abstract val qq: UInt
    abstract val status: OnlineStatus
}

/**
 * 好友在线状态改变
 */
@AnnotatedId(KnownPacketId.FRIEND_ONLINE_STATUS_CHANGE)
object FriendOnlineStatusChangedPacket : SessionPacketFactory<FriendStatusChanged>() {

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): FriendStatusChanged =
        object : FriendStatusChanged() {
            override val qq: UInt
            override val status: OnlineStatus

            init {
                qq = readUInt()
                discardExact(8)
                val id = readUByte()
                status = OnlineStatus.ofId(id) ?: error("Unknown online status id $id")
            }
    }

    //在线     XX XX XX XX 01 00 00 00 00 00 00 00 0A 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
    //忙碌     XX XX XX XX 01 00 00 00 00 00 00 00 32 15 E3 10 00 01 2E 01 00 00 00 00 00 00 00 00 00 00 00 13 08 02 C2 76 E4 B8 DD 00 00 00 00 00 00 00 00 00 00 00
}