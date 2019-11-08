@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion
import net.mamoe.mirai.utils.io.readString


data class GroupFileUploadPacket(inline val xmlMessage: String) : EventPacket

@PacketVersion(date = "2019.7.1", timVersion = "2.3.2.21173")
object GroupFileUploadEventFactory : KnownEventParserAndHandler<GroupFileUploadPacket>(0x002Du) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): GroupFileUploadPacket {
        discardExact(60)
        val size = readShort().toInt()
        discardExact(3)
        return GroupFileUploadPacket(xmlMessage = readString(size))
    }
}
