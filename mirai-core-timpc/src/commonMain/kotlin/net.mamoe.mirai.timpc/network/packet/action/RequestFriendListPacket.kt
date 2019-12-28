@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.action

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.io.writeZero

class FriendList : Packet

internal object RequestFriendListPacket : SessionPacketFactory<FriendList>() {
    @PacketVersion(date = "2019.11.24", timVersion = "2.3.2 (21173)")
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(
        bot, sessionKey, version = TIMProtocol.version0x02
    ) {
        writeByte(0x02)
        writeZero(4)
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): FriendList {

        TODO()
    }
}