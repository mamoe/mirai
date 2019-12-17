@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.timpc.packet.action

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.timpc.TIMProtocol
import net.mamoe.mirai.network.protocol.timpc.packet.*
import net.mamoe.mirai.utils.io.writeZero

class FriendList : Packet

@PacketVersion(date = "2019.11.24", timVersion = "2.3.2 (21173)")
internal object RequestFriendListPacket : SessionPacketFactory<FriendList>() {
    operator fun invoke(
        bot: UInt,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(
        bot, sessionKey, version = TIMProtocol.version0x02
    ) {
        writeByte(0x02)
        writeZero(4)
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): FriendList {

        TODO()
    }
}