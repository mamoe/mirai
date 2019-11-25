@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.getGroup
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion

/**
 * 机器人账号被踢出群
 */
data class BeingKickEvent(val group: Group, val operator: Member) : EventPacket

object BeingKickEventPacketHandler : KnownEventParserAndHandler<BeingKickEvent>(0x0022u) {
    //00 00 00 08 00 0A 00 04 01 00 00
    // 00 36 DD C4 A0
    // 01 2D 5C 53 A6
    // 03 3E 03 3F A2
    // 06 B9 DC C0 ED D4 B1
    //
    // 00 30 31 63 35 35 31 34 63 62 36 64 37 39 61 65 61 66 35 66 33 34 35 64 39 63 32 34 64 65 37 32 36 64 39 64 36 39 36 64 66 66 32 38 64 63 38 32 37 36

    @PacketVersion(date = "2019.11.24", timVersion = "2.3.2 (21173)")
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): BeingKickEvent {
        discardExact(11 + 5 + 5 + 1)
        val group = bot.getGroup(identity.from)
        return BeingKickEvent(group, group.getMember(readUInt()))
    }
}