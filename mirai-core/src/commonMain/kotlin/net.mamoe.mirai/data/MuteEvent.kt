package net.mamoe.mirai.data

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member


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

abstract class EventOfMute : EventPacket {
    abstract val operator: Member
    abstract val group: Group
}

