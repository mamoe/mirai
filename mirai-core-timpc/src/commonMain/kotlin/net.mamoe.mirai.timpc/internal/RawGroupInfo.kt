package net.mamoe.mirai.timpc.internal

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.timpc.TIMPCBot
import net.mamoe.mirai.timpc.network.packet.action.GroupPacket
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalAPI

data class RawGroupInfo(
    val group: Long,
    val owner: Long,
    val name: String,
    val announcement: String,
    /**
     * 含群主
     */
    val members: Map<Long, MemberPermission>
) : GroupPacket.InfoResponse {

    @UseExperimental(MiraiInternalAPI::class)
    fun parseBy(group: Group): GroupInfo = group.withBot {
        this as? TIMPCBot ?: error("internal error: wrong Bot instance passed")

        val memberList = LockFreeLinkedList<Member>()
        members.forEach { entry: Map.Entry<Long, MemberPermission> ->
            memberList.addLast(entry.key.qq().let { group.Member(it, entry.value) })
        }
        return GroupInfo(
            group,
            this@RawGroupInfo.owner.qq().let { group.Member(it, MemberPermission.OWNER) },
            this@RawGroupInfo.name,
            this@RawGroupInfo.announcement,
            ContactList(memberList)
        )
    }
}
