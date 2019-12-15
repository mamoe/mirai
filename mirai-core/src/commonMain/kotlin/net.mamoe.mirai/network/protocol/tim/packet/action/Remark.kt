@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.utils.io.readUShortLVString
import net.mamoe.mirai.utils.io.writeQQ
import net.mamoe.mirai.utils.io.writeZero

/**
 * 给好友设置的备注
 */
inline class FriendNameRemark(val value: String) : Packet

internal object QueryFriendRemarkPacket : SessionPacketFactory<FriendNameRemark>() {
    /**
     * 查询好友的备注
     */
    @PacketVersion(date = "2019.11.27", timVersion = "2.3.2 (21173)")
    operator fun invoke(
        bot: UInt,
        sessionKey: SessionKey,
        target: UInt
    ): OutgoingPacket = buildSessionPacket(
        bot, sessionKey
    ) {
        writeByte(0x0D)
        writeQQ(target)
        writeZero(1)
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): FriendNameRemark {
        //0D 00 5D DA 3D 0F 59 17 3E 05 00 00 06 E6 9F 90 E4 B9 90 00 00 00 00 00 00
        discardExact(11)
        return FriendNameRemark(readUShortLVString())
    }

}