@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.network.protocol.tim.packet.event

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.getGroup
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.io.debugPrintIfFail
import net.mamoe.mirai.utils.io.readRemainingBytes
import net.mamoe.mirai.utils.io.toUHexString

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
@Suppress("SpellCheckingInspection")
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

internal class Unknown0x02DCPacketFlag0x0EMaybeMutePacket(
    val remaining: ByteArray
) : EventOfMute() {
    override val operator: Member get() = error("Getting a field from Unknown0x02DCPacketFlag0x0EMaybeMutePacket")
    override val group: Group get() = error("Getting a field from Unknown0x02DCPacketFlag0x0EMaybeMutePacket")
    override fun toString(): String = "Unknown0x02DCPacketFlag0x0EMaybeMutePacket(remaining=${remaining.toUHexString()})"
}

sealed class EventOfMute : EventPacket {
    abstract val operator: Member
    abstract val group: Group
}

// TODO: 2019/12/14 这可能不只是禁言的包.
internal object MemberMuteEventPacketParserAndHandler : KnownEventParserAndHandler<EventOfMute>(0x02DCu) {
    override suspend fun ByteReadPacket.parse(bot: Bot, identity: EventPacketIdentity): EventOfMute {

        //取消
        //00 00 00 11 00
        // 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00
        // 01 01
        // 22 96 29 7B
        // 0C 01
        // 3E 03 3F A2
        // 5D E5 12 EB
        // 00 01
        // 76 E4 B8 DD
        // 00 00 00 00

        // 禁言
        //00 00 00 11 00
        // 0A 00 04 01 00 00 00 00 0C 00 05 00 01 00
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

        discardExact(3)
        return when (val flag = readByte().toUInt()) {
            0x0Eu -> {
                //00 00 00 0E 00 08 00 02 00 01 00
                // 0A 00 04 01 00 00 00 35 DB 60 A2 11 00 3E 08 07 20 A2 C1 ED AE 03 5A 34 08 A2 FF 8C F0 03 1A 19 08 F4 0E 10 FE 8C D3 EF 05 18 84 A1 F8 F9 06 20 00 28 00 30 A2 FF 8C F0 03 2A 0D 08 00 12 09 08 F4 0E 10 00 18 01 20 00 30 00 38 00
                Unknown0x02DCPacketFlag0x0EMaybeMutePacket(readRemainingBytes())
            }

            0x11u -> debugPrintIfFail("解析禁言包(0x02DC)时"){ // 猜测这个失败是撤回??
                discardExact(15)
                discardExact(2)
                val group = bot.getGroup(readUInt())
                discardExact(2)
                val operator = group.getMember(readUInt())
                discardExact(4) //time
                discardExact(2)
                val memberQQ = readUInt()

                val durationSeconds = readUInt().toInt()
                if (durationSeconds == 0) {
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

            else -> error("Unsupported flag in 0x02DC packet. flag=$flag, remainning=${readBytes().toUHexString()}")
        }
    }
}