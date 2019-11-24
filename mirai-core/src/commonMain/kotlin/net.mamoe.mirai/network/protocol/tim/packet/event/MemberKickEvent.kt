@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.network.protocol.tim.packet.PacketVersion


/**
 * 成员被踢出
 */
data class MemberKickEvent(
    val member: Member
) : EventPacket

@PacketVersion(date = "2019.11.20", timVersion = "2.3.2 (21173)")
object MemberKickEventPacketFactory : KnownEventParserAndHandler<MemberKickEvent>(0x0022u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): MemberKickEvent {

        TODO()
        // return MemberKickEvent()
    }
}