@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.internal.MemberImpl
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.getGroup
import net.mamoe.mirai.getQQ
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.Packet
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.discardExact


//群有新成员加入
//事件 id 00 21
//
//00 00 00 08 00 0A 00 04 01 00 00 00 32 DC FC C8 01 2D 5C 53 A6 03 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 42 34 37 31 30 36 43 30 44 44 34 41 34 44 30 35 30 39 44 45 31 32 30 42 43 35 45 34 44 38 45 42 37 30 36 39 31 45 36 44 45 36 44 39 46 37 36 30

/**
 * 新成员加入. 此时这个人还没被添加到群列表
 */
internal class MemberJoinEventPacket(
    val member: Member,
    val inviter: Member?
) : Packet

/**
 * 成员加入前的事件. 群的成员列表中还没有这个人
 */
data class PreMemberJoinEvent(val member: Member, private val _inviter: Member?) : Event() {
    val group: Group get() = member.group
    val inviter: Member get() = _inviter ?: error("The new member is not a invitee")
    val isInvitee: Boolean get() = _inviter != null
}

/**
 * 成员加入后的事件. 群的成员列表中已经有这个人
 */
data class PostMemberJoinEvent(val member: Member, private val _inviter: Member?) : Event() {
    val group: Group get() = member.group
    val inviter: Member get() = _inviter ?: error("The new member is not a invitee")
    val isInvitee: Boolean get() = _inviter != null
}

@UseExperimental(MiraiInternalAPI::class)
internal object MemberJoinPacketHandler : KnownEventParserAndHandler<MemberJoinEventPacket>(0x0021u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): MemberJoinEventPacket {
        //由 1040400290 邀请的新成员加入
        //00 00 00 08 00 0A 00 04 01 00 00
        // 00 32 DC FC C8
        // 01 2D 5C 53 A6
        // 03 3E 03 3F A2
        // 06 B4 B4 BD A8 D5 DF
        // 00 30 44 31 43 37 36 30 41 43 33 42 46 37 32 39 38 36 41 42 43 44 33 37 41 37 46 30 35 35 46 37 32 39 46 31 31 36 36 37 42 35 45 33 37 43 37 46 44 37
        discardExact(11) //00 00 00 08 00 0A 00 04 01 00 00

        discardExact(1) // 00
        val group = bot.getGroup(readUInt())

        discardExact(1) // 01
        val member = MemberImpl(bot.getQQ(readUInt()), group, MemberPermission.MEMBER)

        return if (readByte().toInt() == 0x03) {
            MemberJoinEventPacket(member, null)
        } else {
            MemberJoinEventPacket(member, group.getMember(readUInt()))
        }
    }

    override suspend fun BotNetworkHandler<*>.handlePacket(packet: MemberJoinEventPacket) {

    }
}