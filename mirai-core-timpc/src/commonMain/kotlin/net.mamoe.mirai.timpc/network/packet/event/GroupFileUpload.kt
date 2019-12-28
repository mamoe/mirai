@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.io.debugPrint


data class GroupFileUploadPacket(inline val xmlMessage: String) : EventPacket

@PacketVersion(date = "2019.7.1", timVersion = "2.3.2 (21173)")
internal object GroupFileUploadEventFactory : KnownEventParserAndHandler<GroupFileUploadPacket>(0x002Du) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): GroupFileUploadPacket {
        this.debugPrint("GroupFileUploadPacket")
        return GroupFileUploadPacket("")
        /*
        discardExact(60)
        val size = readShort().toInt()
        discardExact(3)
        return GroupFileUploadPacket(xmlMessage = readString(size))*/
    }
}
