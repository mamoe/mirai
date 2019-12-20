@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.timpc.network.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readUByte
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.data.EventPacket
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.io.*

/**
 * 群成员列表变动事件.
 *
 * 可为成员增多, 或减少.
 */
interface MemberListChangedEvent : EventPacket

/**
 * 成员主动离开群
 */
@Suppress("unused")
data class MemberQuitEvent(
    val member: Member,
    private val _operator: Member?
) : MemberListChangedEvent {
    /**
     * 是否是被管理员或群主踢出
     */
    val isKick: Boolean get() = _operator != null

    /**
     * 被踢出时的操作人. 若是主动退出则为 `null`
     */
    val operator: Member get() = _operator ?: error("The action is not a kick")
}

/**
 * 机器人被踢出
 */
data class BeingKickEvent(val group: Group, val operator: Member) : MemberListChangedEvent

/**
 * 成员退出. 可能是被踢出也可能是主动退出
 */
internal object MemberGoneEventPacketHandler : KnownEventParserAndHandler<MemberListChangedEvent>(0x0022u) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): MemberListChangedEvent {
        discardExact(11)

        discardExact(1)
        val group = bot.getGroup(readGroup())

        discardExact(1)
        val id = readQQ()
        if (id == bot.qqAccount) {
            discardExact(1)
            return BeingKickEvent(group, group.getMember(readQQ()))
        }

        val member = group.getMember(id)

        return when (val type = readUByte().toInt()) {
            0x02 -> MemberQuitEvent(member, _operator = null)
            0x03 -> MemberQuitEvent(member, _operator = group.getMember(readQQ()))
            else -> error("Unsupported type " + type.toUHexString())
        }

        // 某群员主动离开, 群号 853343432
        // 00 00 00 08 00 0A 00 04 01 00 00
        // 00 (32 DC FC C8)
        // 01 (2D 5C 53 A6)
        // 02
        // 00 30 44 43 31 45 31 38 43 38 31 44 31 34 39 39 41 44 36 44 37 32 42 41 35 43 45 44 30 33 35 42 39 31 45 31 42 43 41 44 42 35 33 33 46 39 31 45 37 31

        // 某群员被群主踢出, 群号 853343432
        // 00 00 00 08 00 0A 00 04 01 00 00
        // 00 (32 DC FC C8)
        // 01 (2D 5C 53 A6)
        // 03 (3E 03 3F A2)
        // 06 B4 B4 BD A8 D5 DF
        // 00 30 45 43 41 34 35 44 34 33 30 34 30 35 35 39 42 46 44 45 35 32 46 31 42 33 46 36 38 30 33 37 42 44 43 30 44 37 36 37 34 39 41 39 37 32 39 33 32 36

        // 机器人被踢出
        // 00 00 00 08 00 0A 00 04 01 00 00
        // 00 (32 DC FC C8)
        // 01 (2D 5C 53 A6)
        // 03 (3E 03 3F A2)
        // 06 B4 B4 BD A8 D5 DF
        // 00 30 32 33 32 63 32 39 36 65 36 35 64 62 64 64 64 64 65 35 62 33 34 64 36 62 34 33 32 61 30 64 61 65 32 30 37 35 38 34 37 34 32 65 32 39 63 35 63 64

    }
}