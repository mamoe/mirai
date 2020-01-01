package net.mamoe.mirai.message

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.BroadcastControllable
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.unsafeWeakRef

@Suppress("unused", "NOTHING_TO_INLINE")
class GroupMessage(
    bot: Bot,
    group: Group,
    val senderName: String,
    /**
     * 发送方权限.
     */
    val permission: MemberPermission,
    sender: Member,
    override val message: MessageChain
) : MessagePacket<Member, Group>(bot), BroadcastControllable {
    val group: Group by group.unsafeWeakRef()
    override val sender: Member by sender.unsafeWeakRef()

    /*
    01 00 09 01 00 06 66 61 69 6C 65 64 19 00 45 01 00 42 AA 02 3F 08 06 50 02 60 00 68 00 88 01 00 9A 01 31 08 0A 78 00 C8 01 00 F0 01 00 F8 01 00 90 02 00 C8 02 00 98 03 00 A0 03 02 B0 03 00 C0 03 00 D0 03 00 E8 03 02 8A 04 04 08 02 08 01 90 04 80 C8 10 0E 00 0E 01 00 04 00 00 08 E4 07 00 04 00 00 00 01 12 00 1E 02 00 09 E9 85 B1 E9 87 8E E6 98 9F 03 00 01 02 05 00 04 00 00 00 03 08 00 04 00 00 00 04
     */
    override val subject: Group get() = group

    inline fun At.member(): Member = group.getMember(this.target)
    inline fun Long.member(): Member = group.getMember(this)
    override fun toString(): String =
        "GroupMessage(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"


    override val shouldBroadcast: Boolean
        get() = bot.qqAccount != sender.id // 自己会收到自己发的消息
}