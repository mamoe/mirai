@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.utils.MiraiInternalAPI


/**
 * At 一个人. 只能发送给一个群.
 */
class At @MiraiInternalAPI constructor(val target: Long, val display: String) : Message {
    @UseExperimental(MiraiInternalAPI::class)
    constructor(member: Member) : this(member.id, "@${member.groupCard}")

    override fun toString(): String = display

    companion object Key : Message.Key<At>

    override fun eq(other: Message): Boolean {
        return other is At && other.target == this.target
    }
}

/**
 * At 这个成员
 */
@Suppress("NOTHING_TO_INLINE")
inline fun Member.at(): At = At(this)