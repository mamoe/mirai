@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.getGroup
import net.mamoe.mirai.qqAccount

// region mute
/**
 * 某群成员被禁言事件
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class MemberMuteEvent(
    val member: Member,
    override val durationSeconds: Int,
    override val operator: Member
) : MuteEvent() {
    override val group: Group get() = operator.group
    override fun toString(): String = "MemberMuteEvent(member=${member.id}, group=${group.id}, operator=${operator.id}, duration=${durationSeconds}s"
}

/**
 * 机器人被禁言事件
 */
class BeingMutedEvent(
    override val durationSeconds: Int,
    override val operator: Member
) : MuteEvent() {
    override val group: Group get() = operator.group
    override fun toString(): String = "BeingMutedEvent(group=${group.id}, operator=${operator.id}, duration=${durationSeconds}s"
}

sealed class MuteEvent : EventOfMute() {
    abstract override val operator: Member
    abstract override val group: Group
    abstract val durationSeconds: Int
}
// endregion

// region unmute
/**
 * 某群成员被解除禁言事件
 */
@Suppress("unused")
class MemberUnmuteEvent(
    val member: Member,
    override val operator: Member
) : UnmuteEvent() {
    override val group: Group get() = operator.group
    override fun toString(): String = "MemberUnmuteEvent(member=${member.id}, group=${group.id}, operator=${operator.id}"
}

/**
 * 机器人被解除禁言事件
 */
class BeingUnmutedEvent(
    override val operator: Member
) : UnmuteEvent() {
    override val group: Group get() = operator.group
    override fun toString(): String = "BeingUnmutedEvent(group=${group.id}, operator=${operator.id}"
}

sealed class UnmuteEvent : EventOfMute() {
    abstract override val operator: Member
    abstract override val group: Group
}

// endregion

sealed class EventOfMute : EventPacket {
    abstract val operator: Member
    abstract val group: Group
}

internal object MemberMuteEventPacketParserAndHandler : KnownEventParserAndHandler<EventOfMute>(0x02DCu) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): EventOfMute {
        //取消
        //00 00 00 11 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00
        // 01 01
        // 22 96 29 7B
        // 0C 01
        // 3E 03 3F A2
        // 5D E5 12 EB
        // 00 01
        // 76 E4 B8 DD
        // 00 00 00 00

        // 禁言
        //00 00 00 11 00 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00
        // 01
        // 01
        // 22 96 29 7B
        // 0C
        // 01
        // 3E 03 3F A2
        // 5D E5 07 85
        // 00
        // 01
        // 76 E4 B8 DD
        // 00 27 8D 00
        discardExact(19)
        discardExact(2)
        val group = bot.getGroup(readUInt())
        discardExact(2)
        val operator = group.getMember(readUInt())
        discardExact(4) //time
        discardExact(2)
        val memberQQ = readUInt()

        val durationSeconds = readUInt().toInt()
        return if (durationSeconds == 0) {
            if (memberQQ == bot.qqAccount) {
                BeingUnmutedEvent(operator)
            } else {
                MemberUnmuteEvent(group.getMember(memberQQ), operator)
            }
        } else {
            if (memberQQ == bot.qqAccount) {
                BeingMutedEvent(durationSeconds, operator)
            } else {
                MemberMuteEvent(group.getMember(memberQQ), durationSeconds, operator)
            }
        }
    }
}