@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.utils.MiraiInternalAPI


//群有新成员加入
//事件 id 00 21
//
//00 00 00 08 00 0A 00 04 01 00 00 00 32 DC FC C8 01 2D 5C 53 A6 03 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 42 34 37 31 30 36 43 30 44 44 34 41 34 44 30 35 30 39 44 45 31 32 30 42 43 35 45 34 44 38 45 42 37 30 36 39 31 45 36 44 45 36 44 39 46 37 36 30

/**
 * 新成员加入. 此时这个人还没被添加到群列表
 */
internal class MemberJoinEventPacket(
    val member: Member
) : Packet

class MemberJoinEvent : BotEvent() {

}

@UseExperimental(MiraiInternalAPI::class)
internal object MemberJoinPacketHandler : KnownEventParserAndHandler<MemberJoinEventPacket>(0x0021u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): MemberJoinEventPacket {

        TODO()
    }

    override suspend fun BotNetworkHandler<*>.handlePacket(packet: MemberJoinEventPacket) {

    }
}