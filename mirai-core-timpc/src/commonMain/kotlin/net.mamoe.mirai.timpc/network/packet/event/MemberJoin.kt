@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.Subscribable
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.timpc.TIMPCBot
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.discardExact
import net.mamoe.mirai.utils.io.readQQ

/**
 * 成员加入前的事件. 群的成员列表中还没有这个人
 */
@UseExperimental(MiraiInternalAPI::class)
inline class PreMemberJoinEvent constructor(private val packet: MemberJoinEventPacket) : MemberJoinEvent {
    override val member: Member get() = packet.member
    override val group: Group get() = packet.member.group
    override val inviter: Member get() = packet.inviter ?: error("The new member is not a invitee")
    override val isInvitee: Boolean get() = packet.inviter != null
}

/**
 * 成员加入后的事件. 群的成员列表中已经有这个人
 */
@UseExperimental(MiraiInternalAPI::class)
inline class PostMemberJoinEvent constructor(private val packet: MemberJoinEventPacket) : MemberJoinEvent {
    override val member: Member get() = packet.member
    override val group: Group get() = packet.member.group
    override val inviter: Member get() = packet.inviter ?: error("The new member is not a invitee")
    override val isInvitee: Boolean get() = packet.inviter != null
}

interface MemberJoinEvent : Subscribable {
    val member: Member
    val group: Group
    val inviter: Member
    val isInvitee: Boolean
}


/**
 * 新成员加入. 此时这个人还没被添加到群列表
 *
 * 仅内部使用
 */
@MiraiInternalAPI
class MemberJoinEventPacket(
    val member: Member,
    val inviter: Member?
) : MemberListChangedEvent // only for internal subscribing

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
        val group = bot.getGroup(readQQ())

        discardExact(1) // 01
        val qq = bot.getQQ(readQQ())
        val member = with(bot) {
            this as? TIMPCBot ?: error("wrong Bot type passed")
            group.Member(qq, MemberPermission.MEMBER)
        }

        return if (readByte().toInt() == 0x03) {
            MemberJoinEventPacket(member, null)
        } else {
            MemberJoinEventPacket(member, group.getMember(readQQ()))
        }
    }

    override suspend fun BotNetworkHandler.handlePacket(packet: MemberJoinEventPacket) {
        PreMemberJoinEvent(packet).broadcast()
        packet.broadcast()
        PostMemberJoinEvent(packet).broadcast()
    }
}