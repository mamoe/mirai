@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.utils.PacketVersion
import net.mamoe.mirai.utils.io.readQQ


data class MemberPermissionChangePacket(
    val member: Member,
    val kind: Kind
) : EventPacket {
    val group: Group get() = member.group

    enum class Kind {
        /**
         * 变成管理员
         */
        BECOME_OPERATOR,
        /**
         * 不再是管理员
         */
        NO_LONGER_OPERATOR,
    } // TODO: 2019/11/2 变成群主的情况
}

@PacketVersion(date = "2019.11.1", timVersion = "2.3.2 (21173)")
internal object GroupMemberPermissionChangedEventFactory : KnownEventParserAndHandler<MemberPermissionChangePacket>(0x002Cu) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): MemberPermissionChangePacket {
        // 群里一个人变成管理员:
        // 00 00 00 08 00 0A 00 04 01 00 00 00 22 96 29 7B 01 01 76 E4 B8 DD 01
        // 取消管理员
        // 00 00 00 08 00 0A 00 04 01 00 00 00 22 96 29 7B 01 00 76 E4 B8 DD 00
        discardExact(remaining - 5)
        val group = bot.getGroup(identity.from)
        val qq = readQQ()
        val kind = when (readByte().toInt()) {
            0x00 -> MemberPermissionChangePacket.Kind.NO_LONGER_OPERATOR
            0x01 -> MemberPermissionChangePacket.Kind.BECOME_OPERATOR
            else -> error("Could not determine permission change kind")
        }
        return MemberPermissionChangePacket(group.getMember(qq), kind)
    }
}
