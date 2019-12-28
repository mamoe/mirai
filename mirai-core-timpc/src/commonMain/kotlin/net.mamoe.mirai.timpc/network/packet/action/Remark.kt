@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.action

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.timpc.network.packet.*
import net.mamoe.mirai.utils.PacketVersion

import net.mamoe.mirai.utils.io.readUShortLVString
import net.mamoe.mirai.utils.io.writeQQ
import net.mamoe.mirai.utils.io.writeZero

internal object QueryFriendRemarkPacket : SessionPacketFactory<FriendNameRemark>() {
    /**
     * 查询好友的备注
     */
    @PacketVersion(date = "2019.11.27", timVersion = "2.3.2 (21173)")
    operator fun invoke(
        bot: Long,
        sessionKey: SessionKey,
        target: Long
    ): OutgoingPacket = buildSessionPacket(
        bot, sessionKey
    ) {
        writeByte(0x0D)
        writeQQ(target)
        writeZero(1)
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler): FriendNameRemark {
        //0D 00 5D DA 3D 0F 59 17 3E 05 00 00 06 E6 9F 90 E4 B9 90 00 00 00 00 00 00
        discardExact(11)
        return FriendNameRemark(readUShortLVString())
    }

}